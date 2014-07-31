package com.subgraph.sgmail.internal.nyms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import com.google.common.base.Charsets;
import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult;
import com.subgraph.sgmail.nyms.NymsKeyInfo;
import com.subgraph.sgmail.nyms.NymsOutgoingProcessingResult;

public class NymsAgentConnection {

  private final static boolean debugLogging = true;
  
  private Process process;
  private int currentId;

  void start() throws IOException, NymsAgentException {
    final String agentPath = findNymsAgentPath();
    final ProcessBuilder pb = new ProcessBuilder(agentPath, "-pipe", "-debug");
    process = pb.start();
  }

  private static String findNymsAgentPath() throws NymsAgentException {
    final String syspath = System.getProperty("osgi.syspath");
    File path = new File(syspath);
    while(path != null) {
      File nymsDir = new File(path, "nyms");
      if(nymsDir.exists() && nymsDir.isDirectory()) {
        File nymsBin = new File(nymsDir, "nymsd");
        if(!nymsBin.exists()) {
          throw new NymsAgentException("Nyms directory found at "+ nymsDir.getPath() + ", but it does not contain nymsd binary");
        } else if (!nymsBin.canExecute()) {
          throw new NymsAgentException("Nyms binary found at "+ nymsBin.getPath() +" but it is not executable");
        } else {
          return nymsBin.getPath();
        }
      }
      path = path.getParentFile();
    }
    throw new NymsAgentException("No /nyms directory found in any parent directory of "+ syspath);

  }
  private int getMessageId() {
    currentId += 1;
    return currentId;
  }
  
  private NymsRequest newRequest(String methodName) {
    return new NymsRequest(process, methodName, getMessageId(), debugLogging);
  }

  boolean isConnected() {
    return process != null && process.isAlive();
  }

  int version() throws NymsAgentException {
    return newRequest("Protocol.Version")
        .send()
        .getIntResult();
  }

  NymsKeyInfo getKeyInfo(String address) throws NymsAgentException {
    return getKeyInfoFromResponse(newRequest("Protocol.GetKeyInfo")
        .addArgument("Address", address)
        .addArgument("Lookup", false)
        .send());
  }
  
  NymsKeyInfo getKeyInfoByKeyId(String keyId) throws NymsAgentException {
    return getKeyInfoFromResponse(newRequest("Protocol.GetKeyInfo")
        .addArgument("KeyId", keyId)
        .addArgument("Lookup", false)
        .send());
  }
  
  private NymsKeyInfo getKeyInfoFromResponse(NymsResponse r) throws NymsAgentException {
    if(!r.getBoolean("HasKey")) {
      return null;
    }
    return new NymsKeyInfoImpl.Builder()
    .fingerprint(r.getString("Fingerprint"))
    .keyId(r.getString("KeyId"))
    .summary(r.getString("Summary"))
    .uids(r.getStringArray("UserIDs"))
    .hasSecretKey(r.getBoolean("HasSecretKey"))
    .isEncrypted(r.getBoolean("IsEncrypted"))
    .imageData(decodeImageData(r.getString("UserImage")))
    .rawPublicKey(r.getString("KeyData"))
    .rawSecretKey(r.getString("SecretKeyData"))
    .build();
  }
  
  private byte[] decodeImageData(String b64ImageData) {
    return Base64.getDecoder().decode(b64ImageData);
  }
  
  NymsIncomingProcessingResult processIncoming(MimeMessage message, String passphrase) throws NymsAgentException {
    final String messageText = renderMessage(message);
    NymsRequest request = newRequest("Protocol.ProcessIncoming");
    request.addArgument("EmailBody", messageText);
    if(passphrase != null) {
      request.addArgument("Passphrase", passphrase);
    }
    return extractIncomingProcessingResult(request.send(), message.getSession());
  }
  
