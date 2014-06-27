package com.subgraph.sgmail.nyms;

import javax.mail.internet.MimeMessage;

public interface NymsAgent {
  int getVersion() throws NymsAgentException;
	
  boolean hasSigningKey(String emailAddress) throws NymsAgentException;
  boolean hasKeyForAddress(String emailAddress) throws NymsAgentException;
  byte[] getAvatarImage(String emailAddress);
	
  MimeMessage processIncomingMessage(MimeMessage incomingMessage) throws NymsAgentException;

  MimeMessage processOutgoingMessage(MimeMessage outgoingMessage) throws NymsAgentException;
}
