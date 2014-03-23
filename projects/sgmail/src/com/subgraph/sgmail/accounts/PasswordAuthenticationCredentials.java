package com.subgraph.sgmail.accounts;

public interface PasswordAuthenticationCredentials extends AuthenticationCredentials {
    String getLogin();
    String getPassword();
}
