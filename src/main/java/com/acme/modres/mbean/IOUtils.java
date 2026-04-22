package com.acme.modres.mbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

  /**
   * Get resource from classpath or Google Cloud Storage
   * This eliminates the need for temporary file creation
   */
  public static InputStream getResourceStream(String path) {
    InputStream stream = null;
    
    try {
      // First try to load from classpath (for local development and packaged resources)
      stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      
      // If not found in classpath, try GCS (for cloud-native storage)
      if (stream == null) {
        try {
          Storage storage = StorageOptions.newBuilder()
              .setProjectId(GCS_PROJECT_ID)
              .build()
              .getService();
          
          BlobId blobId = BlobId.of(GCS_BUCKET_NAME, path);
          Blob blob = storage.get(blobId);
          
          if (blob != null && blob.exists()) {
            byte[] content = blob.getContent();
            stream = new ByteArrayInputStream(content);
          }
        } catch (Exception e) {
          System.err.println("Failed to load from GCS: " + e.getMessage());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return stream;
  }

  /**
   * Write data to Google Cloud Storage instead of local file system
   */
  public static boolean writeToGCS(String path, byte[] data) {
    try {
      Storage storage = StorageOptions.newBuilder()
          .setProjectId(GCS_PROJECT_ID)
          .build()
          .getService();
      
      BlobId blobId = BlobId.of(GCS_BUCKET_NAME, path);
      com.google.cloud.storage.BlobInfo blobInfo = com.google.cloud.storage.BlobInfo.newBuilder(blobId)
          .setContentType("application/json")
          .build();
      
      storage.create(blobInfo, data);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream stream = getResourceStream("ops.json")) {
      if (stream == null) {
        System.err.println("ops.json not found");
        return new OpMetadataList(); // empty default
      }
      
      // Read stream into byte array for JsonInputStream
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      int nRead;
      while ((nRead = stream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      
      // Create a new stream from the buffered data
      try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.toByteArray());
           JsonInputStream is = new JsonInputStream(bais)) {
        OpMetadataList opList = (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
        return opList != null ? opList : new OpMetadataList();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList();
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream stream = getResourceStream("reservations.json")) {
      if (stream == null) {
        System.err.println("reservations.json not found");
        return new ReservationList(); // empty default
      }
      
      // Read stream into byte array for JsonInputStream
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      int nRead;
      while ((nRead = stream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      
      // Create a new stream from the buffered data
      try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.toByteArray());
           JsonInputStream is = new JsonInputStream(bais)) {
        ReservationList reservationList = (ReservationList) is.parseJsonAs(ReservationList.class);
        return reservationList != null ? reservationList : new ReservationList();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList();
    }
  }

}
