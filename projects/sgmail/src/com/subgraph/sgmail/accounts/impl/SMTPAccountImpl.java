package com.subgraph.sgmail.accounts.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.accounts.AuthenticationCredentials;
import com.subgraph.sgmail.accounts.PasswordAuthenticationCredentials;
import com.subgraph.sgmail.accounts.SMTPAccount;
import com.subgraph.sgmail.model.AbstractActivatable;

public class SMTPAccountImpl extends AbstractActivatable implements SMTPAccount {
	
	private final String hostname;
    private final int port;
    private final AuthenticationCredentials authenticationCredentials;

	public SMTPAccountImpl(String hostname, int port, String login, String password) {
		this.hostname = hostname;
		this.port = port;
        this.authenticationCredentials = new PasswordAuthenticationCredentialsImpl(login, password);
	}
	
	public String getHostname() {
		activate(ActivationPurpose.READ);
		return hostname;
	}
	
	public int getPort() {
		activate(ActivationPurpose.READ);
		return port;
	}
	
	public String getUsername() {
        return getPasswordAuth().getLogin();
	}
	
	public String getPassword() {
        return getPasswordAuth().getPassword();
	}

    private PasswordAuthenticationCredentials getPasswordAuth() {
        activate(ActivationPurpose.READ);
        if(!(authenticationCredentials instanceof PasswordAuthenticationCredentials)) {
            throw new IllegalStateException("SMTPAccount credentials are not password authentication: "+ authenticationCredentials);
        }
        return (PasswordAuthenticationCredentials) authenticationCredentials;
    }
}
