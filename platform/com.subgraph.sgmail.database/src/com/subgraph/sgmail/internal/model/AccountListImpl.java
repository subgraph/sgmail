package com.subgraph.sgmail.internal.model;



import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableArrayList;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.accounts.AccountList;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

public class AccountListImpl implements AccountList, Storeable, Activatable {

    private final List<Account> accountList = new ActivatableArrayList<>();

    private transient Activator activator;
    private transient Database database;
    
    private transient EventList<Account> eventList;

    @SuppressWarnings("deprecation")
    public synchronized EventList<Account> getAccounts() {
        activate(ActivationPurpose.READ);
        if(eventList == null) {
            final EventList<Account> baseEventList = new BasicEventList<>(accountList);
            final ObservableElementList.Connector<Account> connector = GlazedLists.beanConnector(Account.class);
            eventList = new ObservableElementList<>(baseEventList, connector);
        }
        return eventList;
    }

    public void addAccount(Account account) {
        activate(ActivationPurpose.READ);
        database.store(account);
        final EventList<Account> eventList = getAccounts();
        try {
            eventList.getReadWriteLock().writeLock().lock();
            eventList.add(account);
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
        database.commit();
    }

    public void removeAccount(Account account) {
        activate(ActivationPurpose.READ);
        final EventList<Account> eventList = getAccounts();
        try {
            eventList.getReadWriteLock().writeLock().lock();
            eventList.remove(account);
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
        database.delete(account);
        database.commit();
    }

	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}

	@Override
	public void setDatabase(Database database) {
		this.database = checkNotNull(database);
	}
}
