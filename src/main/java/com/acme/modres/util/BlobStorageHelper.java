package com.acme.modres.util;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.core.exception.AzureException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlobStorageHelper {

  private static BlobContainerClient containerClient;

  private BlobStorageHelper() {
  }

  public static BlobContainerClient getContainerClient(String endpoint, String containerName) {
    if (containerClient == null) {
      BlobServiceClient serviceClient = new BlobServiceClientBuilder()
          .endpoint(endpoint)
          .credential(new DefaultAzureCredentialBuilder().build())
          .buildClient();
      containerClient = serviceClient.getBlobContainerClient(containerName);
      if (!containerClient.exists()) {
        containerClient.create();
      }
    }
    return containerClient;
  }

  public static byte[] downloadBlob(String endpoint, String containerName, String blobName) throws IOException {
    try {
      BlobContainerClient client = getContainerClient(endpoint, containerName);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      client.getBlobClient(blobName).downloadStream(outputStream);
      return outputStream.toByteArray();
    } catch (AzureException e) {
      throw new IOException("Failed to download blob: " + blobName, e);
    }
  }

  public static void uploadBlob(String endpoint, String containerName, String blobName, byte[] data) throws IOException {
    try {
      BlobContainerClient client = getContainerClient(endpoint, containerName);
      client.getBlobClient(blobName).upload(new ByteArrayInputStream(data), data.length, true);
    } catch (AzureException e) {
      throw new IOException("Failed to upload blob: " + blobName, e);
    }
  }

  public static InputStream downloadBlobAsStream(String endpoint, String containerName, String blobName) throws IOException {
    try {
      BlobContainerClient client = getContainerClient(endpoint, containerName);
      return client.getBlobClient(blobName).openInputStream();
    } catch (AzureException e) {
      throw new IOException("Failed to download blob as stream: " + blobName, e);
    }
  }
}
