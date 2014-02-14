package com.subgraph.sgmail.identity.server;

public class PublicKeyRecord {

    private final byte[] keyData;

    PublicKeyRecord(byte[] keyData) {
        this.keyData = keyData;
    }

    byte[] getKeyData() {
        return keyData;
    }
}
