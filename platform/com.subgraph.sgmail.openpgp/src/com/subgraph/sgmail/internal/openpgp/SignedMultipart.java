package com.subgraph.sgmail.internal.openpgp;

import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParameterList;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;

public class SignedMultipart extends MimeMultipart {

	public SignedMultipart(int digest) throws PGPException {
		super("signed");
		ParameterList plist = new ParameterList();
		plist.set("protocol", "application/pgp-signature");
		plist.set("micalg", getMicalg(digest));
		plist.set("boundary", EncryptedMultipart.createBoundary());
		ContentType cType = new ContentType("multipart", "signed", plist);
		contentType = cType.toString();
		initializeProperties();
	}
	
	static private String getMicalg(int digest) throws PGPException {
		switch (digest) {
		case HashAlgorithmTags.SHA1:
			return "pgp-sha1";
		case HashAlgorithmTags.MD2:
			return "pgp-md2";
		case HashAlgorithmTags.MD5:
			return "pgp-md5";
		case HashAlgorithmTags.RIPEMD160:
			return "pgp-ripemd160";
		case HashAlgorithmTags.SHA256:
			return "pgp-sha256";
		case HashAlgorithmTags.SHA384:
			return "pgp-sha384";
		case HashAlgorithmTags.SHA512:
			return "pgp-sha512";
		case HashAlgorithmTags.SHA224:
			return "pgp-sha224";
		default:
			throw new PGPException(
					"unknown hash algorithm tag in getDigestName: " + digest);
		}
	}
}
