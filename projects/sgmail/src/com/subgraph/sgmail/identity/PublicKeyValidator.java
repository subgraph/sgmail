package com.subgraph.sgmail.identity;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPUserAttributeSubpacketVector;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

public class PublicKeyValidator {
	private final static Logger logger = Logger.getLogger(PublicKeyValidator.class.getName());
	
	private final static int ANY_SIGNATURE_TYPE = -1;
	
	public static boolean LOG_FAILURES = true;
	
	static boolean validate(PGPPublicKeyRing pkr) {
		final PublicKeyValidator v = new PublicKeyValidator(pkr);
		try {
			v.validate();
			return true;
		} catch (KeyValidationException e) {
			if(LOG_FAILURES) {
				logger.warning("Error validating public key ring: "+ e.getMessage());
			}
			return false;
		}
	}

	private final List<PGPPublicKey> publicKeys;
	
	PublicKeyValidator(PGPPublicKeyRing keyring) {
		this.publicKeys = extractPublicKeys(keyring);
	}
	
	private static List<PGPPublicKey> extractPublicKeys(PGPPublicKeyRing pkr) {
		final Iterator<?> it = pkr.getPublicKeys();
		final List<PGPPublicKey> keys = new ArrayList<>();
		while(it.hasNext()) {
			keys.add((PGPPublicKey) it.next());
		}
		return keys;
	}

	void validate() throws KeyValidationException {
		final PGPPublicKey master = getMasterKey();
		if(master.isRevoked()) {
			throw new KeyValidationException("Master key revoked");
		}
		
		try {
			verifySignatures(master);
		} catch (SignatureException e) {
			throw new KeyValidationException("SignatureException processing signatures: "+ e.getMessage(), e);
		} catch (PGPException e) {
			throw new KeyValidationException("PGPException processing signatures: "+ e.getMessage(), e);
		}
	}
	
	private void verifySignatures(PGPPublicKey master) throws KeyValidationException, SignatureException, PGPException {
		
		for(String uid: getUserIds()) {
			verifySelfSignatures(master, uid);
		}
		
		for(PGPUserAttributeSubpacketVector attributes: getUserAttributes()) {
			verifySelfSignatures(master, attributes);
		}
	
		for(PGPPublicKey sub: getSubordinateKeys()) {
			verifyBinding(master, sub);
		}
	}
	
	private void verifyBinding(PGPPublicKey master, PGPPublicKey sub) throws KeyValidationException, SignatureException, PGPException {
		final PGPSignature bindingSignature = getBindingSignature(master, sub);
		verifyCertification(master, sub, bindingSignature);
	}

	private void verifySelfSignatures(PGPPublicKey master, String id) throws KeyValidationException, SignatureException, PGPException {
		boolean atLeastOne = false;
		for(PGPSignature signature: getIdSignatures(master, id, master.getKeyID())) {
			verifySelfSignature(master, id, signature);
			atLeastOne = true;
		}
		if(!atLeastOne) {
			throw new KeyValidationException("No self-signature found for id "+ id);
		}
	}
	
	private void verifySelfSignatures(PGPPublicKey master, PGPUserAttributeSubpacketVector attributes) throws KeyValidationException, SignatureException, PGPException {
		boolean atLeastOne = false;
		for(PGPSignature signature: getAttributeSignatures(master, attributes, master.getKeyID())) {
			verifySelfSignature(master, attributes, signature);
			atLeastOne = true;
		}
		if(!atLeastOne) {
			throw new KeyValidationException("No self-signature found for user attributes");
		}
	}

	private void verifySelfSignature(PGPPublicKey master, String id, PGPSignature signature) throws KeyValidationException, SignatureException, PGPException {
		initializeSignature(signature, master);
		if(!signature.verifyCertification(id, master)) {
			throw new KeyValidationException("Signature verification failed.");
		}
	}

	private void verifySelfSignature(PGPPublicKey master, PGPUserAttributeSubpacketVector attributes, PGPSignature signature) throws KeyValidationException, SignatureException, PGPException {
		initializeSignature(signature, master);
		if(!signature.verifyCertification(attributes, master)) {
			throw new KeyValidationException("Signature verification failed.");
		}
	}
	
