package com.subgraph.sgmail.ui.compose;

import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

public class SendMailTask implements Runnable {

	private final MessageComposer composer;
	private final Message message;
	private final MailAccount account;
	
	SendMailTask(MessageComposer composer, Message msg, MailAccount account) {
		this.composer = composer;
		this.message = msg;
		this.account = account;
	}
	
	@Override
	public void run() {
		final Session session = message.getSession();
		try {
			Transport transport = session.getTransport("smtps");
            final ServerDetails smtpAccount = account.getSMTPAccount();
            transport.connect(smtpAccount.getHostname(), smtpAccount.getPort(), smtpAccount.getLogin(), smtpAccount.getPassword());
			composer.onMailSendProgress("Connected...");
			transport.sendMessage(message, message.getAllRecipients());
			composer.onMailSendSuccess();
		} catch (MessagingException e) {
			composer.onMailSendFailed(e.getMessage());
		}
	}
}
