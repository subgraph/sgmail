package com.subgraph.sgmail.identity.protocol;

import com.google.protobuf.ByteString;

public class PublicKeyData {
	
	public static PublicKeyData fromProtocolMessage(Protocol.PublicKey message) {
		return new PublicKeyData(message.getKeyData().toByteArray());
	}

	private final byte[] keyBytes;
	
	public PublicKeyData(byte[] keyBytes) {
		this.keyBytes = keyBytes;
	}
	
	public byte[] getKeyBytes() {
		return keyBytes;
	}
	
	public Protocol.PublicKey toProtocolMessage() {
		return Protocol.PublicKey.newBuilder()
				.setKeyData(ByteString.copyFrom(keyBytes))
				.build();
	}
}
