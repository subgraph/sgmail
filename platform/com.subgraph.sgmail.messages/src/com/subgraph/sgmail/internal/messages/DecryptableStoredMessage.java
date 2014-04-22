package com.subgraph.sgmail.internal.messages;

import java.util.List;

import com.subgraph.sgmail.messages.MessageAttachment;

public interface DecryptableStoredMessage {
	void setDecryptedMessageDetails(byte[] decryptedRawBytes, String decryptedBody, List<MessageAttachment> decryptedAttachments);
}
