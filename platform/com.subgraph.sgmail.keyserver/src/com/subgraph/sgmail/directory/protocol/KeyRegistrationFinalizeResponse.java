package com.subgraph.sgmail.directory.protocol;

public class KeyRegistrationFinalizeResponse implements Message {

    public static KeyRegistrationFinalizeResponse fromProtocolMessage(Protocol.KeyRegistrationFinalizeResponse msg) {
        return new KeyRegistrationFinalizeResponse(msg.getIsSuccess(), msg.getErrorMessage());
    }

    private final boolean isSuccess;
    private final String errorMessage;

    public KeyRegistrationFinalizeResponse(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Protocol.KeyRegistrationFinalizeResponse toProtocolMessage() {
        return Protocol.KeyRegistrationFinalizeResponse.newBuilder()
                .setIsSuccess(isSuccess)
                .setErrorMessage(errorMessage)
                .build();
    }
}
