package com.subgraph.sgmail.identity;

import java.util.List;

import org.bouncycastle.openpgp.PGPPublicKeyRing;

public interface PublicKey {
	final static int KEY_SOURCE_LOCAL_KEYRING = 1;
	final static int KEY_SOURCE_MANUAL_IMPORT = 2;
	final static int KEY_SOURCE_PUBLIC_KEYSERVER = 3;
	
	int getKeySource();
	PGPPublicKeyRing getPGPPublicKeyRing();
	List<String> getUserIds();

}
