package com.subgraph.sgmail.internal.identity.gpg;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.primitives.UnsignedLongs;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.internal.identity.PublicKeyValidator;

import org.bouncycastle.openpgp.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class GnuPGKeyringLoader {
	private final static Logger logger = Logger.getLogger(GnuPGKeyringLoader.class.getName());

	private final static String DEFAULT_PUBLIC_KEYRING_NAME = "pubring.gpg";
	private final static String DEFAULT_SECRET_KEYRING_NAME = "secring.gpg";
	private final static File DEFAULT_GNUPG_DIRECTORY = 
			new File(System.getProperty("user.home") + File.separator + ".gnupg");
	
	private final File gpgDirectory;
	private final String publicKeyringFilename;
	private final String secretKeyringFilename;
	
	private final List<PublicIdentity> publicIdentities = new ArrayList<>();
	private final List<PrivateIdentity> privateIdentities = new ArrayList<>();
	
	private boolean isLoaded;

	public GnuPGKeyringLoader() {
		this(DEFAULT_GNUPG_DIRECTORY);
	}
	
	public GnuPGKeyringLoader(File gpgDirectory) {
		this(gpgDirectory, DEFAULT_PUBLIC_KEYRING_NAME, DEFAULT_SECRET_KEYRING_NAME);
	}
	
	public GnuPGKeyringLoader(File gpgDirectory, String publicKeyringFilename, String secretKeyringFilename) {
		this.gpgDirectory = gpgDirectory;
		this.publicKeyringFilename = publicKeyringFilename;
		this.secretKeyringFilename = secretKeyringFilename;
	}
	
	public synchronized List<PublicIdentity> getPublicIdentities() {
		if(!isLoaded) {
			reloadKeyring();
		}
		return ImmutableList.copyOf(publicIdentities);
	}
	
	public synchronized List<PrivateIdentity> getPrivateIdentities() {
		if(!isLoaded) {
			reloadKeyring();
		}
		return ImmutableList.copyOf(privateIdentities);
	}
	
	public synchronized void reloadKeyring() {
		reloadPublicIdentities();
		reloadPrivateIdentities();
		isLoaded = true;
	}
	
	private void reloadPublicIdentities() {
		final List<PublicIdentity> ids = new ArrayList<>();
		for(PGPPublicKeyRing pkr: loadPublicKeys()) {
			if(PublicKeyValidator.validate(pkr, false)) {
				ids.add(new GnuPGPublicIdentity(pkr));
			}
		}
		publicIdentities.clear();
		publicIdentities.addAll(ids);
	}

    private PublicIdentity getPublicIdentityByKeyId(long keyId) {
        for(PublicIdentity pub: publicIdentities) {
            if(pub.getPGPPublicKeyRing().getPublicKey().getKeyID() == keyId) {
                return pub;
            }
        }
        return null;
    }

	private void reloadPrivateIdentities() {
		final List<PrivateIdentity> ids = new ArrayList<>();
		for(PGPSecretKeyRing skr: loadSecretKeys()) {
            PublicIdentity pub = getPublicIdentityByKeyId(skr.getPublicKey().getKeyID());
            if(pub == null) {
                logger.warning("No public key found for keyid "+ UnsignedLongs.toString(skr.getPublicKey().getKeyID(), 16));
            } else {
                ids.add(new GnuPGPrivateIdentity(skr, pub.getPGPPublicKeyRing()));
            }
		}
		privateIdentities.clear();
		privateIdentities.addAll(ids);
	}
	
	private List<PGPPublicKeyRing> loadPublicKeys() {
		final File publicKeyringPath = new File(gpgDirectory, publicKeyringFilename);
		try {
			return getKeysFromKeyRingCollection( loadPublicKeyCollection(publicKeyringPath) );
		} catch (IOException e) {
			logger.warning("IOException loading public keyring "+ publicKeyringPath + ": "+ e.getMessage());
		} catch (PGPException e) {
			logger.warning("PGPException parsing public keyring "+ publicKeyringPath + " : "+ e.getMessage());
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
	
	private PGPPublicKeyRingCollection loadPublicKeyCollection(File publicKeyringPath) throws IOException, PGPException {
		final ByteSource byteSource = Files.asByteSource(publicKeyringPath);
		return new PGPPublicKeyRingCollection(byteSource.read());
	}
	
	private List<PGPSecretKeyRing> loadSecretKeys() {
		final File secretKeyringPath = new File(gpgDirectory, secretKeyringFilename);
		try {
			return loadSecretKeysFromKeyRingCollection( loadSecretKeyCollection(secretKeyringPath));
		} catch (IOException e) {
			logger.warning("IOException loading secret keyring "+ secretKeyringPath + ": "+ e.getMessage());
		} catch (PGPException e) {
			logger.warning("PGPException parsing secret keyring "+ secretKeyringPath + ": "+ e.getMessage());
		}
		return Collections.emptyList();
	}

	private List<PGPSecretKeyRing> loadSecretKeysFromKeyRingCollection(PGPSecretKeyRingCollection collection) {
		final List<PGPSecretKeyRing> keys = new ArrayList<>();
		final Iterator<?> it = collection.getKeyRings();
		while(it.hasNext()) {
			keys.add(((PGPSecretKeyRing) it.next()));
		}
		return keys;
	}

	private PGPSecretKeyRingCollection loadSecretKeyCollection(File secretKeyringPath) throws IOException, PGPException  {
		final ByteSource byteSource = Files.asByteSource(secretKeyringPath);
		return new PGPSecretKeyRingCollection(byteSource.read());
	}
}
