package com.subgraph.sgmail.identity;

import org.bouncycastle.openpgp.PGPSecretKeyRing;

public interface PrivateIdentity {
	PGPSecretKeyRing getPGPSecretKeyRing();
	PublicIdentity getPublicIdentity();
}
