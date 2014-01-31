package com.subgraph.sgmail.identity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

public class GnuPGKeyringLoader {
	private final static Logger logger = Logger.getLogger(GnuPGKeyringLoader.class.getName());
	
	private final File keyringPath;
	private final List<PublicIdentity> localIdentities = new ArrayList<>();
	
	private boolean isLoaded;
	
	public GnuPGKeyringLoader() {
		this(getDefaultKeyringPath());
	}
	
	public GnuPGKeyringLoader(File keyringPath) {
		this.keyringPath = keyringPath;
	}
	
	public List<PublicIdentity> getLocalKeys() {
		if(!isLoaded) {
			reloadKeyring();
		}
		return ImmutableList.copyOf(localIdentities);
	}
	
	public void reloadKeyring() {
		final List<PublicIdentity> identities = new ArrayList<>();
		
		for(PGPPublicKeyRing pkr: loadPublicKeys()) {
			if(PublicKeyValidator.validate(pkr)) {
				identities.add(new GnuPGPublicIdentity(pkr));
			}
		}
		
		localIdentities.clear();
		localIdentities.addAll(identities);
		isLoaded = true;
		
	}
	
	private List<PGPPublicKeyRing> loadPublicKeys() {
		try {
			return getKeysFromKeyRingCollection( loadPublicKeyCollection() );
		} catch (IOException e) {
			logger.warning("IOException loading keyring "+ keyringPath + ": "+ e.getMessage());
		} catch (PGPException e) {
			logger.warning("PGPException parsing keyring "+ keyringPath + " : "+ e.getMessage());
		}
		return Collections.emptyList();
	}
	
	private List<PGPPublicKeyRing> getKeysFromKeyRingCollection(PGPPublicKeyRingCollection collection) {
		final List<PGPPublicKeyRing> keys = new ArrayList<>();
		final Iterator<?> it = collection.getKeyRings();
		while(it.hasNext()) {
			keys.add((PGPPublicKeyRing) it.next());
		}
		return keys;
	}
	
	private PGPPublicKeyRingCollection loadPublicKeyCollection() throws IOException, PGPException {
		final ByteSource byteSource = Files.asByteSource(keyringPath);
		return new PGPPublicKeyRingCollection(byteSource.read());
	}

	private static File getDefaultKeyringPath() {
		final File home = new File(System.getProperty("user.home"));
		return new File(home, ".gnupg/pubring.gpg");
	}
}
