package com.subgraph.sgmail.events;

public class NextMessageEvent {
	private final boolean isNewOnly;
	private final boolean isNextConversation;
	
	public NextMessageEvent(boolean isNewOnly, boolean isNextConversation) {
		this.isNewOnly = isNewOnly;
		this.isNextConversation = isNextConversation;
	}
	
	public boolean isNewOnly() {
		return isNewOnly;
	}
	
	public boolean isNextConversation() {
		return isNextConversation;
	}
}
