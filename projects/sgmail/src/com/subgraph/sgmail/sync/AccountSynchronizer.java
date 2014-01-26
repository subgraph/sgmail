package com.subgraph.sgmail.sync;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.mail.Flags.Flag;
import javax.mail.MessagingException;

import com.subgraph.sgmail.model.GmailIMAPAccount;
import com.subgraph.sgmail.model.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.StoredMessage;
import com.sun.mail.imap.IMAPStore;

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

		if(!(account instanceof GmailIMAPAccount)) {
			System.out.println("only gmail supported for now");
			return;
		}
		
		runningTask = new SynchronizeTask(model, remoteStore, account);
		executor.execute(runningTask);
		
		isRunning = true;
	}
	
	public void updateFlags(StoredMessage message, Flag flag, boolean isSet) {
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
