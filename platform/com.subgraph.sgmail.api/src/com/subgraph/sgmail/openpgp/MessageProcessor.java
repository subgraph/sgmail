package com.subgraph.sgmail.openpgp;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.bouncycastle.openpgp.PGPException;

import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.messages.StoredMessage;

public interface MessageProcessor {
	void decryptMessage(StoredMessage inputMesage, PrivateIdentity key) throws IOException, MessagingException, PGPException, OpenPGPException;
	MimeMessage encryptMessage(MimeMessage inputMessage, List<PublicIdentity> recipientIdentities) throws MessagingException, IOException, PGPException;
	boolean isEncrypted(StoredMessage message);
	List<Long> getDecryptionKeyIds(StoredMessage inputMessage) throws IOException, MessagingException;
	MimeMessage signMessage(MimeMessage inputMessage, PrivateIdentity signingIdentity) throws MessagingException, IOException, SignatureException, PGPException;
	MimeMessage signAndEncryptMessage(MimeMessage inputMessage, List<PublicIdentity> recipientIdentities, PrivateIdentity signingIdentity) throws MessagingException, IOException, PGPException, SignatureException;
	
	

}
