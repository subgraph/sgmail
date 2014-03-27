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
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoredIMAPMessageFactory {
    public static interface MessageIdGenerator {
        long getConversationId(MimeMessage message);
        long getUniqueMessageId(MimeMessage message);
    }

    private final AttachmentExtractor attachmentExtractor = new AttachmentExtractor();

    public StoredIMAPMessage createFromJavamailMessage(IMAPAccount imapAccount, MimeMessage message, long messageUID) throws MessagingException, IOException {
        final long conversationId = imapAccount.generateConversationIdForMessage(message);
        final long uniqueMessageId = imapAccount.generateUniqueMessageIdForMessage(message);
        final List<StoredMessageLabel> gmailLabels = getGmailLabels(imapAccount, message);
        return createFromJavamailMessage(message, new long[] {conversationId, uniqueMessageId, messageUID}, gmailLabels);
   }

    public StoredIMAPMessage createFromJavamailMessage(MimeMessage message, long messageUID, MessageIdGenerator idGenerator) throws MessagingException, IOException {
        final long conversationId = idGenerator.getConversationId(message);
        final long uniqueMessageId = idGenerator.getUniqueMessageId(message);
        return createFromJavamailMessage(message, new long[] { conversationId, uniqueMessageId, messageUID}, null);
    }

    private StoredIMAPMessage createFromJavamailMessage(MimeMessage message, long[] ids, List<StoredMessageLabel> labels) throws MessagingException, IOException {
        final long conversationId = ids[0];
        final long uniqueMessageId = ids[1];
        final long messageUID = ids[2];

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
                .labels(labels)
                .attachments(attachmentExtractor.getAttachments(message))
             .build();
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

    private String getDisplayText(MimeMessage message) {
        return MessageBodyUtils.getTextBody(message);
    }

    private String getSubject(MimeMessage message) throws MessagingException {
        return Strings.nullToEmpty(message.getSubject());
    }

    private MessageUser getSender(MimeMessage message) throws MessagingException {
        return addressToMessageUser(message.getSender(), MessageUser.UserType.USER_SENDER);
    }

    private List<MessageUser> getRecipients(MimeMessage message) throws MessagingException {
        final List<MessageUser> recipients = new ArrayList<>();
        addRecipientsToList(recipients, message, Message.RecipientType.TO, MessageUser.UserType.USER_TO);
        addRecipientsToList(recipients, message, Message.RecipientType.CC, MessageUser.UserType.USER_CC);
        return recipients;
    }


    private void addRecipientsToList(List<MessageUser> recipientList, MimeMessage message, Message.RecipientType recipientType, MessageUser.UserType userType) throws MessagingException {
        if (message.getRecipients(recipientType) == null) {
            return;
        }
        for (Address address : message.getRecipients(recipientType)) {
            MessageUser mu = addressToMessageUser(address, userType);
            if (mu != null) {
                recipientList.add(mu);
            }
        }
    }

    private MessageUser addressToMessageUser(Address address, MessageUser.UserType type) {
        if (address == null) {
            return null;
        }
        final String username = getAddressUsername(address);
        final String email = getAddressEmail(address);
        if (email == null) {
            return null;
        } else {
            return MessageUser.create(username, email, type);
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
            return 0;
        } else {
            return date.getTime();
        }
    }

    private long getMessageFlags(MimeMessage message) throws MessagingException {
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
                labelList.add(account.getMessageLabelByName(label));
            }
        }
        return labelList;
    }
}
