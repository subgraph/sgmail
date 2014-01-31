package com.subgraph.sgmail.identity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

public class GnuPGPublicIdentity implements PublicIdentity {

	private final PGPPublicKeyRing publicKeyRing;
	
	private List<String> cachedUserIds;
	
	GnuPGPublicIdentity(PGPPublicKeyRing pkr) {
		this.publicKeyRing = pkr;
	}

	@Override
	public PGPPublicKeyRing getPGPPublicKeyRing() {
		return publicKeyRing;
	}
	
	public synchronized List<String> getUserIds() {
		if(cachedUserIds == null) {
			cachedUserIds = generateUserIds();
		}
		return cachedUserIds;
	}
	
	private List<String> generateUserIds() {
		final List<String> uids = new ArrayList<>();
		final PGPPublicKey pk = publicKeyRing.getPublicKey();
		if(pk != null) {
			final Iterator<?> it = pk.getUserIDs();
			while(it.hasNext()) {
				uids.add((String) it.next());
			}
		}
		return uids;
	}

	@Override
	public int getKeySource() {
		return KEY_SOURCE_LOCAL_KEYRING;
	}
}
