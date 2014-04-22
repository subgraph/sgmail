package com.subgraph.sgmail.identity;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import java.util.List;

public class GnuPGPublicIdentity implements PublicIdentity {

	private final PGPPublicKeyRing publicKeyRing;
    private final OpenPGPKeyUtils keyUtils;
	

	GnuPGPublicIdentity(PGPPublicKeyRing pkr) {
		this.publicKeyRing = pkr;
        this.keyUtils = new OpenPGPKeyUtils(pkr);
	}

	@Override
	public PGPPublicKeyRing getPGPPublicKeyRing() {
		return publicKeyRing;
	}

    @Override
    public List<PGPPublicKey> getPublicKeys() {
        return keyUtils.getPublicKeys();
    }

    public synchronized List<String> getUserIds() {
        return keyUtils.getUserIDs();
	}

	@Override
	public int getKeySource() {
		return KEY_SOURCE_LOCAL_KEYRING;
	}

	@Override
	public byte[] getImageData() {
        return keyUtils.getImageData();
	}
}
