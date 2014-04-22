package com.subgraph.sgmail.openpgp;

import org.bouncycastle.bcpg.HashAlgorithmTags;

public class MessageProcessingPreferences {
	private final static int DEFAULT_SIGNING_HASH = HashAlgorithmTags.SHA512;

	public int getSigningDigest() {
		return DEFAULT_SIGNING_HASH;
	}

}
