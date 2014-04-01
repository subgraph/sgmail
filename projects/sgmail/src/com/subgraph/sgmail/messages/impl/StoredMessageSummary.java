package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.google.common.base.Charsets;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.model.AbstractActivatable;
import com.subgraph.sgmail.model.LocalMimeMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class StoredMessageSummary extends AbstractActivatable {

    private final byte[] subject;
    private final byte[] bodySnippet;
    private final MessageUser sender;
    private final StoredMessageContent content;
    private int referenceCount;

    private transient MimeMessage cachedMimeMessage;

    StoredMessageSummary(StoredMessageBuilder builder, StoredMessageContent content) {
        this(builder.subject.getBytes(Charsets.UTF_8), builder.bodySnippet.getBytes(Charsets.UTF_8), builder.sender, content);
    }

    StoredMessageSummary(byte[] subject, byte[] bodySnippet, MessageUser sender, StoredMessageContent content) {
        this.subject = subject;
        this.bodySnippet = bodySnippet;
        this.sender = sender;
        this.content = content;
    }

    byte[] getRawMessageBytes() {
        activate(ActivationPurpose.READ);
        return content.getRawMessageBytes();
    }

    String getSubject() {
        activate(ActivationPurpose.READ);
        return new String(subject, Charsets.UTF_8);
    }

    String getBodySnippet() {
        activate(ActivationPurpose.READ);
        return new String(bodySnippet, Charsets.UTF_8);
    }

    String getBodyText() {
        activate(ActivationPurpose.READ);
        return content.getBodyText();
    }

    MessageUser getSender() {
        activate(ActivationPurpose.READ);
        return sender;
    }

    List<MessageUser> getToRecipients() {
        activate(ActivationPurpose.READ);
        return content.getToRecipients();
    }

    List<MessageUser> getCCRecipients() {
        activate(ActivationPurpose.READ);
        return content.getCCRecipients();
    }

    List<MessageAttachment> getAttachments() {
        activate(ActivationPurpose.READ);
        return content.getAttachments();
    }

    InputStream getRawMessageStream() {
        return new ByteArrayInputStream(getRawMessageBytes());
    }


    synchronized MimeMessage toMimeMessage(StoredMessage msg) throws MessagingException {
        activate(ActivationPurpose.READ);
        if (cachedMimeMessage == null) {
            cachedMimeMessage = new LocalMimeMessage(msg, model.getSession(), getRawMessageStream());
        }
        return cachedMimeMessage;
    }

    synchronized int incrementReferenceCount() {
        referenceCount += 1;
        return referenceCount;
    }

    synchronized int decrementReferenceCount() {
        if(referenceCount <= 0) {
            throw new IllegalStateException("decrementReferenceCount() called while referenceCount == "+ referenceCount);
        }
        referenceCount -= 1;
        return referenceCount;

    }

    synchronized int getReferenceCount() {
        return referenceCount;
    }
}
