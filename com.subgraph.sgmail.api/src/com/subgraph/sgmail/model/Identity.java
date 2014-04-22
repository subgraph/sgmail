package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;

public class Identity extends AbstractActivatable {
	
	private final StoredPublicIdentity publicIdentity;
	private final StoredPrivateIdentity privateIdentity;
	
	public Identity(StoredPrivateIdentity privateIdentity, StoredPublicIdentity publicIdentity) {
		this.privateIdentity = privateIdentity;
		this.publicIdentity = publicIdentity;
	}
	
	public PublicIdentity getPublicIdentity() {
		activate(ActivationPurpose.READ);
		return publicIdentity;
	}

	public PrivateIdentity getPrivateIdentity() {
		activate(ActivationPurpose.READ);
		return privateIdentity;
	}

	public boolean isPublished() {
		return false;
	}
}
