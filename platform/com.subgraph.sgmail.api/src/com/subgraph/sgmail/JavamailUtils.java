package com.subgraph.sgmail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredMessage;

public interface JavamailUtils {
	Session getSessionInstance();
    List<MessageAttachment> getAttachments(MimeMessage message) throws MessagingException, IOException;
    String getTextBody(MimeMessage message);
    String createQuotedBody(MimeMessage message);
    String getSenderText(MimeMessage message, boolean verbose);
    String getSentDateText(MimeMessage message);
    String getToRecipientText(MimeMessage message, boolean verbose);
    String getSubjectText(MimeMessage message);
    InternetAddress getSenderAddress(MimeMessage message);
	InputStream extractAttachment(MessageAttachment attachment, StoredMessage message) throws AttachmentExtractionException;
    
}
