package com.subgraph.sgmail.internal.messages;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.google.common.base.Charsets;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StoredMessageContent implements Activatable {
    private byte[] bodyText;
    private final MessageUser[] toRecipients;
    private final MessageUser[] ccRecipients;
    private MessageAttachment[] attachments;
    private final StoredMessageRawData rawData;
    
	private transient Activator activator;

    StoredMessageContent(StoredMessageBuilder builder) {
        this(builder.bodyText.getBytes(Charsets.UTF_8), builder.getToRecipients(), builder.getCCRecipients(), builder.getAttachmentArray(), builder.rawDataBytes);
    }

    StoredMessageContent(byte[] bodyText, MessageUser[] toRecipients, MessageUser[] ccRecipients, MessageAttachment[] attachments, byte[] rawDataBytes) {
        this.bodyText = bodyText;
        this.toRecipients = toRecipients;
        this.ccRecipients = ccRecipients;
        this.attachments = attachments;
        this.rawData = new StoredMessageRawData(rawDataBytes);
    }

    byte[] getRawMessageBytes(boolean decrypted) {
        activate(ActivationPurpose.READ);
        return rawData.getMessageBytes(decrypted);
    }

    String getBodyText() {
        activate(ActivationPurpose.READ);
        return new String(bodyText, Charsets.UTF_8);
    }

    List<MessageUser> getToRecipients() {
        activate(ActivationPurpose.READ);
        return getRecipientList(toRecipients);
    }

    List<MessageUser> getCCRecipients() {
        activate(ActivationPurpose.READ);
        return getRecipientList(ccRecipients);
    }

    List<MessageAttachment> getAttachments() {
        activate(ActivationPurpose.READ);
        if(attachments == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(attachments);
        }
    }
    

    private static List<MessageUser> getRecipientList(MessageUser[] users) {
        if(users == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(users);
        }
    }
    

    
    void setDecryptedMessageDetails(byte[] decryptedRawBytes, String decryptedBody, List<MessageAttachment> decryptedAttachments) {
    	activate(ActivationPurpose.WRITE);
    	this.attachments = decryptedAttachments.toArray(new MessageAttachment[decryptedAttachments.size()]);
    	this.bodyText = decryptedBody.getBytes(Charsets.UTF_8);
    	rawData.setDecryptedMessageBytes(decryptedRawBytes);
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
