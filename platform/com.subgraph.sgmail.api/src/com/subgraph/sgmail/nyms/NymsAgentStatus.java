package com.subgraph.sgmail.nyms;

public interface NymsAgentStatus {
  boolean isAvailable();
  int getVersion();
  String getErrorMessage();
}
