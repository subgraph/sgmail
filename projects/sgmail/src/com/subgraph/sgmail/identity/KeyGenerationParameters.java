package com.subgraph.sgmail.identity;


public class KeyGenerationParameters {
	private final static KeyLength DEFAULT_SIGNING_KEY_LENGTH = KeyLength.KEY_2048;
	private final static KeyLength DEFAULT_ENCRYPTION_KEY_LENGTH = KeyLength.KEY_2048;
	private final static KeyType DEFAULT_KEY_TYPE = KeyType.RSA_AND_RSA;
	
	public enum KeyType { RSA_AND_RSA, DSA_AND_ELGAMAL, DSA_SIGN_ONLY, RSA_SIGN_ONLY }
	public enum ExpiryPeriod { DAYS, WEEKS, MONTHS, YEARS }
	public enum KeyLength { KEY_1024, KEY_2048, KEY_4096, KEY_8192 };
	
	
	
	private KeyLength signingKeyLength = DEFAULT_SIGNING_KEY_LENGTH;
	private KeyLength encryptionKeyLength = DEFAULT_ENCRYPTION_KEY_LENGTH;
	private KeyType keyType = DEFAULT_KEY_TYPE;
	
	private ExpiryPeriod expiryPeriod;
	private int expiry = 0;
	private String realName = "";
	private String emailAddress = "";
	
	public String getRealName() {
		return realName;
	}
	
	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public ExpiryPeriod getExpiryPeriod() {
		return expiryPeriod;
	}
	
	public int getExpiryValue() {
		return expiry;
	}
	
	public void setExpiry(int value, ExpiryPeriod period) {
		this.expiryPeriod = period;
		this.expiry = value;
	}
	
	public void setNoExpiry() {
		this.expiry = 0;
		this.expiryPeriod = null;
	}
	
	public KeyLength getSigningKeyLength() {
		return signingKeyLength;
	}
	
	public KeyLength getEncryptionKeyLength() {
		return encryptionKeyLength;
	}
	
	public KeyType getKeyType() {
		return keyType;
	}
}
