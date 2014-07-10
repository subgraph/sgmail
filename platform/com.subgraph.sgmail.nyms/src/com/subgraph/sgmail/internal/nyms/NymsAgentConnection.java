package com.subgraph.sgmail.internal.nyms;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsKeyInfo;

public class NymsAgentConnection {
  private final static boolean debugLogging = true;

  private Process process;
  private int currentId;

  void start() throws IOException, NymsAgentException {
    final String agentPath = findNymsAgentPath();
    final ProcessBuilder pb = new ProcessBuilder(agentPath, "-pipe");
    process = pb.start();
  }

  private static String findNymsAgentPath() throws NymsAgentException {
    final String syspath = System.getProperty("osgi.syspath");
    File path = new File(syspath);
    while(path != null) {
      File nymsDir = new File(path, "nyms");
      if(nymsDir.exists() && nymsDir.isDirectory()) {
        File nymsBin = new File(nymsDir, "nymsd");
        if(!nymsBin.exists()) {
          throw new NymsAgentException("Nyms directory found at "+ nymsDir.getPath() + ", but it does not contain nymsd binary");
        } else if (!nymsBin.canExecute()) {
          throw new NymsAgentException("Nyms binary found at "+ nymsBin.getPath() +" but it is not executable");
        } else {
          return nymsBin.getPath();
        }
      }
      path = path.getParentFile();
    }
    throw new NymsAgentException("No /nyms directory found in any parent directory of "+ syspath);

  }
  private int getMessageId() {
    currentId += 1;
    return currentId;
  }
  
  private NymsRequest newRequest(String methodName) {
    return new NymsRequest(process, methodName, getMessageId(), debugLogging);
  }

  boolean isConnected() {
    return process != null && process.isAlive();
  }

  int version() throws NymsAgentException {
    return newRequest("Protocol.Version")
        .send()
        .getIntResult();
  }

  NymsKeyInfo getKeyInfo(String address) throws NymsAgentException {
    return getKeyInfoFromResponse(newRequest("Protocol.GetKeyInfo")
        .addArgument("Address", address)
        .addArgument("Lookup", false)
        .send());
  }
  
  private NymsKeyInfo getKeyInfoFromResponse(NymsResponse r) throws NymsAgentException {
    if(!r.getBoolean("HasKey")) {
      return null;
    }
    return new NymsKeyInfoImpl.Builder()
    .fingerprint(r.getString("Fingerprint"))
    .summary(r.getString("Summary"))
    .uids(r.getStringArray("UserIDs"))
    .hasSecretKey(r.getBoolean("HasSecretKey"))
    .isEncrypted(r.getBoolean("IsEncrypted"))
    .imageData(decodeImageData(r.getString("UserImage")))
    .rawPublicKey(r.getString("KeyData"))
    .rawSecretKey(r.getString("SecretKeyData"))
    .build();
  }
  
  private byte[] decodeImageData(String b64ImageData) {
    return Base64.getDecoder().decode(b64ImageData);
  }
  
  String processIncoming(String message) throws NymsAgentException {
    return newRequest("Protocol.ProcessIncoming")
        .addArgument("EmailBody", message)
        .send()
        .getString("EmailBody");
  }

  String processOutgoing(String message) throws NymsAgentException {
    return newRequest("Protocol.ProcessOutgoing")
        .addArgument("EmailBody", message)
        .send()
        .getString("EmailBody");
  }
  
  NymsKeyInfo generateKeys(String emailAddress, String realName, String comment) throws NymsAgentException {
    return getKeyInfoFromResponse(newRequest("Protocol.GenerateKeys")
        .addArgument("Email", emailAddress)
        .addArgument("RealName", realName)
        .addArgument("Comment", comment)
        .send());
  }

  boolean unlockPrivateKey(String keyId, String passphrase) throws NymsAgentException {
    return newRequest("Protocol.UnlockPrivateKey")
        .addArgument("KeyId", keyId)
        .addArgument("Passphrase", passphrase)
        .send()
        .getBooleanResult();
  }
}
