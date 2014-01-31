package com.subgraph.sgmail.identity;

import java.io.File;
import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

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
			try {
				reloadKeyring();
			} catch (IOException | PGPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ImmutableList.copyOf(localIdentities);
	}
	
	public void reloadKeyring() throws IOException, PGPException {
		System.out.println("loading");
		final ByteSource byteSource = Files.asByteSource(keyringPath);
		final PGPPublicKeyRingCollection keyrings = new PGPPublicKeyRingCollection(byteSource.read());
		final Iterator<?> it = keyrings.getKeyRings("com", true);
		final List<GnuPGPublicIdentity> keys = new ArrayList<>();
		while(it.hasNext()) {
			PGPPublicKeyRing pkr = (PGPPublicKeyRing) it.next();
			verifyPublicKeyRing(pkr);
			keys.add(new GnuPGPublicIdentity(pkr));
		}
		localIdentities.clear();
		localIdentities.addAll(keys);
		isLoaded = true;
	}
	
	private void verifyPublicKeyRing(PGPPublicKeyRing pkr) {
		final PGPPublicKey master = getMasterKey(pkr);
		final Iterator<?> it = pkr.getPublicKeys();
		while(it.hasNext()) {
			PGPPublicKey pk = (PGPPublicKey) it.next();
			if(pk.isMasterKey()) {
				continue;
			}
			try {
				if(verifyBindingSignature(master, pk)) {
					System.out.println("verify");
				}
			} catch (SignatureException | PGPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

				

		}
	}
	
	private PGPPublicKey getMasterKey(PGPPublicKeyRing pkr) {
		Iterator<?> it = pkr.getPublicKeys();
		while(it.hasNext()) {
			PGPPublicKey pk = (PGPPublicKey) it.next();
			if(pk.isMasterKey()) {
				return pk;
			}
		}
		return null;
	}

	private boolean verifyBindingSignature(PGPPublicKey master, PGPPublicKey sub) throws SignatureException, PGPException {
		final PGPSignature signature = getBindingSignature(master, sub);
		if(signature == null) {
			System.out.println("not found");
		// XXX
			return false;
		}
		signature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), master);
		return signature.verifyCertification(master, sub);
	}
	
	private PGPSignature getBindingSignature(PGPPublicKey master, PGPPublicKey sub) throws PGPException, SignatureException {
		final Iterator<?> it = sub.getSignatures();
		//System.out.println("master = "+ master.getKeyID());
		while(it.hasNext()) {
			PGPSignature sig = (PGPSignature) it.next();
			//System.out.println("keyid: "+ sig.getKeyID() + " type: "+ sig.getSignatureType());
			if(sig.getKeyID() == master.getKeyID() && sig.getSignatureType() == PGPSignature.SUBKEY_BINDING) {
				return sig;
				//sig.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), master);
				//if(!sig.verifyCertification(master, sub))	 {
					
				//}
			}
		}
		return null;
	}
	private static File getDefaultKeyringPath() {
		final File home = new File(System.getProperty("user.home"));
		return new File(home, ".gnupg/pubring.gpg");
	}

}
