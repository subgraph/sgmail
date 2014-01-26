package com.subgraph.sgmail.ui.compose;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import com.subgraph.sgmail.model.IMAPAccount;

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
			transport.connect(account.getSMTPHostname(), account.getSMTPPort(), account.getSMTPUsername(), account.getSMTPPassword());
			composer.onMailSendProgress("Connected...");
			transport.sendMessage(message, message.getAllRecipients());
			composer.onMailSendSuccess();
		} catch (MessagingException e) {
			composer.onMailSendFailed(e.getMessage());
		}
	}
}
