package com.subgraph.sgmail.internal.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;

public class UniqueIdGenerator implements Storeable, Activatable {

    private int currentId;
    
	private transient Activator activator;
	private transient Database database;
	
    public synchronized int next() {
        activate(ActivationPurpose.WRITE);
        currentId += 1;
        database.commit();
        return currentId;
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
