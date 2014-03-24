package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.messages.impl.MessageUserImpl;

public interface MessageUser {
    static MessageUser createSender(String username, String address) {
        return new MessageUserImpl(username, address, UserType.USER_SENDER);
    }
    static MessageUser createTo(String username, String address) {
        return new MessageUserImpl(username, address, UserType.USER_TO);
    }
    static MessageUser createCC(String username, String address) {
        return new MessageUserImpl(username, address, UserType.USER_CC);
    }
    static MessageUser create(String username, String address, UserType type) {
        return new MessageUserImpl(username, address, type);
    }

    enum UserType { USER_SENDER, USER_TO, USER_CC }
    String getUsername();
    String getAddress();
    UserType getType();
}
