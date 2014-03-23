package com.subgraph.sgmail.events;

import com.subgraph.sgmail.messages.StoredIMAPMessage;

public class MessageStateChangedEvent {

	private final StoredIMAPMessage message;

    public MessageStateChangedEvent(StoredIMAPMessage message) {
		this.message = message;
	}
	
	public StoredIMAPMessage getMessage() {
		return message;
	}
}


