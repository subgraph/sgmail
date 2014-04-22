package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.sun.mail.imap.IMAPStore;

import javax.mail.MessagingException;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

public class AccountSynchronizer {
	private final Logger logger = Logger.getLogger(AccountSynchronizer.class.getName());
	
	private final Model model;
	private final IMAPAccount account;
	private final IMAPStore remoteStore;
	
	private boolean isRunning;
	private SynchronizeTask runningTask;
	
	public AccountSynchronizer(Model model, IMAPAccount account) {
		this.model = model;
		this.account = account;
		this.remoteStore = account.getRemoteStore();
	}

	public synchronized void stop() {
		if(!isRunning) {
			return;
		}
		
		checkState(runningTask != null);

		runningTask.stop();
		runningTask = null;
		disconnect();
		isRunning = false;
	}
	
	public synchronized void start() {
		if(isRunning) {
			return;
		}
		
		checkState(runningTask == null);

		runningTask = new SynchronizeTask(model, remoteStore, account);
        model.getExecutor().execute(runningTask);

		isRunning = true;
	}

    /*
	public void updateFlags(StoredIMAPMessage message, Flag flag, boolean isSet) {
		final UpdateFlagsTask task = new UpdateFlagsTask(remoteStore, message, flag, isSet);
        model.getExecutor().execute(task);
	}
	*/

	IMAPStore getRemoteStore() {
		return remoteStore;
	}

	private void disconnect() {
		try {
			if(remoteStore.isConnected()) {
				remoteStore.close();
			}
		} catch (MessagingException e) {
			logger.warning("Error closing connection to remote server: "+ e.getMessage());
		}
	}	
}
