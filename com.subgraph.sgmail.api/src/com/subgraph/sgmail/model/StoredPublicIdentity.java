package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.identity.OpenPGPKeyUtils;
import com.subgraph.sgmail.identity.PublicIdentity;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import java.io.IOException;
import java.util.List;

public class StoredPublicIdentity extends AbstractActivatable implements PublicIdentity {
	
	private byte[] bytes;
	private final int keySource;

    private transient OpenPGPKeyUtils cachedKeyUtils;

	public StoredPublicIdentity(byte[] bytes, int keySource) {
		this.bytes = bytes;
		this.keySource = keySource;
	}

    public synchronized void updateKeyBytes(byte[] bytes) {
        activate(ActivationPurpose.WRITE);
        this.bytes = bytes;
        cachedKeyUtils = null;
    }

	@Override
	public int getKeySource() {
		activate(ActivationPurpose.READ);
        return keySource;
	}

    private synchronized OpenPGPKeyUtils getKeyUtils() {
        activate(ActivationPurpose.READ);
        if(cachedKeyUtils == null) {
            try {
                cachedKeyUtils = OpenPGPKeyUtils.createFromPublicKeyBytes(bytes);
            } catch (IOException e) {
                throw new IllegalStateException("IOException decoding StoredPublicIdentity key bytes: "+ e);
            }
        }
        return cachedKeyUtils;
    }

	@Override
	public synchronized PGPPublicKeyRing getPGPPublicKeyRing() {
        return getKeyUtils().getPublicKeyRing();
	}

    @Override
    public synchronized List<PGPPublicKey> getPublicKeys() {
        return getKeyUtils().getPublicKeys();
    }

    @Override
	public synchronized List<String> getUserIds() {
        return getKeyUtils().getUserIDs();
	}

	@Override
	public byte[] getImageData() {
        return getKeyUtils().getImageData();
	}
}
