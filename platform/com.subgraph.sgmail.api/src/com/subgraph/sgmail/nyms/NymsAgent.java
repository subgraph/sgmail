package com.subgraph.sgmail.nyms;

import javax.mail.internet.MimeMessage;

public interface NymsAgent {
  enum MessageStatus { CLEAR, ENCRYPTED_MIME, SIGNED_MIME, ENCRYPTED_INLINE, SIGNED_INLINE };

  NymsAgentStatus getStatus();
	
  boolean hasSigningKey(String emailAddress) throws NymsAgentException;
  boolean hasKeyForAddress(String emailAddress) throws NymsAgentException;
  NymsKeyInfo getKeyInfo(String emailAddress) throws NymsAgentException;
  NymsKeyInfo getKeyInfoByKeyId(String keyId) throws NymsAgentException;
  byte[] getAvatarImage(String emailAddress) throws NymsAgentException;
	
  NymsIncomingProcessingResult processIncomingMessage(MimeMessage incomingMessage) throws NymsAgentException;
  NymsIncomingProcessingResult processIncomingMessage(MimeMessage incomingMessage, String passphrase) throws NymsAgentException;


  NymsOutgoingProcessingResult processOutgoingMessage(MimeMessage outgoingMessage, boolean requestSigning, boolean requestEncryption, String passphrase) throws NymsAgentException;

  NymsKeyGenerationParameters createKeyGenerationParameters(String emailAddress);
  NymsKeyInfo generateKeys(NymsKeyGenerationParameters parameters) throws NymsAgentException;
  
  boolean unlockPrivateKey(NymsKeyInfo key, String passphrase) throws NymsAgentException;
  
  MessageStatus getMessageStatus(MimeMessage message);
  
}
