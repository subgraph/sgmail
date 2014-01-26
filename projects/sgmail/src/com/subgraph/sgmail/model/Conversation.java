package com.subgraph.sgmail.model;

import java.util.List;

public interface Conversation {
	 boolean hasUndeletedMessages();

	
	int getMessageCount();


	List<StoredMessage> getMessages();

	
	StoredMessage getLeadMessage();
	int getNewMessageCount();
}
