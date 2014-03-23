package com.subgraph.sgmail.ui.compose;

import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.accounts.SMTPAccount;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public class SendMailTask implements Runnable {

	private final MessageComposer composer;
	private final Message message;
	private final IMAPAccount account;
	
	SendMailTask(MessageComposer composer, Message msg, IMAPAccount account) {
		this.composer = composer;
		this.message = msg;
		this.account = account;
	}
	
	@Override
	public void run() {
		final Session session = message.getSession();
		try {
			Transport transport = session.getTransport("smtps");
            final SMTPAccount smtpAccount = account.getSMTPAccount();
            transport.connect(smtpAccount.getHostname(), smtpAccount.getPort(), smtpAccount.getUsername(), smtpAccount.getPassword());
			composer.onMailSendProgress("Connected...");
			transport.sendMessage(message, message.getAllRecipients());
			composer.onMailSendSuccess();
		} catch (MessagingException e) {
			composer.onMailSendFailed(e.getMessage());
		}
	}
}
