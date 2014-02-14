package com.subgraph.sgmail.identity.server;


import com.google.common.primitives.UnsignedLongs;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class KeyRegistrationMailer {
    public final static String SMTP_SERVER_PROPERTY = "com.subgraph.identity.smtpServer";
    public final static String SMTP_LOGIN_PROPERTY = "com.subgraph.identity.smtpLogin";
    public final static String SMTP_PASSWORD_PROPERTY = "com.subgraph.identity.smtpPassword";
    public final static String FROM_ADDRESS_PROPERTY = "com.subgraph.identity.fromAddress";

    public static KeyRegistrationMailer create(Properties properties) {
        final String smtpServer = properties.getProperty(SMTP_SERVER_PROPERTY);
        final String smtpLogin = properties.getProperty(SMTP_LOGIN_PROPERTY);
        final String smtpPassword = properties.getProperty(SMTP_PASSWORD_PROPERTY);
        final String fromAddress = properties.getProperty(FROM_ADDRESS_PROPERTY);
        return new KeyRegistrationMailer(smtpServer, smtpLogin, smtpPassword, fromAddress);
    }

    private final Session session;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String fromAddress;

    private KeyRegistrationMailer(String smtpServer, String smtpUsername, String smtpPassword, String fromAddress) {
        this.session = createSession(smtpServer);
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.fromAddress = fromAddress;
    }

    private Session createSession(String smtpServer) {
        final Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpServer);
        properties.put("mail.transport.protocol", "smtps");
        return Session.getInstance(properties);
    }

    public void queueRequest(KeyRegistrationState request) {
        try {
            sendMail(request);
            request.setCurrentState(KeyRegistrationState.State.STATE_MAIL_SENT);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void sendMail(KeyRegistrationState krs) throws MessagingException {
        Transport.send(createMessageFor(krs), smtpUsername, smtpPassword);
    }

    private MimeMessage createMessageFor(KeyRegistrationState krs) throws MessagingException {
        final MimeMessage message = new MimeMessage(session);
        final Address from = new InternetAddress(fromAddress);
        final Address to = new InternetAddress(krs.getEmailAddress());

        message.setRecipient(Message.RecipientType.TO, to);
        message.setFrom(from);
        message.setSubject("...");
        message.setText("body text here");

        message.addHeader("X-SGMAIL-IDENTITY-REGISTRATION", createRegistrationHeaderValue(krs));

        return message;
    }

    private String createRegistrationHeaderValue(KeyRegistrationState krs) {
        return UnsignedLongs.toString(krs.getRequestId(), 16) +
               ":" +
               UnsignedLongs.toString(krs.getMailId(), 16);

    }


}
