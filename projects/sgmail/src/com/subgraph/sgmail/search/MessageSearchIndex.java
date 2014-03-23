package com.subgraph.sgmail.search;

import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.search.impl.MessageSearchIndexImpl;

import java.io.File;
import java.io.IOException;

public interface MessageSearchIndex {
    static MessageSearchIndex create(File indexDirectory) {
        return new MessageSearchIndexImpl(indexDirectory);
    }
    void addMessage(StoredMessage message) throws IOException;
    void removeMessage(StoredMessage message) throws IOException;
    SearchResult search(String queryString) throws IOException;
    void commit();
    void close();
}
