package com.subgraph.sgmail.messages;

import ca.odell.glazedlists.EventList;

public interface StoredFolder {
    String getName();
    void rename(String newName);
    EventList<StoredMessage> getMessageEventList();
    int getMessageCount();
    void addMessage(StoredMessage message);
    void expungeMessages();
    void clearFolder();
}
