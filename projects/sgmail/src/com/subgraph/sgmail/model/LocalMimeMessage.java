package com.subgraph.sgmail.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class LocalMimeMessage extends MimeMessage {
	
	private final StoredMessage storedMessage;
	
	public LocalMimeMessage(StoredMessage storedMessage, Session session, InputStream is) 
			throws MessagingException {
		super(session, is);
		this.storedMessage = checkNotNull(storedMessage);
	}
	
	public StoredMessage getStoredMessage() {
		return storedMessage;
	}
}
