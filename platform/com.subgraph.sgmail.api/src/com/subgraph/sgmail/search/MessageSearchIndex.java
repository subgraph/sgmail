package com.subgraph.sgmail.search;

import com.subgraph.sgmail.messages.StoredMessage;

import java.io.File;
import java.io.IOException;

public interface MessageSearchIndex {
	void setIndexDirectory(File indexDirectory);
    void addMessage(StoredMessage message) throws IOException;
    void removeMessage(StoredMessage message) throws IOException;
    SearchResult search(String queryString) throws IOException;
    void commit();
    void close();
}
