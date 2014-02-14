package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.identity.AbstractPrivateIdentity;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.IOException;

public class StoredPrivateIdentity extends AbstractPrivateIdentity implements PrivateIdentity, Activatable {

	private final byte[] bytes;
	private String passphrase;
	private final StoredPublicIdentity publicIdentity;
	
	private transient PGPSecretKeyRing cachedKeyRing;
	private transient Activator activator;
		
	public StoredPrivateIdentity(byte[] bytes, String passphrase, StoredPublicIdentity publicIdentity) {
		this.bytes = bytes;
		this.passphrase = passphrase;
		this.publicIdentity = publicIdentity;
	}
	
	@Override
	public synchronized PGPSecretKeyRing getPGPSecretKeyRing() {
		if(cachedKeyRing == null) {
			cachedKeyRing = createSecretKeyRing();
		}
		return cachedKeyRing;
	}

	@Override
	public PublicIdentity getPublicIdentity() {
		activate(ActivationPurpose.READ);
		return publicIdentity;
	}

	private PGPSecretKeyRing createSecretKeyRing() {
		activate(ActivationPurpose.READ);
		try {
			return new PGPSecretKeyRing(bytes, new JcaKeyFingerprintCalculator());
		} catch (IOException e) {
			throw new IllegalStateException("IOException creating key ring "+ e.getMessage(), e);
		} catch (PGPException e) {
			throw new IllegalStateException("PGPException creating key ring "+ e.getMessage(), e);
		}
	}

	@Override
	public String getPassphrase() {
		activate(ActivationPurpose.READ);
		return passphrase;
	}

    @Override
    public boolean isValidPassphrase(String passphrase) {
        return false;
    }

    @Override
	public void setPassphrase(String passphrase) {
		activate(ActivationPurpose.WRITE);
		this.passphrase = passphrase;
	}
	
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}
}
