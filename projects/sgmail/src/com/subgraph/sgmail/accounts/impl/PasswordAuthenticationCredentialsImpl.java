package com.subgraph.sgmail.accounts.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.accounts.PasswordAuthenticationCredentials;
import com.subgraph.sgmail.model.AbstractActivatable;

public class PasswordAuthenticationCredentialsImpl extends AbstractActivatable implements PasswordAuthenticationCredentials {
    private final String login;
    private final String password;

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
}
