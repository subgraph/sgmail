package com.subgraph.sgmail.accounts;

public interface SMTPAccount {
    String getHostname();
    int getPort();
    String getUsername();
    String getPassword();
}
