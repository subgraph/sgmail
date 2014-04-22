package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.imap.LocalIMAPFolder;
import com.sun.mail.imap.IMAPFolder;

public class ClientToServerSynchronize implements Runnable {

	private final LocalIMAPFolder localFolder;
	private final IMAPFolder remoteFolder;
	
	ClientToServerSynchronize(LocalIMAPFolder localFolder, IMAPFolder remoteFolder) {
		this.localFolder = localFolder;
		this.remoteFolder = remoteFolder;
	}

	@Override
	public void run() {

	}

}
