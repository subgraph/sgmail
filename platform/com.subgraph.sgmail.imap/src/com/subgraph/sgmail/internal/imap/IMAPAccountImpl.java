package com.subgraph.sgmail.internal.imap;

import static com.google.common.base.Preconditions.checkNotNull;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.imap.LocalIMAPFolder;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPStore;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMAPAccountImpl implements IMAPAccount, Storeable, Activatable {
    private final MailAccount account;
    private final ServerDetails imapServerDetails;


    private int currentConversationId = 1;
    private TLongIntMap googleConversationMap = new TLongIntHashMap();
    private TLongIntMap googleMessageIdMap = new TLongIntHashMap();
    private Map<String, LocalIMAPFolder> folderMap = new HashMap<>();

    private transient IMAPStore cachedStore;
	private transient Activator activator;
	private transient Database database;

    public IMAPAccountImpl(MailAccount account, ServerDetails imapServerDetails) {
        this.account = account;
        this.imapServerDetails = imapServerDetails;
    }

    @Override
    public synchronized IMAPStore getRemoteStore(Session session, boolean preferOnionAddress) {
        activate(ActivationPurpose.READ);
        if(cachedStore == null) {
            cachedStore = (IMAPStore) imapServerDetails.createRemoteStore(session, preferOnionAddress);
        }
        return cachedStore;
    }

    @Override
    public ServerDetails getIMAPServerDetails() {
        activate(ActivationPurpose.READ);
        return imapServerDetails;
    }

    private final static List<String> GMAIL_DOMAINS = ImmutableList.of("gmail.com", "googlemail.com");

    @Override
    public boolean isGmailAccount() {
        activate(ActivationPurpose.READ);
        final String hostname = imapServerDetails.getHostname();
        for(String domain: GMAIL_DOMAINS) {
            if(hostname.toLowerCase().endsWith(domain)) {
                return true;
            }
        }
        return false;
    }

    
    @Override
    public LocalIMAPFolder getFolderByName(String name) {
        activate(ActivationPurpose.READ);
        synchronized (folderMap) {
            if(!folderMap.containsKey(name)) {
                activate(ActivationPurpose.WRITE);
                final StoredFolder folder = account.getFolderByName(name);
                final LocalIMAPFolderImpl localFolder = new LocalIMAPFolderImpl(this, folder);
                database.store(localFolder);
                folderMap.put(name, localFolder);
                database.commit();
            }
            return folderMap.get(name);
        }
    }

    @Override
    public MailAccount getMailAccount() {
        activate(ActivationPurpose.READ);
        return account;
    }

    @Override
    public synchronized int generateConversationIdForMessage(MimeMessage message) throws MessagingException {
        activate(ActivationPurpose.READ);
        if(message instanceof GmailMessage) {
            long threadId = ((GmailMessage) message).getThrId();
            if(!googleConversationMap.containsKey(threadId)) {
                activate(ActivationPurpose.WRITE);
                googleConversationMap.put(threadId, currentConversationId);
                currentConversationId += 1;
                database.commit();
            }
            return googleConversationMap.get(threadId);
        }
        throw new UnsupportedOperationException("Generation of conversation id only supported for gmail for now");
    }

    @Override
    public synchronized StoredMessage getMessageForMimeMessage(MimeMessage message) throws MessagingException {
        activate(ActivationPurpose.READ);
        if(!(message instanceof GmailMessage)) {
            return null;
        }
        long id = ((GmailMessage) message).getMsgId();
        if(!googleMessageIdMap.containsKey(id)) {
            return null;
        }
        return account.getMessageById(googleMessageIdMap.get(id));
    }

    @Override
    public synchronized int generateUniqueMessageIdForMessage(MimeMessage message, Model model) throws MessagingException {
        activate(ActivationPurpose.READ);
        if(message instanceof GmailMessage) {
            long messageId = ((GmailMessage) message).getMsgId();
            if (!googleMessageIdMap.containsKey(messageId)) {
                activate(ActivationPurpose.WRITE);
                googleMessageIdMap.put(messageId, model.getNextUniqueId());
                database.commit();
            }
            return googleMessageIdMap.get(messageId);
        }
        throw new UnsupportedOperationException("Generation of message id only supported for gmail for now");
    }

    @Override
    public boolean isAutomaticSyncEnabled() {
        return true;
    }
    
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}
	
	@Override
	public void setDatabase(Database database) {
		this.database = checkNotNull(database);
	}

}
