package com.subgraph.sgmail.identity;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

import java.security.SignatureException;

public class GnuPGPrivateIdentity extends AbstractPrivateIdentity implements PrivateIdentity {

	private final static BcPBESecretKeyDecryptorBuilder decryptorBuilder = 
			new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider());
	
	private static PBESecretKeyDecryptor createDecryptor(String passphrase) {
		return decryptorBuilder.build(passphrase.toCharArray());
	}

	private final PGPSecretKeyRing secretKeyring;
    private final GnuPGPublicIdentity publicIdentity;
	private String passphrase;
	
	public GnuPGPrivateIdentity(PGPSecretKeyRing secretKeyring, PGPPublicKeyRing publicKeyRing) {
		this.secretKeyring = secretKeyring;
        this.publicIdentity = new GnuPGPublicIdentity(publicKeyRing);
	}
	
	@Override
	public PGPSecretKeyRing getPGPSecretKeyRing() {
		return secretKeyring;
	}

	@Override
	public PublicIdentity getPublicIdentity() {
        return publicIdentity;
	}

    @Override
	public boolean isValidPassphrase(String passphrase) {
		try {
			secretKeyring.getSecretKey().extractPrivateKey(createDecryptor(passphrase));
			return true;
		} catch (PGPException e) {
			return false;
		}
	}

	@Override
	public void setPassphrase(String passphrase) throws OpenPGPException {
		if(!isValidPassphrase(passphrase)) {
			throw new OpenPGPException("Invalid passphrase for encypted GnuPGPrivateIdentity");
		}
		this.passphrase = passphrase;
	}
	
	@Override
	public String getPassphrase() {
		return passphrase;
	}

    @Override
    public void addImageData(byte[] imageData) {
        final OpenPGPKeyUtils keyUtils = new OpenPGPKeyUtils(getPublicIdentity().getPGPPublicKeyRing(), secretKeyring, passphrase);
        try {
            keyUtils.addImageAttribute(imageData);
        } catch (PGPException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

}
