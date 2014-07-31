package com.subgraph.sgmail.nyms;

import java.util.List;

import javax.mail.internet.MimeMessage;

public interface NymsOutgoingProcessingResult {
  enum ProcessingResult { 
    NotSignedOrEncrypted, SignedOnly, EncryptedOnly, SignedAndEncrypted, 
    SignFailedNoPrivateKey, SignFailedPassphraseNeeded,
    EncryptFailedMissingPubkeys, OtherFailure }
  ProcessingResult getProcessingResult();
  MimeMessage getProcessedMessage();
  String getFailureMessage();
  List<String> getMissingEncryptKeyIds();
}
