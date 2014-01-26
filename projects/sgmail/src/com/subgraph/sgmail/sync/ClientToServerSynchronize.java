package com.subgraph.sgmail.sync;

import java.util.ArrayList;
import java.util.List;

import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.subgraph.sgmail.model.StoredFolder;
import com.subgraph.sgmail.model.StoredMessage;
import com.sun.mail.imap.IMAPFolder;

public class ClientToServerSynchronize implements Runnable {

	private final StoredFolder localFolder;
	private final IMAPFolder remoteFolder;
	
	ClientToServerSynchronize(StoredFolder localFolder, IMAPFolder remoteFolder) {
		this.localFolder = localFolder;
		this.remoteFolder = remoteFolder;
	}

	@Override
	public void run() {
		try {
			synchronizeFlagChanges();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
					
	}
	
	private void synchronizeFlagChanges() throws MessagingException {
		final List<StoredMessage> changed = getMessagesWithFlagChanges();
		if(changed.isEmpty()) {
			return;
		}
		final long[] uids = getUIDsForMessages(changed);
		loadRemoteMessageFlags(uids);
		
		for(StoredMessage sm: changed) {
			Message m = remoteFolder.getMessageByUID(sm.getMessageUID());
			sm.mergeFlags(m);
			sm.clearOfflineUpdatedFlags();
		}
	}
	
	private void loadRemoteMessageFlags(long[] uids) throws MessagingException {
		final Message[] messages = remoteFolder.getMessagesByUID(uids);
		final FetchProfile fp = new FetchProfile();
		fp.add(IMAPFolder.FetchProfileItem.FLAGS);
		remoteFolder.fetch(messages, fp);
	}

	private List<StoredMessage> getMessagesWithFlagChanges() {
		final List<StoredMessage> changed = new ArrayList<>();
		for(StoredMessage sm: localFolder.getMessages()) {
			if(sm.getOfflineUpdatedFlags() != 0) {
				changed.add(sm);
			}
		}
		return changed;
	}
	
	private long[] getUIDsForMessages(List<StoredMessage> messages) {
		final long[] uids = new long[messages.size()];
		for(int i = 0; i < uids.length; i++) {
			uids[i] = messages.get(i).getMessageUID();
		}
		return uids;
	}

}
