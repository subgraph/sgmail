package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.messages.impl.*;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

public class StoredMessages {
    /**
     * Create a new StoredIMAPMessage instance from a Javamail IMAPMessage and an IMAP message UID value.
     *
     * @param message a Javamail IMAP message
     * @param messageUID IMAP message UID value
     *
     * @return a new StoredIMAPMessage
     *
     * @throws MessagingException if an exception occurs while extracting information from Javamail Message instance.
     */
    public static StoredIMAPMessage createIMAPMessage(IMAPAccount imapAccount, IMAPMessage message, long messageUID) throws MessagingException {
        final long conversationId = imapAccount.generateConversationIdForMessage(message);
        final long uniqueMessageId = imapAccount.generateUniqueMessageIdForMessage(message);
        final StoredIMAPMessage imapMessage = createIMAPMessage(message, conversationId, uniqueMessageId, messageUID);
        if(message instanceof GmailMessage) {
            addGmailLabels(imapAccount, (GmailMessage) message, imapMessage);
        }
        return imapMessage;
    }

    public static StoredIMAPMessage createIMAPMessage(MimeMessage message, long conversationId, long uniqueMessageId, long messageUID) throws MessagingException {
        final StoredIMAPMessageSummary messageData = StoredIMAPMessageSummary.createForJavamailMessage(message, uniqueMessageId, messageUID);
        final long messageDate = getMessageTimestamp(message);
        final StoredIMAPMessage imapMessage =  new StoredIMAPMessageImpl(conversationId, messageDate, messageData);
        imapMessage.setFlags(getMessageFlags(message));
        return imapMessage;
    }

    public static StoredIMAPFolder createIMAPFolder(IMAPAccount imapAccount, String folderName) {
        return new StoredIMAPFolderImpl(imapAccount, folderName);
    }

    public static StoredMessageLabelCollection createLabelCollection(Account account) {
        return new StoredMessageLabelCollectionImpl(account);
    }

    private static long getMessageTimestamp(MimeMessage message) throws MessagingException {
        final Date date = message.getReceivedDate();
        if(date == null) {
            return 0;
        } else {
            return date.getTime();
        }
    }

    private static long getMessageFlags(MimeMessage message) throws MessagingException {
        return FlagUtils.getFlagsFromMessage(message);
    }

    private static void addGmailLabels(IMAPAccount account, GmailMessage gmailMessage, StoredIMAPMessage imapMessage) throws MessagingException {
        for(String label: gmailMessage.getLabels()) {
            if(!label.isEmpty()) {
                imapMessage.addLabel(account.getMessageLabelByName(label));
            }
        }
    }
}
