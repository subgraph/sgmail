package com.subgraph.sgmail.identity.protocol;

public class KeyLookupRequest implements Message {
	
	public static KeyLookupRequest fromProtocolMessage(Protocol.KeyLookupRequest msg) {
		return new KeyLookupRequest(msg.getEmail(), msg.getFingerprint());
	}
    public static KeyLookupRequest createLookupByEmail(String emailAddress) {
        return new KeyLookupRequest(emailAddress, null);
    }

	private final String emailAddress;
	private final String fingerprint;
	
	private KeyLookupRequest(String emailAddress, String fingerprint) {
		this.emailAddress = emailAddress;
		this.fingerprint = fingerprint;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public String getFingerprint() {
		return fingerprint;
	}

	public Protocol.KeyLookupRequest toProtocolMessage() {
		final Protocol.KeyLookupRequest.Builder builder = Protocol.KeyLookupRequest.newBuilder();
		if(emailAddress != null && !emailAddress.isEmpty()) {
			builder.setEmail(emailAddress);
		}
		if(fingerprint != null && !fingerprint.isEmpty()) {
			builder.setFingerprint(fingerprint);
		}
		return builder.build();
	}
}
