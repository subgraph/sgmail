package com.subgraph.sgmail.internal.identity;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.subgraph.sgmail.IRandom;
import com.subgraph.sgmail.identity.KeyGenerationParameters;
import com.subgraph.sgmail.identity.PrivateIdentity;

public class KeyGenerationParametersImpl implements KeyGenerationParameters {
	
	private final IRandom random;
	private final ListeningExecutorService executor;
	
	private String comment = "";
	private String realName = "";
	private String emailAddress = null;
	private KeyType keyType = DEFAULT_KEY_TYPE;
	private KeyLength encryptionKeyLength = DEFAULT_ENCRYPTION_KEY_LENGTH;
	private KeyLength signingKeyLength = DEFAULT_SIGNING_KEY_LENGTH;
	private int expiryValue;
	private ExpiryPeriod expiryPeriod;
	
	public KeyGenerationParametersImpl(IRandom random, ListeningExecutorService executor) {
		this.random = random;
		this.executor = executor;
	}
	
	@Override
	public KeyGenerationParameters comment(String value) { 
		this.comment = value;
		return this;
	}
	
	@Override
	public String getComment() {
		return comment;
	}
	
	@Override
	public KeyGenerationParameters realName(String value) {
		this.realName = value;
		return this;
	}
	
	@Override
	public String getRealName() {
		return realName;
	}

	@Override
	public KeyGenerationParameters emailAddress(String value) {
		this.emailAddress = value;
		return this;
	}
	
	@Override
	public String getEmailAddress() {
		return emailAddress;
	}

	@Override
	public KeyGenerationParameters expiry(int value, ExpiryPeriod period) {
		this.expiryValue = value;
		this.expiryPeriod = period;
		return this;
	}
	
	@Override
	public int getExpiryValue() {
		return expiryValue;
	}
	
	@Override
	public ExpiryPeriod getExpiryPeriod() {
		return expiryPeriod;
	}

	@Override
	public KeyGenerationParameters keyType(KeyType value) {
		this.keyType = value;
		return this;
	}
	
	@Override
	public KeyType getKeyType() {
		return keyType;
	}

	@Override
	public KeyGenerationParameters signingKeyLength(KeyLength value) {
		this.signingKeyLength = value;
		return this;
	}

	@Override
	public KeyLength getSigningKeyLength() {
		return signingKeyLength;
	}
	
	@Override
	public KeyGenerationParameters encryptionKeyLength(KeyLength value) {
		this.encryptionKeyLength = value;
		return this;
	}
	
	@Override
	public KeyLength getEncryptionKeyLength() {
		return encryptionKeyLength;
	}

	@Override
	public ListenableFuture<PrivateIdentity> generate() {
		return executor.submit( new KeyGenerationTask(random, this) );
	}
}
