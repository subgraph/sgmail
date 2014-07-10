package com.subgraph.sgmail.internal.nyms;

import java.util.ArrayList;
import java.util.List;

import com.subgraph.sgmail.nyms.NymsKeyInfo;

public class NymsKeyInfoImpl implements NymsKeyInfo {
  private final boolean hasSecretKey;
  private final boolean isEncrypted;
  private final String fingerprint;
  private final String keyId;
  private final String summary;
  private final List<String> uids;
  private final byte[] imageData;
  private final String b64PublicKey;
  private final String b64SecretKey;
  
  private NymsKeyInfoImpl(Builder builder) {
    this.hasSecretKey = builder.hasSecretKey;
    this.isEncrypted = builder.isEncrypted;
    this.fingerprint = builder.fingerprint;
    this.keyId = builder.keyId;
    this.summary = builder.summary;
    this.uids = builder.uidList;
    this.imageData = builder.imageData;
    this.b64PublicKey = builder.b64PublicKey;
    this.b64SecretKey = builder.b64SecretKey;
  }

  @Override
  public String getFingerprint() {
    return fingerprint;
  }

  @Override
  public String getSummary() {
    return summary;
  }

  @Override
  public List<String> getUIDs() {
    return uids;
  }

  @Override
  public String toString() {
    return "NymsKeyInfo [fingerprint=" + fingerprint + ", summary="
        + summary + ", uids=" + uids + "]";
  }

  @Override
  public byte[] getUserImageData() {
    return imageData;
  }

  @Override
  public boolean hasSecretKey() {
    return hasSecretKey;
  }

  @Override
  public boolean isSecretKeyEncrypted() {
    return isEncrypted;
  }

  @Override
  public String getRawPublicKey() {
    return b64PublicKey;
  }

  @Override
  public String getRawSecretKey() {
    return b64SecretKey;
  }


  @Override
  public String getKeyId() {
    return keyId;
  }


  static class Builder {
    private boolean hasSecretKey;
    private boolean isEncrypted;
    private List<String> uidList = new ArrayList<>();
    private String fingerprint;
    private String keyId;
    private String summary;
    private byte[] imageData;
    private String b64PublicKey;
    private String b64SecretKey;
    Builder hasSecretKey(boolean val) { this.hasSecretKey = val; return this; }
    Builder isEncrypted(boolean val) { this.isEncrypted = val; return this; }
    Builder uids(List<String> val) { this.uidList = val; return this; }
    Builder fingerprint(String val) { this.fingerprint = val; return this; }
    Builder keyId(String val) { this.keyId = val; return this; }
    Builder summary(String val) { this.summary = val; return this; }
    Builder imageData(byte[] val) { this.imageData = val; return this; }
    Builder rawPublicKey(String val) { this.b64PublicKey = val; return this; }
    Builder rawSecretKey(String val) { this.b64SecretKey = val; return this; }
    NymsKeyInfo build() {
      return new NymsKeyInfoImpl(this);
    }
  }
}
