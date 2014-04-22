package com.subgraph.sgmail.identity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import com.subgraph.sgmail.model.Model;

public class PublicIdentityCache {
	
	private final Model model;
	
	private final GnuPGKeyringLoader localKeys = new GnuPGKeyringLoader();
	
	public PublicIdentityCache(Model model) {
		this.model = model;
	}

	public List<PublicIdentity> findKeysFor(String emailAddress) {
		final List<PublicIdentity> result = new ArrayList<>();
		for(PublicIdentity pk: localKeys.getPublicIdentities()) {
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
	
	public PrivateIdentity findPrivateKey(String emailAddress) {
		for(PrivateIdentity pv: getLocalPrivateIdentities()) {
			if(searchPublicKeys(pv.getPGPSecretKeyRing(), emailAddress)) {
				return pv;
			}
		}
		return null;
	}
	
	private boolean searchPublicKeys(PGPSecretKeyRing skr, String emailAddress) {
		Iterator<?> it = skr.getPublicKeys();
		while(it.hasNext()) {
			PGPPublicKey pk = (PGPPublicKey) it.next();
			if(searchIds(pk, emailAddress)) {
				return true;
			}
			
		}
		return false;
		
	}
	
	private boolean searchIds(PGPPublicKey pk, String emailAddress) {
		Iterator<?> it = pk.getUserIDs();
		while(it.hasNext()) {
			String id = (String) it.next();
			if(id.trim().equalsIgnoreCase(emailAddress) || id.trim().toLowerCase().contains(emailAddress.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	public List<PrivateIdentity> getLocalPrivateIdentities() {
		return localKeys.getPrivateIdentities();
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
