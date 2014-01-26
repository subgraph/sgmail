package com.subgraph.sgmail.events;

import static com.google.common.base.Preconditions.checkNotNull;

import com.subgraph.sgmail.model.ConversationSource;

public class ConversationSourceSelectedEvent {
	
	private final ConversationSource source;
	
	public ConversationSourceSelectedEvent(ConversationSource source) {
		this.source = checkNotNull(source);
	}
	
	public ConversationSource getSelectedSource() {
		return source;
	}
}
