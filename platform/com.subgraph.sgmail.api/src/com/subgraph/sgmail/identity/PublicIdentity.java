package com.subgraph.sgmail.identity;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import java.util.List;

public interface PublicIdentity {
    final static int KEY_SOURCE_USER_GENERATED = 1;
	final static int KEY_SOURCE_LOCAL_KEYRING = 2;
    final static int KEY_SOURCE_IDENTITY_SERVER = 3;
	final static int KEY_SOURCE_MANUAL_IMPORT = 4;
	final static int KEY_SOURCE_PUBLIC_KEYSERVER = 5;
	
	int getKeySource();
	PGPPublicKeyRing getPGPPublicKeyRing();
    List<PGPPublicKey> getPublicKeys();
	byte[] getImageData();
	List<String> getUserIds();
	String renderText();
}
