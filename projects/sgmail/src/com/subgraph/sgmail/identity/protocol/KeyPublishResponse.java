package com.subgraph.sgmail.identity.protocol;

public class KeyPublishResponse implements Message {
	
	public static KeyPublishResponse fromProtocolMessage(Protocol.KeyPublishResponse message) {
		return new KeyPublishResponse(message.getIsSuccess(), message.getErrorMessage());
	}

	private final boolean isSuccess;
	private final String errorMessage;
	
	KeyPublishResponse(boolean isSuccess, String errorMessage) {
		this.isSuccess = isSuccess;
		this.errorMessage = errorMessage;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public Protocol.KeyPublishResponse toProtocolMessage() {
		Protocol.KeyPublishResponse.Builder builder = Protocol.KeyPublishResponse.newBuilder();
		builder.setIsSuccess(isSuccess);
		if(errorMessage != null && !errorMessage.isEmpty()) {
			builder.setErrorMessage(errorMessage);
		}
		return builder.build();
	}
}
