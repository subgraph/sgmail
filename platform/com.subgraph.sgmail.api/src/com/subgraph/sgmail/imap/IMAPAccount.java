package com.subgraph.sgmail.imap;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.messages.StoredMessage;
import com.sun.mail.imap.IMAPStore;

public interface IMAPAccount {

	boolean isAutomaticSyncEnabled();

	int generateUniqueMessageIdForMessage(MimeMessage message, Model model) throws MessagingException;

	StoredMessage getMessageForMimeMessage(MimeMessage message) throws MessagingException;

	MailAccount getMailAccount();

	int generateConversationIdForMessage(MimeMessage message) throws MessagingException;

	LocalIMAPFolder getFolderByName(String name);

	IMAPStore getRemoteStore(Session session, boolean preferOnionAddress);

	ServerDetails getIMAPServerDetails();

	boolean isGmailAccount();

}
