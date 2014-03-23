package com.subgraph.sgmail.model;

import com.subgraph.sgmail.messages.StoredIMAPMessage;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocalMimeMessage extends MimeMessage {
	
	private final StoredIMAPMessage storedMessage;
	
	public LocalMimeMessage(StoredIMAPMessage storedMessage, Session session, InputStream is)
			throws MessagingException {
		super(session, is);
		this.storedMessage = checkNotNull(storedMessage);
	}
	
	public StoredIMAPMessage getStoredMessage() {
		return storedMessage;
	}
}
