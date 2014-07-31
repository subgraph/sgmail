package com.subgraph.sgmail.internal.messages;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class StoredMessageImpl implements StoredMessage, DecryptableStoredMessage, Activatable {

    private final int messageId;
    private final int conversationId;
    private final int messageDate;

    protected final StoredMessageSummary summary;

    private int flags;
    private long labelBits;
    private long[] extendedLabelBits;
    
	private transient Activator activator;

    StoredMessageImpl(StoredMessageBuilder builder, StoredMessageSummary summary) {
        this.messageId = builder.messageId;
        this.conversationId = builder.conversationId;
        this.messageDate = builder.messageDate;
        this.summary = summary;
    }

    @Override
    public int getMessageId() {
        activate(ActivationPurpose.READ);
        return messageId;
    }

    public int getConversationId() {
       activate(ActivationPurpose.READ);
       return conversationId;
    }

    public int getMessageDate() {
        activate(ActivationPurpose.READ);
        return messageDate;
    }

    @Override
    public void addLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.WRITE);
        if(label.getIndex() > 64) {
            addExtendedLabel(label);
        } else {
            labelBits |= getLabelBit(label);
        }
    }

    @Override
    public void removeLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.WRITE);
        if(label.getIndex() > 64) {
            removeExtendedLabel(label);
        } else {
            labelBits &= ~getLabelBit(label);
        }
    }

    @Override
    public boolean containsLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.READ);
        if(label.getIndex() > 64) {
            return containsExtendedLabel(label);
        } else {
            final long bit = getLabelBit(label);
            return (labelBits & bit) == bit;
        }
    }

    private long getLabelBit(StoredMessageLabel label) {
        if(label.getIndex() > 64) {
            throw new IllegalArgumentException("Label has extended index: "+ label.getIndex());
        }
        return 1L << (label.getIndex() - 1);
    }

    void addExtendedLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.WRITE);
        final int idx = getExtendedLabelWordIndex(label, true);
        extendedLabelBits[idx] |= getExtendedLabelBit(label);
    }

    void removeExtendedLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.WRITE);
        final int idx = getExtendedLabelWordIndex(label, true);
        extendedLabelBits[idx] &= ~(getExtendedLabelBit(label));
    }

    boolean containsExtendedLabel(StoredMessageLabel label) {
        activate(ActivationPurpose.WRITE);
        if(extendedLabelBits == null) {
            return false;
        }
        final int idx = getExtendedLabelWordIndex(label, false);
        if(idx >= extendedLabelBits.length) {
            return false;
        }
        final long bit = getExtendedLabelBit(label);
        return (extendedLabelBits[idx] & bit) == bit;
    }

    private int getExtendedLabelWordIndex(StoredMessageLabel label, boolean ensure) {
        if(label.getIndex() <= 64) {
            throw new IllegalArgumentException("Label index is not an extended value: "+ label.getIndex());
        }
        final int idx = (label.getIndex() - 65) / 64;
        if(ensure) {
            ensureLabelWordIndexExists(idx);
        }
        return idx;
    }

    private void ensureLabelWordIndexExists(int idx) {
        if(extendedLabelBits == null) {
            extendedLabelBits = new long[idx + 1];
        } else if(idx >= extendedLabelBits.length) {
            extendedLabelBits = Arrays.copyOf(extendedLabelBits, idx + 1);
        }
    }

    private long getExtendedLabelBit(StoredMessageLabel label) {
        return (1L << (label.getIndex() - 1) % 64);
    }

    @Override
    public String getSubject() {
        activate(ActivationPurpose.READ);
        return summary.getSubject();
    }

    @Override
    public String getBodySnippet() {
        activate(ActivationPurpose.READ);
        return summary.getBodySnippet();
    }

    @Override
    public String getBodyText() {
        activate(ActivationPurpose.READ);
        return summary.getBodyText();
    }

    @Override
    public List<MessageAttachment> getAttachments() {
        activate(ActivationPurpose.READ);
        return summary.getAttachments();
    }

    @Override
    public MessageUser getSender() {
        activate(ActivationPurpose.READ);
        return summary.getSender();
    }

    @Override
    public List<MessageUser> getToRecipients() {
        activate(ActivationPurpose.READ);
        return summary.getToRecipients();
    }

    @Override
    public List<MessageUser> getCCRecipients() {
        activate(ActivationPurpose.READ);
        return summary.getCCRecipients();
    }

    @Override
    public boolean needsDecryption() {
      return isFlagSet(FLAG_ENCRYPTED) && !isFlagSet(FLAG_DECRYPTED);
    }

    @Override
    public boolean isSigned() {
      return isFlagSet(FLAG_SIGNED);
    }

    @Override
    public int getSignatureStatus() {
      activate(ActivationPurpose.READ);
      return summary.getSignatureStatus();
    }

    @Override
    public void setSignatureStatus(int value) {
      activate(ActivationPurpose.READ);
      summary.setSignatureStatus(value);
    }

    @Override
    public boolean isFlagSet(int flag) {
        activate(ActivationPurpose.READ);
        return (flags & flag) == flag;
    }

    @Override
    public int getFlags() {
        activate(ActivationPurpose.READ);
        return flags;
    }

    @Override
    public void setFlags(int value) {
        activate(ActivationPurpose.WRITE);
        this.flags = value;
    }

    @Override
    public void addFlag(int flag) {
        setFlags(getFlags() | flag);
    }

    @Override
    public void removeFlag(int flag) {
        setFlags(getFlags() & ~flag);
    }

    @Override
    public byte[] getRawMessageBytes(boolean decrypted) {
        activate(ActivationPurpose.READ);
        return summary.getRawMessageBytes(decrypted);
    }

    @Override
    public InputStream getRawMessageStream(boolean decrypted) {
        activate(ActivationPurpose.READ);
        return summary.getRawMessageStream(decrypted);
    }

    @Override
    public MimeMessage toMimeMessage(Session session) throws MessagingException {
        return summary.toMimeMessage(this, session);
    }

    @Override
    public void purgeContent() {

    }

    @Override
    public int incrementReferenceCount() {
        activate(ActivationPurpose.READ);
        return summary.incrementReferenceCount();
    }

    @Override
    public int decrementReferenceCount() {
        activate(ActivationPurpose.READ);
        return summary.decrementReferenceCount();
    }

    @Override
    public int getReferenceCount() {
        activate(ActivationPurpose.READ);
        return summary.getReferenceCount();
    }
    
	@Override
	public void setDecryptedMessageDetails(byte[] decryptedRawBytes, String decryptedBody, List<MessageAttachment> decryptedAttachments) {
		activate(ActivationPurpose.WRITE);
		flags |= StoredMessage.FLAG_DECRYPTED;
		summary.setDecryptedMessageDetails(decryptedRawBytes, decryptedBody, decryptedAttachments);
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
