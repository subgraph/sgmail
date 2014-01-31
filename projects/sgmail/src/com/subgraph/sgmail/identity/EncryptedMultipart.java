package com.subgraph.sgmail.identity;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParameterList;

public class EncryptedMultipart extends MimeMultipart {
	
	
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
		String boundary = "blablahblaha";
		ParameterList plist = new ParameterList();
		plist.set("protocol", "application/pgp-encrypted");
		ContentType cType = new ContentType("multipart", "encrypted", plist); 
		cType.setParameter("boundary", boundary);
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
