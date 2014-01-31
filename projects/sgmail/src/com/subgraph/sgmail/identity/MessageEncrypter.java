package com.subgraph.sgmail.identity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

import com.google.common.base.Charsets;

public class MessageEncrypter {
	
	public byte[] encryptMessageBody(String input, List<PublicIdentity> recipientIdentities) throws IOException, PGPException {
		PGPEncryptedDataGenerator gen = new PGPEncryptedDataGenerator(new BcPGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_128).setWithIntegrityPacket(true).setSecureRandom(new SecureRandom()));
		for(PublicIdentity id: recipientIdentities) {
			gen.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(getEncryptionKeyForIdentity(id)));
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ArmoredOutputStream armored = new ArmoredOutputStream(out);
		armored.setHeader("Version", "SGMail");
		
		byte[] literal = createLiteralData(input);
		
		
		OutputStream genOut = gen.open(armored, literal.length);
		genOut.write(literal);
		gen.close();
		armored.close();
		return out.toByteArray();
	}
	
	
	private byte[] createLiteralData(String body) {
		final byte[] inputBytes = body.getBytes(Charsets.UTF_8);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PGPLiteralDataGenerator gen = new PGPLiteralDataGenerator();
		
		try {
			OutputStream genOut = gen.open(out, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, inputBytes.length, new Date());
			genOut.write(inputBytes);
			genOut.close();
			return out.toByteArray();
					
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private PGPPublicKey getEncryptionKeyForIdentity(PublicIdentity identity) {
		PGPPublicKeyRing pkr = identity.getPGPPublicKeyRing();
		Iterator<?> it = pkr.getPublicKeys();
		while(it.hasNext()) {
			PGPPublicKey pk = (PGPPublicKey) it.next();
			if(pk.isEncryptionKey()) {
				return pk;
			}
		}
		return null;
	}

}
