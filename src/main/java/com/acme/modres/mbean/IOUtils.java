package com.acme.modres.mbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public final class IOUtils {

  // GCS configuration - should be externalized to environment variables
  private static final String GCS_BUCKET_NAME = System.getenv().getOrDefault("GCS_BUCKET_NAME", "modresorts-data");
  private static final String GCS_PROJECT_ID = System.getenv().getOrDefault("GCP_PROJECT_ID", "default-project");
  private static final boolean USE_GCS = Boolean.parseBoolean(System.getenv().getOrDefault("USE_GCS", "false"));

  /**
   * Get input stream from classpath resource or Google Cloud Storage
   * This eliminates local file system dependencies
   */
  public static InputStream getResourceAsStream(String path) {
    if (USE_GCS) {
      // Load from Google Cloud Storage
      try {
        Storage storage = StorageOptions.newBuilder()
            .setProjectId(GCS_PROJECT_ID)
            .build()
            .getService();
        
        BlobId blobId = BlobId.of(GCS_BUCKET_NAME, path);
        Blob blob = storage.get(blobId);
        
        if (blob != null && blob.exists()) {
          byte[] content = blob.getContent();
          return new ByteArrayInputStream(content);
        }
      } catch (Exception e) {
        System.err.println("Error loading from GCS, falling back to classpath: " + e.getMessage());
      }
    }
    
    // Fallback to classpath resource (for local development or when GCS is not configured)
    return IOUtils.class.getClassLoader().getResourceAsStream(path);
  }

  public static OpMetadataList getOpListFromConfig() {
    // Use try-with-resources to ensure proper resource cleanup
    try (InputStream inputStream = getResourceAsStream("ops.json")) {
      if (inputStream == null) {
        System.err.println("ops.json not found");
        return new OpMetadataList(); // return empty default
      }
      
      try (JsonInputStream is = new JsonInputStream(inputStream)) {
        OpMetadataList opList = (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
        return opList != null ? opList : new OpMetadataList();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList(); // return empty default on error
    }
  }

  public static ReservationList getReservationListFromConfig() {
    // Use try-with-resources to ensure proper resource cleanup
    try (InputStream inputStream = getResourceAsStream("reservations.json")) {
      if (inputStream == null) {
        System.err.println("reservations.json not found");
        return new ReservationList(); // return empty default
      }
      
      try (JsonInputStream is = new JsonInputStream(inputStream)) {
        ReservationList reservationList = (ReservationList) is.parseJsonAs(ReservationList.class);
        return reservationList != null ? reservationList : new ReservationList();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList(); // return empty default on error
    }
  }

}
