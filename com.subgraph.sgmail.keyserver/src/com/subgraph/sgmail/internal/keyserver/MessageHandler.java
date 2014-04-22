package com.subgraph.sgmail.identity.server;

import com.subgraph.sgmail.identity.protocol.Message;

public interface MessageHandler {
    void handleMessage(Message message, ConnectionTask connection);
}
