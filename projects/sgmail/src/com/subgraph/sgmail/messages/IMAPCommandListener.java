package com.subgraph.sgmail.messages;

public interface IMAPCommandListener {
    void commandAdded(StoredIMAPFolder folder, IMAPCommand command);
}
