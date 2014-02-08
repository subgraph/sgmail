package com.subgraph.sgmail.identity;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

public class GnuPGPrivateIdentity extends AbstractPrivateIdentity implements PrivateIdentity {

	private final static BcPBESecretKeyDecryptorBuilder decryptorBuilder = 
			new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider());
	
	private static PBESecretKeyDecryptor createDecryptor(String passphrase) {
		return decryptorBuilder.build(passphrase.toCharArray());
	}

	private final PGPSecretKeyRing secretKeyring;
	private String passphrase;
	
	public GnuPGPrivateIdentity(PGPSecretKeyRing secretKeyring) {
		this.secretKeyring = secretKeyring;
	}
	
	@Override
	public PGPSecretKeyRing getPGPSecretKeyRing() {
		return secretKeyring;
	}

	@Override
	public PublicIdentity getPublicIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean testPassphrase(String passphrase) {
		try {
			secretKeyring.getSecretKey().extractPrivateKey(createDecryptor(passphrase));
			return true;
		} catch (PGPException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void setPassphrase(String passphrase) throws OpenPGPException {
		if(!testPassphrase(passphrase)) {
			throw new OpenPGPException("Invalid passphrase for encypted GnuPGPrivateIdentity");
		}
		this.passphrase = passphrase;
	}
	
	@Override
	public String getPassphrase() {
		return passphrase;
	}
}
