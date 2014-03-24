package com.subgraph.sgmail.messages.impl;

import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;

import java.util.List;

public class StoredIMAPMessageBuilder implements StoredIMAPMessage.Builder {
    private final StoredIMAPMessageSummary.Builder summaryBuilder;
    private long conversationId;
    private long messageDate;
    private long flags;
    private List<MessageAttachment> messageAttachments;
    private List<StoredMessageLabel> messageLabels;

    public StoredIMAPMessageBuilder(byte[] rawBytes) {
        this.summaryBuilder = new StoredIMAPMessageSummary.Builder(rawBytes);
    }

    @Override
    public StoredIMAPMessage.Builder messageUID(long value) {
        summaryBuilder.messageUID(value);
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder subject(String value) {
        summaryBuilder.subject(value);
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder displayText(String value) {
        summaryBuilder.displayText(value);
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder uniqueMessageId(long value) {
        summaryBuilder.uniqueMessageId(value);
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder conversationId(long value) {
        conversationId = value;
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder messageDate(long value) {
        this.messageDate = value;
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder flags(long value) {
        this.flags = value;
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder sender(MessageUser value) {
        summaryBuilder.sender(value);
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder recipients(List<MessageUser> value) {
        for (MessageUser user : value) {
            summaryBuilder.addRecipient(user);
        }
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder attachments(List<MessageAttachment> value) {
        messageAttachments = value;
        return this;
    }

    @Override
    public StoredIMAPMessage.Builder labels(List<StoredMessageLabel> value) {
        messageLabels = value;
        return this;
    }

    @Override
    public StoredIMAPMessage build() {
        if(messageAttachments != null) {
            for (MessageAttachment attachment : messageAttachments) {
                summaryBuilder.addAttachment(attachment);
            }
        }
        final StoredIMAPMessageSummary summary = new StoredIMAPMessageSummary(summaryBuilder);
        final StoredIMAPMessage message = new StoredIMAPMessageImpl(conversationId, messageDate, summary);
        message.setFlags(flags);
        if(messageLabels != null) {
            for (StoredMessageLabel label : messageLabels) {
                message.addLabel(label);
            }
        }
        return message;
    }
}
