package com.acme.modres.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

import com.acme.modres.mbean.reservation.ReservationList;
import com.google.gson.Gson;

public final class IOUtils {

  /**
   * Returns the classpath resource as a file, without copying it to a temporary
   * file: the resource ships with the application and is only read.
   * Returns null when the resource cannot be found as a file.
   */
  public static File getFileFromRelativePath(String path) {
    try {
      URL url = IOUtils.class.getClassLoader().getResource(path);
      if (url != null && "file".equals(url.getProtocol())) {
        return new File(url.toURI());
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns the content of a classpath resource, or null when it is not found.
   */
  public static byte[] getResourceBytes(String path) throws IOException {
    try (InputStream is = IOUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (is == null) {
        return null;
      }
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) >= 0) {
        bytes.write(buffer, 0, length);
      }
      return bytes.toByteArray();
    }
  }

  private static Object parseResourceAs(String path, Class<?> cls) {
    try (InputStream is = IOUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (is == null) {
        return null;
      }
      try (Reader reader = new InputStreamReader(is)) {
        return new Gson().fromJson(reader, cls);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    return (OpMetadataList) parseResourceAs("ops.json", OpMetadataList.class);
  }

  public static ReservationList getReservationListFromConfig() {
    return (ReservationList) parseResourceAs("reservations.json", ReservationList.class);
  }

}
