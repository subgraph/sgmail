package com.subgraph.sgmail.identity.protocol;

import java.util.ArrayList;
import java.util.List;

public class KeyLookupResponse implements Message {
	
	public static KeyLookupResponse fromProtocolMessage(Protocol.KeyLookupResponse msg) {
		final KeyLookupResponse response = new KeyLookupResponse();
		for(Protocol.PublicKey pk: msg.getKeysList()) {
			response.addPublicKey( PublicKeyData.fromProtocolMessage(pk));
		}
		return response;
	}

	private final List<PublicKeyData> keys = new ArrayList<PublicKeyData>();
	
	public void addPublicKey(PublicKeyData pk) {
		keys.add(pk);
	}

	public List<PublicKeyData> getKeys() {
		return keys;
	}

	public Protocol.KeyLookupResponse toProtocolMessage() {
		final Protocol.KeyLookupResponse.Builder builder = Protocol.KeyLookupResponse.newBuilder();
		for(PublicKeyData pkd: keys) {
			builder.addKeys(pkd.toProtocolMessage());
		}
		return builder.build();
	}
}

