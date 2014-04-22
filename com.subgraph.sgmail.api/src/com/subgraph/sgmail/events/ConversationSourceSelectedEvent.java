package com.subgraph.sgmail.events;

import ca.odell.glazedlists.GroupingList;
import com.subgraph.sgmail.messages.StoredMessage;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConversationSourceSelectedEvent {


    private final GroupingList<StoredMessage> conversationList;


	public ConversationSourceSelectedEvent(GroupingList<StoredMessage> conversationList) {
        this.conversationList = checkNotNull(conversationList);
	}

    public GroupingList<StoredMessage> getSelectedSource() {
        return conversationList;
    }
}
