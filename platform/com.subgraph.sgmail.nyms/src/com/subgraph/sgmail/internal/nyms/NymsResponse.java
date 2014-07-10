package com.subgraph.sgmail.internal.nyms;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import com.subgraph.sgmail.nyms.NymsAgentException;

public class NymsResponse {
  private final String methodName;
  private final JsonObject responseObject;
  private final JsonObject resultObject;
  
  NymsResponse(String methodName, JsonObject responseObject) {
    this.methodName = methodName;
    this.responseObject = responseObject;
    this.resultObject = getResultObject(responseObject);
  }
  
  private static JsonObject getResultObject(JsonObject response) {
    final JsonValue resultValue = response.get("result");
    if(resultValue == null || resultValue.getValueType() != ValueType.OBJECT) {
      return null;
    }
    return (JsonObject) resultValue;
  }
  
  void testErrorResponse() throws NymsAgentException {
    if(isErrorResponse()) {
      throw new NymsAgentException("Error calling "+ methodName +": "+ getErrorMessage());
    }
  }

  boolean isErrorResponse() {
    final JsonValue value = responseObject.get("error");
    return value != null && value.getValueType() != ValueType.NULL;
  }
  
  String getErrorMessage() {
    final JsonValue value = responseObject.get("error");
    if(value == null || value.getValueType() != ValueType.STRING) {
      return "";
    }
    return responseObject.getString("error");
  }
  
  String getString(String name) throws NymsAgentException {
    final JsonValue val = getResultValue(name);
    if(val == null || val.getValueType() != ValueType.STRING) {
      throw new NymsAgentException("No string result field '"+ name +"' in response to "+ methodName);
    }
    return ((JsonString)val).getString();
  }
  
  boolean getBoolean(String name) throws NymsAgentException {
    final JsonValue val = getResultValue(name);
    if(val == null) {
      throw new NymsAgentException("No boolean result field '"+ name +"' in response to "+ methodName);
    }
    return val.getValueType() == ValueType.TRUE;
  }
  
  boolean getBooleanResult() throws NymsAgentException {
    final JsonValue val = responseObject.get("result");
    if(val == null || (val.getValueType() != ValueType.TRUE && val.getValueType() != ValueType.FALSE)) {
      throw new NymsAgentException("Response does not contain boolean result type as expected in response to "+ methodName);
    }
    return val.getValueType() == ValueType.TRUE;
  }
  
  int getInt(String name) throws NymsAgentException {
    final JsonValue val = getResultValue(name);
    if(val == null || val.getValueType() != ValueType.NUMBER) {
      throw new NymsAgentException("Result field '"+ name +"' is not an integer as expected in response to "+ methodName);
    }
    return ((JsonNumber)val).intValue();
  }
  
  int getIntResult() throws NymsAgentException {
    final JsonValue val = responseObject.get("result");
    if(val == null || val.getValueType() != ValueType.NUMBER) {
      throw new NymsAgentException("Response does not contain integer result type as expected in response to "+ methodName);
    }
    return ((JsonNumber)val).intValue();
  }
  
  List<String> getStringArray(String name) throws NymsAgentException {
    final JsonValue val = getResultValue(name);
    if(val.getValueType() != ValueType.ARRAY) {
      throw new NymsAgentException("Result field '"+ name +"' is not an array as expected in response to "+ methodName);
    }
    final List<String> result = new ArrayList<>();
    for(JsonValue arrayVal: ((JsonArray)val)) {
      if(arrayVal.getValueType() != ValueType.STRING) {
        throw new NymsAgentException("Result field '"+ name +"' contained an array element which was not a string as expected in response to "+ methodName);
      }
      result.add(((JsonString)arrayVal).getString());
    }
    return result;
  }
  
  private JsonValue getResultValue(String name) throws NymsAgentException {
    if(resultObject == null) {
      throw new NymsAgentException("No result object in response to "+ methodName);
    }
    return resultObject.get(name);
  }
}
