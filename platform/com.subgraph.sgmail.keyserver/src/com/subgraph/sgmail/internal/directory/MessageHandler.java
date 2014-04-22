package com.subgraph.sgmail.internal.directory;

import com.subgraph.sgmail.directory.protocol.Message;


public interface MessageHandler {
    void handleMessage(Message message, ConnectionTask connection);
}
