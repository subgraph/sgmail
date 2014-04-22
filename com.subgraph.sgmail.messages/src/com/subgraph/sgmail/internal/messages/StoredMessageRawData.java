package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.model.AbstractActivatable;

public class StoredMessageRawData extends AbstractActivatable {

    private final byte[] messageBytes;

    public StoredMessageRawData(byte[] messageBytes) {
        this.messageBytes = messageBytes;
    }

    byte[] getMessageBytes() {
        activate(ActivationPurpose.READ);
        return messageBytes;
    }
}
