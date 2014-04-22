package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.google.common.base.Charsets;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.model.AbstractActivatable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StoredMessageContent extends AbstractActivatable {
    private final byte[] bodyText;
    private final MessageUser[] toRecipients;
    private final MessageUser[] ccRecipients;
    private final MessageAttachment[] attachments;
    private final StoredMessageRawData rawData;

    StoredMessageContent(StoredMessageBuilder builder) {
        this(builder.bodyText.getBytes(Charsets.UTF_8), builder.getToRecipients(), builder.getCCRecipients(), builder.getAttachmentArray(), builder.rawDataBytes);
    }

    StoredMessageContent(byte[] bodyText, MessageUser[] toRecipients, MessageUser[] ccRecipients, MessageAttachment[] attachments, byte[] rawDataBytes) {
        this.bodyText = bodyText;
        this.toRecipients = toRecipients;
        this.ccRecipients = ccRecipients;
        this.attachments = attachments;
        this.rawData = new StoredMessageRawData(rawDataBytes);
    }

    byte[] getRawMessageBytes() {
        activate(ActivationPurpose.READ);
        return rawData.getMessageBytes();
    }

    String getBodyText() {
        activate(ActivationPurpose.READ);
        return new String(bodyText, Charsets.UTF_8);
    }

    List<MessageUser> getToRecipients() {
        activate(ActivationPurpose.READ);
        return getRecipientList(toRecipients);
    }

    List<MessageUser> getCCRecipients() {
        activate(ActivationPurpose.READ);
        return getRecipientList(ccRecipients);
    }

    List<MessageAttachment> getAttachments() {
        activate(ActivationPurpose.READ);
        if(attachments == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(attachments);
        }
    }

    private static List<MessageUser> getRecipientList(MessageUser[] users) {
        if(users == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(users);
        }
    }
}
