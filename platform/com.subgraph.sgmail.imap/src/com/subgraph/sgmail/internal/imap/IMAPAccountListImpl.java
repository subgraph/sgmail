package com.subgraph.sgmail.internal.imap;

import static com.google.common.base.Preconditions.checkNotNull;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableArrayList;
import com.db4o.ta.Activatable;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.imap.IMAPAccountList;

import java.util.List;

public class IMAPAccountListImpl implements IMAPAccountList, Storeable, Activatable {

    private final List<IMAPAccount> accountList = new ActivatableArrayList<>();
    
	private transient Activator activator;
	private transient Database database;

    @Override
    public void addAccount(IMAPAccount account) {
        activate(ActivationPurpose.READ);
        database.store(account);
        accountList.add(account);
        database.commit();
    }

    @Override
    public List<IMAPAccount> getAccounts() {
        activate(ActivationPurpose.READ);
        return ImmutableList.copyOf(accountList);
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
