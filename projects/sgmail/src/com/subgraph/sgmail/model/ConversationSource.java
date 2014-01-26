package com.subgraph.sgmail.model;

import java.util.List;

public interface ConversationSource {
	int getNewMessageCount();
	List<Conversation> getConversations();
}
