package com.subgraph.sgmail.attachments;

import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredMessage;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class StoredMessageAttachmentExtractor {

    private final StoredMessage message;

    public static class AttachmentExtractionException extends Exception {
        public AttachmentExtractionException(String message) {
            super(message);
        }

        public AttachmentExtractionException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    public StoredMessageAttachmentExtractor(StoredMessage message) {
        this.message = message;
    }

    public InputStream extractAttachment(MessageAttachment attachment) throws AttachmentExtractionException {
        final MimeBodyPart attachmentPart = extractPartByPath(getBaseMultipart(), attachment.getMimePath(), 0);
        try {
            return attachmentPart.getInputStream();
        } catch (IOException e) {
            throw new AttachmentExtractionException("IOException reading attachment", e);
        } catch (MessagingException e) {
            throw new AttachmentExtractionException("MessagingException reading attachment", e);
        }
    }

    private MimeMultipart getBaseMultipart() throws AttachmentExtractionException {
        try {
            final MimeMessage mimeMessage = message.toMimeMessage();
            return getMultipartFromPart(mimeMessage);
        } catch (MessagingException e) {
            throw new AttachmentExtractionException("Exception extracting mime message from stored message", e);
        }
    }

    private MimeMultipart getMultipartFromPart(Part part) throws AttachmentExtractionException {
        try {
            final Object content = part.getContent();
            if(!(content instanceof MimeMultipart)) {
                throw new AttachmentExtractionException("Message content is not multipart as expected");
            }
            return (MimeMultipart) content;
        } catch (IOException e) {
            throw new AttachmentExtractionException("IOException extracting message content", e);
        } catch (MessagingException e) {
            throw new AttachmentExtractionException("Exception extracting multipart from message", e);
        }
    }

    MimeBodyPart extractPartByPath(MimeMultipart multipart, List<Integer> mimePath, int pathDepth) throws AttachmentExtractionException {
        if(pathDepth >= mimePath.size()) {
            throw new AttachmentExtractionException("path depth of "+ pathDepth +" exceeds length of mimePath "+ mimePath.size());
        }

        final MimeBodyPart bodyPart = getPartFromMultipart(multipart, mimePath.get(pathDepth));
        if(pathDepth == (mimePath.size() - 1)) {
            return bodyPart;
        }
        return extractPartByPath(getMultipartFromPart(bodyPart), mimePath, pathDepth + 1);
    }

    private MimeBodyPart getPartFromMultipart(MimeMultipart multipart, int index) throws AttachmentExtractionException {
        try {
            if(index >= multipart.getCount()) {
                throw new AttachmentExtractionException("Cannot extract attachment because mimePath element exceeds multipart count");
            }
            return (MimeBodyPart) multipart.getBodyPart(index);
        } catch (MessagingException e) {
            throw new AttachmentExtractionException("Exception extracting body part from multipart", e);
        }
    }
}
