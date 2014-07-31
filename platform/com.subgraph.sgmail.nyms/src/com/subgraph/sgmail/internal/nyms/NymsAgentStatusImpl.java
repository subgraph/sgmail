package com.subgraph.sgmail.internal.nyms;

import com.subgraph.sgmail.nyms.NymsAgentStatus;

public class NymsAgentStatusImpl implements NymsAgentStatus {
  private final boolean available;
  private final int version;
  private final String message;

  NymsAgentStatusImpl(String errorMessage) {
    this.available = false;
    this.version = 0;
    this.message = errorMessage;
  }
  
  NymsAgentStatusImpl(int version) {
    this.available = true;
    this.version = version;
    this.message = "";
  }

  @Override
  public boolean isAvailable() {
    return available;
  }

  @Override
  public int getVersion() {
    return version;
  }

  @Override
  public String getErrorMessage() {
    return message;
  }

}
