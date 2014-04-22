package com.subgraph.sgmail.testutils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class JavamailUtils {
    public static String TEST_FROM_ADDRESS = "from@example.com";
    public static String TEST_TO_ADDRESS = "to@example.com";
    public static String TEST_MESSAGE_SUBJECT = "Test Message Subject";
    public static String TEST_MESSAGE_BODY = "Test Message Body";


    public static MimeMessage createTestMimeMessage() {
        try {
            MimeMessage mimeMessage = constructTestMimeMessage(TEST_FROM_ADDRESS, TEST_TO_ADDRESS, TEST_MESSAGE_SUBJECT);
            mimeMessage.setText(TEST_MESSAGE_BODY);
            mimeMessage.saveChanges();
            return mimeMessage;
        } catch (MessagingException e) {
            throw new IllegalStateException("Unexpected exception building test mime message "+ e, e);
        }
    }

    public static MimeMessage createTestMimeMessageWithAttachment() throws MessagingException {
        MimeMultipart mp = new MimeMultipart();
        MimeBodyPart body = new MimeBodyPart();
        body.setText(TEST_MESSAGE_BODY);
        mp.addBodyPart(body);
        MimeBodyPart attach = new MimeBodyPart();
        attach.setDisposition(Part.ATTACHMENT);
        attach.setFileName("attachment.txt");
        attach.setContent(TEST_MESSAGE_BODY, "text/plain");
        mp.addBodyPart(attach);
        final MimeMessage mimeMessage = constructTestMimeMessage(TEST_FROM_ADDRESS, TEST_TO_ADDRESS, TEST_MESSAGE_SUBJECT);
        mimeMessage.setContent(mp);
        mimeMessage.saveChanges();
        return mimeMessage;
    }

    private static MimeMessage constructTestMimeMessage(String fromAddress, String toAddress, String subject) throws MessagingException {
        final Session session = Session.getInstance(new Properties());
        final MimeMessage message = new MimeMessage(session);
        final Address from = new InternetAddress(fromAddress);
        final Address to = new InternetAddress(toAddress);
        message.setSender(from);
        message.addRecipient(Message.RecipientType.TO, to);
        message.setSubject(subject);
        return message;
    }


}
