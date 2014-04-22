package com.subgraph.sgmail.messages;

import java.util.List;

public interface MessageAttachment {
    List<Integer> getMimePath();
    String getMimePrimaryType();
    String getMimeSubType();
    String getFilename();
    String getDescription();
    long getFileLength();
}
