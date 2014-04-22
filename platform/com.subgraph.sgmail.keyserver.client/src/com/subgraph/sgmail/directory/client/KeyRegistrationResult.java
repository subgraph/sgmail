package com.subgraph.sgmail.directory.client;

public class KeyRegistrationResult {

    private final boolean isError;
    private final String errorMessage;

    KeyRegistrationResult() {
        this.isError = false;
       this.errorMessage = "";
    }

    KeyRegistrationResult(String errorMessage) {
        this.isError = true;
        this.errorMessage = errorMessage;
    }

    public boolean isError() {
        return isError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
