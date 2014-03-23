package com.subgraph.sgmail.messages;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public interface StoredIMAPMessage extends StoredMessage {
    StoredIMAPFolder getIMAPFolder();
    long getMessageUID();
    int getMessageNumber();
    void setMessageNumber(int value);
    MimeMessage toMimeMessage() throws MessagingException;
}
