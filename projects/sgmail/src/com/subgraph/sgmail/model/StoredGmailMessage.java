package com.subgraph.sgmail.model;

import java.util.Set;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableHashSet;

public class StoredGmailMessage extends StoredMessage {

	private final long googleMessageId;
	private final long googleThreadId;
	private final Set<GmailLabel> labels;
	
	public StoredGmailMessage(long uid, byte[] messageData, long flags, long googleMessageId, long googleThreadId) {
		super(uid, messageData, flags);
		this.googleMessageId = googleMessageId;
		this.googleThreadId = googleThreadId;
		this.labels = new ActivatableHashSet<>();
	}

	public long getGoogleMessageId() {
		activate(ActivationPurpose.READ);
		return googleMessageId;
	}
	
	public long getGoogleThreadId() {
		activate(ActivationPurpose.READ);
		return googleThreadId;
	}
	
	public synchronized void addLabel(GmailLabel label) {
		activate(ActivationPurpose.READ);
		labels.add(label);
		label.addMessage(this);
	}
	
	public synchronized void removeLabel(GmailLabel label) {
		activate(ActivationPurpose.READ);
		labels.remove(label);
		label.removeMessage(this);
	}

	public synchronized Set<GmailLabel> getLabels() {
		activate(ActivationPurpose.READ);
		return labels;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (googleMessageId ^ (googleMessageId >>> 32));
		result = prime * result
				+ (int) (googleThreadId ^ (googleThreadId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoredGmailMessage other = (StoredGmailMessage) obj;
		if (googleMessageId != other.googleMessageId)
			return false;
		if (googleThreadId != other.googleThreadId)
			return false;
		return true;
	}
}
