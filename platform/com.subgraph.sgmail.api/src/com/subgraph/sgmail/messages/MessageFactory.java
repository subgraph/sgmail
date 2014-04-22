package com.subgraph.sgmail.messages;

import java.util.List;

public interface MessageFactory {
	MessageAttachment createMessageAttachment(List<Integer> mimePath, String primaryType, String subType, String filename, String description, long length);
	MessageUser createMessageUser(String username, String address);
	boolean updateMessageWithDecryptedData(StoredMessage message, byte[] rawDecryptedMessage);
	StoredMessage.Builder createStoredMessageBuilder(byte[] rawBytes);
}
