package com.subgraph.sgmail.internal.nyms;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import com.subgraph.sgmail.nyms.NymsAgentException;

public class NymsAgentConnection {
  private final static String NYMS_AGENT_PATH = "/usr/local/bin/nyms";

  private Process process;
  private int currentId;

  void start() throws IOException {
    final ProcessBuilder pb = new ProcessBuilder(NYMS_AGENT_PATH, "-pipe");
    process = pb.start();
  }

  private int getMessageId() {
    currentId += 1;
    return currentId;
  }

  boolean isConnected() {
    return process != null && process.isAlive();
  }

  int version() throws NymsAgentException {
    final NymsRequest request = new NymsRequest("Protocol.Version", getMessageId());
    final JsonObject response = call(request);
    testErrorResponse("Protocol.Version", response);
    return response.getInt("result");
  }

  boolean hasKeyForAddress(String address) {
    final NymsRequest request = new NymsRequest("Protocol.HasKeyForAddress", getMessageId());
    request.addArgument("Address", address);
    request.addArgument("Lookup", false);

    System.out.println("res: " + call(request));
    return false;
  }

  String processIncoming(String message) throws NymsAgentException {
    final NymsRequest request = new NymsRequest("Protocol.ProcessIncoming", getMessageId());
    request.addArgument("EmailBody", message);
    final JsonObject response = call(request);
    testErrorResponse("Protocol.ProcessIncoming", response);
    return getResultString(response, "EmailBody");
  }

  String processOutgoing(String message) throws NymsAgentException {
    final NymsRequest request = new NymsRequest("Protocol.ProcessOutgoing", getMessageId());
    request.addArgument("EmailBody", message);
    final JsonObject response = call(request);
    testErrorResponse("Protocol.ProcessOutgoing", response);
    return getResultString(response, "EmailBody");
  }
  
  private JsonObject call(NymsRequest req) {
    req.writeTo(process.getOutputStream());
    return readResponse();
  }

  private JsonObject readResponse() {
    final JsonReader reader = Json.createReader(process.getInputStream());
    return reader.readObject();
  }

  private void testErrorResponse(String methodName, JsonObject response) throws NymsAgentException {
    if (isErrorResponse(response)) {
      String message = getErrorString(response);
      throw new NymsAgentException("Error calling " + methodName + ": " + message);
    }
  }

  private boolean isErrorResponse(JsonObject response) {
    return isErrorValue(response.get("error"));
  }

  private String getErrorString(JsonObject response) {
    final JsonValue error = response.get("error");
    if (!isErrorValue(error)) {
      return "No error";
    }
    if (error.getValueType() == ValueType.STRING) {
      return response.getString("error");
    }
    return "Error is not a string value.";
  }

  private boolean isErrorValue(JsonValue errValue) {
    return errValue != null && errValue.getValueType() != ValueType.NULL;
  }

  private String getResultString(JsonObject response, String name) throws NymsAgentException {
    JsonValue resultValue = response.get("result");
    if (resultValue == null) {
      throw new NymsAgentException("No result parameter in response");
    }
    if (resultValue.getValueType() != ValueType.OBJECT) {
      throw new NymsAgentException("result parameter is not an object as expected: type = "+ resultValue.getValueType());
    }
    JsonObject result = (JsonObject) resultValue;
    JsonValue stringValue = result.get(name);
    if(stringValue == null || stringValue.getValueType() != ValueType.STRING) {
      return null;
    }
    return ((JsonString) stringValue).getString();
  }
}
