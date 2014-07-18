package com.subgraph.sgmail.nyms;

import javax.mail.internet.MimeMessage;

public interface NymsIncomingProcessingResult {
  enum SignatureVerificationResult { NOT_SIGNED, SIGNATURE_VALID, SIGNATURE_INVALID, NO_VERIFY_PUBKEY };
  enum DecryptionResult { NOT_ENCRYPTED, DECRYPTION_SUCCESS, PASSPHRASE_NEEDED, DECRYPTION_FAILED };
  SignatureVerificationResult getSignatureVerificationResult();
  DecryptionResult getDecryptionResult();
  MimeMessage getDecryptedMessage();
}
