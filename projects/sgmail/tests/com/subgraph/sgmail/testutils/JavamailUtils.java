package com.subgraph.sgmail.testutils;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class JavamailUtils {
    public static String TEST_FROM_ADDRESS = "from@example.com";
    public static String TEST_TO_ADDRESS = "to@example.com";
    public static String TEST_MESSAGE_SUBJECT = "Test Message Subject";
    public static String TEST_MESSAGE_BODY = "Test Message Body";


    public static MimeMessage createTestMimeMessage() {
        try {
            return constructTestMimeMessage(TEST_FROM_ADDRESS, TEST_TO_ADDRESS, TEST_MESSAGE_SUBJECT, TEST_MESSAGE_BODY);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unexpected exception building test mime message "+ e, e);
        }
    }

    private static MimeMessage constructTestMimeMessage(String fromAddress, String toAddress, String subject, String body) throws MessagingException {
        final Session session = Session.getInstance(new Properties());
        final MimeMessage message = new MimeMessage(session);
        final Address from = new InternetAddress(fromAddress);
        final Address to = new InternetAddress(toAddress);
        message.setSender(from);
        message.addRecipient(Message.RecipientType.TO, to);
        message.setSubject(subject);
        message.setText(body);
        message.saveChanges();
        return message;
    }


}
