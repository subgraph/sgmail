package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.model.Model;
import com.sun.mail.imap.IMAPStore;

import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

public class AccountSynchronizer {
	private final Logger logger = Logger.getLogger(AccountSynchronizer.class.getName());
	
	private final Executor executor;
	private final Model model;
	private final IMAPAccount account;
	private final IMAPStore remoteStore;
	
	private boolean isRunning;
	private SynchronizeTask runningTask;
	
	public AccountSynchronizer(Executor executor, Model model, IMAPAccount account) {
		this.executor = executor;
		this.model = model;
		this.account = account;
		this.remoteStore = (IMAPStore) account.getRemoteStore();
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
		executor.execute(runningTask);
		
		isRunning = true;
	}

	public void updateFlags(StoredIMAPMessage message, Flag flag, boolean isSet) {
		final UpdateFlagsTask task = new UpdateFlagsTask(remoteStore, message, flag, isSet);
		executor.execute(task);
	}

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
