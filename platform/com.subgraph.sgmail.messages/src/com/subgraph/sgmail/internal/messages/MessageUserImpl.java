package com.subgraph.sgmail.internal.messages;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.messages.MessageUser;

import static com.google.common.base.Preconditions.checkNotNull;

public class MessageUserImpl implements MessageUser, Activatable {

    private final String username;
    private final String address;
    
	private transient Activator activator;

    public MessageUserImpl(String username, String address) {
        this.username = username;
        this.address = checkNotNull(address);
    }

    @Override
    public String getUsername() {
        activate(ActivationPurpose.READ);
        return username;
    }

    @Override
    public String getAddress() {
        activate(ActivationPurpose.READ);
        return address;
    }
    @Override
	public String getText(boolean verbose) {
		activate(ActivationPurpose.READ);
		if(username != null && !username.isEmpty()) {
			if(verbose) {
				return username + " <"+ address +">";
			} else {
				return username;
			}
		} else {
			return address;
		}
	}
	
	@Override
	public String toString() {
		return getText(true);
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
