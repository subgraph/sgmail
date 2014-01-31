package com.subgraph.sgmail.identity;

import java.util.ArrayList;
import java.util.List;

import com.subgraph.sgmail.model.Model;

public class PublicKeyCache {
	
	private final Model model;
	private final LocalPublicKeys localKeys = new LocalPublicKeys();
	
	public PublicKeyCache(Model model) {
		this.model = model;
	}

	List<PublicKey> findKeysFor(String emailAddress) {
		final List<PublicKey> result = new ArrayList<>();
		for(PublicKey pk: localKeys.getLocalKeys()) {
			if(keyMatchesEmail(pk, emailAddress)) {
				result.add(pk);
			}
		}
		return result;
	}
	
	List<PublicKey> findBestKeysFor(String emailAddress) {
		return findKeysFor(emailAddress);
	}
	
	private boolean keyMatchesEmail(PublicKey key, String emailAddress) {
		final String a = "<"+ emailAddress + ">";
		for(String s: key.getUserIds()) {
			if(s.trim().equalsIgnoreCase(emailAddress) || s.trim().toLowerCase().contains(a.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
