package com.subgraph.sgmail.messages;


import com.subgraph.sgmail.accounts.Account;

public interface StoredMessageLabel {
    Account getAccount();
    String getName();
}
