package com.subgraph.sgmail.openpgp;

import java.security.SecureRandom;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParameterList;

import com.google.common.primitives.UnsignedLongs;

public class EncryptedMultipart extends MimeMultipart {
	private final static Random r = new SecureRandom();
	static String createBoundary() {
		final String s = UnsignedLongs.toString(r.nextLong(), 16);
		return "----------------" + s;
	}
	
	public EncryptedMultipart() {
		super("encrypted");
		final MimeBodyPart controlSection = new MimeBodyPart();
		try {
			controlSection.setContent("Version: 1\n", "application/pgp-encrypted");
			addBodyPart(controlSection);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ParameterList plist = new ParameterList();
		plist.set("protocol", "application/pgp-encrypted");
		plist.set("boundary", createBoundary());
		ContentType cType = new ContentType("multipart", "encrypted", plist); 
		contentType = cType.toString();
		initializeProperties();
	}
	
	public void setBody(byte[] body) {
		final MimeBodyPart bodySection = new MimeBodyPart();
		try {
			bodySection.setContent(body, "application/octet-stream");
			addBodyPart(bodySection);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
