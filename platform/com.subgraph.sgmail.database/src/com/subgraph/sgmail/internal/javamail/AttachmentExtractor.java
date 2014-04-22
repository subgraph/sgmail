package com.subgraph.sgmail.internal.javamail;

import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageFactory;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class AttachmentExtractor {
	
    private final static Logger logger = Logger.getLogger(AttachmentExtractor.class.getName());
    
    private final MessageFactory messageFactory;
    
    AttachmentExtractor(MessageFactory messageFactory) {
    	this.messageFactory = messageFactory;
    }
    
    List<MessageAttachment> getAttachments(MimeMessage message) throws MessagingException, IOException {
        final List<MessageAttachment> attachments = new ArrayList<>();
        if(isMultipartContentType(message.getContentType()) && (message.getContent() instanceof MimeMultipart)) {
            processMultipartAttachments((MimeMultipart) message.getContent(), Collections.emptyList(), attachments);
        }
        return attachments;
    }

    private void processMultipartAttachments(MimeMultipart multipart, List<Integer> mimePath, List<MessageAttachment> attachments) throws MessagingException, IOException, MessagingException {
        final int count = multipart.getCount();
        for(int i = 0; i < count; i++) {
            processMimeBodyPart((MimeBodyPart) multipart.getBodyPart(i), updateMimePath(mimePath, i), attachments);
        }
    }

    private void processMimeBodyPart(MimeBodyPart bodyPart, List<Integer> mimePath, List<MessageAttachment> attachments) throws MessagingException, IOException {
        if (isMultipartContentType(bodyPart.getContentType())) {
            processMultipartBodyPart(bodyPart, mimePath, attachments);
        } else if (isAttachment(bodyPart.getDisposition(), bodyPart.getFileName(), bodyPart.getContentType())) {
            processAttachmentBodyPart(bodyPart, mimePath, attachments);
        }
    }

    private void processAttachmentBodyPart(MimeBodyPart bodyPart, List<Integer> mimePath, List<MessageAttachment> attachments) throws MessagingException {
        final String filename = getFilename(bodyPart, attachments.size());
        final ContentType contentType = getContentTypeForPart(bodyPart);
        final MessageAttachment attachment = messageFactory.createMessageAttachment(mimePath, contentType.getPrimaryType(), contentType.getSubType(), filename, bodyPart.getDescription(), bodyPart.getSize());
        attachments.add(attachment);
    }

    private void processMultipartBodyPart(MimeBodyPart bodyPart, List<Integer> mimePath, List<MessageAttachment> attachments) throws MessagingException, IOException {
        final Object content = bodyPart.getContent();
        if(!(content instanceof MimeMultipart)) {
            logger.warning("Message body part has contentType "+ bodyPart.getContentType() + " but getContent() returns "+ content.getClass());
        } else {
            processMultipartAttachments((MimeMultipart) content, mimePath, attachments);
        }
    }

    private boolean isAttachment(String disposition, String filename, String contentType) {
        if(Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
            return true;
        } else if (filename == null) {
            return false;
        }

        if(Part.INLINE.equalsIgnoreCase(disposition)) {
            try {
                ContentType ct = new ContentType(contentType);
                return !ct.getPrimaryType().equalsIgnoreCase("text");
            } catch (ParseException e) {
                return false;
            }
        }
        return true;
    }

    private String getFilename(MimeBodyPart bodyPart, int n) throws MessagingException {
        final String filename = cleanFilename(bodyPart.getFileName());
        if (filename == null) {
            return String.format("attachment%02d", n + 1);
        } else {
            return filename;
        }
    }

    private String cleanFilename(String input) {
        if(input == null) {
            return null;
        }
        String s = input.trim();
        while(s.endsWith(File.separator)) {
           s = s.substring(0, s.length() - 1);
        }
        int idx = s.lastIndexOf(File.separatorChar);
        if(idx != -1) {
            return s.substring(idx + 1);
        }
        return s;
    }

    private List<Integer> updateMimePath(List<Integer> mimePath, int newIndex) {
        final List<Integer> newMimePath = new ArrayList<>(mimePath);
        newMimePath.add(newIndex);
        return newMimePath;
    }

    private boolean isMultipartContentType(String contentType) throws ParseException {
        if(contentType == null) {
            return false;
        }
        final ContentType ct = new ContentType(contentType);
        return "multipart".equalsIgnoreCase(ct.getPrimaryType());
    }

    private ContentType getContentTypeForPart(Part part) throws MessagingException {
        final String ct = part.getContentType();
        if(ct == null) {
            return null;
        } else {
            return new ContentType(ct);
        }
    }
}
