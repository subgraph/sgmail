package com.subgraph.sgmail.directory.protocol;

import com.google.protobuf.ByteString;

public class PublicKeyData {
	public static int PUBLIC_KEY_TYPE_IDENTITY_SERVER = 1;

	public static PublicKeyData fromProtocolMessage(Protocol.PublicKey message) {
		return new PublicKeyData(message.getKeyData().toByteArray());
	}

	private final byte[] keyBytes;
	
	public PublicKeyData(byte[] keyBytes) {
		this.keyBytes = keyBytes;
	}

    public int getKeyType() {
        return 1;
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
