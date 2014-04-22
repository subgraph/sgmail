package com.subgraph.sgmail.internal.messages;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageFactory;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessage.Builder;

public class MessageFactoryService implements MessageFactory {

	private JavamailUtils javamailUtils;
	
	void setJavamailUtils(JavamailUtils javamailUtils) {
		this.javamailUtils = javamailUtils;
	}
	
	@Override
	public MessageAttachment createMessageAttachment(List<Integer> mimePath,
			String primaryType, String subType, String filename,
			String description, long length) {
		return new MessageAttachmentImpl.Builder()
		.mimePath(mimePath)
		.mimePrimaryType(primaryType)
		.mimeSubType(subType)
		.filename(filename)
		.description(description)
		.length(length)
		.build();
	}

	@Override
	public MessageUser createMessageUser(String username, String address) {
		return new MessageUserImpl(username, address);
	}

	@Override
	public Builder createStoredMessageBuilder(byte[] rawBytes) {
		return new StoredMessageBuilder(rawBytes);
	}

	@Override
	public boolean updateMessageWithDecryptedData(StoredMessage message, byte[] rawDecryptedMessage) {
		final DecryptableStoredMessage decryptable = getDecryptable(message);
		
		try {
			final MimeMessage mimeMessage = message.toMimeMessage(javamailUtils.getSessionInstance());
			final String body = javamailUtils.getTextBody(mimeMessage);
			final List<MessageAttachment> attachments = javamailUtils.getAttachments(mimeMessage);
			decryptable.setDecryptedMessageDetails(rawDecryptedMessage, body, attachments);
			return true;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
		// TODO Auto-generated method stub
		
	}
	
	private DecryptableStoredMessage getDecryptable(StoredMessage message) {
		if(!(message instanceof DecryptableStoredMessage)) {
			throw new IllegalArgumentException("Message is not a DecryptableStoredMessage as expected "+ message.getClass().getName());
		}
		return (DecryptableStoredMessage) message;
	}
	
	
}
