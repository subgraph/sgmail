package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.imap.IMAPAccountList;
import com.subgraph.sgmail.model.Model;

import java.util.HashMap;
import java.util.Map;

public class SynchronizationManager {
	
	private final Model model;
	private final Map<IMAPAccount, AccountSynchronizer> synchronizers = new HashMap<>();
	
	private boolean isRunning;
	
	public SynchronizationManager(Model model) {
		this.model = model;
		model.registerEventListener(this);
		initializeSynchronizers();
	}

	private void initializeSynchronizers() {
        synchronized(synchronizers) {
            final IMAPAccountList imapAccountList = model.getModelSingleton(IMAPAccountList.class);
            if(imapAccountList == null) {
                return;
            }

            for(IMAPAccount account: imapAccountList.getAccounts()){
                synchronizers.put(account, new AccountSynchronizer(model, account));
			}
		}
	}
	

    /*
	@Subscribe
	public void onAccountAdded(AccountAddedEvent event) {
		synchronized (synchronizers) {
			addAccount(event.getAccount());
		}
	}
	*/
/*
	public void updateFlag(IMAPAccount account, StoredIMAPMessage message, Flag flag, boolean isSet) {
		synchronized(synchronizers) {
			if(synchronizers.containsKey(account)) {
				synchronizers.get(account).updateFlags(message, flag, isSet);
			} else {
				throw new IllegalStateException("No entry found in synchronization manager for account");
			}
		}
	}
	*/

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
		//executor.shutdownNow();
        /*
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
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

    /*
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
	*/
}
