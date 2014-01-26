package com.subgraph.sgmail.events;

import static com.google.common.base.Preconditions.checkNotNull;

import com.subgraph.sgmail.model.Conversation;
import com.subgraph.sgmail.model.ConversationSource;

public class ConversationAddedEvent {
	private final ConversationSource source;
	private final Conversation conversation;
	
	public ConversationAddedEvent(ConversationSource source, Conversation conversation) {
		this.source = checkNotNull(source);
		this.conversation = checkNotNull(conversation);
	}
	
	public ConversationSource getSource() {
		return source;
	}
	
	public Conversation getConversation() {
		return conversation;
	}

}
