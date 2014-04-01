package com.subgraph.sgmail.openpgp;

import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.Random;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class OpenPGPEncryptor {
	//private final static Logger logger = Logger.getLogger(OpenPGPEncryptor.class.getName());
	
	public MimeMultipart createEncryptedPart(byte[] bodyData, List<PublicIdentity> recipientIdentities) throws MessagingException, IOException, PGPException {
		final byte[] encryptedBodyData = encryptMessageBody(bodyData, recipientIdentities);
		final EncryptedMultipart encryptedMultipart = new EncryptedMultipart();
		encryptedMultipart.setBody(encryptedBodyData);
		return encryptedMultipart;
	}
	
	public byte[] encryptMessageBody(byte[] bodyData, List<PublicIdentity> recipientIdentities) throws IOException, PGPException {
        final SecureRandom sr = Random.getInstance().getSecureRandom();
        PGPEncryptedDataGenerator gen = new PGPEncryptedDataGenerator(new BcPGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_128).setWithIntegrityPacket(true).setSecureRandom(sr));
		for(PublicIdentity id: recipientIdentities) {
			gen.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(getEncryptionKeyForIdentity(id)));
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ArmoredOutputStream armored = new ArmoredOutputStream(out);
		armored.setHeader("Version", "SGMail");
		
		byte[] literal = createLiteralData(bodyData);
		
		
		OutputStream genOut = gen.open(armored, literal.length);
		genOut.write(literal);
		gen.close();
		armored.close();
		return out.toByteArray();
	}
	
	private byte[] createLiteralData(byte[] bodyData) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PGPLiteralDataGenerator gen = new PGPLiteralDataGenerator();
		
		try {
			OutputStream genOut = gen.open(out, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, bodyData.length, new Date());
			genOut.write(bodyData);
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
