package com.subgraph.sgmail.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags.Flag;

import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.AccountAddedEvent;
import com.subgraph.sgmail.model.Account;
import com.subgraph.sgmail.model.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.StoredMessage;
import com.sun.mail.imap.IMAPStore;

public class SynchronizationManager {
	
	private final Model model;
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final Map<IMAPAccount, AccountSynchronizer> synchronizers = new HashMap<>();
	
	private boolean isRunning;
	
	public SynchronizationManager(Model model) {
		this.model = model;
		model.registerEventListener(this);
		initializeSynchronizers();
	}

	private void initializeSynchronizers() {
		synchronized(synchronizers) {
			for(Account account: model.getAccounts()) {
				addAccount(account);
			}
		}
	}
	
	private void addAccount(Account account) {
		if(account instanceof IMAPAccount) {
			final IMAPAccount imap = (IMAPAccount) account;
			synchronizers.put(imap, new AccountSynchronizer(executor, model, imap));
		}
	}

	@Subscribe
	public void onAccountAdded(AccountAddedEvent event) {
		synchronized (synchronizers) {
			addAccount(event.getAccount());
		}
	}

	public void updateFlag(IMAPAccount account, StoredMessage message, Flag flag, boolean isSet) {
		synchronized(synchronizers) {
			if(synchronizers.containsKey(account)) {
				synchronizers.get(account).updateFlags(message, flag, isSet);
			} else {
				throw new IllegalStateException("No entry found in synchronization manager for account");
			}
		}
	}

	public synchronized void start() {
		if(isRunning) {
			return;
		}

		synchronized (synchronizers) {
			for(IMAPAccount account: synchronizers.keySet()) {
				if(account.isAutomaticSyncEnabled()) {
					synchronizers.get(account).start();
				}
			}
		}
		isRunning = true;
	}

	public synchronized void close() {
		stop();
		executor.shutdownNow();
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void stop() {
		if(!isRunning) {
			return;
		}
		for(AccountSynchronizer as: synchronizers.values()) {
			as.stop();
		}
		isRunning = false;
	}
	
	public IMAPStore getStoreForAccount(IMAPAccount account) {
		synchronized(synchronizers) {
			final AccountSynchronizer as = synchronizers.get(account);
			if(as == null) {
				return null;
			} else {
				return as.getRemoteStore();
			}
		}
	}
}
