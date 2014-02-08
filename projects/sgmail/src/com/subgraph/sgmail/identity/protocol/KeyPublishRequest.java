package com.subgraph.sgmail.identity.protocol;

public class KeyPublishRequest implements Message {
	
	public static KeyPublishRequest fromProtocolMessage(Protocol.KeyPublishRequest message) {
		final PublicKeyData keyData = PublicKeyData.fromProtocolMessage(message.getKey());
		return new KeyPublishRequest(keyData);
	}

	private final PublicKeyData keyData;
	
	public KeyPublishRequest(PublicKeyData keyData) {
		this.keyData = keyData;
	}
	
	public PublicKeyData getKeyData() {
		return keyData;
	}
	
	public Protocol.KeyPublishRequest toProtocolMessage() {
		return Protocol.KeyPublishRequest.newBuilder()
				.setKey(keyData.toProtocolMessage())
				.build();
	}
}
