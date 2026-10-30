package com.acme.modres.mbean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.google.gson.Gson;

public final class IOUtils {

  public static byte[] getBytesFromRelativePath(String path) {
    InputStream initialStream = null;
    ByteArrayOutputStream baos = null;
    try {
      initialStream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      if (initialStream == null) {
        return null;
      }
      baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int length;
      while ((length = initialStream.read(buffer)) != -1) {
        baos.write(buffer, 0, length);
      }
      return baos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (initialStream != null) {
        try {
          initialStream.close();
        } catch (IOException e) {
        }
      }
      if (baos != null) {
        try {
          baos.close();
        } catch (IOException e) {
        }
      }
    }

    return null;
  }

  public static OpMetadataList getOpListFromConfig() {
    InputStream is = null;
    try {
      is = IOUtils.class.getClassLoader().getResourceAsStream("ops.json");
      if (is == null) {
        return null;
      }
      Gson gson = new Gson();
      return gson.fromJson(new java.io.InputStreamReader(is), OpMetadataList.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
        }
      }
    }
  }

  public static ReservationList getReservationListFromConfig() {
    InputStream is = null;
    try {
      is = IOUtils.class.getClassLoader().getResourceAsStream("reservations.json");
      if (is == null) {
        return null;
      }
      Gson gson = new Gson();
      return gson.fromJson(new java.io.InputStreamReader(is), ReservationList.class);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
        }
      }
    }
  }

}
