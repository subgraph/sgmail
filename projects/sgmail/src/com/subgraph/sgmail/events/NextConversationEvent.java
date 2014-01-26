package com.subgraph.sgmail.events;

public class NextConversationEvent {
	private final boolean isNewOnly;
	
	public NextConversationEvent(boolean isNewOnly) {
		this.isNewOnly = isNewOnly;
	}

	public boolean isNewOnly() {
		return isNewOnly;
	}
}
