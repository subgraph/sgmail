package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableHashSet;
import com.google.common.collect.ImmutableSet;
import com.subgraph.sgmail.messages.*;
import com.subgraph.sgmail.model.AbstractActivatable;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class StoredMessageImpl extends AbstractActivatable implements StoredMessage {

    private final long conversationId;
    private final long messageDate;
    protected final StoredMessageSummary summary;

    private StoredFolder folder;
    private long flags;
    private Set<StoredMessageLabel> labels = new ActivatableHashSet<>();

    protected StoredMessageImpl(long conversationId, long messageDate, StoredMessageSummary summary) {
        this.conversationId = conversationId;
        this.messageDate = messageDate;
        this.summary = summary;
    }

    @Override
    public long getUniqueMessageId() {
        activate(ActivationPurpose.READ);
        return summary.getUniqueMessageId();
    }

    public long getConversationId() {
       activate(ActivationPurpose.READ);
       return conversationId;
    }

    public long getMessageDate() {
        activate(ActivationPurpose.READ);
        return messageDate;
    }

    @Override
    public StoredFolder getFolder() {
        activate(ActivationPurpose.READ);
        return folder;
    }

    @Override
    public void setFolder(StoredFolder folder) {
        activate(ActivationPurpose.WRITE);
        this.folder = folder;
    }

    @Override
    public void addLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.WRITE);
        labels.add(label);
    }

    @Override
    public void removeLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.WRITE);
        labels.remove(label);
    }

    @Override
    public Set<StoredMessageLabel> getLabels() {
        activate(ActivationPurpose.READ);
        return ImmutableSet.copyOf(labels);
    }

    @Override
    public String getSubject() {
        activate(ActivationPurpose.READ);
        return summary.getSubject();
    }

    @Override
    public String getDisplayText() {
        activate(ActivationPurpose.READ);
        return summary.getDisplayText();
    }

    @Override
    public List<MessageAttachment> getAttachments() {
        activate(ActivationPurpose.READ);
        return summary.getAttachments();
    }

    @Override
    public MessageUser getSender() {
        activate(ActivationPurpose.READ);
        return summary.getSender();
    }

    @Override
    public List<MessageUser> getRecipients() {
        activate(ActivationPurpose.READ);
        return summary.getRecipients();
    }

    @Override
    public boolean isFlagSet(long flag) {
        activate(ActivationPurpose.READ);
        return (flags & flag) == flag;
    }

    @Override
    public long getFlags() {
        activate(ActivationPurpose.READ);
        return flags;
    }

    @Override
    public void setFlags(long value) {
        activate(ActivationPurpose.WRITE);
        this.flags = value;
    }

    @Override
    public void addFlag(long flag) {
        setFlags(getFlags() | flag);
    }

    @Override
    public void removeFlag(long flag) {
        setFlags(getFlags() & ~flag);
    }

    @Override
    public byte[] getRawMessageBytes() {
        activate(ActivationPurpose.READ);
        return summary.getRawMessageBytes();
    }

    @Override
    public InputStream getRawMessageStream() {
        activate(ActivationPurpose.READ);
        return summary.getRawMessageStream();
    }
}
