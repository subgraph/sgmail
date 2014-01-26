package com.subgraph.sgmail.sync;

import java.util.logging.Logger;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.MessagingException;

import com.subgraph.sgmail.model.StoredFolder;
import com.subgraph.sgmail.model.StoredMessage;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

public class UpdateFlagsTask implements Runnable {
	private final static Logger logger = Logger.getLogger(UpdateFlagsTask.class.getName());
	
	private final IMAPStore store;
	private final StoredMessage message;
	private final Flag flag;
	private final boolean isSet;
	
	UpdateFlagsTask(IMAPStore store, StoredMessage message, Flag flag, boolean isSet) {
		this.store = store;
		this.message = message;
		this.flag = flag;
		this.isSet = isSet;
	}

	@Override
	public void run() {
		if(!performRemoteUpdate()) {
			final long flagBit = StoredMessage.getFlagBitFromFlag(flag);
			message.addOfflineUpdatedFlag(flagBit);
		}
	}
	
	private boolean performRemoteUpdate() {
		if(!store.isConnected()) {
			return false;
		}
		
		try {
			final IMAPFolder remoteFolder = getRemoteFolder();
			final IMAPMessage remoteMessage = (IMAPMessage) remoteFolder.getMessageByUID(message.getMessageUID());
			if(remoteMessage == null) {
				logger.warning("No remote message found with UID = "+ message.getMessageUID());
				return false;
			}
			remoteMessage.setFlag(flag, isSet);
			remoteFolder.close(false);
			return true;
		} catch (MessagingException e) {
			logger.warning("Error setting remote message flag: "+ e.getMessage());
			return false;
		}
	}
	
	private IMAPFolder getRemoteFolder() throws MessagingException {
		final StoredFolder localFolder = message.getFolder();
		final IMAPFolder remoteFolder = (IMAPFolder) store.getFolder(localFolder.getFullName());
		remoteFolder.open(Folder.READ_WRITE);
		return remoteFolder;
	}
}
