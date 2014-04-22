package com.subgraph.sgmail.identity;

import java.util.List;

public interface IdentityManager {
	List<PublicIdentity> findPublicKeysByAddress(String emailAddress);
	List<PrivateIdentity> getLocalPrivateIdentities();
	PrivateIdentity findPrivateKeyByAddress(String emailAddress);
	Contact getContactByEmailAddress(String emailAddress);
	KeyGenerationParameters createKeyGenerator();
}
