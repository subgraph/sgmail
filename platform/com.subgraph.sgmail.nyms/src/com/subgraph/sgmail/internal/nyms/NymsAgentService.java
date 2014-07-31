package com.subgraph.sgmail.internal.nyms;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;

import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsAgentStatus;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult.DecryptionResult;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult.SignatureVerificationResult;
import com.subgraph.sgmail.nyms.NymsKeyGenerationParameters;
import com.subgraph.sgmail.nyms.NymsKeyInfo;
import com.subgraph.sgmail.nyms.NymsOutgoingProcessingResult;

public class NymsAgentService implements NymsAgent {
  private final static String BEGIN_PGP = "-----BEGIN";
  private final static String BEGIN_PGP_ENCRYPTED = "-----BEGIN PGP MESSAGE-----";
  private final static String BEGIN_PGP_SIGNED = "-----BEGIN PGP SIGNED MESSAGE-----";

  private JavamailUtils javamailUtils;
  private NymsAgentStatus status;
  private NymsAgentConnection connection;
  private final Set<String> addressesWithoutKeys = new HashSet<>();
  private final Set<String> keyIdsWithoutKeys = new HashSet<>();
  private final Map<String,NymsKeyInfo> cachedKeyInfo = new HashMap<>();
  private final Map<String, NymsKeyInfo> cachedKeyInfoByKeyId = new HashMap<>();

  
  public void setJavamailUtils(JavamailUtils javamailUtils) {
    this.javamailUtils = javamailUtils;
  }
  
  @Override
  public synchronized NymsAgentStatus getStatus() {
    if(status == null) {
      status = createStatus();
    }
    return status;
  }
  
  private NymsAgentStatus createStatus() {
    try {
      final int version = getConnection().version();
      return new NymsAgentStatusImpl(version);
    } catch (NymsAgentException e) {
      return new NymsAgentStatusImpl(e.getMessage());
    }
  }

  @Override
  public boolean hasKeyForAddress(String emailAddress) throws NymsAgentException {
    return getKeyInfo(emailAddress) != null;
  }

  @Override
  public NymsIncomingProcessingResult processIncomingMessage(MimeMessage incomingMessage) throws NymsAgentException {
    return processIncomingMessage(incomingMessage, null);
  }

  @Override
  public NymsIncomingProcessingResult processIncomingMessage(MimeMessage incomingMessage, String passphrase)
      throws NymsAgentException {
    if(getMessageStatus(incomingMessage) == MessageStatus.CLEAR) {
      return new NymsIncomingProcessingResultImpl(SignatureVerificationResult.NOT_SIGNED, DecryptionResult.NOT_ENCRYPTED);
    }
    return getConnection().processIncoming(incomingMessage, passphrase);
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
  public NymsKeyInfo getKeyInfoByKeyId(String keyId) throws NymsAgentException {
    if(keyIdsWithoutKeys.contains(keyId)) {
      return null;
    } else if(cachedKeyInfoByKeyId.containsKey(keyId)) {
      return cachedKeyInfoByKeyId.get(keyId);
    }
    final NymsKeyInfo info = getConnection().getKeyInfoByKeyId(keyId);
    if(info == null) {
      keyIdsWithoutKeys.add(keyId);
    } else {
      cachedKeyInfoByKeyId.put(keyId, info);
    }
    return info;
  }

  @Override
  public NymsOutgoingProcessingResult processOutgoingMessage(MimeMessage outgoingMessage, boolean requestSigning, boolean requestEncryption, String passphrase) throws NymsAgentException {
    if (!doesOutgoingMessageNeedProcessing(outgoingMessage)) {
      return NymsOutgoingProcessingResultImpl.create(outgoingMessage, false, false);
    }
    return getConnection().processOutgoing(outgoingMessage, requestSigning, requestEncryption, passphrase);
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

  @Override
  public MessageStatus getMessageStatus(MimeMessage message) {
    final String mpType = getMultipartType(message);
    if(mpType.equalsIgnoreCase("encrypted")) {
      return MessageStatus.ENCRYPTED_MIME;
    } else if(mpType.equalsIgnoreCase("signed")) {
      return MessageStatus.SIGNED_MIME;
    }
    final String body = javamailUtils.getTextBody(message);
    if(body.contains(BEGIN_PGP)) {
      if(body.contains(BEGIN_PGP_ENCRYPTED)) {
        return MessageStatus.ENCRYPTED_INLINE;
      } else if(body.contains(BEGIN_PGP_SIGNED)) {
        return MessageStatus.SIGNED_INLINE;
      }
    }
    return MessageStatus.CLEAR;
  }

  private String getMultipartType(MimeMessage message) {
    try {
      final String ct = message.getContentType();
      final ContentType contentType = new ContentType(ct);
      if(contentType.getPrimaryType().equalsIgnoreCase("multipart")) {
        return contentType.getSubType();
      }
    } catch (MessagingException e) {
      // ignore, fall through
    }
    return "";
  }
}
