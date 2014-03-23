package com.subgraph.sgmail.messages;

public interface MessageUser {
    enum UserType { USER_SENDER, USER_TO, USER_CC_}
    String getUsername();
    String getAddress();
    UserType getType();
}
