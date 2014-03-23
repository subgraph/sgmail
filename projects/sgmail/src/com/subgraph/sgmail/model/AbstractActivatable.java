package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;

import static com.google.common.base.Preconditions.checkNotNull;

public class AbstractActivatable implements Activatable {
	
	private transient Activator activator;

	protected transient Model model;
	
	void onActivate(Model model) {
		this.model = checkNotNull(model);
        initialize();
	}

    protected void initialize() {

    }
	
	public void commit() {
		model.commit();
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
