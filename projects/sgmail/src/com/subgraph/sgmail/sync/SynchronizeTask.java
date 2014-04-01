package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.imap.LocalIMAPFolder;
import com.subgraph.sgmail.model.Model;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.ResyncData;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.event.MailEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SynchronizeTask implements Runnable {
	private final static Logger logger = Logger.getLogger(SynchronizeTask.class.getName());

	private final Model model;
	private final IMAPAccount account;
	private final Store remoteStore;
	private final AtomicBoolean stopFlag = new AtomicBoolean();
	
	private IMAPFolder idleFolder;
	
	public SynchronizeTask(Model model, Store remoteStore, IMAPAccount account) {
		this.model = model;
		this.account = account;
		this.remoteStore = remoteStore;
	}
	
	void stop() {
		stopFlag.set(true);
		if(idleFolder != null) {
			try {
				idleFolder.close(false);
				remoteStore.close();
			} catch (MessagingException e) {
				logger.warning("Error closing idle folder while stopping synchronize task: "+ e.getMessage());
			}
		}
	}

	@Override
	public void run() {
		try {
			synchronizeAccount();
		} catch (MessagingException e) {
			if(!stopFlag.get()) {
				logger.warning("Error while synchronizing account: "+ e.getMessage());
			}
		} catch (Exception e) {
			if(!stopFlag.get()) {
				throw e;
			}
		}
	}
	
	private void synchronizeAccount() throws MessagingException {
		if(!remoteStore.isConnected()) {
			remoteStore.connect();
		}

		final List<IMAPFolder> folders = getFoldersToSynchronize();
		
		for(IMAPFolder f: folders) {
            if((f.getType() & Folder.HOLDS_MESSAGES) != 0) {
                synchronizeFolder(f);
            }
		}
        if(account.isGmailAccount()) {
			idleFolderByName(folders, "[Gmail]/All Mail");
		} else {
            idleFolderByName(folders, "INBOX");
        }
	}

	private void idleFolderByName(List<IMAPFolder> folders, String name) throws MessagingException {
		for(IMAPFolder f: folders) {
			if(f.getFullName().equals(name)) {
				idleFolder(f);
				return;
			}
		}
	}

	private List<IMAPFolder> getFoldersToSynchronize() throws MessagingException {
		IMAPFolder defaultFolder = (IMAPFolder) remoteStore.getDefaultFolder();
		final List<IMAPFolder> folders = new ArrayList<>();
		addChildFolders(folders, defaultFolder);
		return folders;
	}

	private void addChildFolders(List<IMAPFolder> folderList, Folder folder) throws MessagingException {
		for(Folder f: folder.list()) {
			if(f instanceof IMAPFolder) {
				addIMAPFolder(folderList, (IMAPFolder) f);
			}
		}
	}
	
	private void addIMAPFolder(List<IMAPFolder> folderList, IMAPFolder folder) throws MessagingException {
		if(account.isGmailAccount()) {
			if(folder.getFullName().startsWith("[Gmail]/")) {
				folderList.add(folder);
			}
		} else {
			folderList.add(folder);
		}
		
		for(String a: folder.getAttributes()) {
			if(a.equals("\\HasChildren")) {
				addChildFolders(folderList, folder);
			}
		}
	}

	private void synchronizeFolder(IMAPFolder remoteFolder) throws MessagingException {
		final LocalIMAPFolder localFolder = account.getFolderByName(remoteFolder.getFullName());

		openRemote(remoteFolder);
		final ClientToServerSynchronize c2s = new ClientToServerSynchronize(localFolder, remoteFolder);
		c2s.run();
		
		final ServerToClientFolderSynchronize s2c = new ServerToClientFolderSynchronize(model, account, remoteFolder, localFolder, stopFlag);
		s2c.synchronize();
		
		if(remoteFolder.isOpen()) {
			remoteFolder.close(false);
		}
		idleFolder = null;
	}
	
	private void idleFolder(IMAPFolder remoteFolder) throws MessagingException {
		final LocalIMAPFolder localFolder = account.getFolderByName(remoteFolder.getFullName());
		openRemote(remoteFolder);
		final ServerToClientFolderSynchronize s2c = new ServerToClientFolderSynchronize(model, account, remoteFolder, localFolder, stopFlag);
		idleFolder = remoteFolder;
		s2c.runIdle();
		if(remoteFolder.isOpen()) {
			remoteFolder.close(false);
		}
		idleFolder = null;		
	}

	private void openRemote(IMAPFolder remoteFolder) throws MessagingException {
		if(remoteFolder.isOpen()) {
			return;
		} else if(remoteSupportsCondstore(remoteFolder)) {
			openWithCondstore(remoteFolder);
		} else {
			remoteFolder.open(Folder.READ_WRITE);
		}
	}
	
	private void openWithCondstore(IMAPFolder remoteFolder) throws MessagingException {
		final List<MailEvent> events = remoteFolder.open(Folder.READ_WRITE, ResyncData.CONDSTORE);
		if(events != null && !events.isEmpty()) {
			logger.warning("Unexpected FETCH and VANISH events while opening folder with CONDSTORE "+ events);
		}
	}

	private boolean remoteSupportsCondstore(IMAPFolder remoteFolder) throws MessagingException {
		final IMAPStore store = (IMAPStore) remoteFolder.getStore();
		return store.hasCapability("CONDSTORE");
	}
}
