package com.subgraph.sgmail.identity.client;


import org.bouncycastle.openpgp.PGPPublicKeyRing;

public class KeyLookupResult {


    static KeyLookupResult createErrorResult(String message) {
        return new KeyLookupResult(null, null, false, true, message);
    }

    static KeyLookupResult createSuccessResult(PGPPublicKeyRing pkr, byte[] keyData) {
        return new KeyLookupResult(pkr, keyData, false, false, "");
    }

    static KeyLookupResult createNotFoundResult() {
        return new KeyLookupResult(null, null, true, false, "");
    }


    private final PGPPublicKeyRing keyRing;
    private final byte[] keyData;

    private final boolean isNotFoundResult;
    private final boolean isErrorResult;
    private final String errorMessage;

    private KeyLookupResult(PGPPublicKeyRing keyRing, byte[] keyData, boolean isNotFoundResult, boolean isErrorResult, String errorMessage) {
        this.keyRing = keyRing;
        this.keyData = keyData;
        this.isNotFoundResult = isNotFoundResult;
        this.isErrorResult = isErrorResult;
        this.errorMessage = errorMessage;
    }

    public PGPPublicKeyRing getPublicKeyRing() {
        return keyRing;
    }

    public byte[] getKeyData() {
        return keyData;
    }

    public boolean isErrorResult() {
        return isErrorResult;
    }

    public boolean isNotFoundResult() { return isNotFoundResult; }

    public String getErrorMessage() {
        return errorMessage;
    }
}
