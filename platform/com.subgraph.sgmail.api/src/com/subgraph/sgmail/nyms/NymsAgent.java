package com.subgraph.sgmail.nyms;

import javax.mail.internet.MimeMessage;

public interface NymsAgent {
  int getVersion() throws NymsAgentException;
	
  boolean hasSigningKey(String emailAddress) throws NymsAgentException;
  boolean hasKeyForAddress(String emailAddress) throws NymsAgentException;
  NymsKeyInfo getKeyInfo(String emailAddress) throws NymsAgentException;
  byte[] getAvatarImage(String emailAddress) throws NymsAgentException;
	
  NymsIncomingProcessingResult processIncomingMessage(MimeMessage incomingMessage) throws NymsAgentException;

  MimeMessage processOutgoingMessage(MimeMessage outgoingMessage) throws NymsAgentException;

  NymsKeyGenerationParameters createKeyGenerationParameters(String emailAddress);
  NymsKeyInfo generateKeys(NymsKeyGenerationParameters parameters) throws NymsAgentException;
  
  boolean unlockPrivateKey(NymsKeyInfo key, String passphrase) throws NymsAgentException;
  
}
