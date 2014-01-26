package com.subgraph.sgmail.events;

import static com.google.common.base.Preconditions.checkNotNull;

import com.subgraph.sgmail.model.GmailLabel;

public class LabelAddedEvent {
	private final GmailLabel label;
	
	public LabelAddedEvent(GmailLabel label) {
		this.label = checkNotNull(label);
	}
	
	public GmailLabel getLabel() {
		return label;
	}
}
