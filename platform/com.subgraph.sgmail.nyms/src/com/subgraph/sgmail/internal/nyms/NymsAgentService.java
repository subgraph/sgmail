package com.subgraph.sgmail.internal.nyms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.google.common.base.Charsets;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsKeyGenerationParameters;
import com.subgraph.sgmail.nyms.NymsKeyInfo;

public class NymsAgentService implements NymsAgent {

  private NymsAgentConnection connection;
  private final Set<String> addressesWithoutKeys = new HashSet<>();
  private final Map<String,NymsKeyInfo> cachedKeyInfo = new HashMap<>();

  @Override
  public int getVersion() throws NymsAgentException {
    return getConnection().version();
  }

  @Override
  public boolean hasKeyForAddress(String emailAddress) throws NymsAgentException {
    return getKeyInfo(emailAddress) != null;
  }

  @Override
  public MimeMessage processIncomingMessage(MimeMessage incomingMessage) throws NymsAgentException {
    if (!doesIncomingMessageNeedProcessing(incomingMessage)) {
      return incomingMessage;
    }
    final String messageText = renderMessage(incomingMessage);
    final String processed = getConnection().processIncoming(messageText);
    return parseMessage(processed, incomingMessage.getSession());
  }

  @Override
  public NymsKeyInfo getKeyInfo(String emailAddress) throws NymsAgentException {
    if(addressesWithoutKeys.contains(emailAddress)) {
      return null;
    } else if(cachedKeyInfo.containsKey(emailAddress)) {
      return cachedKeyInfo.get(emailAddress);
    }
    final NymsKeyInfo info = getConnection().getKeyInfo(emailAddress);
    if(info == null) {
      addressesWithoutKeys.add(emailAddress);
    } else {
      cachedKeyInfo.put(emailAddress, info);
    }
    return info;
  }

  @Override
  public MimeMessage processOutgoingMessage(MimeMessage outgoingMessage) throws NymsAgentException {
    if (!doesOutgoingMessageNeedProcessing(outgoingMessage)) {
      return outgoingMessage;
    }
    final String messageText = renderMessage(outgoingMessage);
    final String processed = getConnection().processOutgoing(messageText);
    return parseMessage(processed, outgoingMessage.getSession());
  }

  private synchronized NymsAgentConnection getConnection() throws NymsAgentException {
    if (connection == null || !connection.isConnected()) {
      connection = new NymsAgentConnection();
      try {
        connection.start();
      } catch (final IOException e) {
        connection = null;
        throw new NymsAgentException("Error launching nyms process: "+ e.getMessage(), e);
      }
    }
    return connection;
  }

  private String renderMessage(MimeMessage message) throws NymsAgentException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      message.writeTo(out);
      return new String(out.toByteArray(), Charsets.ISO_8859_1);
    } catch (IOException | MessagingException e) {
      throw new NymsAgentException("Error converting message to string: "+ e.getMessage(), e);
    }
  }

  private MimeMessage parseMessage(String messageText, Session session) throws NymsAgentException {
    final ByteArrayInputStream in = new ByteArrayInputStream(
        messageText.getBytes(Charsets.ISO_8859_1));
    try {
      return new MimeMessage(session, in);
    } catch (final MessagingException e) {
      throw new NymsAgentException("Error parsing message received from nyms agent: "+ e.getMessage(), e);
    }
  }
  
  private boolean doesIncomingMessageNeedProcessing(MimeMessage message) {
    try {
      final String ct = message.getContentType();

    } catch (MessagingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return true;
  }
  
  private boolean doesOutgoingMessageNeedProcessing(MimeMessage message) {
    return true;
  }

  @Override
  public boolean hasSigningKey(String emailAddress) throws NymsAgentException {
    final NymsKeyInfo info = getKeyInfo(emailAddress);
    if(info == null) {
      return false;
    }
    return info.hasSecretKey();
  }

  @Override
  public byte[] getAvatarImage(String emailAddress) throws NymsAgentException {
    final NymsKeyInfo info = getKeyInfo(emailAddress);
    if(info == null) {
      return null;
    }
    return info.getUserImageData();
  }

  @Override
  public NymsKeyGenerationParameters createKeyGenerationParameters(
      String emailAddress) {
    return new NymsKeyGenerationParametersImpl(emailAddress);
  }

  @Override
  public NymsKeyInfo generateKeys(NymsKeyGenerationParameters parameters) throws NymsAgentException {
    return getConnection().generateKeys(
        parameters.getEmailAddress(), 
        parameters.getRealName(), 
        parameters.getComment());
  }

  @Override
  public boolean unlockPrivateKey(NymsKeyInfo key, String passphrase) throws NymsAgentException {
    return getConnection().unlockPrivateKey(key.getKeyId(), passphrase);
  }
}
