package com.subgraph.sgmail.events;

import com.subgraph.sgmail.messages.StoredMessageLabel;

import static com.google.common.base.Preconditions.checkNotNull;

public class LabelAddedEvent {
    private final StoredMessageLabel label;

	public LabelAddedEvent(StoredMessageLabel label) {
		this.label = checkNotNull(label);
	}
	
	public StoredMessageLabel getLabel() {
		return label;
	}
}
