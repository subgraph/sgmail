package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.model.AbstractActivatable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StoredMessageSummary extends AbstractActivatable {

    private final StoredMessageRawData messageRawData;
    private final long uniqueMessageId;
    private final String subject;
    private final String displayText;
    private final MessageUser sender;
    private final List<MessageUser> recipients;
    private final List<MessageAttachment> attachments;

    StoredMessageSummary(Builder builder) {
        this.messageRawData = builder.messageRawData;
        this.uniqueMessageId = builder.uniqueMessageId;
        this.subject = builder.subject;
        this.displayText = builder.displayText;
        this.sender = builder.sender;
        this.recipients = builder.recipients;
        this.attachments = builder.attachments;
    }

    byte[] getRawMessageBytes() {
        activate(ActivationPurpose.READ);
        return messageRawData.getMessageBytes();
    }

    String getSubject() {
        activate(ActivationPurpose.READ);
        return subject;
    }

    String getDisplayText() {
        activate(ActivationPurpose.READ);
        return displayText;
    }

    MessageUser getSender() {
        activate(ActivationPurpose.READ);
        return sender;
    }

    List<MessageUser> getRecipients() {
        activate(ActivationPurpose.READ);
        return recipients;
    }

    List<MessageAttachment> getAttachments() {
        activate(ActivationPurpose.READ);
        return attachments;
    }

    InputStream getRawMessageStream() {
        return new ByteArrayInputStream(getRawMessageBytes());
    }

    long getUniqueMessageId() {
        activate(ActivationPurpose.READ);
        return uniqueMessageId;
    }

    static class Builder {
        private StoredMessageRawData messageRawData;
        private long uniqueMessageId;
        private String subject;
        private String displayText;
        private MessageUser sender;
        private List<MessageUser> recipients;
        private List<MessageAttachment> attachments;

        Builder(byte[] rawBytes) {
            messageRawData = new StoredMessageRawData(rawBytes);
            recipients = new ArrayList<>();
            attachments = new ArrayList<>();
        }

        Builder uniqueMessageId(long value) { uniqueMessageId = value; return this; }
        Builder subject(String value) { subject = value; return this; }
        Builder displayText(String value) { displayText = value; return this; }
        Builder sender(MessageUser value) { sender = value; return this; }
        Builder addRecipient(MessageUser value) { recipients.add(value); return this; }
        Builder addAttachment(MessageAttachment value) { attachments.add(value); return this; }

        StoredMessageSummary build() {
            return new StoredMessageSummary(this);
        }
    }
}