  private NymsIncomingProcessingResult extractIncomingProcessingResult(NymsResponse r, Session session) throws NymsAgentException {
    final int verifyCode = r.getInt("VerifyResult");
    final int decryptCode = r.getInt("DecryptResult");
    final String emailBody = r.getString("EmailBody");
    if(decryptCode == NymsIncomingProcessingResultImpl.DECRYPT_PASSPHRASE_NEEDED) {
      final List<String> encryptedKeyIds = r.getStringArray("EncryptedKeyIds");
      return NymsIncomingProcessingResultImpl.createPassphraseNeeded(encryptedKeyIds);
    }
    if(decryptCode == NymsIncomingProcessingResultImpl.DECRYPT_FAILED || verifyCode == NymsIncomingProcessingResultImpl.VERIFY_FAILED) {
      final String failureMessage = r.getString("FailureMessage");
      return NymsIncomingProcessingResultImpl.createFailed(verifyCode, decryptCode, failureMessage);
    }

    if(emailBody != null && !emailBody.isEmpty()) {
      final byte[] rawBody = emailBody.getBytes(Charsets.ISO_8859_1);
      return NymsIncomingProcessingResultImpl.create(verifyCode, decryptCode, rawBody, parseMessage(emailBody, session));
    } else {
      return NymsIncomingProcessingResultImpl.create(verifyCode, decryptCode, null, null);
    }
  }
  
  
  NymsOutgoingProcessingResult processOutgoing(MimeMessage message, boolean sign, boolean encrypt, String passphrase) throws NymsAgentException {
    final String messageText = renderMessage(message);
    return extractOutgoingProcessingResult(newRequest("Protocol.ProcessOutgoing")
        .addArgument("EmailBody", messageText)
        .addArgument("Sign", sign)
        .addArgument("Encrypt", encrypt)
        .addArgument("Passphrase", passphrase)
        .send(), message);
  }
  
  private NymsOutgoingProcessingResult extractOutgoingProcessingResult(NymsResponse r, MimeMessage originalMessage) throws NymsAgentException {
    final int resultCode = r.getInt("ResultCode");
    final MimeMessage msg = extractOutgoingMessage(r, originalMessage.getSession());
    switch(resultCode) {
    case NymsOutgoingProcessingResultImpl.NOT_SIGNED_OR_ENCRYPTED:
      return NymsOutgoingProcessingResultImpl.create(originalMessage, false, false);
    case NymsOutgoingProcessingResultImpl.ENCRYPTED_ONLY:
      return NymsOutgoingProcessingResultImpl.create(msg, false, true);
    case NymsOutgoingProcessingResultImpl.SIGNED_ONLY:
      return NymsOutgoingProcessingResultImpl.create(msg, true, false);
    case NymsOutgoingProcessingResultImpl.SIGNED_AND_ENCRYPTED:
      return NymsOutgoingProcessingResultImpl.create(msg, true, true);
    case NymsOutgoingProcessingResultImpl.SIGN_FAILED_NO_KEY:
      return NymsOutgoingProcessingResultImpl.createNoSigningKey();
    case NymsOutgoingProcessingResultImpl.SIGN_FAILED_NEED_PASSPHRASE:
      return NymsOutgoingProcessingResultImpl.createNeedPassphrase();
    case NymsOutgoingProcessingResultImpl.ENCRYPT_FAILED_MISSING_KEYS:
      return NymsOutgoingProcessingResultImpl.createMissingPublicKeys(extractMissingKeys(r));
    case NymsOutgoingProcessingResultImpl.OTHER_FAILURE:
      return NymsOutgoingProcessingResultImpl.createFailure(r.getString("FailureMessage"));
    default:
      return NymsOutgoingProcessingResultImpl.createFailure("Unknown ResultCode value: "+ resultCode);
    }
  }
  
  private MimeMessage extractOutgoingMessage(NymsResponse r, Session session) throws NymsAgentException {
    final String emailBody = r.getString("EmailBody");
    if(emailBody == null || emailBody.isEmpty()) {
      return null;
    }
    return parseMessage(emailBody, session);
  }
  
  private List<String> extractMissingKeys(NymsResponse r) {
    // XXX implement me
    return Collections.emptyList();
  }
  
  NymsKeyInfo generateKeys(String emailAddress, String realName, String comment) throws NymsAgentException {
    return getKeyInfoFromResponse(newRequest("Protocol.GenerateKeys")
        .addArgument("Email", emailAddress)
        .addArgument("RealName", realName)
        .addArgument("Comment", comment)
        .send());
  }

  boolean unlockPrivateKey(String keyId, String passphrase) throws NymsAgentException {
    return newRequest("Protocol.UnlockPrivateKey")
        .addArgument("KeyId", keyId)
        .addArgument("Passphrase", passphrase)
        .send()
        .getBooleanResult();
  }

  private static String renderMessage(MimeMessage message) throws NymsAgentException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      message.writeTo(out);
      return new String(out.toByteArray(), Charsets.ISO_8859_1);
    } catch (IOException | MessagingException e) {
      throw new NymsAgentException("Error converting message to string: "+ e.getMessage(), e);
    }
  }

  private static MimeMessage parseMessage(String messageText, Session session) throws NymsAgentException {
    final ByteArrayInputStream in = new ByteArrayInputStream(
        messageText.getBytes(Charsets.ISO_8859_1));
    try {
      return new MimeMessage(session, in);
    } catch (final MessagingException e) {
      throw new NymsAgentException("Error parsing message received from nyms agent: "+ e.getMessage(), e);
    }
  }
}
