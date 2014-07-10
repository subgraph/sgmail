package com.subgraph.sgmail.nyms;

public interface NymsKeyGenerationParameters {
  static enum PublicKeyAlgorithm { KEY_RSA };

  static class PublicKeyParameters {
    private final PublicKeyAlgorithm algorithm;
    private final int keySize;

    public PublicKeyParameters(PublicKeyAlgorithm algorithm, int keySize) {
      this.algorithm = algorithm;
      this.keySize = keySize;
    }

    PublicKeyAlgorithm getAlgorithm() {
      return algorithm;
    }

    int getKeySize() {
      return keySize;
    }
  }
  
  final static PublicKeyParameters DEFAULT_SIGNING_TYPE = new PublicKeyParameters(PublicKeyAlgorithm.KEY_RSA, 2048);
  final static PublicKeyParameters DEFAULT_ENCRYPTION_TYPE = new PublicKeyParameters(PublicKeyAlgorithm.KEY_RSA, 2048);


  void setRealName(String realName);

  void setComment(String comment);

  void setSigningKeyType(PublicKeyParameters type);

  void setEncryptionKeyType(PublicKeyParameters type);

  String getEmailAddress();

  String getRealName();

  String getComment();

  PublicKeyParameters getSigningKeyType();

  PublicKeyParameters getEncryptionKeyType();
}
