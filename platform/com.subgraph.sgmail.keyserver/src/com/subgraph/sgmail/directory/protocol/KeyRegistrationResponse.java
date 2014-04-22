package com.subgraph.sgmail.directory.protocol;

public class KeyRegistrationResponse implements Message {

    public static KeyRegistrationResponse fromProtocolMessage(Protocol.KeyRegistrationResponse msg) {
        return new KeyRegistrationResponse(msg.getIsSuccess(), msg.getRequestId(), msg.getErrorMessage());
    }

    public static KeyRegistrationResponse createErrorResponse(String message) {
        return new KeyRegistrationResponse(false, 0, message);
    }

    public static KeyRegistrationResponse createSuccessResponse(long requestId) {
        return new KeyRegistrationResponse(true, requestId, "");
    }

    private final boolean isSuccess;
    private final long requestId;
    private final String errorMessage;

    KeyRegistrationResponse(boolean isSuccess, long requestId, String errorMessage) {
        this.isSuccess = isSuccess;
        this.requestId = requestId;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public long getRequestId() {
        return requestId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Protocol.KeyRegistrationResponse toProtocolMessage() {
        return Protocol.KeyRegistrationResponse.newBuilder()
                .setIsSuccess(isSuccess)
                .setRequestId(requestId)
                .setErrorMessage(errorMessage)
                .build();
    }

}
