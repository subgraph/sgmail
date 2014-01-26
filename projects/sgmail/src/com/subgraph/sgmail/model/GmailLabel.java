package com.subgraph.sgmail.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.subgraph.sgmail.events.ConversationAddedEvent;

public class GmailLabel extends AbstractActivatable implements ConversationSource {
	
	private final GmailIMAPAccount account;
	
	private String name;
	private List<StoredGmailMessage> messages = new ActivatableArrayList<>();
	private ConversationCollection<Long> conversations = new ConversationCollection<>();
	
	private transient Set<Long> messageIds;
	
	public GmailLabel(GmailIMAPAccount account, String name) {
		this.account = checkNotNull(account);
		this.name = checkNotNull(name);
	}
	
	ConversationCollection<Long> getConversationCollection() {
		return conversations;
	}

	public BasicConversation getConversationForMessage(StoredGmailMessage message) {
		activate(ActivationPurpose.READ);
		return conversations.getConversationByKey(message.getGoogleThreadId(), true);
	}
	
	public List<Conversation> getConversations() {
		activate(ActivationPurpose.READ);
		return new ArrayList<Conversation>(conversations.getConversations());
	}

	public GmailIMAPAccount getAccount() {
		activate(ActivationPurpose.READ);
		return account;
	}

	public String getName() {
		activate(ActivationPurpose.READ);
		return name;
	}

	public synchronized int getNewMessageCount() {
		activate(ActivationPurpose.READ);
		int newCount = 0;
		
		for(StoredMessage sm: messages) {
			if(sm != null && sm.isNewMessage()) {
				newCount += 1;
			}
		}
		return newCount;
	}
	
	public synchronized void addMessage(StoredGmailMessage message) {
		activate(ActivationPurpose.READ);
		checkNotNull(message);
		
		if(isDuplicateMessage(message)) {
			return;
		}

		messages.add(message);
		message.addLabel(this);
		
		addMessageToConversation(message);

	}
	
	private boolean isDuplicateMessage(StoredGmailMessage message) {
		final Set<Long> ids = getMessageIds();
		synchronized(ids) {
			return !ids.add(message.getGoogleMessageId());
		}
	}

	private Set<Long> getMessageIds() {
		if(messageIds == null) {
			messageIds = new HashSet<>();
			for(StoredGmailMessage m: messages) {
				if(m != null) {
					messageIds.add(m.getGoogleMessageId());
				}
			}
		}
		return messageIds;
	}

	private void addMessageToConversation(StoredGmailMessage message) {
		final BasicConversation c = getConversationForMessage(message);
		synchronized(c) {
			c.addMessage(message);
			if(c.getMessageCount() == 1) {
				model.postEvent(new ConversationAddedEvent(this, c));
			}
		}
	}

	public synchronized List<StoredGmailMessage> getMessages() {
		activate(ActivationPurpose.READ);
		return messages;
	}

	public void removeMessage(StoredGmailMessage message) {
		activate(ActivationPurpose.READ);
		final BasicConversation conversation = getConversationForMessage(message);
		synchronized(conversation) {
			conversation.removeMessage(message);
			if(conversation.getMessageCount() == 0) {
				conversations.removeConversation(message.getGoogleThreadId());
			}
		}
	}
	
}
