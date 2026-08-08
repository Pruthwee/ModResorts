package com.acme.modres.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AzureBlobStorageService {
  private static final String CONNECTION_STRING_ENV = "AZURE_STORAGE_CONNECTION_STRING";
  private static final String CONTAINER_ENV = "AZURE_STORAGE_CONTAINER";
  private static final String DEFAULT_CONTAINER = "modresorts-data";

  private BlobContainerClient getContainerClient() {
    String connectionString = System.getenv(CONNECTION_STRING_ENV);
    if (connectionString == null || connectionString.trim().isEmpty()) {
      throw new IllegalStateException("Azure Storage connection string is not configured in environment variable " + CONNECTION_STRING_ENV);
    }

    String containerName = System.getenv(CONTAINER_ENV);
    if (containerName == null || containerName.trim().isEmpty()) {
      containerName = DEFAULT_CONTAINER;
    }

    BlobContainerClient client = new BlobContainerClientBuilder()
        .connectionString(connectionString)
        .containerName(containerName)
        .buildClient();

    if (!client.exists()) {
      client.create();
    }
    return client;
  }

  public byte[] readBlob(String blobName) throws IOException {
    BlobClient blobClient = getContainerClient().getBlobClient(blobName);
    if (!blobClient.exists()) {
      throw new IOException("Blob not found: " + blobName);
    }

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      blobClient.download(outputStream);
      return outputStream.toByteArray();
    }
  }

  public String uploadBytes(String blobName, byte[] content) {
    BlobClient blobClient = getContainerClient().getBlobClient(blobName);
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
      blobClient.upload(inputStream, content.length, true);
      return blobClient.getBlobUrl();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to upload blob " + blobName, e);
    }
  }

  public String uploadText(String blobName, String content) {
    return uploadBytes(blobName, content.getBytes(StandardCharsets.UTF_8));
  }

  public String uploadWithGeneratedName(String prefix, byte[] content) {
    return uploadBytes(prefix + "-" + UUID.randomUUID() + ".zip", content);
  }

  public InputStream openBlobStream(String blobName) throws IOException {
    return new ByteArrayInputStream(readBlob(blobName));
  }
}
