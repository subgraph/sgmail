package com.subgraph.sgmail.model;

import java.util.List;
import java.util.Map;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.db4o.collections.ActivatableHashMap;
import com.google.common.collect.ImmutableList;

public class ConversationCollection<T> extends AbstractActivatable {
	
	final List<BasicConversation> conversationList = new ActivatableArrayList<>();
	final Map<T, BasicConversation> conversationMap = new ActivatableHashMap<>();
	
	public List<BasicConversation> getConversations() {
		activate(ActivationPurpose.READ);
		synchronized(this) {
			return ImmutableList.copyOf(conversationList);
		}
	}
	
	public BasicConversation getConversationByKey(T key, boolean createIfAbsent) {
		activate(ActivationPurpose.READ);
		synchronized (this) {
			if(conversationMap.containsKey(key)) {
				return conversationMap.get(key);
			} else if(createIfAbsent) {
				return createConversation(key);
			} else {
				return null;
			}
		}
	}
	
	public void removeConversation(T key) {
		activate(ActivationPurpose.READ);
		synchronized(this) {
			final Conversation c = conversationMap.remove(key);
			if(c != null) {
				conversationList.remove(c);
			}
		}
	}

	private BasicConversation createConversation(T key) {
		final BasicConversation c = new BasicConversation();
		model.store(c);
		conversationList.add(c);
		conversationMap.put(key, c);
		return c;
	}
}
