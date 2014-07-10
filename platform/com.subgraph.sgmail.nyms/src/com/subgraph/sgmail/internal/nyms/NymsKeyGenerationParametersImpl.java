package com.subgraph.sgmail.internal.nyms;

import com.subgraph.sgmail.nyms.NymsKeyGenerationParameters;

public class NymsKeyGenerationParametersImpl implements NymsKeyGenerationParameters {
  
  private final String emailAddress;
  private String realName;
  private String comment;
  private PublicKeyParameters signingKeyType = DEFAULT_SIGNING_TYPE;
  private PublicKeyParameters encryptionKeyType = DEFAULT_ENCRYPTION_TYPE;

  public NymsKeyGenerationParametersImpl(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  @Override
  public void setRealName(String realName) {
    this.realName = realName;
  }

  @Override
  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public void setSigningKeyType(PublicKeyParameters type) {
    this.signingKeyType = type;
  }

  @Override
  public void setEncryptionKeyType(PublicKeyParameters type) {
    this.encryptionKeyType = type;
  }

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }

  @Override
  public String getRealName() {
    return realName;
  }

  @Override
  public String getComment() {
    return comment;
  }

  @Override
  public PublicKeyParameters getSigningKeyType() {
    return signingKeyType;
  }

  @Override
  public PublicKeyParameters getEncryptionKeyType() {
    return encryptionKeyType;
  }
}
