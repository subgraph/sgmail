package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.PublicKeyDecoder;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import java.io.IOException;
import java.util.List;

public class StoredPublicIdentity extends AbstractActivatable implements PublicIdentity {
	
	private final byte[] bytes;
	private final int keySource;

    private transient PublicKeyDecoder cachedDecoder;

	public StoredPublicIdentity(byte[] bytes, int keySource) {
		this.bytes = bytes;
		this.keySource = keySource;
	}

	@Override
	public int getKeySource() {
		activate(ActivationPurpose.READ);
        return keySource;
	}

    private synchronized PublicKeyDecoder getDecoder() {
        activate(ActivationPurpose.READ);
        if(cachedDecoder == null) {
            try {
                cachedDecoder = PublicKeyDecoder.createFromBytes(bytes);
            } catch (IOException e) {
                throw new IllegalStateException("IOException decoding StoredPublicIdentity key bytes: "+ e);
            }
        }
        return cachedDecoder;
    }

	@Override
	public synchronized PGPPublicKeyRing getPGPPublicKeyRing() {
        return getDecoder().getPublicKeyRing();
	}

    @Override
    public synchronized List<PGPPublicKey> getPublicKeys() {
        return getDecoder().getPublicKeys();
    }

	@Override
	public synchronized List<String> getUserIds() {
        return getDecoder().getUserIDs();
	}

	@Override
	public byte[] getImageData() {
        return getDecoder().getImageData();
	}
}
