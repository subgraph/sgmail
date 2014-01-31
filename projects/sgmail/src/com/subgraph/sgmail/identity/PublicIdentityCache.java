package com.subgraph.sgmail.identity;

import java.util.ArrayList;
import java.util.List;

import com.subgraph.sgmail.model.Model;

public class PublicIdentityCache {
	
	private final Model model;
	
	private final GnuPGKeyringLoader localKeys = new GnuPGKeyringLoader();
	
	public PublicIdentityCache(Model model) {
		this.model = model;
	}

	public List<PublicIdentity> findKeysFor(String emailAddress) {
		final List<PublicIdentity> result = new ArrayList<>();
		for(PublicIdentity pk: localKeys.getLocalKeys()) {
			if(keyMatchesEmail(pk, emailAddress)) {
				result.add(pk);
			}
		}
		for(PublicIdentity pk: model.getStoredPublicIdentities()) {
			if(keyMatchesEmail(pk, emailAddress)) {
				result.add(pk);
			}
		}
		return result;
	}
	
	public List<PublicIdentity> findBestKeysFor(String emailAddress) {
		return findKeysFor(emailAddress);
	}
	
	private boolean keyMatchesEmail(PublicIdentity key, String emailAddress) {
		final String a = "<"+ emailAddress + ">";
		for(String s: key.getUserIds()) {
			if(s.trim().equalsIgnoreCase(emailAddress) || s.trim().toLowerCase().contains(a.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
