package com.acme.modres.cloud;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cloud-native storage facade for ModResorts application data.
 *
 * Persistent and temporary file operations are routed to Azure Blob Storage.
 * For local developer compatibility only, reads can fall back to classpath resources
 * when AZURE_STORAGE_CONNECTION_STRING is not configured. Writes never use local disk.
 */
public final class AzureBlobStorageService {
  private static final Logger LOGGER = Logger.getLogger(AzureBlobStorageService.class.getName());

  private static final String CONNECTION_STRING_ENV = "AZURE_STORAGE_CONNECTION_STRING";
  private static final String CONTAINER_ENV = "AZURE_STORAGE_CONTAINER";
  private static final String DEFAULT_CONTAINER = "modresorts";

  private AzureBlobStorageService() {
  }

  public static String readText(String blobName) throws IOException {
    byte[] bytes = readBytes(blobName);
    return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
  }

  public static byte[] readBytes(String blobName) throws IOException {
    if (isBlobConfigured()) {
      BlobClient blobClient = getContainerClient().getBlobClient(blobName);
      return blobClient.downloadContent().toBytes();
    }

    LOGGER.warning("Azure Blob Storage is not configured; falling back to classpath resource for read: " + blobName);
    try (InputStream inputStream = AzureBlobStorageService.class.getClassLoader().getResourceAsStream(blobName)) {
      if (inputStream == null) {
        return null;
      }
      return readAllBytes(inputStream);
    }
  }

  public static void uploadBytes(String blobName, byte[] content, String contentType) throws IOException {
    if (!isBlobConfigured()) {
      throw new IOException("Azure Blob Storage is required for write operations. Configure " + CONNECTION_STRING_ENV);
    }

    BlobClient blobClient = getContainerClient().getBlobClient(blobName);
    BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
      blockBlobClient.upload(inputStream, content.length, true);
      if (contentType != null && !contentType.trim().isEmpty()) {
        blockBlobClient.setHttpHeaders(new com.azure.storage.blob.models.BlobHttpHeaders().setContentType(contentType));
      }
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Unable to upload blob " + blobName, e);
      throw e;
    }
  }

  private static BlobContainerClient getContainerClient() {
    BlobContainerClient containerClient = new BlobContainerClientBuilder()
        .connectionString(System.getenv(CONNECTION_STRING_ENV))
        .containerName(getContainerName())
        .buildClient();
    containerClient.createIfNotExists();
    return containerClient;
  }

  private static String getContainerName() {
    String configuredContainer = System.getenv(CONTAINER_ENV);
    return configuredContainer == null || configuredContainer.trim().isEmpty() ? DEFAULT_CONTAINER : configuredContainer;
  }

  private static boolean isBlobConfigured() {
    String connectionString = System.getenv(CONNECTION_STRING_ENV);
    return connectionString != null && !connectionString.trim().isEmpty();
  }

  private static byte[] readAllBytes(InputStream inputStream) throws IOException {
    byte[] buffer = new byte[8192];
    int bytesRead;
    try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      return outputStream.toByteArray();
    }
  }
}
