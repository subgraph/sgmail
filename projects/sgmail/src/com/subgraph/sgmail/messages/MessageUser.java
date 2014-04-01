package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.messages.impl.MessageUserImpl;

public interface MessageUser {

    static MessageUser create(String username, String address) {
        return new MessageUserImpl(username, address);
    }

    String getUsername();
    String getAddress();
}
