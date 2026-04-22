package com.acme.modres.mbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.service.AzureBlobStorageService;
import com.acme.modres.util.JsonInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cloud-native IO utilities using Azure Blob Storage
 * Replaces local file system operations with Azure Blob Storage
 */
@Component
public final class IOUtils {

  private static AzureBlobStorageService blobStorageService;

  @Autowired
  public void setBlobStorageService(AzureBlobStorageService service) {
    IOUtils.blobStorageService = service;
  }

  /**
   * Get input stream from Azure Blob Storage or classpath resource
   * @param path Resource path
   * @return InputStream for the resource
   */
  public static InputStream getResourceInputStream(String path) throws IOException {
    // First try to get from Azure Blob Storage
    if (blobStorageService != null && blobStorageService.blobExists(path)) {
      try {
        return blobStorageService.getBlobInputStream(path);
      } catch (IOException e) {
        System.err.println("Failed to load from Azure Blob Storage, falling back to classpath: " + e.getMessage());
      }
    }
    
    // Fallback to classpath resource
    InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
    if (stream == null) {
      throw new IOException("Resource not found: " + path);
    }
    return stream;
  }

  /**
   * Upload data to Azure Blob Storage
   * @param blobName Name of the blob
   * @param data Data to upload
   */
  public static void uploadToBlob(String blobName, byte[] data) throws IOException {
    if (blobStorageService != null) {
      blobStorageService.uploadBlob(blobName, data);
    } else {
      throw new IOException("Azure Blob Storage service not available");
    }
  }

  /**
   * Download data from Azure Blob Storage
   * @param blobName Name of the blob
   * @return Downloaded data
   */
  public static byte[] downloadFromBlob(String blobName) throws IOException {
    if (blobStorageService != null) {
      return blobStorageService.downloadBlob(blobName);
    } else {
      throw new IOException("Azure Blob Storage service not available");
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream is = getResourceInputStream("ops.json")) {
      JsonInputStream jsonStream = new JsonInputStream(is);
      OpMetadataList opList = (OpMetadataList) jsonStream.parseJsonAs(OpMetadataList.class);
      jsonStream.close();
      return opList;
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList(); // Return empty list as fallback
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream is = getResourceInputStream("reservations.json")) {
      JsonInputStream jsonStream = new JsonInputStream(is);
      ReservationList reservationList = (ReservationList) jsonStream.parseJsonAs(ReservationList.class);
      jsonStream.close();
      return reservationList;
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList(); // Return empty list as fallback
    }
  }
}
