package com.subgraph.sgmail.openpgp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;

import com.google.common.io.ByteStreams;
import com.subgraph.sgmail.identity.OpenPGPException;
import com.subgraph.sgmail.identity.PrivateIdentity;

public class OpenPGPDecryptor {
	
	private final static Logger logger = Logger.getLogger(OpenPGPDecryptor.class.getName());
	private final static BcPBESecretKeyDecryptorBuilder decryptorBuilder = 
			new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider());
	
	private static PBESecretKeyDecryptor createDecryptor(String passphrase) {
		return decryptorBuilder.build(passphrase.toCharArray());
	}
	
	public List<Long> getDecryptionKeyIds(byte[] encryptedData) throws IOException {
		final List<Long> result = new ArrayList<>();
		for(PGPPublicKeyEncryptedData pked: extractPublicKeyEncryptedData(encryptedData)) {
			result.add(pked.getKeyID());
		}
		return result;
	}
	
	
	
	public byte[] decryptBody(byte[] encryptedData, PrivateIdentity identity) throws IOException, PGPException, OpenPGPException {
		for(PGPPublicKeyEncryptedData pked: extractPublicKeyEncryptedData(encryptedData)) {
			if(identity.containsKeyId(pked.getKeyID())) {
				return decryptPublicKeyEncryptedData(pked, identity);
			}
		}
		// XXX
		return null;
	}
	
	private byte[] decryptPublicKeyEncryptedData(PGPPublicKeyEncryptedData pked, PrivateIdentity identity) throws PGPException, IOException, OpenPGPException {
		final PGPSecretKey secretKey = identity.getPGPSecretKeyRing().getSecretKey(pked.getKeyID());
		final String passphrase = identity.getPassphrase();
		if(passphrase == null) {
			throw new OpenPGPException("No passphrase set on PrivateIdentity");
		}
		final PGPPrivateKey privateKey = secretKey.extractPrivateKey(createDecryptor(identity.getPassphrase()));
		final PublicKeyDataDecryptorFactory ddf = new BcPublicKeyDataDecryptorFactory(privateKey);
		
		return getContent(pked.getDataStream(ddf));
	}
	
	private byte[] getContent(InputStream dataStream) throws IOException, PGPException {
		final PGPObjectFactory factory = new PGPObjectFactory(dataStream);
		final Object content = factory.nextObject();
		if(content instanceof PGPCompressedData) {
			PGPCompressedData compressed = (PGPCompressedData) content;
			return getContent(compressed.getDataStream());
		} else if(content instanceof PGPLiteralData) {
			PGPLiteralData literal = (PGPLiteralData) content;
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			ByteStreams.copy(literal.getDataStream(), output);
			return output.toByteArray();
		} else {
			// XXX
			return null;
		}
		
	}
	
	private List<PGPPublicKeyEncryptedData> extractPublicKeyEncryptedData(byte[] encryptedData) throws IOException {
		final List<PGPPublicKeyEncryptedData> result = new ArrayList<>();
		final PGPObjectFactory factory = new PGPObjectFactory(encryptedData);
		final PGPEncryptedDataList edl = findEncryptedDataList(factory);
		if(edl == null) {
			return result;
		}
		final Iterator<?> it = edl.getEncryptedDataObjects();
		while(it.hasNext()) {
			PGPEncryptedData data = (PGPEncryptedData) it.next();
			if(data instanceof PGPPublicKeyEncryptedData) {
				result.add((PGPPublicKeyEncryptedData) data);
			}
		}
		return result;
	}

	private PGPEncryptedDataList findEncryptedDataList(PGPObjectFactory factory) throws IOException {
		while(true) {
			Object ob = factory.nextObject();
			if(ob == null) {
				logger.warning("No PGPEncryptedDataList found in message.");
				return null;
			} else if(ob instanceof PGPEncryptedDataList) {
				return (PGPEncryptedDataList) ob;
			}
		}
	}

}
