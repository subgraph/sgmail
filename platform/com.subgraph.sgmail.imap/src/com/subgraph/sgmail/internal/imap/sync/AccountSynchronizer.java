package com.subgraph.sgmail.internal.imap.sync;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.database.Preferences;
import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.messages.MessageFactory;
import com.subgraph.sgmail.search.MessageSearchIndex;
import com.sun.mail.imap.IMAPStore;

public class AccountSynchronizer {
	private final Logger logger = Logger.getLogger(AccountSynchronizer.class.getName());
	
	private final MessageFactory basicMessageFactory;
	private final Model model;
	private final JavamailUtils javamailUtils;
	private final MessageSearchIndex searchIndex;
	private final ExecutorService executor;
	private final IMAPAccount account;
	private final Preferences rootPreferences;
	private boolean isTorEnabled;
	
	private IMAPStore remoteStore;
	
	private boolean isRunning;
	private SynchronizeTask runningTask;
	
	public AccountSynchronizer(Preferences rootPreferences, ExecutorService executor, MessageFactory basicMessageFactory, Model model, JavamailUtils javamailUtils, MessageSearchIndex searchIndex, IMAPAccount account) {
		this.executor = executor;
		this.basicMessageFactory = basicMessageFactory;
		this.model = model;
		this.javamailUtils = javamailUtils;
		this.searchIndex = searchIndex;
		this.account = account;
		this.rootPreferences = rootPreferences;
		this.isTorEnabled = rootPreferences.getBoolean(Preferences.TOR_ENABLED);
		this.remoteStore = account.getRemoteStore(javamailUtils.getSessionInstance(), isTorEnabled);
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

		final boolean torEnabledFlag = rootPreferences.getBoolean(Preferences.TOR_ENABLED);
		if(isTorEnabled != torEnabledFlag) {
			isTorEnabled = torEnabledFlag;
			disconnect();
			remoteStore = account.getRemoteStore(javamailUtils.getSessionInstance(), isTorEnabled);
		}
		
		runningTask = new SynchronizeTask(basicMessageFactory, model, javamailUtils, searchIndex, remoteStore, account);
        executor.execute(runningTask);

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
