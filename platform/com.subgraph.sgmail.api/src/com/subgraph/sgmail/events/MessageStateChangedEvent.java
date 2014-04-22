package com.subgraph.sgmail.events;

import com.subgraph.sgmail.messages.StoredMessage;

public class MessageStateChangedEvent {

	private final StoredMessage message;

    public MessageStateChangedEvent(StoredMessage message) {
		this.message = message;
	}
	
	public StoredMessage getMessage() {
		return message;
	}
}