	private List<PGPSignature> getIdSignatures(PGPPublicKey master, String id, long keyId) {
		return getSignaturesMatchingKeyId(master.getSignaturesForID(id), keyId);
	}
	
	private List<PGPSignature> getAttributeSignatures(PGPPublicKey master, PGPUserAttributeSubpacketVector attributes, long keyId) {
		return getSignaturesMatchingKeyId( master.getSignaturesForUserAttribute(attributes), keyId);
	}

	private List<PGPSignature> getSignaturesMatchingKeyId(Iterator<?> it, long keyId) {
		final List<PGPSignature> signatures = new ArrayList<>();
		while(it.hasNext()) {
			PGPSignature sig = (PGPSignature) it.next();
			if(sig.getKeyID() == keyId) {
				signatures.add(sig);
			}
		}
		return signatures;
	}
	
	private PGPPublicKey getMasterKey() throws KeyValidationException {
		if(publicKeys.isEmpty()) {
			throw new KeyValidationException("No keys");
		}
		
		if(!publicKeys.get(0).isMasterKey()) {
			throw new KeyValidationException("First key is not master key as expected");
		}
		return publicKeys.get(0);
	}
	
	private List<String> getUserIds() throws KeyValidationException  {
		final List<String> uids = new ArrayList<String>(); 
		final Iterator<?> it = getMasterKey().getUserIDs();
		while(it.hasNext()) {
			uids.add((String) it.next());
		}
		return uids;
	}
	
	private List<PGPUserAttributeSubpacketVector> getUserAttributes() throws KeyValidationException {
		final List<PGPUserAttributeSubpacketVector> attribs = new ArrayList<>();
		final Iterator<?> it = getMasterKey().getUserAttributes();
		while(it.hasNext()) {
			attribs.add((PGPUserAttributeSubpacketVector) it.next());
		}
		return attribs;
	}
	
	private List<PGPPublicKey> getSubordinateKeys() throws KeyValidationException {
		final List<PGPPublicKey> subkeys = new ArrayList<>();
		if(publicKeys.isEmpty()) {
			throw new KeyValidationException("No keys");
		}
		for(int i = 1; i < publicKeys.size(); i++) {
			PGPPublicKey pk = publicKeys.get(i);
			if(pk.isMasterKey()) {
				throw new KeyValidationException("Master key found at index != 0, i = "+ i);
			}
			subkeys.add(pk);
		}
		return subkeys;
	}
	
	private void verifyCertification(PGPPublicKey signingKey, PGPPublicKey targetKey, PGPSignature signature) throws KeyValidationException, SignatureException, PGPException {
		initializeSignature(signature, signingKey);
		if(!signature.verifyCertification(signingKey, targetKey)) {
			throw new KeyValidationException("Signature verification failed.");
		}
	}

	private void initializeSignature(PGPSignature signature, PGPPublicKey signingKey) throws KeyValidationException {
		try {
			signature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), signingKey);
		} catch (PGPException e) {
			throw new KeyValidationException("Error initializing signature verification "+ e, e);
		}
	}
	
	private PGPSignature getBindingSignature(PGPPublicKey master, PGPPublicKey sub) throws KeyValidationException {
		for(PGPSignature sig: getSignaturesForKey(sub, PGPSignature.SUBKEY_BINDING)) {
			if(sig.getKeyID() == master.getKeyID()) {
				return sig;
			}
		}
		throw new KeyValidationException("No binding signature found for subkey 0x"+ Long.toString(sub.getKeyID(), 16));
	}
	
	
	private List<PGPSignature> getSignaturesForKey(PGPPublicKey pk, int signatureType) {
		final List<PGPSignature> signatures = new ArrayList<>();
		final Iterator<?> it = (signatureType == ANY_SIGNATURE_TYPE) ? (pk.getSignatures()) : (pk.getSignaturesOfType(signatureType));
		while(it.hasNext()) {
			signatures.add((PGPSignature) it.next());
		}
		return signatures;
	}
}
