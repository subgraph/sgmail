package com.subgraph.sgmail.messages;

import ca.odell.glazedlists.EventList;

import java.util.List;

public interface StoredFolder {
    String getName();
    void rename(String newName);
    List<StoredMessage> getMessages();
    EventList<StoredMessage> getMessageEventList();
    boolean hasNewMessages();
    int getMessageCount();
    void addMessage(StoredMessage message);
    void expungeMessages();
    void clearFolder();
}
