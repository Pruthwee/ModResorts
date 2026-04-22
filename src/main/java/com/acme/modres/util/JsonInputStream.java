package com.acme.modres.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

/**
 * JSON input stream utility
 * Updated to work with InputStream for cloud-native resource loading
 */
public class JsonInputStream {

  private InputStream inputStream;

  public JsonInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public Object parseJsonAs(Class<?> cls) {
    Object jsonObject = null;
    try {
      Gson gson = new Gson();
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      jsonObject = gson.fromJson(reader, cls);
    } catch (Exception e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
    }
  }
}
