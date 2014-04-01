package com.subgraph.sgmail.accounts;

import com.subgraph.sgmail.accounts.impl.BasicMailAccount;

public interface MailAccount extends Account {
    static MailAccount create(String label, String emailAddress, String realName, ServerDetails smtpServer) {
        return new BasicMailAccount(label, emailAddress, realName, smtpServer);
    }
    ServerDetails getSMTPAccount();
    String getEmailAddress();
    String getDomain();
    String getRealname();

}
