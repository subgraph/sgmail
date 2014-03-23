package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.messages.StoredIMAPFolder;
import com.sun.mail.imap.IMAPFolder;

public class ClientToServerSynchronize implements Runnable {

	private final StoredIMAPFolder localFolder;
	private final IMAPFolder remoteFolder;
	
	ClientToServerSynchronize(StoredIMAPFolder localFolder, IMAPFolder remoteFolder) {
		this.localFolder = localFolder;
		this.remoteFolder = remoteFolder;
	}

	@Override
	public void run() {

	}

}
