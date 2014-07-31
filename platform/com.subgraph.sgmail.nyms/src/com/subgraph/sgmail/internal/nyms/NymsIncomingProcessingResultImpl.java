package com.subgraph.sgmail.internal.nyms;

import java.util.Collections;
import java.util.List;

import javax.mail.internet.MimeMessage;

import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult;

public class NymsIncomingProcessingResultImpl implements NymsIncomingProcessingResult {
  
  final static int VERIFY_NOT_SIGNED = 0;
  final static int VERIFY_SIGNATURE_VALID = 1;
  final static int VERIFY_SIGNATURE_INVALID = 2;
  final static int VERIFY_NO_PUBKEY = 3;
  final static int VERIFY_FAILED = 4;

  final static int DECRYPT_NOT_ENCRYPTED = 0;
  final static int DECRYPT_SUCCESS = 1;
  final static int DECRYPT_PASSPHRASE_NEEDED = 2;
  final static int DECRYPT_FAILED_NO_PRIVATE = 3;
  final static int DECRYPT_FAILED = 4;
  
  static NymsIncomingProcessingResult create(int verifyCode, int decryptCode, byte[] rawMessage, MimeMessage message) throws NymsAgentException {
    return new NymsIncomingProcessingResultImpl(
        getSignatureVerificationResult(verifyCode), 
        getDecryptionResult(decryptCode),
        rawMessage,
        message, "", Collections.emptyList());
  }
  
  static NymsIncomingProcessingResult createFailed(int verifyCode, int decryptCode, String failureMessage) throws NymsAgentException {
    return new NymsIncomingProcessingResultImpl(
        getSignatureVerificationResult(verifyCode), 
        getDecryptionResult(decryptCode), 
        null, null, failureMessage, 
        Collections.emptyList());
  }
  
  static NymsIncomingProcessingResult createPassphraseNeeded(List<String> encryptedKeyIds) {
    return new NymsIncomingProcessingResultImpl(
        SignatureVerificationResult.NOT_SIGNED, 
        DecryptionResult.PASSPHRASE_NEEDED, 
        null, null, "", 
        encryptedKeyIds);
  }
  
  private static SignatureVerificationResult getSignatureVerificationResult(int code) throws NymsAgentException {
    switch(code) {
    case VERIFY_NOT_SIGNED:
      return SignatureVerificationResult.NOT_SIGNED;
    case VERIFY_SIGNATURE_VALID:
      return SignatureVerificationResult.SIGNATURE_VALID;
    case VERIFY_SIGNATURE_INVALID:
      return SignatureVerificationResult.SIGNATURE_INVALID;
    case VERIFY_NO_PUBKEY:
      return SignatureVerificationResult.NO_VERIFY_PUBKEY;
    case VERIFY_FAILED:
      return SignatureVerificationResult.VERIFY_FAILED;
    default:
      throw new NymsAgentException("Unexpected VerifyResult value "+ code);
    }
  }
  
  private static DecryptionResult getDecryptionResult(int code) throws NymsAgentException {
    switch(code) {
    case DECRYPT_NOT_ENCRYPTED:
      return DecryptionResult.NOT_ENCRYPTED;
    case DECRYPT_SUCCESS:
      return DecryptionResult.DECRYPTION_SUCCESS;
    case DECRYPT_PASSPHRASE_NEEDED:
      return DecryptionResult.PASSPHRASE_NEEDED;
    case DECRYPT_FAILED_NO_PRIVATE:
      return DecryptionResult.DECRYPTION_FAILED_NO_PRIVATEKEY;
    case DECRYPT_FAILED:
      return DecryptionResult.DECRYPTION_FAILED;
    default:
      throw new NymsAgentException("Unexpected DecryptResult value "+ code);
    }
  }

  private final SignatureVerificationResult verificationResult;
  private final DecryptionResult decryptionResult;
  private final byte[] rawDecryptedMessage;
  private final String failureMessage;
  private final List<String> encryptedKeyIds;
  private final MimeMessage decryptedMessage;

  public NymsIncomingProcessingResultImpl(SignatureVerificationResult verificationResult, DecryptionResult decryptionResult) {
    this(verificationResult, decryptionResult, null, null, "", Collections.emptyList());
  }

  public NymsIncomingProcessingResultImpl(SignatureVerificationResult verificationResult, DecryptionResult decryptionResult, byte[] rawMessage, MimeMessage decryptedMessage, String failureMessage, List<String> encryptedKeyIds) {
    this.verificationResult = verificationResult;
    this.decryptionResult = decryptionResult;
    this.rawDecryptedMessage = rawMessage;
    this.decryptedMessage = decryptedMessage;
    this.failureMessage = failureMessage;
    this.encryptedKeyIds = encryptedKeyIds;
  }

  @Override
  public SignatureVerificationResult getSignatureVerificationResult() {
    return verificationResult;
  }

  @Override
  public DecryptionResult getDecryptionResult() {
    return decryptionResult;
  }
  
  @Override
  public byte[] getRawDecryptedMessage() {
    return rawDecryptedMessage;
  }

  @Override
  public MimeMessage getDecryptedMessage() {
    return decryptedMessage;
  }

  @Override
  public String getFailureMessage() {
    return failureMessage;
  }

  @Override
  public List<String> getEncryptedKeyIds() {
    return encryptedKeyIds;
  }
}
