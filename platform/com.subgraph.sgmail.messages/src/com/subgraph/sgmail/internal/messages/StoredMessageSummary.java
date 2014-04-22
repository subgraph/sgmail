package com.subgraph.sgmail.internal.messages;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.google.common.base.Charsets;
import com.subgraph.sgmail.messages.LocalMimeMessage;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class StoredMessageSummary implements Activatable {

    private final byte[] subject;
    private byte[] bodySnippet;
    private final MessageUser sender;
    private final StoredMessageContent content;
    private int referenceCount;

    private transient MimeMessage cachedMimeMessage;
	private transient Activator activator;

    StoredMessageSummary(StoredMessageBuilder builder, StoredMessageContent content) {
        this(builder.subject.getBytes(Charsets.UTF_8), builder.bodySnippet.getBytes(Charsets.UTF_8), builder.sender, content);
    }

    StoredMessageSummary(byte[] subject, byte[] bodySnippet, MessageUser sender, StoredMessageContent content) {
        this.subject = subject;
        this.bodySnippet = bodySnippet;
        this.sender = sender;
        this.content = content;
    }

    byte[] getRawMessageBytes(boolean decrypted) {
        activate(ActivationPurpose.READ);
        return content.getRawMessageBytes(decrypted);
    }

    String getSubject() {
        activate(ActivationPurpose.READ);
        return new String(subject, Charsets.UTF_8);
    }

    String getBodySnippet() {
        activate(ActivationPurpose.READ);
        return new String(bodySnippet, Charsets.UTF_8);
    }

    String getBodyText() {
        activate(ActivationPurpose.READ);
        return content.getBodyText();
    }

    MessageUser getSender() {
        activate(ActivationPurpose.READ);
        return sender;
    }

    List<MessageUser> getToRecipients() {
        activate(ActivationPurpose.READ);
        return content.getToRecipients();
    }

    List<MessageUser> getCCRecipients() {
        activate(ActivationPurpose.READ);
        return content.getCCRecipients();
    }

    List<MessageAttachment> getAttachments() {
        activate(ActivationPurpose.READ);
        return content.getAttachments();
    }

    InputStream getRawMessageStream(boolean decrypted) {
        return new ByteArrayInputStream(getRawMessageBytes(decrypted));
    }
    
    void setDecryptedMessageDetails(byte[] decryptedRawBytes, String decryptedBody, List<MessageAttachment> decryptedAttachments) {
    	activate(ActivationPurpose.WRITE);
    	cachedMimeMessage = null;
    	final String snippet = StoredMessageBuilder.createSnippetFromBody(decryptedBody);
    	bodySnippet = snippet.getBytes(Charsets.UTF_8);
    	content.setDecryptedMessageDetails(decryptedRawBytes, decryptedBody, decryptedAttachments);
    }
    
    synchronized MimeMessage toMimeMessage(StoredMessage msg, Session session) throws MessagingException {
        activate(ActivationPurpose.READ);
        if (cachedMimeMessage == null) {
            cachedMimeMessage = new LocalMimeMessage(msg, session, getRawMessageStream(true));
        }
        return cachedMimeMessage;
    }

    synchronized int incrementReferenceCount() {
        referenceCount += 1;
        return referenceCount;
    }

    synchronized int decrementReferenceCount() {
        if(referenceCount <= 0) {
            throw new IllegalStateException("decrementReferenceCount() called while referenceCount == "+ referenceCount);
        }
        referenceCount -= 1;
        return referenceCount;

    }

    synchronized int getReferenceCount() {
        return referenceCount;
    }
    
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}

}
