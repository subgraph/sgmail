package com.subgraph.sgmail.accounts;

import javax.mail.Session;
import javax.mail.Store;

public interface ServerDetails {
    String getProtocol();
    String getHostname();
    String getOnionHostname();
    int getPort();
    AuthenticationCredentials getCredentials();
    boolean isPasswordAuthentication();
    String getLogin();
    String getPassword();
    Store createRemoteStore(Session session, boolean preferOnionAddress);
}
