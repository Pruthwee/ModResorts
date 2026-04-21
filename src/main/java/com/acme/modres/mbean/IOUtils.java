package com.acme.modres.mbean;

import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

/**
 * Utility class for I/O operations.
 * Updated to use classpath resources instead of temporary file system writes.
 * For cloud deployments, files should be stored in S3 or other cloud storage.
 */
public final class IOUtils {

  /**
   * Get input stream from classpath resource.
   * This avoids writing to ephemeral local file system in cloud environments.
   * 
   * @param path The resource path
   * @return InputStream from the resource
   */
  public static InputStream getResourceAsStream(String path) {
    try {
      InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      if (stream == null) {
        throw new RuntimeException("Resource not found: " + path);
      }
      return stream;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to load resource: " + path, e);
    }
  }

  /**
   * Load operation metadata list from configuration.
   * Uses classpath resources to avoid local file system dependencies.
   * 
   * @return OpMetadataList loaded from configuration
   */
  public static OpMetadataList getOpListFromConfig() {
    try (InputStream stream = getResourceAsStream("ops.json");
         JsonInputStream is = new JsonInputStream(stream)) {
      OpMetadataList opList = new OpMetadataList();
      opList = (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
      return opList;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Load reservation list from configuration.
   * Uses classpath resources to avoid local file system dependencies.
   * 
   * @return ReservationList loaded from configuration
   */
  public static ReservationList getReservationListFromConfig() {
    try (InputStream stream = getResourceAsStream("reservations.json");
         JsonInputStream is = new JsonInputStream(stream)) {
      ReservationList reservationList = new ReservationList();
      reservationList = (ReservationList) is.parseJsonAs(ReservationList.class);
      return reservationList;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
