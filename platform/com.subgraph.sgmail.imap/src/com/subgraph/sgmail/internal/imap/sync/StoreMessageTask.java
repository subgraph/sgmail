package com.subgraph.sgmail.internal.imap.sync;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.subgraph.sgmail.imap.LocalIMAPFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.search.MessageSearchIndex;

public class StoreMessageTask implements Runnable {
    private final static Logger logger = Logger.getLogger(StoreMessageTask.class.getName());
    private final StoredMessage message;
    private final long messageUID;
    private final MessageSearchIndex searchIndex;
    private final LocalIMAPFolder folder;

    StoreMessageTask(StoredMessage message, long messageUID, LocalIMAPFolder folder, MessageSearchIndex searchIndex) {
        this.message = message;
        this.messageUID = messageUID;
        this.folder = folder;
        this.searchIndex = searchIndex;
    }

    @Override
    public void run() {
        try {
            storeMessage();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected exception in StoreMessageTask: "+ e, e);
        }
    }

    private void storeMessage() {
        folder.appendMessage(message, messageUID);
        try {
            searchIndex.addMessage(message);
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException indexing message: "+ e, e);
        }
    }
}
