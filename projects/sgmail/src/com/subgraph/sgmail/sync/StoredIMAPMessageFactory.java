package com.subgraph.sgmail.sync;

import com.google.common.base.Strings;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.messages.impl.FlagUtils;
import com.subgraph.sgmail.ui.MessageBodyUtils;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPMessage;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoredIMAPMessageFactory {
    StoredIMAPMessage createFromJavamailMessage(IMAPAccount imapAccount, IMAPMessage message, long messageUID) throws MessagingException {
        final long conversationId = imapAccount.generateConversationIdForMessage(message);
        final long uniqueMessageId = imapAccount.generateUniqueMessageIdForMessage(message);
        return StoredIMAPMessage.Builder.create(readRawBytes(message))
                .conversationId(conversationId)
                .uniqueMessageId(uniqueMessageId)
                .messageUID(messageUID)
                .subject(getSubject(message))
                .displayText(getDisplayText(message))
                .sender(getSender(message))
                .recipients(getRecipients(message))
                .messageDate(getMessageTimestamp(message))
                .flags(getMessageFlags(message))
                .labels(getGmailLabels(imapAccount, message))
                .build();
    }

    private byte[] readRawBytes(IMAPMessage message) throws MessagingException {
        message.setPeek(true);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            message.writeTo(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new MessagingException("IOException reading message body "+ e, e);
        }
    }

    private String getDisplayText(IMAPMessage message) {
        return MessageBodyUtils.getTextBody(message);
    }

    private String getSubject(IMAPMessage message) throws MessagingException {
        return Strings.nullToEmpty(message.getSubject());
    }

    private MessageUser getSender(IMAPMessage message) throws MessagingException {
        return addressToMessageUser(message.getSender(), MessageUser.UserType.USER_SENDER);
    }

    private List<MessageUser> getRecipients(IMAPMessage message) throws MessagingException {
        final List<MessageUser> recipients = new ArrayList<>();
        addRecipientsToList(recipients, message, Message.RecipientType.TO, MessageUser.UserType.USER_TO);
        addRecipientsToList(recipients, message, Message.RecipientType.CC, MessageUser.UserType.USER_CC);
        return recipients;
    }


    private void addRecipientsToList(List<MessageUser> recipientList, IMAPMessage message, Message.RecipientType recipientType, MessageUser.UserType userType) throws MessagingException {
        if(message.getRecipients(recipientType) == null) {
            return;
        }
        for(Address address: message.getRecipients(recipientType)) {
            MessageUser mu = addressToMessageUser(address, userType);
            if(mu != null) {
                recipientList.add(mu);
            }
        }
    }

    private MessageUser addressToMessageUser(Address address, MessageUser.UserType type) {
        if(address == null) {
            return null;
        }
        final String username = getAddressUsername(address);
        final String email = getAddressEmail(address);
        if(email == null) {
            return null;
        } else {
            return MessageUser.create(username, email, type);
        }
    }

    private String getAddressUsername(Address address) {
        if(!(address instanceof InternetAddress)) {
            return null;
        } else {
            return ((InternetAddress) address).getPersonal();
        }
    }

    private String getAddressEmail(Address address) {
        if(!(address instanceof  InternetAddress)) {
            return null;
        } else {
            return ((InternetAddress) address).getAddress();
        }
    }

    private long getMessageTimestamp(IMAPMessage message) throws MessagingException {
        final Date date = message.getReceivedDate();
        if(date == null) {
            return 0;
        } else {
            return date.getTime();
        }
    }

    private long getMessageFlags(IMAPMessage message) throws MessagingException {
        return FlagUtils.getFlagsFromMessage(message);
    }

    private List<StoredMessageLabel> getGmailLabels(IMAPAccount account, IMAPMessage message) throws MessagingException {
        if(!(message instanceof GmailMessage)) {
            return null;
        }
        final GmailMessage gmailMessage = (GmailMessage) message;
        if(gmailMessage.getLabels() == null || gmailMessage.getLabels().length == 0) {
            return null;
        }
        final List<StoredMessageLabel> labelList = new ArrayList<>();
        for(String label: gmailMessage.getLabels()) {
            if(!label.isEmpty()) {
                labelList.add(account.getMessageLabelByName(label));
            }
        }
        return labelList;
    }
}
