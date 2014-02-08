package com.subgraph.sgmail.identity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.bcpg.attr.ImageAttribute;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUserAttributeSubpacketVector;

public class GnuPGPublicIdentity implements PublicIdentity {

	private final PGPPublicKeyRing publicKeyRing;
	
	private List<String> cachedUserIds;
	
	private byte[] imageBytes;
	private boolean imageGenerated = false;
	
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

	@Override
	public byte[] getImageBytes() {
		if(!imageGenerated) {
			imageBytes = generateImageBytes();
		}
		return imageBytes;
	}
	
	private byte[] generateImageBytes() {
		final PGPPublicKeyRing pkr = getPGPPublicKeyRing();
		final Iterator<?> it = pkr.getPublicKey().getUserAttributes();
		while(it.hasNext()) {
			PGPUserAttributeSubpacketVector uasv = (PGPUserAttributeSubpacketVector) it.next();
			ImageAttribute image = uasv.getImageAttribute();
			if(image != null) {
				return image.getImageData();
			}
		}
		return null;
	}
}
