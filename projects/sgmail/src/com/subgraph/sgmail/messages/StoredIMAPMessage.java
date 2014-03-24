package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.messages.impl.StoredIMAPMessageBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

public interface StoredIMAPMessage extends StoredMessage {
    StoredIMAPFolder getIMAPFolder();
    long getMessageUID();
    int getMessageNumber();
    void setMessageNumber(int value);
    MimeMessage toMimeMessage() throws MessagingException;

    interface Builder {
        static Builder create(byte[] rawBytes) {
            return new StoredIMAPMessageBuilder(rawBytes);
        }
        Builder messageUID(long value);
        Builder subject(String value);
        Builder displayText(String value);
        Builder uniqueMessageId(long value);
        Builder conversationId(long value);
        Builder messageDate(long value);
        Builder flags(long value);
        Builder sender(MessageUser value);
        Builder recipients(List<MessageUser> value);
        Builder attachments(List<MessageAttachment> value);
        Builder labels(List<StoredMessageLabel> value);
        StoredIMAPMessage build();
    }
}
