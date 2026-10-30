package com.acme.modres.mbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.acme.modres.mbean.reservation.ReservationList;
import com.azure.core.exception.AzureException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public final class IOUtils {

  // Settings (environment) naming the Azure Blob Storage account endpoint and the container
  // that keeps the files the application produces.
  private static final String STORAGE_ENDPOINT_SETTING = "AZURE_STORAGE_ENDPOINT";
  private static final String STORAGE_CONTAINER_SETTING = "AZURE_STORAGE_CONTAINER";

  // Built once, on first use, inside saveToBlobStorage
  private static BlobContainerClient containerClient;

  /**
   * Reads a resource shipped on the classpath and returns its bytes.
   */
  public static byte[] getBytesFromRelativePath(String path) throws IOException {
    try (InputStream in = IOUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        throw new FileNotFoundException(path);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) >= 0) {
        out.write(buffer, 0, length);
      }
      return out.toByteArray();
    }
  }

  /**
   * Saves data that has to outlive this instance in Azure Blob Storage instead of the local disk.
   * Authenticates with the default Azure credential chain (managed identity on Azure).
   */
  public static synchronized void saveToBlobStorage(String blobName, byte[] data) throws IOException {
    try {
      if (containerClient == null) {
        String endpoint = System.getenv(STORAGE_ENDPOINT_SETTING);
        String container = System.getenv(STORAGE_CONTAINER_SETTING);
        if (endpoint == null || endpoint.trim().isEmpty()) {
          throw new IOException(STORAGE_ENDPOINT_SETTING + " is not set");
        }
        if (container == null || container.trim().isEmpty()) {
          throw new IOException(STORAGE_CONTAINER_SETTING + " is not set");
        }
        containerClient = new BlobServiceClientBuilder()
            .endpoint(endpoint.trim())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient()
            .getBlobContainerClient(container.trim());
      }
      containerClient.getBlobClient(blobName).upload(new ByteArrayInputStream(data), data.length, true);
    } catch (AzureException | IllegalArgumentException e) {
      throw new IOException("Could not save " + blobName + " to Blob Storage: " + e.getMessage(), e);
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    return parseJsonResource("ops.json", OpMetadataList.class);
  }

  public static ReservationList getReservationListFromConfig() {
    return parseJsonResource("reservations.json", ReservationList.class);
  }

  private static <T> T parseJsonResource(String path, Class<T> cls) {
    try (Reader reader = new InputStreamReader(new ByteArrayInputStream(getBytesFromRelativePath(path)))) {
      return new Gson().fromJson(reader, cls);
    } catch (IOException | JsonParseException e) {
      e.printStackTrace();
      return null;
    }
  }

}
