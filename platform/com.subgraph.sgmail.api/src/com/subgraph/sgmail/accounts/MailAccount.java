package com.subgraph.sgmail.accounts;

public interface MailAccount extends Account {
    ServerDetails getSMTPAccount();
    String getEmailAddress();
    String getDomain();
    String getRealname();
}
