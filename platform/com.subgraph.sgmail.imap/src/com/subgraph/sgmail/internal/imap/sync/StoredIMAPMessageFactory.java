package com.subgraph.sgmail.internal.imap.sync;

import com.google.common.base.Strings;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.internal.imap.FlagUtils;
import com.subgraph.sgmail.messages.MessageFactory;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPMessage;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoredIMAPMessageFactory {
    public static interface MessageIdGenerator {
        int getConversationId(MimeMessage message);
        int getUniqueMessageId(MimeMessage message);
    }

    private final Model model;
    private final JavamailUtils javamailUtils;
    private final MessageFactory basicMessageFactory;
    
    public StoredIMAPMessageFactory(Model model, JavamailUtils javamailUtils, MessageFactory basicMessageFactory) {
    	this.model = model;
    	this.javamailUtils = javamailUtils;
    	this.basicMessageFactory = basicMessageFactory;
	}

    public StoredMessage createFromJavamailMessage(IMAPAccount imapAccount, MimeMessage message) throws MessagingException, IOException {
        final StoredMessage duplicate = imapAccount.getMessageForMimeMessage(message);
        if(duplicate != null) {
            return duplicate;
        }
        final int conversationId = imapAccount.generateConversationIdForMessage(message);
        final int messageId = imapAccount.generateUniqueMessageIdForMessage(message, model);
        final List<StoredMessageLabel> gmailLabels = getGmailLabels(imapAccount, message);

        final MimeMessage reparsed = reparseMessage(message);
        return createFromJavamailMessage(reparsed, messageId, conversationId, gmailLabels);
   }

    public StoredMessage createFromJavamailMessage(MimeMessage message, MessageIdGenerator idGenerator) throws MessagingException, IOException {
        final int conversationId = idGenerator.getConversationId(message);
        final int messageId = idGenerator.getUniqueMessageId(message);
        return createFromJavamailMessage(message, messageId, conversationId, null);
    }

    private StoredMessage createFromJavamailMessage(MimeMessage message, int messageId, int conversationId, List<StoredMessageLabel> labels) throws MessagingException, IOException {
    	return basicMessageFactory.createStoredMessageBuilder(readRawBytes(message))
                .conversationId(conversationId)
                .messageId(messageId)
                .subject(getSubject(message))
                .bodyText(javamailUtils.getTextBody(message))
                .sender(getSender(message))
                 .toRecipients(getRecipients(message, Message.RecipientType.TO))
                 .ccRecipients(getRecipients(message, Message.RecipientType.CC))

                .messageDate((int) (getMessageTimestamp(message) / 1000))
                .flags(getMessageFlags(message))
                .labels(labels)
                .attachments(javamailUtils.getAttachments(message))
             .build();
    }

    private MimeMessage reparseMessage(MimeMessage message) throws IOException, MessagingException {
        if(message instanceof IMAPMessage) {
            ((IMAPMessage) message).setPeek(true);
        }
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        message.writeTo(output);
        final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        return new MimeMessage(message.getSession(), input);
    }

    private byte[] readRawBytes(MimeMessage message) throws MessagingException {
        if(message instanceof IMAPMessage) {
            ((IMAPMessage)message).setPeek(true);
        }
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            message.writeTo(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new MessagingException("IOException reading message body " + e, e);
        }
    }

    private String getSubject(MimeMessage message) throws MessagingException {
        return Strings.nullToEmpty(message.getSubject());
    }

    private MessageUser getSender(MimeMessage message) throws MessagingException {
    	Address[] from = message.getFrom();
    	if(from != null && from.length != 0) {
    		return addressToMessageUser(from[0]);
    	} else {
    		return addressToMessageUser(message.getSender());
    	}
    }

    private List<MessageUser> getRecipients(MimeMessage message, Message.RecipientType type) throws MessagingException {
        final List<MessageUser> recipients = new ArrayList<>();
        addRecipientsToList(recipients, message, type);
        return recipients;
    }


    private void addRecipientsToList(List<MessageUser> recipientList, MimeMessage message, Message.RecipientType recipientType) throws MessagingException {
        if (message.getRecipients(recipientType) == null) {
            return;
        }
        for (Address address : message.getRecipients(recipientType)) {
            MessageUser mu = addressToMessageUser(address);
            if (mu != null) {
                recipientList.add(mu);
            }
        }
    }

    private MessageUser addressToMessageUser(Address address) {
        if (address == null) {
            return null;
        }
        final String username = getAddressUsername(address);
        final String email = getAddressEmail(address);
        if (email == null) {
            return null;
        } else {
        	return basicMessageFactory.createMessageUser(username, email);
        }
    }

    private String getAddressUsername(Address address) {
        if (!(address instanceof InternetAddress)) {
            return null;
        } else {
            return ((InternetAddress) address).getPersonal();
        }
    }

    private String getAddressEmail(Address address) {
        if (!(address instanceof InternetAddress)) {
            return null;
        } else {
            return ((InternetAddress) address).getAddress();
        }
    }

    private long getMessageTimestamp(MimeMessage message) throws MessagingException {
        final Date date = message.getReceivedDate();
        if (date == null) {
        	final Date sent = message.getSentDate();
        	if(sent == null) {
        		return 0;
        	} else {
        		return sent.getTime();
        	}
        } else {
            return date.getTime();
        }
    }

    private int getMessageFlags(MimeMessage message) throws MessagingException {
        return FlagUtils.getFlagsFromMessage(message);
    }

    private List<StoredMessageLabel> getGmailLabels(IMAPAccount account, MimeMessage message) throws MessagingException {
        if (!(message instanceof GmailMessage)) {
            return null;
        }
        final GmailMessage gmailMessage = (GmailMessage) message;
        if (gmailMessage.getLabels() == null || gmailMessage.getLabels().length == 0) {
            return null;
        }
        final List<StoredMessageLabel> labelList = new ArrayList<>();
        for (String label : gmailMessage.getLabels()) {
            if (!label.isEmpty()) {
                labelList.add(account.getMailAccount().getMessageLabelByName(label));
            }
        }
        return labelList;
    }
}
