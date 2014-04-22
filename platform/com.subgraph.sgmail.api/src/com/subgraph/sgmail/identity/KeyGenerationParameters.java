package com.subgraph.sgmail.identity;

import com.google.common.util.concurrent.ListenableFuture;

public interface KeyGenerationParameters {
	enum KeyType { RSA_AND_RSA, DSA_AND_ELGAMAL, DSA_SIGN_ONLY, RSA_SIGN_ONLY }
	enum ExpiryPeriod { DAYS, WEEKS, MONTHS, YEARS }
	enum KeyLength { KEY_1024, KEY_2048, KEY_4096, KEY_8192 };
	
	final static KeyLength DEFAULT_SIGNING_KEY_LENGTH = KeyLength.KEY_2048;
	final static KeyLength DEFAULT_ENCRYPTION_KEY_LENGTH = KeyLength.KEY_2048;
	final static KeyType DEFAULT_KEY_TYPE = KeyType.RSA_AND_RSA;
	
	KeyGenerationParameters comment(String value);
	KeyGenerationParameters realName(String value);
	KeyGenerationParameters emailAddress(String value);
	KeyGenerationParameters expiry(int value, ExpiryPeriod period);
	KeyGenerationParameters keyType(KeyType value);
	KeyGenerationParameters signingKeyLength(KeyLength value);
	KeyGenerationParameters encryptionKeyLength(KeyLength value);
	
	ListenableFuture<PrivateIdentity> generate();

	String getComment();
	String getRealName();
	String getEmailAddress();
	ExpiryPeriod getExpiryPeriod();
	int getExpiryValue();
	KeyType getKeyType();
	KeyLength getSigningKeyLength();
	KeyLength getEncryptionKeyLength();
	
	static class KeyGenerationException extends Exception {
		private static final long serialVersionUID = 1L;
		public KeyGenerationException(String message, Throwable cause) {
			super(message, cause);
		}

		public KeyGenerationException(String message) {
			super(message);
		}
	}
}
