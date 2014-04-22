package com.subgraph.sgmail.openpgp;

import com.google.common.io.ByteStreams;
import com.subgraph.sgmail.identity.OpenPGPException;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPException;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

public class MessageProcessor {
	
	private final static Logger logger = Logger.getLogger(MessageProcessor.class.getName());

	private final MessageProcessingPreferences preferences;
	private final OpenPGPEncryptor encryptor;
	private final OpenPGPDecryptor decryptor;
	private final OpenPGPSigner signer;
	
	public MessageProcessor(MessageProcessingPreferences preferences) {
		this.preferences = preferences;
		this.encryptor = new OpenPGPEncryptor();
		this.decryptor = new OpenPGPDecryptor();
		this.signer = new OpenPGPSigner();
	}
	
	public MessageProcessor() {
		this(new MessageProcessingPreferences());
	}
	
	public MessageProcessingPreferences getPreferences() {
		return preferences;
	}
	
	public boolean isEncrypted(MimeMessage message) {
		try {
			final Object content = message.getContent();
			if(content instanceof MimeMultipart) {
				final MimeMultipart mm = (MimeMultipart) content;
				final ContentType ct = new ContentType(mm.getContentType());
				return "encrypted".equalsIgnoreCase(ct.getSubType());
			}
		} catch (IOException | MessagingException e) {
			logger.warning("Unexpected exception reading message body to determine if it contains encrypted content: "+ e);
		}
		return false;
	}

	public MimeMessage decryptMessage(MimeMessage inputMessage, PrivateIdentity key) throws IOException, MessagingException, PGPException, OpenPGPException {
		final byte[] body = getEncryptedBody(inputMessage);
		final byte[] decrypted = decryptor.decryptBody(body, key);
		final MimeBodyPart part = new MimeBodyPart(new ByteArrayInputStream(decrypted));
		final MimeMessage decryptedMessage = duplicateMessage(inputMessage);
		decryptedMessage.setContent(part.getContent(), part.getContentType());
		return decryptedMessage;
	}
	
	public List<Long> getDecryptionKeyIds(MimeMessage inputMessage) throws IOException, MessagingException {
		final byte[] body = getEncryptedBody(inputMessage);
		return decryptor.getDecryptionKeyIds(body);
	}
	
	private byte[] getEncryptedBody(MimeMessage inputMessage) throws IOException, MessagingException {
		if(!isEncrypted(inputMessage)) {
			throw new IllegalArgumentException();
		}
		final MimeMultipart encryptedMultipart = (MimeMultipart) inputMessage.getContent();
		Object ob = encryptedMultipart.getBodyPart(1).getContent();
		if(!(ob instanceof InputStream)) {
			throw new IllegalStateException("Body content is not expected type: "+ ob.getClass());
		}
		final InputStream input = (InputStream) ob;
		final ArmoredInputStream armor = new ArmoredInputStream(input);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		ByteStreams.copy(armor, out);
		return out.toByteArray();
	}
	
	
	public MimeMessage encryptMessage(MimeMessage inputMessage, List<PublicIdentity> recipientIdentities) throws MessagingException, IOException, PGPException {	
		final MimeMessage encryptedMessage = duplicateMessage(inputMessage);
		
		final byte[] bodyData = getContentToEncrypt(inputMessage);
		final MimeMultipart mp = encryptor.createEncryptedPart(bodyData, recipientIdentities);
		encryptedMessage.setContent(mp);
		return encryptedMessage;
	}
	
	private MimeMessage duplicateMessage(MimeMessage message) throws MessagingException {
		message.saveChanges();
		final String originalMessageId = message.getMessageID();
        final MimeMessage newMessage = new MimeMessage(message.getSession()) {
			protected void updateMessageID() throws MessagingException {
		    	setHeader("Message-ID", originalMessageId); 
		    }
		};
		copyHeaders(message, newMessage);
		return newMessage;
	}

	private void copyHeaders(MimeMessage from, MimeMessage to) throws MessagingException {
		Enumeration<?> headers = from.getAllHeaderLines();
		while(headers.hasMoreElements()) {
			String line = (String) headers.nextElement();
			to.addHeaderLine(line);
		}
	}
	
	class UpdateableBodyPart extends MimeBodyPart {
		public void update() throws MessagingException  {
			updateHeaders();
		}
	}

	byte[] getContentToEncrypt(Part input) throws IOException, MessagingException {
		final UpdateableBodyPart body = new UpdateableBodyPart();
		
		body.setContent(input.getContent(), input.getContentType());
		body.update();

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		body.writeTo(output);
		return output.toByteArray();
	}
	
	public MimeMessage signMessage(MimeMessage inputMessage, PrivateIdentity signingIdentity) throws MessagingException, IOException, SignatureException, PGPException {
		final int digest = preferences.getSigningDigest();
		final MimeMessage signedMessage = duplicateMessage(inputMessage);
		final SignedMultipart signedMultipart = new SignedMultipart(digest);
		final MimeBodyPart body = createSignedBodyPart(inputMessage);
		signedMultipart.addBodyPart(body);
		signedMultipart.addBodyPart(createSignaturePart(body, signingIdentity, digest));
		signedMessage.setContent(signedMultipart);
		return signedMessage;
	}
	
	private MimeBodyPart createSignaturePart(MimeBodyPart body, PrivateIdentity signingIdentity, int digest) throws IOException, MessagingException, SignatureException, PGPException {
		final byte[] inputBytes = getContentBytes(body);
		final byte[] signatureBytes = signer.createSignature(inputBytes, signingIdentity, digest);
		final MimeBodyPart signaturePart = new MimeBodyPart(new InternetHeaders(), signatureBytes);
		signaturePart.setHeader("Content-Type", "application/pgp-signature");
		return signaturePart;
	}

	private byte[] getContentBytes(MimeBodyPart part) throws IOException, MessagingException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		part.writeTo(output);
		return output.toByteArray();
	}

	public MimeBodyPart createSignedBodyPart(MimePart input) throws IOException, MessagingException {
		final UpdateableBodyPart bodyPart = new UpdateableBodyPart();
		final Object content = input.getContent();
		if(content instanceof MimeMultipart) {
			bodyPart.setContent((Multipart) content);
		} else if(content instanceof String) {
			final String text = (String) content;
			final String subtype = getTextSubtype(input.getContentType());
			bodyPart.setText(text, null, subtype);
		} else {
			bodyPart.setContent(content, input.getContentType());
		}
		
		bodyPart.update();
		return bodyPart;
	}

	private String getTextSubtype(String contentType) throws ParseException {
		if(contentType == null) {
			return "plain";
		}
		
		final ContentType ct = new ContentType(contentType);
		
		if("text".equalsIgnoreCase(ct.getPrimaryType())) {
			return ct.getSubType();
		} else {
			throw new IllegalArgumentException("Not a text content-type");
		}
	}
	public MimeMessage signAndEncryptMessage(MimeMessage inputMessage, List<PublicIdentity> recipientIdentities, PrivateIdentity signingIdentity) throws MessagingException, IOException, PGPException, SignatureException {
		return encryptMessage(signMessage(inputMessage, signingIdentity), recipientIdentities);
	}

}
