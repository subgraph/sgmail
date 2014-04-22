package com.subgraph.sgmail.directory.protocol;

import com.google.protobuf.ByteString;

public class KeyRegistrationFinalizeRequest implements Message {

    public static KeyRegistrationFinalizeRequest fromProtocolMessage(Protocol.KeyRegistrationFinalizeRequest msg) {
        return new KeyRegistrationFinalizeRequest(msg.getRequestId(), msg.getHash().toByteArray());
    }

    private final long requestId;
    private final byte[] hashValue;

    public KeyRegistrationFinalizeRequest(long requestId, byte[] hashValue) {
        this.requestId = requestId;
        this.hashValue = hashValue;
    }

    public long getRequestId() {
        return requestId;
    }

    public byte[] getHashValue() {
        return hashValue;
    }

    public Protocol.KeyRegistrationFinalizeRequest toProtocolMessage() {
        return Protocol.KeyRegistrationFinalizeRequest.newBuilder()
                .setRequestId(requestId)
                .setHash(ByteString.copyFrom(hashValue))
                .build();
    }
}
