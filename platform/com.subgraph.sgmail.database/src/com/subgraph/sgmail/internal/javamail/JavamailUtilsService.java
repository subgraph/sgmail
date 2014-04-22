package com.subgraph.sgmail.internal.javamail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.AttachmentExtractionException;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.database.Preferences;
import com.subgraph.sgmail.events.PreferenceChangedEvent;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageFactory;
import com.subgraph.sgmail.messages.StoredMessage;

public class JavamailUtilsService implements JavamailUtils {

	private AttachmentExtractor attachmentExtractor;
	private StoredMessageAttachmentExtractor storedMessageAttachmentExtractor;
	private MessageFactory messageFactory;
	private IEventBus eventBus;
	
	private final Session session = Session.getInstance(new Properties());
	
	public void activate() {
		attachmentExtractor = new AttachmentExtractor(messageFactory);
		storedMessageAttachmentExtractor = new StoredMessageAttachmentExtractor(session);
		eventBus.register(this);
	}
	
	public void deactivate() {
		attachmentExtractor = null;
		storedMessageAttachmentExtractor = null;
		eventBus.unregister(this);
	}
	
	public void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}
	
	public void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	@Override
	public Session getSessionInstance() {
		return session;
	}
	
	@Override
	public List<MessageAttachment> getAttachments(MimeMessage message)
			throws MessagingException, IOException {
		return attachmentExtractor.getAttachments(message);
	}
	
	@Override
	public InputStream extractAttachment(MessageAttachment attachment, StoredMessage message) throws AttachmentExtractionException {
		return storedMessageAttachmentExtractor.extractAttachment(attachment, message);
	}
	
	@Override
	public String getTextBody(MimeMessage message) {
		return MessageBodyUtils.getTextBody(message);
	}
	
	@Override
	public String getSenderText(MimeMessage message, boolean verbose) {
		return MessageUtils.getSender(message, verbose);
	}
	
	@Override
	public String getSentDateText(MimeMessage message) {
		return MessageUtils.getSentDate(message);
	}
	
	@Override
	public String getToRecipientText(MimeMessage message, boolean verbose) {
		return MessageUtils.getRecipient(message, verbose);
	}
	
	@Override
	public String getSubjectText(MimeMessage message) {
		return MessageUtils.getSubject(message);
	}
	
	@Override
	public InternetAddress getSenderAddress(MimeMessage message) {
		return MessageUtils.getSenderAddress(message);
	}
	
	@Override
	public String createQuotedBody(MimeMessage message) {
		try {
			return MessageBodyUtils.createQuotedBody(message);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	@Subscribe
	public void onPreferenceChanged(PreferenceChangedEvent event) {
		if(event.isPreferenceName(Preferences.IMAP_DEBUG_OUTPUT)) {
			boolean flag = event.getPreferences().getBoolean(Preferences.IMAP_DEBUG_OUTPUT);
			session.setDebug(flag);
		}
	}
}
