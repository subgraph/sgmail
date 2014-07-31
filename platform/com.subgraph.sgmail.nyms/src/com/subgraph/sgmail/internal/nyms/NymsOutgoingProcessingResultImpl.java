package com.subgraph.sgmail.internal.nyms;

import java.util.Collections;
import java.util.List;

import javax.mail.internet.MimeMessage;

import com.subgraph.sgmail.nyms.NymsOutgoingProcessingResult;

public class NymsOutgoingProcessingResultImpl implements NymsOutgoingProcessingResult {
  final static int NOT_SIGNED_OR_ENCRYPTED = 0;
  final static int SIGNED_ONLY = 1;
  final static int ENCRYPTED_ONLY = 2;
  final static int SIGNED_AND_ENCRYPTED = 3;
  final static int SIGN_FAILED_NO_KEY = 4;
  final static int SIGN_FAILED_NEED_PASSPHRASE = 5;
  final static int ENCRYPT_FAILED_MISSING_KEYS = 6;
  final static int OTHER_FAILURE = 7;

  static NymsOutgoingProcessingResult create(MimeMessage message, boolean isSigned, boolean isEncrypted) {
    return new NymsOutgoingProcessingResultImpl(getSuccessResultType(isSigned, isEncrypted), message, "", Collections.emptyList());
  }
  
  static NymsOutgoingProcessingResult createFailure(String failureMessage) {
    return new NymsOutgoingProcessingResultImpl(ProcessingResult.OtherFailure, null, failureMessage, Collections.emptyList());
  }
  
  static NymsOutgoingProcessingResult createNeedPassphrase() {
    return new NymsOutgoingProcessingResultImpl(ProcessingResult.SignFailedPassphraseNeeded, null, "", Collections.emptyList());
  }
  
  static NymsOutgoingProcessingResult createNoSigningKey() {
    return new NymsOutgoingProcessingResultImpl(ProcessingResult.SignFailedNoPrivateKey, null, "", Collections.emptyList());
  }
  
  static NymsOutgoingProcessingResult createMissingPublicKeys(List<String> pubkeys) {
    return new NymsOutgoingProcessingResultImpl(ProcessingResult.EncryptFailedMissingPubkeys, null, "", pubkeys);
  }

  private static ProcessingResult getSuccessResultType(boolean isSigned, boolean isEncrypted) {
    if(isSigned) {
      if(isEncrypted) {
        return ProcessingResult.SignedAndEncrypted;
      } else {
        return ProcessingResult.SignedOnly;
      }
    }
    
    if(isEncrypted) {
      return ProcessingResult.EncryptedOnly;
    } else {
      return ProcessingResult.NotSignedOrEncrypted;
    }
    
  }
  private final ProcessingResult processingResult;
  private final MimeMessage processedMessage;
  private final String failureMessage;
  private final List<String> missingEncryptKeys;
  
  private NymsOutgoingProcessingResultImpl(ProcessingResult processingResult, MimeMessage processedMessage, String failureMessage, List<String> missingEncryptKeys) {
    this.processingResult = processingResult;
    this.processedMessage = processedMessage;
    this.failureMessage = failureMessage;
    this.missingEncryptKeys = missingEncryptKeys;
  }

  @Override
  public ProcessingResult getProcessingResult() {
    return processingResult;
  }

  @Override
  public MimeMessage getProcessedMessage() {
    return processedMessage;
  }

  @Override
  public String getFailureMessage() {
    return failureMessage;
  }

  @Override
  public List<String> getMissingEncryptKeyIds() {
    return missingEncryptKeys;
  }
}
