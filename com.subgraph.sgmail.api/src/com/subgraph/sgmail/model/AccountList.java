package com.subgraph.sgmail.model;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.subgraph.sgmail.accounts.Account;

import java.util.List;

public class AccountList extends AbstractActivatable {

    private final List<Account> accountList = new ActivatableArrayList<>();

    private transient EventList<Account> eventList;

    @SuppressWarnings("deprecation")
    public synchronized EventList<Account> getAccounts() {
        activate(ActivationPurpose.READ);
        if(eventList == null) {
            final EventList<Account> baseEventList = new BasicEventList<>(accountList);
            final ObservableElementList.Connector connector = GlazedLists.beanConnector(Account.class);
            eventList = new ObservableElementList<>(baseEventList, connector);
        }
        return eventList;
    }

    public void addAccount(Account account) {
        activate(ActivationPurpose.READ);
        model.store(account);
        final EventList<Account> eventList = getAccounts();
        try {
            eventList.getReadWriteLock().writeLock().lock();
            eventList.add(account);
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
        model.commit();
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
        model.delete(account);
        model.commit();
    }
}
