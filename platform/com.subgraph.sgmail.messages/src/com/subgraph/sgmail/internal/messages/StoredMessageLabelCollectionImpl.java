package com.subgraph.sgmail.internal.messages;

import static com.google.common.base.Preconditions.checkNotNull;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableArrayList;
import com.db4o.ta.Activatable;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.messages.StoredMessageLabelCollection;

import java.util.List;

public class StoredMessageLabelCollectionImpl implements StoredMessageLabelCollection, Storeable, Activatable {

    private final Account account;
    private List<StoredMessageLabel> labels = new ActivatableArrayList<>();
    private int currentLabelIndex = 1;
    
	private transient Database database;
	private transient Activator activator;

    public StoredMessageLabelCollectionImpl(Account account) {
        this.account = account;
    }

    @Override
    public synchronized List<StoredMessageLabel> getLabels() {
        activate(ActivationPurpose.READ);
        return ImmutableList.copyOf(labels);
    }

    @Override
    public synchronized StoredMessageLabel getLabelByName(String name) {
        activate(ActivationPurpose.READ);
        for(StoredMessageLabel l: labels) {
            if(l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }

    public synchronized StoredMessageLabel createNewLabel(String name) {
        activate(ActivationPurpose.READ);
        final StoredMessageLabel label = new StoredMessageLabelImpl(account, name, currentLabelIndex);
        currentLabelIndex += 1;
        database.store(label);
        labels.add(label);
        database.commit();
        return label;
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
