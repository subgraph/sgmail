package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.google.common.base.Strings;
import com.subgraph.sgmail.ui.MessageBodyUtils;
import com.sun.mail.imap.IMAPMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StoredIMAPMessageSummary extends StoredMessageSummary {

    private final long messageUID;
    private int messageNumber;

    StoredIMAPMessageSummary(Builder builder) {
        super(builder);
        this.messageUID = builder.messageUID;
    }

    long getMessageUID() {
        activate(ActivationPurpose.READ);
        return messageUID;
    }

    int getMessageNumber() {
        activate(ActivationPurpose.READ);
        return messageNumber;
    }

    void setMessageNumber(int value) {
        activate(ActivationPurpose.WRITE);
        this.messageNumber = value;
    }

    static class Builder extends StoredMessageSummary.Builder {
        private long messageUID;

        public Builder messageUID(long value) { messageUID = value; return this; }

        public Builder(byte[] rawBytes) {
            super(rawBytes);
        }

        public StoredIMAPMessageSummary build() {
            return new StoredIMAPMessageSummary(this);
        }
    }

   public static StoredIMAPMessageSummary createForJavamailMessage(MimeMessage message, long uniqueMessageId, long messageUID) throws MessagingException {
       final Builder builder = new Builder(getRawMessageBody(message));
       builder.subject(getSubject(message));
       builder.displayText(MessageBodyUtils.getTextBody(message));
       builder.messageUID(messageUID);
       builder.uniqueMessageId(uniqueMessageId);
       return builder.build();
    }

    private static byte[] getRawMessageBody(MimeMessage message) throws MessagingException {
        if(message instanceof IMAPMessage) {
            ((IMAPMessage) message).setPeek(true);
        }
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            message.writeTo(output);
            return output.toByteArray();
        } catch(IOException e) {
            throw new MessagingException("IOException reading message body "+ e, e);
        }
    }

    private static String getSubject(MimeMessage message) throws MessagingException {
        return Strings.nullToEmpty(message.getSubject());
    }

}
