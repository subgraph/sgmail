package com.subgraph.sgmail.accounts;

import com.subgraph.sgmail.accounts.impl.PasswordAuthenticationCredentialsImpl;
import com.subgraph.sgmail.accounts.impl.SMTPAccountImpl;

public class Accounts {
    public static AuthenticationCredentials createPasswordCredential(String username, String password) {
        return new PasswordAuthenticationCredentialsImpl(username, password);
    }
    public static SMTPAccount createSMTPAccount(String hostname, int port, String login, String password) {
        return new SMTPAccountImpl(hostname, port, login, password);
    }
}
