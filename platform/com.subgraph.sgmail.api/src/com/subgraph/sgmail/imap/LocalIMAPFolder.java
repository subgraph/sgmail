package com.subgraph.sgmail.imap;

import java.util.Set;

import com.subgraph.sgmail.messages.StoredMessage;

import gnu.trove.list.TLongList;

public interface LocalIMAPFolder {

	long getUIDNext();

	void setUIDValidity(long value);

	long getUIDValidity();

	void setUIDNext(long value);

	long getHighestModSeq();

	void setHighestModSeq(long value);

	TLongList getUIDMap();

	void clearFolder();

	StoredMessage getMessageByMessageNumber(int number);

	void appendMessage(StoredMessage message, long messageUID);

	int getMessageCount();

	void expungeMessagesByUID(Set<Long> uids);
	
	void commit();

}
