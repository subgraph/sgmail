package com.subgraph.sgmail.events;

import com.subgraph.sgmail.messages.StoredMessage;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConversationSelectedEvent {

    private final List<StoredMessage> conversationList;
	
	public ConversationSelectedEvent(List<StoredMessage> conversationList) {
        this.conversationList = checkNotNull(conversationList);
	}

    public List<StoredMessage> getSelectedConversation() {
        return conversationList;
    }
}
