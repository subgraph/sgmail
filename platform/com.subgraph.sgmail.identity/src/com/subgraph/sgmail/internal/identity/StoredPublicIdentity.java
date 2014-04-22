package com.subgraph.sgmail.internal.identity;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.identity.PublicIdentity;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import java.io.IOException;
import java.util.List;

public class StoredPublicIdentity implements PublicIdentity, Activatable {
	
	private byte[] bytes;
	private final int keySource;

    private transient OpenPGPKeyUtils cachedKeyUtils;
    private transient Activator activator;

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

	@Override
	public String renderText() {
		final PublicKeyRenderer renderer = new PublicKeyRenderer(this);
		return renderer.renderPublicIdentity();
	}
}
