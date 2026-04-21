package com.acme.modres.mbean;

import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

/**
 * Cloud-ready IOUtils that reads from classpath resources instead of creating temporary files.
 * This eliminates ephemeral storage dependencies and makes the application stateless.
 */
public final class IOUtils {

  /**
   * Reads a resource from classpath directly without creating temporary files.
   * This is cloud-compatible as it doesn't rely on local file system.
   * 
   * @param path the classpath resource path
   * @return InputStream for the resource
   */
  public static InputStream getResourceAsStream(String path) {
    return IOUtils.class.getClassLoader().getResourceAsStream(path);
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream is = getResourceAsStream("ops.json")) {
      if (is == null) {
        return new OpMetadataList(); // empty default
      }
      try (JsonInputStream jis = new JsonInputStream(is)) {
        OpMetadataList opList = (OpMetadataList) jis.parseJsonAs(OpMetadataList.class);
        return opList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList(); // empty default
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream is = getResourceAsStream("reservations.json")) {
      if (is == null) {
        return new ReservationList(); // empty default
      }
      try (JsonInputStream jis = new JsonInputStream(is)) {
        ReservationList reservationList = (ReservationList) jis.parseJsonAs(ReservationList.class);
        return reservationList;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList(); // empty default
    }
  }

}
