package com.subgraph.sgmail.identity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.Callable;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;

public class KeyGenerationTask implements Callable<KeyGenerationResult>{
	
	private final static boolean USE_RSA_GENERAL_KEYS = true;
	
	private final static int[] PREFERRED_SYMMETRIC = new int[] {
		SymmetricKeyAlgorithmTags.AES_256,
		SymmetricKeyAlgorithmTags.AES_192,
		SymmetricKeyAlgorithmTags.AES_128,
		SymmetricKeyAlgorithmTags.CAST5
	};
	private final static int[] PREFERRED_HASH = new int[] {
		HashAlgorithmTags.SHA512,
		HashAlgorithmTags.SHA384,
		HashAlgorithmTags.SHA256,
		HashAlgorithmTags.SHA224
	};
	
	private final KeyGenerationParameters parameters;
	
	public KeyGenerationTask(KeyGenerationParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public KeyGenerationResult call()  {
		try {
			final PGPKeyRingGenerator krg = createKeyRingGenerator(parameters.getEmailAddress(), 0xc0, new char[0]);
			final PGPSecretKeyRing secretKeyRing = krg.generateSecretKeyRing();
			final PGPPublicKeyRing publicKeyRing = krg.generatePublicKeyRing();
			return new KeyGenerationResult(secretKeyRing, publicKeyRing);
		} catch (PGPException e) {
			return new KeyGenerationResult("Error generating keys "+ e.getMessage());
		}
	}
	
	private PGPKeyRingGenerator createKeyRingGenerator(String id, int s2kCount, char[] passPhrase) throws PGPException {
		final PGPKeyPair masterKey = createSigningKeys();
		final PGPKeyPair subKey = createEncryptionKeys();
		
		final PGPSignatureSubpacketGenerator masterGenerator = createSigningKeySubpacketGenerator();
		final PGPSignatureSubpacketGenerator subkeyGenerator = createEncryptionKeySubpacketGenerator();
		
		final PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
		final PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);
		
		final PBESecretKeyEncryptor ske = (new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha256Calc, s2kCount)).build(passPhrase);
		
		final PGPKeyRingGenerator krg = new PGPKeyRingGenerator(
				PGPSignature.POSITIVE_CERTIFICATION,
				masterKey,
				id,
				sha1Calc,
				masterGenerator.generate(),
				null,
				new BcPGPContentSignerBuilder(masterKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA512),
				ske);
		krg.addSubKey(subKey, subkeyGenerator.generate(), null);
		return krg;
	}

	private PGPKeyPair createSigningKeys() throws PGPException {
		final RSAKeyPairGenerator kpg = createRSAGenerator(2048);
		final int type = (USE_RSA_GENERAL_KEYS) ? (PGPPublicKey.RSA_GENERAL) : (PGPPublicKey.RSA_SIGN);
		return new BcPGPKeyPair(type, kpg.generateKeyPair(), new Date());
	}
	
	private PGPKeyPair createEncryptionKeys() throws PGPException {
		final RSAKeyPairGenerator kpg = createRSAGenerator(2048);
		final int type = (USE_RSA_GENERAL_KEYS) ? (PGPPublicKey.RSA_GENERAL) : (PGPPublicKey.RSA_ENCRYPT);
		return new BcPGPKeyPair(type, kpg.generateKeyPair(), new Date());
	}
	
	private RSAKeyPairGenerator createRSAGenerator(int keyLength) {
		final RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
		kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), keyLength, 12));
		return kpg;
	}

	private PGPSignatureSubpacketGenerator createSigningKeySubpacketGenerator() {
		final PGPSignatureSubpacketGenerator g = new PGPSignatureSubpacketGenerator();
		g.setKeyFlags(false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER);
		g.setPreferredSymmetricAlgorithms(false, PREFERRED_SYMMETRIC);
		g.setPreferredHashAlgorithms(false, PREFERRED_HASH);
		g.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);
		return g;
	}
	
	private PGPSignatureSubpacketGenerator createEncryptionKeySubpacketGenerator() {
		final PGPSignatureSubpacketGenerator g = new PGPSignatureSubpacketGenerator();
		g.setKeyFlags(false, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
		return g;
	}
}
