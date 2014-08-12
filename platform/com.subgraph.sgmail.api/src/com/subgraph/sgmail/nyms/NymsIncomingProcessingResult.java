package com.subgraph.sgmail.nyms;

import java.util.List;

import javax.mail.internet.MimeMessage;

public interface NymsIncomingProcessingResult {
  enum SignatureVerificationResult { NOT_SIGNED, SIGNATURE_VALID, SIGNATURE_INVALID, SIGNATURE_KEY_EXPIRED, NO_VERIFY_PUBKEY, VERIFY_FAILED };
  enum DecryptionResult { NOT_ENCRYPTED, DECRYPTION_SUCCESS, PASSPHRASE_NEEDED, DECRYPTION_FAILED_NO_PRIVATEKEY, DECRYPTION_FAILED };
  SignatureVerificationResult getSignatureVerificationResult();
  DecryptionResult getDecryptionResult();
  byte[] getRawDecryptedMessage();
  MimeMessage getDecryptedMessage();
  String getFailureMessage();
  List<String> getEncryptedKeyIds();
  String getSignatureKeyId();
}
