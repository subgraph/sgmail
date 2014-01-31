package com.subgraph.sgmail.model;

import java.io.IOException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;

public class StoredPrivateIdentity extends AbstractActivatable implements PrivateIdentity {

	private final byte[] bytes;
	private final StoredPublicIdentity publicIdentity;
	
	private transient PGPSecretKeyRing cachedKeyRing;
		
	public StoredPrivateIdentity(byte[] bytes, StoredPublicIdentity publicIdentity) {
		this.bytes = bytes;
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
}
