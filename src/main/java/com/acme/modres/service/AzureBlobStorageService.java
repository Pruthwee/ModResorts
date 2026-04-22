package com.acme.modres.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Azure Blob Storage service for cloud-native file operations
 * Replaces local file system dependencies
 */
@Service
public class AzureBlobStorageService {

    private final BlobServiceClient blobServiceClient;
    private static final String CONTAINER_NAME = "modresorts-data";

    @Autowired
    public AzureBlobStorageService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
        ensureContainerExists();
    }

    private void ensureContainerExists() {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            if (!containerClient.exists()) {
                containerClient.create();
            }
        } catch (Exception e) {
            // Container may already exist or we may not have permissions to create
            // Log and continue
            System.err.println("Warning: Could not ensure container exists: " + e.getMessage());
        }
    }

    /**
     * Upload data to Azure Blob Storage
     */
    public void uploadBlob(String blobName, byte[] data) throws IOException {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
                blobClient.upload(inputStream, data.length, true);
            }
        } catch (Exception e) {
            throw new IOException("Failed to upload blob: " + blobName, e);
        }
    }

    /**
     * Download data from Azure Blob Storage
     */
    public byte[] downloadBlob(String blobName) throws IOException {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                throw new IOException("Blob does not exist: " + blobName);
            }
            
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                blobClient.download(outputStream);
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            throw new IOException("Failed to download blob: " + blobName, e);
        }
    }

    /**
     * Check if blob exists
     */
    public boolean blobExists(String blobName) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Delete blob from Azure Blob Storage
     */
    public void deleteBlob(String blobName) throws IOException {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.delete();
        } catch (Exception e) {
            throw new IOException("Failed to delete blob: " + blobName, e);
        }
    }

    /**
     * Get input stream for reading blob data
     */
    public InputStream getBlobInputStream(String blobName) throws IOException {
        byte[] data = downloadBlob(blobName);
        return new ByteArrayInputStream(data);
    }
}
