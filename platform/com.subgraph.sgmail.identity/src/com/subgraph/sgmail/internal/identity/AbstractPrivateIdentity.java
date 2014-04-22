package com.subgraph.sgmail.internal.identity;

import java.util.Iterator;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

import com.subgraph.sgmail.openpgp.OpenPGPException;

public abstract class AbstractPrivateIdentity {

	transient private final static BcPBESecretKeyDecryptorBuilder decryptorBuilder = 
			new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider());
	
	private static PBESecretKeyDecryptor createDecryptor(String passphrase) {
		return decryptorBuilder.build(passphrase.toCharArray());
	}
	
	private transient PGPPrivateKey cachedSigningKey;
	private transient PGPPrivateKey cachedEncryptionKey;
	
	public abstract PGPSecretKeyRing getPGPSecretKeyRing();
	public abstract String getPassphrase();

    protected void clearCachedValues() {
        cachedSigningKey = null;
        cachedEncryptionKey = null;
    }

	public PGPPrivateKey getSigningKey() throws OpenPGPException  {
		if(cachedSigningKey == null) {
			cachedSigningKey = extractPrivateKey(getPGPSecretKeyRing().getSecretKey());
		}
		return cachedSigningKey;
	}
	
	public PGPPrivateKey getEncryptionKey() throws OpenPGPException  {
		if(cachedEncryptionKey == null) {
			cachedEncryptionKey = extractEncryptionKey();
		}
		return cachedEncryptionKey;
	}
	
	private PGPPrivateKey extractEncryptionKey() throws OpenPGPException {
		final PGPSecretKey master = getPGPSecretKeyRing().getSecretKey();
		final Iterator<?> it = getPGPSecretKeyRing().getSecretKeys();
		while(it.hasNext()) {
			PGPSecretKey sk = (PGPSecretKey) it.next();
			if(sk.getPublicKey().isEncryptionKey() && sk != master) {
				return extractPrivateKey(sk);
			}
		}
		return null;
	}

	private PGPPrivateKey extractPrivateKey(PGPSecretKey secretKey) throws OpenPGPException {
		final String passphrase = getPassphrase();
		if(passphrase == null) {
			throw new OpenPGPException("Cannot extract private key because passphrase has not been set.");
		}
		try {
			return secretKey.extractPrivateKey(createDecryptor(passphrase));
		} catch (PGPException e) {
			throw new OpenPGPException("Error extracting private key: "+ e.getMessage(), e);
		}
	}
	
	public boolean containsKeyId(long keyId) {
		return getPGPSecretKeyRing().getSecretKey(keyId) != null;
	}

	public PGPPrivateKey getPrivateKeyByKeyId(long keyId) throws OpenPGPException {
		final PGPSecretKey sk = getPGPSecretKeyRing().getSecretKey(keyId);
		if(sk == null) {
			return null;
		} else {
			return extractPrivateKey(sk);
		}
	}
}
