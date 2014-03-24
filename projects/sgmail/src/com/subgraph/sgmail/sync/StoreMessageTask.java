package com.subgraph.sgmail.sync;

import com.subgraph.sgmail.messages.StoredIMAPFolder;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.search.MessageSearchIndex;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreMessageTask implements Runnable {
    private final static Logger logger = Logger.getLogger(StoreMessageTask.class.getName());
    private final StoredIMAPMessage message;
    private final MessageSearchIndex searchIndex;
    private final StoredIMAPFolder folder;

    StoreMessageTask(StoredIMAPMessage message, StoredIMAPFolder folder, MessageSearchIndex searchIndex) {
        this.message = message;
        this.folder = folder;
        this.searchIndex = searchIndex;
    }

    @Override
    public void run() {
        folder.addMessage(message);
        try {
            searchIndex.addMessage(message);
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException indexing message: "+ e, e);
        }
    }
}
