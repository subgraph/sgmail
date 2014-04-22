package com.subgraph.sgmail.accounts;

import com.subgraph.sgmail.accounts.impl.PasswordAuthenticationCredentialsImpl;
import com.subgraph.sgmail.accounts.impl.ServerDetailsImpl;

import javax.mail.Store;

public interface ServerDetails {
    static ServerDetails create(String protocol, String hostname, String onionHostname, int port, String login, String password) {
        final AuthenticationCredentials credentials = new PasswordAuthenticationCredentialsImpl(login, password);
        return new ServerDetailsImpl(protocol, hostname, onionHostname, port, credentials);
    }
    String getProtocol();
    String getHostname();
    String getOnionHostname();
    String getConnectHostname();
    int getPort();
    AuthenticationCredentials getCredentials();
    boolean isPasswordAuthentication();
    String getLogin();
    String getPassword();
    Store createRemoteStore();
}
