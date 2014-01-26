package com.subgraph.sgmail.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;

public class BasicConversation extends AbstractActivatable implements Conversation {
	
	private final List<StoredMessage> messages = new ActivatableArrayList<>();
	
	public void addMessage(StoredMessage message) {
		activate(ActivationPurpose.READ);
		checkNotNull(message);
		synchronized (this) {
			messages.add(message);	
		}
	}
	
	public boolean removeMessage(StoredMessage message) {
		activate(ActivationPurpose.READ);
		synchronized(this) {
			return messages.remove(message);
		}
	}
	
	public boolean hasUndeletedMessages() {
		activate(ActivationPurpose.READ);
		synchronized (this) {
			for(StoredMessage sm: messages) {
				if(sm != null && !sm.isFlagSet(StoredMessage.FLAG_DELETED)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public int getMessageCount() {
		activate(ActivationPurpose.READ);
		synchronized (this) {
			return messages.size();
		}
	}

	public List<StoredMessage> getMessages() {
		activate(ActivationPurpose.READ);
		synchronized (this) {
			return new ArrayList<>(messages);
		}
	}
	
	public StoredMessage getLeadMessage() {
		activate(ActivationPurpose.READ);
		synchronized(this) {
			for(StoredMessage sm: messages) {
				if(!sm.isFlagSet(StoredMessage.FLAG_DELETED)) {
					return sm;
				}
			}
			return null;
		}
	}
	
	
	public int getNewMessageCount() {
		int count = 0;
		synchronized(this) {
			for(StoredMessage m: messages) {
				if(m != null && m.isNewMessage()) {
					count += 1;
				}
			}
		}
		return count;
	}
	
}
