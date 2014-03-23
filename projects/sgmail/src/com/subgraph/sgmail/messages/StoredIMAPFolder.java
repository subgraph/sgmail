package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.accounts.IMAPAccount;

import java.util.List;

public interface StoredIMAPFolder extends StoredFolder {
    IMAPAccount getIMAPAccount();
    List<StoredIMAPMessage> getIMAPMessages();

    long getUIDValidity();
    void setUIDValidity(long value);

    long getUIDNext();
    void setUIDNext(long value);

    long getHighestModSeq();
    void setHighestModSeq(long value);

    List<Long> getUIDMap();
    StoredIMAPMessage getMessageByMessageNumber(int number);
    void addMessage(StoredIMAPMessage message);
    void expungeMessagesByUID(List<Long> uids);
    void commit();
}
