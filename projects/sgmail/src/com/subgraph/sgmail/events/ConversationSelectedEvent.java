package com.subgraph.sgmail.events;

import static com.google.common.base.Preconditions.checkNotNull;

import com.subgraph.sgmail.model.Conversation;

public class ConversationSelectedEvent {
	
	private final Conversation conversation;
	
	public ConversationSelectedEvent(Conversation conversation) {
		this.conversation = checkNotNull(conversation);
	}
	
	public Conversation getSelectedConversation() {
		return conversation;
	}
}
