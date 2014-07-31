package com.subgraph.sgmail.internal.nyms;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import com.google.common.base.Charsets;
import com.subgraph.sgmail.nyms.NymsAgentException;

public class NymsRequest {
  private final Process process;
  private final String method;
  private final int id;
  private final boolean debugLogging;
  private final JsonObjectBuilder argsBuilder;

  NymsRequest(Process process, String method, int id, boolean debugLogging) {
    this.process = process;
    this.method = method;
    this.id = id;
    this.debugLogging = debugLogging;
    this.argsBuilder = Json.createObjectBuilder();
  }

  NymsRequest addArgument(String name, String value) {
    argsBuilder.add(name, value);
    return this;
  }

  NymsRequest addArgument(String name, boolean value) {
    argsBuilder.add(name, value);
    return this;
  }

  JsonObject getRequestObject() {
    final JsonArray params = Json.createArrayBuilder().add(argsBuilder.build())
        .build();
    return Json.createObjectBuilder().add("jsonrpc", "2.0")
        .add("method", method).add("params", params).add("id", id).build();
  }

  void writeTo(OutputStream out) {
    final JsonWriter writer = Json.createWriter(out);
    final JsonObject msg = getRequestObject();
    if(debugLogging) {
      System.out.println("Sending: " + msg);
    }
    writer.writeObject(msg);
    try {
      out.flush();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  NymsResponse send() throws NymsAgentException {
    synchronized(process) {
    writeTo(process.getOutputStream());
    final Reader r = new InputStreamReader(process.getInputStream(), Charsets.UTF_8);
    final JsonReader reader = Json.createReader(r);
    final JsonObject response = reader.readObject();
    if(debugLogging) {
      System.out.println("response: "+ response);
    }
    final NymsResponse nr = new NymsResponse(method, response);
    nr.testErrorResponse();
    return nr;
    }
  }
}
