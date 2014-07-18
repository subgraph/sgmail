package com.subgraph.sgmail.internal.nyms;

import javax.mail.internet.MimeMessage;

import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult;

public class NymsIncomingProcessingResultImpl implements NymsIncomingProcessingResult {
  
  private final static int VERIFY_NOT_SIGNED = 0;
  private final static int VERIFY_SIGNATURE_VALID = 1;
  private final static int VERIFY_SIGNATURE_INVALID = 2;
  private final static int VERIFY_NO_PUBKEY = 3;

  private final static int DECRYPT_NOT_ENCRYPTED = 0;
  private final static int DECRYPT_SUCCESS = 1;
  private final static int DECRYPT_PASSPHRASE_NEEDED = 2;
  private final static int DECRYPT_FAILED = 3;
  
  static NymsIncomingProcessingResult create(int verifyCode, int decryptCode, MimeMessage message) throws NymsAgentException {
    return new NymsIncomingProcessingResultImpl(
        getSignatureVerificationResult(verifyCode), 
        getDecryptionResult(decryptCode),
        message);
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
    case DECRYPT_FAILED:
      return DecryptionResult.DECRYPTION_FAILED;
    default:
      throw new NymsAgentException("Unexpected DecryptResult value "+ code);
    }
  }

  private final SignatureVerificationResult verificationResult;
  private final DecryptionResult decryptionResult;
  private final MimeMessage decryptedMessage;

  public NymsIncomingProcessingResultImpl(SignatureVerificationResult verificationResult, DecryptionResult decryptionResult) {
    this(verificationResult, decryptionResult, null);
  }

  public NymsIncomingProcessingResultImpl(SignatureVerificationResult verificationResult, DecryptionResult decryptionResult, MimeMessage decryptedMessage) {
    this.verificationResult = verificationResult;
    this.decryptionResult = decryptionResult;
    this.decryptedMessage = decryptedMessage;
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
  public MimeMessage getDecryptedMessage() {
    return decryptedMessage;
  }
}
