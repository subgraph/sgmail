package com.subgraph.sgmail.internal.nyms;

import java.io.IOException;
import java.io.OutputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

public class NymsRequest {
  private final String method;
  private final int id;
  private final JsonObjectBuilder argsBuilder;

  NymsRequest(String method, int id) {
    this.method = method;
    this.id = id;
    this.argsBuilder = Json.createObjectBuilder();
  }

  void addArgument(String name, String value) {
    argsBuilder.add(name, value);
  }

  void addArgument(String name, boolean value) {
    argsBuilder.add(name, value);
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
    System.out.println("Sending: " + msg);
    writer.writeObject(msg);
    try {
      out.flush();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
