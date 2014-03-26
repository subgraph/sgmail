package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.messages.StoredIMAPFolder;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.messages.impl.FlagUtils;
import com.subgraph.sgmail.model.Model;
import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPFolder.FetchProfileItem;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.ModifiedSinceTerm;

import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ServerToClientFolderSynchronize  {
	private final static Logger logger = Logger.getLogger(ServerToClientFolderSynchronize.class.getName());
	
	private final Model model;
	private final IMAPAccount account;
	private final IMAPFolder remoteFolder;
	private final StoredIMAPFolder localFolder;
	private final AtomicBoolean stopFlag;
    private final StoredIMAPMessageFactory imapMessageFactory = new StoredIMAPMessageFactory();

    public ServerToClientFolderSynchronize(Model model, IMAPAccount account, IMAPFolder remoteFolder, StoredIMAPFolder localFolder, AtomicBoolean stopFlag) {
		this.model = model;
		this.account = account;
		this.remoteFolder = remoteFolder;
		this.localFolder = localFolder;
		this.stopFlag = stopFlag;
	}

	public void synchronize() {
		try {
			processUidValidity(remoteFolder.getUIDValidity());
			runSynchronize();
		} catch (MessagingException e) {
			logger.warning("Error occurred while synchronizing folder "+ e);
		}
	}
	
	private void processUidValidity(long uidValidity) {
		if(localFolder.getUIDValidity() == uidValidity) {
			return;
		}
		
		if(localFolder.getUIDValidity() != 0) {
			localFolder.clearFolder();
		}
		
		localFolder.setUIDValidity(uidValidity);
		
	}

	private boolean remoteSupportsCondstore() throws MessagingException {
		final IMAPStore store = (IMAPStore) remoteFolder.getStore();
		return store.hasCapability("CONDSTORE");
	}

	private void runSynchronize() throws MessagingException {
		final long rUidNext = remoteFolder.getUIDNext();
		final long lUidNext = localFolder.getUIDNext();
		
		if(rUidNext < 0) {
			processUnknownUidNext();
		} else if(lUidNext > lUidNext) {
			processDecreasedUidNext(lUidNext, rUidNext);
		} else if(lUidNext == rUidNext) {
			processUnchangedUidNext();
		} else {
			processIncreasedUidNext(lUidNext, rUidNext);
		}
		synchronizeFlags();
		localFolder.setUIDNext(rUidNext);
		localFolder.commit();
	}

	private void processUnknownUidNext() throws MessagingException {
		loadFullUIDMapping();
	}

	private void processDecreasedUidNext(long lUidNext, long rUidNext) throws MessagingException {
		logger.warning("Server returned UIDNEXT value of "+ rUidNext +" which is lower than previously recorded value of "+ lUidNext);
		localFolder.clearFolder();
		loadFullUIDMapping();
	}

	private void processUnchangedUidNext() throws MessagingException {
		final long rCount = remoteFolder.getMessageCount();
		final long lCount = localFolder.getMessageCount();
		if(rCount == lCount) {
			return;
		} else if(lCount < rCount) {
			logger.warning("EXISTS count has decreased while UIDNEXT stays the same");
			loadFullUIDMapping();
		} else {
			// rCount < lCount Some messages have been deleted
			loadFullUIDMapping();
		}
	}
	
	private void processIncreasedUidNext(long lUidNext, long rUidNext) throws MessagingException {
		final long nextDiff = rUidNext - lUidNext;
		final long countDiff = remoteFolder.getMessageCount() - localFolder.getMessageCount();
		if(nextDiff != countDiff) {
			loadFullUIDMapping();
			return;
		}
		final Message[] newMessages = remoteFolder.getMessagesByUID(lUidNext, rUidNext);
		if(newMessages.length != countDiff) {
			logger.warning("Expecting "+ countDiff +" messages and got "+ newMessages.length);
		}
		appendMessagesToLocal(newMessages);
	}

	private void loadFullUIDMapping() throws MessagingException {
		final List<Long> localUIDs = localFolder.getUIDMap();
		final Message[] messages = loadRemoteUIDMapping();
		final List<Long> remoteUIDs = getMessageUIDs(messages);
	
		final List<Long> newUIDs = expungeMissingLocal(localUIDs, remoteUIDs);
		
		final Message[] newMessages = remoteFolder.getMessagesByUID( getUIDArrayFromList(newUIDs) );
		appendMessagesToLocal(newMessages);
	}
	
	private void appendMessagesToLocal(Message[] messages) throws MessagingException {
		fetchDetails(messages);
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (Message m : messages) {
                if (stopFlag.get()) {
                    return;
                }
                if (!(m instanceof IMAPMessage)) {
                    logger.warning("Message is not an IMAPMessage " + m);
                } else {
                    storeNewMessage(executor, (IMAPMessage) m);
                }
            }
        } catch (IOException e) {
            // TODO fix
            e.printStackTrace();
        } finally {
            shutdownExecutor(executor);
            model.getMessageSearchIndex().commit();
        }
	}

    private void shutdownExecutor(ExecutorService executor) {
        final int WAIT_SECONDS = 120;
        executor.shutdown();
        try {
            if(!executor.awaitTermination(WAIT_SECONDS, TimeUnit.SECONDS)) {
                logger.warning("Search index executor didn't finish processing after "+ WAIT_SECONDS + " seconds.");
                return;
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void storeNewMessage(ExecutorService executor, IMAPMessage message) throws MessagingException, IOException {
        final StoredIMAPMessage storedMessage = imapMessageFactory.createFromJavamailMessage(account, message, remoteFolder.getUID(message));
        executor.submit(new StoreMessageTask(storedMessage, localFolder, model.getMessageSearchIndex()));
    }

	private void fetchDetails(Message[] messages) throws MessagingException {
		final FetchProfile fp = new FetchProfile();
		fp.add(IMAPFolder.FetchProfileItem.FLAGS);
		if(remoteFolder instanceof GmailFolder) {
			fp.add(GmailFolder.FetchProfileItem.MSGID);
			fp.add(GmailFolder.FetchProfileItem.THRID);
			fp.add(GmailFolder.FetchProfileItem.LABELS);
		}
		remoteFolder.fetch(messages, fp);
	}
	
	private long[] getUIDArrayFromList(List<Long> uids) {
		final long[] result = new long[uids.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = uids.get(i);
		}
		return result;
	}

	private List<Long> expungeMissingLocal(List<Long> localUIDs, List<Long> remoteUIDs) {
		final List<Long> expungeUIDs = new ArrayList<>();
		final List<Long> newUIDs = new ArrayList<>();
		final Iterator<Long> it = localUIDs.iterator();
		
		long localUID;
		for(long uid: remoteUIDs) {
			while(it.hasNext() && (localUID = it.next()) != uid) {
				expungeUIDs.add(localUID);
			}
			if(!it.hasNext()) {
				newUIDs.add(uid);
			}
		}
		if(!expungeUIDs.isEmpty()) {
			localFolder.expungeMessagesByUID(expungeUIDs);
		}
		
		return newUIDs;
	}
	
	private List<Long> getMessageUIDs(Message[] messages) throws MessagingException {
		final List<Long> result = new ArrayList<>();
		for(Message m: messages) {
			result.add(remoteFolder.getUID(m));
		}
		return result;
	}

	private Message[] loadRemoteUIDMapping() throws MessagingException {
		final FetchProfile fp = new FetchProfile();
		fp.add(UIDFolder.FetchProfileItem.UID);
		final Message[] messages = remoteFolder.getMessages();
		remoteFolder.fetch(messages, fp);
		return messages;
	}
	
	
	private void synchronizeFlags() throws MessagingException {
		System.out.println("synchronizing flags...");
		if(localFolder.getUIDNext() == 0) {
			return;
		}
		if(remoteSupportsCondstore()) {
			synchronizeFlagsWithCondstore(localFolder.getUIDNext(), localFolder.getHighestModSeq());
		} else {
			synchronizeFlagsWithoutCondstore(localFolder.getUIDNext());
		}
	}

	private void synchronizeFlagsWithoutCondstore(long maxUid) throws MessagingException {
		
		Message[] msgs = remoteFolder.getMessagesByUID(0, maxUid);
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfileItem.FLAGS);
		remoteFolder.fetch(msgs, fp);
		for(Message m: msgs) {
			if(stopFlag.get()) {
				return;
			}
			synchronizeMessageFlags(m);
		}
	}

	private void synchronizeFlagsWithCondstore(long maxUid, long modseq) throws MessagingException {
		final Message[] msgs = remoteFolder.search(new ModifiedSinceTerm(modseq));
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfileItem.FLAGS);
		remoteFolder.fetch(msgs, fp);
		for(Message m: msgs) {
			if(stopFlag.get()) {
				return;
			}
			synchronizeMessageFlags(m);
		}
		localFolder.setHighestModSeq(remoteFolder.getHighestModSeq());
	}
	
	private void synchronizeMessageFlags(Message m) throws MessagingException {
		if(localFolder.getMessageCount() < m.getMessageNumber()) {
			return;
		}
		final StoredIMAPMessage localMessage = localFolder.getMessageByMessageNumber(m.getMessageNumber());
        final long flagBits = FlagUtils.getFlagsFromMessage(m);
		if(localMessage.getFlags() != flagBits) {
			localMessage.setFlags(flagBits);
			model.postEvent(new MessageStateChangedEvent(localMessage));
		}
	}
	
	private MessageCountListener createMessageCountListener() {
		return new MessageCountListener() {
			@Override
			public void messagesAdded(MessageCountEvent event) {
				processMessagesAdded(event.getMessages());
			}

			@Override
			public void messagesRemoved(MessageCountEvent event) {
				processMessagesRemoved(event.getMessages());
			}
		};
	}
	
	private void processMessagesAdded(Message[] messages) {
		try {
			appendMessagesToLocal(messages);
		} catch (MessagingException e) {
			logger.warning("Error processing added messages: "+ e.getMessage());
		}
	}
	
	private void processMessagesRemoved(Message[] messages) {
		final List<Long> uids = new ArrayList<>();
		try {
			for(Message m: messages) {
				uids.add(remoteFolder.getUID(m));
			}
			localFolder.expungeMessagesByUID(uids);
		} catch(MessagingException e) {
			logger.warning("Error processing removed messages: "+ e.getMessage());
		}
	}
	
	public void runIdle() throws MessagingException {
		final MessageCountListener listener = createMessageCountListener();
		remoteFolder.addMessageCountListener(listener);
		try {
			while(!stopFlag.get()) {
				System.out.println("run idle");
				remoteFolder.idle();
				System.out.println("idle end");
			}
		} finally {
			remoteFolder.removeMessageCountListener(listener);
		}
	}
}
