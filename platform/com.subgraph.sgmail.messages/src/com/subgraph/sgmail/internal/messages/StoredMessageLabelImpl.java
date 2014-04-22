package com.subgraph.sgmail.internal.messages;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.messages.StoredMessageLabel;

public class StoredMessageLabelImpl implements StoredMessageLabel, Activatable {

    private final Account account;
    private final String name;
    private final int index;
    
	private transient Activator activator;

    public StoredMessageLabelImpl(Account account, String name, int index) {
        this.account = account;
        this.name = name;
        this.index = index;
    }

    @Override
    public Account getAccount() {
        activate(ActivationPurpose.READ);
        return account;
    }

    @Override
    public String getName() {
        activate(ActivationPurpose.READ);
        return name;
    }

    @Override
    public int getIndex() {
        activate(ActivationPurpose.READ);
        return index;
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

}
