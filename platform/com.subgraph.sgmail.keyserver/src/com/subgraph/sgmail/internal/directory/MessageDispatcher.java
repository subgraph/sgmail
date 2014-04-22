package com.subgraph.sgmail.internal.directory;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.subgraph.sgmail.directory.protocol.Message;

public class MessageDispatcher {
    private final static Logger logger = Logger.getLogger(MessageDispatcher.class.getName());

    private Map<Class<? extends Message>, MessageHandler> handlerMap = new HashMap<>();

    public <T extends Message> void addHandler(Class<T> messageClass, MessageHandler handler) {
        handlerMap.put(messageClass, handler);
    }

    public <T extends Message> MessageHandler findHandler(Class<T> messageClass) {
        return handlerMap.get(messageClass);
    }

    public void handleMessage(Message message, ConnectionTask connection) {
        final MessageHandler handler = findHandler(message.getClass());
        if(handler == null) {
            logger.warning("No handler registered for message type "+ message.getClass().getName());
        } else {
            handler.handleMessage(message, connection);
        }
    }
}
