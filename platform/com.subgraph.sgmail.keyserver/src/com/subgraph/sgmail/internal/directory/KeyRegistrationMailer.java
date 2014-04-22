package com.subgraph.sgmail.internal.directory;


import com.google.common.primitives.UnsignedLongs;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.UUID;

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
    private final String smtpHostname;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String fromAddress;

    private KeyRegistrationMailer(String smtpHostname, String smtpUsername, String smtpPassword, String fromAddress) {
        this.session = Session.getInstance(new Properties());
        this.session.setDebug(true);
        this.smtpHostname = smtpHostname;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.fromAddress = fromAddress;
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
        Transport transport = session.getTransport("smtps");
        try {
            transport.connect(smtpHostname, smtpUsername, smtpPassword);
            final MimeMessage message = createMessageFor(krs);
            message.saveChanges();
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    private MimeMessage createMessageFor(KeyRegistrationState krs) throws MessagingException {
        final MimeMessage message = new MimeMessage(session) {
            protected void updateMessageID() throws MessagingException {
                setHeader("Message-ID", generateMessageID());
            }
        };

        final Address from = new InternetAddress(fromAddress);
        final Address to = new InternetAddress(krs.getEmailAddress());

        message.setRecipient(Message.RecipientType.TO, to);
        message.setFrom(from);
        message.setSubject("SGMail Identity Registration");
        message.setText("This is an automated mail sent by the SGMail identity registration server.  ");

        message.addHeader("X-SGMAIL-IDENTITY-REGISTRATION", createRegistrationHeaderValue(krs));

        return message;
    }

    private String generateMessageID() {
        final UUID uuid = UUID.randomUUID();
        return "<"+ uuid + ">";
    }

    private String createRegistrationHeaderValue(KeyRegistrationState krs) {
        return UnsignedLongs.toString(krs.getRequestId(), 16) +
               ":" +
               UnsignedLongs.toString(krs.getMailId(), 16);

    }


}
