package com.subgraph.sgmail.internal.openpgp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SignatureException;

import javax.mail.MessagingException;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

import com.subgraph.sgmail.identity.PrivateIdentity;

public class OpenPGPSigner {

	private final static BcPBESecretKeyDecryptorBuilder decryptorBuilder = 
			new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider());
	
	private static PBESecretKeyDecryptor createDecryptor(String passphrase) {
		return decryptorBuilder.build(passphrase.toCharArray());
	}
	
	public byte[] createSignature(byte[] inputBytes, PrivateIdentity signingIdentity, int digest) throws PGPException, SignatureException, IOException, MessagingException {
		PGPSignatureGenerator sigGen = createSignatureGenerator(signingIdentity, digest);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ArmoredOutputStream armor = new ArmoredOutputStream(bytes);
		armor.setHeader("Version", "SGMail");

		BCPGOutputStream out = new BCPGOutputStream(armor);
		
		sigGen.update(inputBytes);
		sigGen.generate().encode(out);
		armor.close();
		return bytes.toByteArray();
	}
	
	private PGPSignatureGenerator createSignatureGenerator(PrivateIdentity signingIdentity, int digest) throws PGPException {
		final PGPSecretKeyRing skr = signingIdentity.getPGPSecretKeyRing();
		final String passphrase = signingIdentity.getPassphrase();
		final PGPSecretKey secretKey = skr.getSecretKey();
		final PGPPrivateKey privateKey = secretKey.extractPrivateKey(createDecryptor(passphrase));
	
		final PGPSignatureGenerator generator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), digest));
		generator.init(PGPSignature.CANONICAL_TEXT_DOCUMENT, privateKey);
		return generator;
	}
}
