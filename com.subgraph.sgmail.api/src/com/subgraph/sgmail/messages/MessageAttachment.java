package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.messages.impl.MessageAttachmentImpl;

import java.util.List;

public interface MessageAttachment {
    static MessageAttachment create(List<Integer> mimePath, String primaryType, String subType, String filename, String description, long length) {
        return new MessageAttachmentImpl.Builder()
                .mimePath(mimePath)
                .mimePrimaryType(primaryType)
                .mimeSubType(subType)
                .filename(filename)
                .description(description)
                .length(length)
                .build();
    }

    List<Integer> getMimePath();
    String getMimePrimaryType();
    String getMimeSubType();
    String getFilename();
    String getDescription();
    long getFileLength();
}
