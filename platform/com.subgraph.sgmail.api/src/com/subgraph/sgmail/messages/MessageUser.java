package com.subgraph.sgmail.messages;

public interface MessageUser {
    String getUsername();
    String getAddress();
    String getText(boolean verbose);
}
