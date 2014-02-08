package com.subgraph.sgmail.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.identity.PublicIdentity;

public class StoredPublicIdentity extends AbstractActivatable implements PublicIdentity {
	
	
	private final byte[] bytes;
	private final int keySource;
	
	private transient PGPPublicKeyRing cachedKeyRing;
	private transient List<String> cachedUserIds;
	
	StoredPublicIdentity(byte[] bytes, int keySource) {
		this.bytes = bytes;
		this.keySource = keySource;
	}

	@Override
	public int getKeySource() {
		activate(ActivationPurpose.READ);
		return keySource;
	}

	@Override
	public synchronized PGPPublicKeyRing getPGPPublicKeyRing() {
		if(cachedKeyRing == null) {
			cachedKeyRing = createPublicKeyRing();
		}
		return cachedKeyRing;
	}

	private PGPPublicKeyRing createPublicKeyRing() {
		activate(ActivationPurpose.READ);
		try {
			return new PGPPublicKeyRing(bytes, new JcaKeyFingerprintCalculator());
		} catch (IOException e) {
			throw new IllegalStateException("IOException creating key ring "+ e.getMessage(), e);
		}
	}
	
	@Override
	public synchronized List<String> getUserIds() {
		if(cachedUserIds == null) {
			cachedUserIds = generateUserIds();
		}
		return cachedUserIds;
	}
	
	private List<String> generateUserIds() {
		final List<String> uids = new ArrayList<>();
		final PGPPublicKey pk = getPGPPublicKeyRing().getPublicKey();
		if(pk != null) {
			final Iterator<?> it = pk.getUserIDs();
			while(it.hasNext()) {
				uids.add((String) it.next());
			}
		}
		return uids;
	}

	@Override
	public byte[] getImageBytes() {
		// TODO Auto-generated method stub
		return null;
	}
}
