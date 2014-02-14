package com.subgraph.sgmail.identity.protocol;

public class KeyRegistrationRequest implements Message {

    public static KeyRegistrationRequest fromProtocolMessage(Protocol.KeyRegistrationRequest msg) {
        return new KeyRegistrationRequest(
                msg.getKey().getKeyData().toByteArray(),
                msg.getEmailAddress());
    }

    private final byte[] keyData;
    private final String emailAddress;

    public KeyRegistrationRequest(byte[] keyData, String emailAddress) {
        this.keyData = keyData;
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public byte[] getKeyData() {
        return keyData;
    }

    public Protocol.KeyRegistrationRequest toProtocolMessage() {
        PublicKeyData pkd = new PublicKeyData(keyData);
        final Protocol.KeyRegistrationRequest.Builder builder = Protocol.KeyRegistrationRequest.newBuilder();
        builder.setEmailAddress(emailAddress);
        builder.setKey(pkd.toProtocolMessage());
        return builder.build();
    }

}
