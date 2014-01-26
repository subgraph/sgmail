package com.subgraph.sgmail.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;

public class StoredFolder extends AbstractActivatable implements ConversationSource {

	private final Account account;
	private String fullName;
	private long uidValidity;
	private long uidNext;
	private long highestModSeq;
	
	private final ArrayList<StoredMessage> messages = new ActivatableArrayList<>();
	

	private transient List<Long> cachedUIDMap;
	
	public StoredFolder(Account account, String fullName) {
		this.account = account;
		this.fullName = fullName;
	}

	public Account getAccount() {
		activate(ActivationPurpose.READ);
		return account;
	}

	public boolean hasNewMessages() {
		activate(ActivationPurpose.READ);
		return false;
	}

	public String getFullName() {
		activate(ActivationPurpose.READ);
		return fullName;
	}

	public int getMessageCount() {
		activate(ActivationPurpose.READ);
		return messages.size();
	}

	public void setMessage(int index, StoredMessage message) {
		activate(ActivationPurpose.READ);
		ensureCapacity(index);
		messages.set(index, message);
	}

	private void ensureCapacity(int maxIndex) {
		if((maxIndex + 1) < messages.size()) {
			return;
		}
		final int needed = (maxIndex - messages.size()) + 1;
		
		for(int i = 0; i < needed; i++) {
			messages.add(null);	
		}
	}

	public StoredMessage getMessage(int index) {
		activate(ActivationPurpose.READ);
		if (messages.size() >= index) {
			return messages.get(index - 1);
		}
		return null;
	}

	public void addMessage(StoredMessage message) {
		activate(ActivationPurpose.READ);
		synchronized (messages) {
			model.store(message);
			messages.add(message);
			message.setFolder(this);
			message.setMessageNumber(messages.size());
		}
		model.commit();
	}

	public void rename(String newName) {
		activate(ActivationPurpose.WRITE);
		this.fullName = newName;
	}

	public void expunge() {
		activate(ActivationPurpose.READ);
		synchronized (messages) {
			if (!hasDeletedMessages()) {
				return;
			}
			final List<StoredMessage> savedMessages = new ArrayList<>();
			for (StoredMessage sm : messages) {
				if (!sm.getDeleted()) {
					savedMessages.add(sm);
				}
			}
			messages.clear();
			messages.addAll(savedMessages);
			cachedUIDMap = null;
		}
	}

	public void expungeMessagesByUID(List<Long> uids) {
		final Set<Long> uidset = new HashSet<>(uids);
		final List<StoredMessage> saved = new ArrayList<>();
		final List<StoredMessage> removed = new ArrayList<>();
		synchronized(messages) {
			for(StoredMessage m: messages) {
				if(uidset.contains(m.getMessageUID())) {
					removed.add(m);
				} else {
					saved.add(m);
					m.setMessageNumber(saved.size());
				}
			}
			messages.clear();
			messages.addAll(saved);
			cachedUIDMap = null;
		}
		
		for(StoredMessage m: removed) {
			model.delete(m);
		}
		model.commit();
	}
	
	public List<Long> getUIDMap() {
		activate(ActivationPurpose.READ);
		synchronized(messages) {
			if(cachedUIDMap == null) {
				cachedUIDMap = generateUIDMap();
			}
			return cachedUIDMap;
		}
	}

	private List<Long> generateUIDMap() {
		final List<Long> result = new ArrayList<>();
		for(StoredMessage sm: messages) {
			result.add(sm.getMessageUID());
		}
		return result;
	}
	
	private boolean hasDeletedMessages() {
		for (StoredMessage sm : messages) {
			if (sm.getDeleted()) {
				return true;
			}
		}
		return false;
	}

	public List<StoredMessage> getMessages() {
		activate(ActivationPurpose.READ);
		return messages;
	}
	
	public long getUIDValidity() {
		activate(ActivationPurpose.READ);
		return uidValidity;
	}
	
	public void setUIDValidity(long value) {
		activate(ActivationPurpose.WRITE);
		this.uidValidity = value;
	}
	
	public long getUIDNext() {
		activate(ActivationPurpose.READ);
		return uidNext;
	}
	
	public void setUIDNext(long value) {
		activate(ActivationPurpose.WRITE);
		this.uidNext = value;
	}

	public long getHighestModSeq() {
		activate(ActivationPurpose.READ);
		return highestModSeq;
	}
	
	public void setHighestModSeq(long value) {
		activate(ActivationPurpose.WRITE);
		highestModSeq = value;
	}

	public void clearFolder() {
		activate(ActivationPurpose.WRITE);
		synchronized (messages) {
			uidNext = 0;
			uidValidity = 0;
			highestModSeq = 0;
			messages.clear();
		}
	}

	@Override
	public List<Conversation> getConversations() {
		return new ArrayList<Conversation>(messages);
	}

	@Override
	public int getNewMessageCount() {
		int count = 0;
		for(Conversation c: getConversations()) {
			count += c.getNewMessageCount();
		}
		return count;
	}
}
