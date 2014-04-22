package com.subgraph.sgmail.internal.identity;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

public class KeyGenerationResult {

	private final PGPSecretKeyRing secretKeyRing;
	private final PGPPublicKeyRing publicKeyRing;
	
	private final boolean isErrorResult;
	private final String errorMessage;
	
	
	KeyGenerationResult(PGPSecretKeyRing secretKeyRing, PGPPublicKeyRing publicKeyRing) {
		this.secretKeyRing = secretKeyRing;
		this.publicKeyRing = publicKeyRing;
		this.isErrorResult = false;
		this.errorMessage = "";
	}
	
	KeyGenerationResult(String errorMessage) {
		this.secretKeyRing = null;
		this.publicKeyRing = null;
		this.isErrorResult = true;
		this.errorMessage = errorMessage;
	}
	
	public boolean isErrorResult() {
		return isErrorResult;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public PGPSecretKeyRing getSecretKeyRing() {
		return secretKeyRing;
	}
	
	public PGPPublicKeyRing getPublicKeyRing() {
		return publicKeyRing;
	}
}
