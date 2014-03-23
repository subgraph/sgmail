package com.subgraph.sgmail.accounts;

import com.subgraph.sgmail.messages.StoredIMAPFolder;

import java.util.List;

public interface IMAPAccount extends MailAccount {
    void setAutomaticSyncEnabled(boolean value);
    boolean isAutomaticSyncEnabled();
    boolean isGmailAccount();
    List<StoredIMAPFolder> getFolders();
    StoredIMAPFolder getFolderByName(String name);
}
