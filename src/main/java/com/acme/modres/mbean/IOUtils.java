package com.acme.modres.mbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.service.GcsStorageService;
import com.acme.modres.util.JsonInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class IOUtils {

  private static GcsStorageService gcsStorageService;

  @Autowired
  public void setGcsStorageService(GcsStorageService service) {
    IOUtils.gcsStorageService = service;
  }

  /**
   * Get InputStream from classpath resource or GCS
   */
  public static InputStream getInputStreamFromResource(String path) {
    InputStream stream = null;
    
    // First try classpath
    try {
      stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      if (stream != null) {
        return stream;
      }
    } catch (Exception e) {
      System.err.println("Could not load from classpath: " + path);
    }
    
    // Try GCS if available
    if (gcsStorageService != null) {
      try {
        byte[] content = gcsStorageService.downloadFile(path);
        return new ByteArrayInputStream(content);
      } catch (IOException e) {
        System.err.println("Could not load from GCS: " + path);
        e.printStackTrace();
      }
    }
    
    return null;
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream is = getInputStreamFromResource("ops.json")) {
      if (is == null) {
        System.err.println("Could not find ops.json");
        return new OpMetadataList();
      }
      
      JsonInputStream jsonStream = new JsonInputStream(is);
      OpMetadataList opList = (OpMetadataList) jsonStream.parseJsonAs(OpMetadataList.class);
      jsonStream.close();
      return opList;
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList();
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream is = getInputStreamFromResource("reservations.json")) {
      if (is == null) {
        System.err.println("Could not find reservations.json");
        return new ReservationList();
      }
      
      JsonInputStream jsonStream = new JsonInputStream(is);
      ReservationList reservationList = (ReservationList) jsonStream.parseJsonAs(ReservationList.class);
      jsonStream.close();
      return reservationList;
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList();
    }
  }

}
