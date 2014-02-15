package com.subgraph.sgmail.identity;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

public interface PrivateIdentity {
	boolean containsKeyId(long keyId);
	PGPPrivateKey getPrivateKeyByKeyId(long keyId) throws OpenPGPException;
	void setPassphrase(String passphrase) throws OpenPGPException;
	String getPassphrase();
    void addImageData(byte[] imageData);
    boolean isValidPassphrase(String passphrase);
	PGPPrivateKey getSigningKey() throws OpenPGPException;
	PGPPrivateKey getEncryptionKey() throws OpenPGPException;
	PGPSecretKeyRing getPGPSecretKeyRing();
	PublicIdentity getPublicIdentity();
}
