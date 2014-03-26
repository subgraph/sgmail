package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.model.AbstractActivatable;

import java.util.List;
import java.util.Objects;

public class MessageAttachmentImpl extends AbstractActivatable implements MessageAttachment {

    private final List<Integer> mimePath;
    private final String mimePrimaryType;
    private final String mimeSubType;
    private final String filename;
    private final String description;
    private final long length;


    private MessageAttachmentImpl(Builder builder) {
        this.mimePath = ImmutableList.copyOf(builder.mimePath);
        this.mimePrimaryType = Objects.requireNonNull(builder.mimePrimaryType);
        this.mimeSubType = Objects.requireNonNull(builder.mimeSubType);
        this.filename = Objects.requireNonNull(builder.filename);
        this.description = Strings.nullToEmpty(builder.description);
        this.length = builder.length;
    }

    @Override
    public List<Integer> getMimePath() {
        activate(ActivationPurpose.READ);
        return mimePath;
    }

    @Override
    public String getMimePrimaryType() {
        activate(ActivationPurpose.READ);
        return mimePrimaryType;
    }

    @Override
    public String getMimeSubType() {
        activate(ActivationPurpose.READ);
        return mimeSubType;
    }

    @Override
    public String getFilename() {
        activate(ActivationPurpose.READ);
        return filename;
    }

    @Override
    public String getDescription() {
        activate(ActivationPurpose.READ);
        return description;
    }

    @Override
    public long getFileLength() {
        activate(ActivationPurpose.READ);
        return length;
    }

    public static class Builder {
        private List<Integer> mimePath;
        private String mimePrimaryType;
        private String mimeSubType;
        private String description;
        private String filename;
        private long length;

        public Builder mimePath(List<Integer> value) { this.mimePath = value; return this; }
        public Builder mimePrimaryType(String value) { this.mimePrimaryType = value; return this; }
        public Builder mimeSubType(String value) { this.mimeSubType = value; return this; }
        public Builder description(String value) { this.description = value; return this; }
        public Builder filename(String value) { this.filename = value; return this; }
        public Builder length(long value) { this.length = value; return this; }
        public MessageAttachment build() {
            return new MessageAttachmentImpl(this);
        }
    }
}
