package com.subgraph.sgmail.nyms;

import java.util.List;

public interface NymsKeyInfo {
  boolean hasSecretKey();
  boolean isSecretKeyEncrypted();
  String getFingerprint();
  String getKeyId();
  String getSummary();
  List<String> getUIDs();
  byte[] getUserImageData();
  String getRawPublicKey();
  String getRawSecretKey();
}
