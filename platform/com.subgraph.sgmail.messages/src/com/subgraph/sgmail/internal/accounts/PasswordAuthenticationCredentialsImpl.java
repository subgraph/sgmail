package com.subgraph.sgmail.internal.accounts;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.accounts.PasswordAuthenticationCredentials;

public class PasswordAuthenticationCredentialsImpl implements PasswordAuthenticationCredentials, Activatable {
    private final String login;
    private final String password;
    
	private transient Activator activator;

    public PasswordAuthenticationCredentialsImpl(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getLogin() {
        activate(ActivationPurpose.READ);
        return login;
    }

    @Override
    public String getPassword() {
        activate(ActivationPurpose.READ);
        return password;
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
