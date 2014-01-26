package com.subgraph.sgmail.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.common.base.Splitter;

public class MessageBodyUtils {
	private final static Logger logger = Logger.getLogger(MessageBodyUtils.class.getName());
	
	public static String getTextBody(Message message) {
		if(!(message instanceof MimeMessage)) {
			logger.warning("Message is not expected type of MimeMessage");
			return "";
		}
		
		final MimeMessage mime = (MimeMessage) message;
		try {
			return getTextBodyFromMimeContent(mime.getContent());
		} catch (IOException | MessagingException e) {
			logger.log(Level.WARNING, "Error getting content from message "+ e, e);
			return "";
		}
	}
	
	private static String getTextBodyFromMimeContent(Object content) throws MessagingException {
		if(content instanceof String) {
			return (String) content;
		} else if(content instanceof MimeMultipart) {
			return getTextBodyFromMimeMultipart((MimeMultipart) content);
		} else {
			logger.warning("Could not convert message body of type "+ content.getClass().getName() +" into text representation");
			return "";
		}
		
	}
	
	private static String getTextBodyFromMimeMultipart(MimeMultipart multi) throws MessagingException {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < multi.getCount(); i++) {
			BodyPart bodyPart = multi.getBodyPart(i);
			if(bodyPart instanceof MimeBodyPart) {
				String bodyText = getTextBodyFromBodyPart((MimeBodyPart) bodyPart);
				if(bodyText != null) {
					sb.append(bodyText);
				}
			}
		}
		return sb.toString();
	}
	
	private static String getTextBodyFromBodyPart(MimeBodyPart part) throws MessagingException {
		final String type = part.getContentType();
		//if(type == null || !type.contains("text/plain")) {
			//return null;
		//}
		
		try {
			final Object content = part.getContent();
			if(content instanceof String) {
				if(type != null && type.contains("text/plain")) {
					return (String) content;
				}
			} else if(content instanceof MimeMultipart) {
				return getTextBodyFromMimeMultipart((MimeMultipart) content);
			}
		} catch (IOException e) {
			logger.warning("IOException reading part content "+ e);
		}
		return null;
	}
	
	public static String createQuotedBody(Message message) throws MessagingException {
		final StringBuilder sb = new StringBuilder();
		final String body = getTextBody(message);
		final String replyLine = createReplyLine(message);
		sb.append('\n');
		sb.append(replyLine);
		for(String line: Splitter.on('\n').split(body)) {
			if(line.startsWith(">")) {
				sb.append(">");
			} else {
				sb.append("> ");
			}
			sb.append(line);
			sb.append('\n');
		}
		return sb.toString();
	}
	
	
	private final static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
	private final static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);
	
	private static String createReplyLine(Message message) throws MessagingException {
		final Date sent = getSentDate(message);
		final String sender = MessageUtils.getSender(message, false);
		if(sent == null) {
			return sender + " wrote:\n";
		} else {
			synchronized(dateFormat) {
				return "On "+ dateFormat.format(sent) +" at "+ timeFormat.format(sent) +", "+ sender + " wrote:\n";
			}
		}
	}
	
	private static Date getSentDate(Message message) throws MessagingException {
		return (message.getSentDate() != null) ? (message.getSentDate()) : (message.getReceivedDate());
	}
}
