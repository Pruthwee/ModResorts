package com.acme.modres.service;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service for interacting with Google Cloud Storage
 */
@Service
public class GcsStorageService {

    @Value("${gcs.bucket.name:modresorts-bucket}")
    private String bucketName;

    @Value("${gcs.project.id:#{null}}")
    private String projectId;

    private Storage storage;

    @PostConstruct
    public void init() {
        try {
            if (projectId != null && !projectId.isEmpty()) {
                storage = StorageOptions.newBuilder()
                        .setProjectId(projectId)
                        .build()
                        .getService();
            } else {
                storage = StorageOptions.getDefaultInstance().getService();
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize GCS client. Using fallback mode.");
            e.printStackTrace();
        }
    }

    /**
     * Upload content to GCS
     */
    public void uploadFile(String objectName, byte[] content) throws IOException {
        if (storage == null) {
            throw new IOException("GCS Storage not initialized");
        }
        
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, content);
    }

    /**
     * Download content from GCS
     */
    public byte[] downloadFile(String objectName) throws IOException {
        if (storage == null) {
            throw new IOException("GCS Storage not initialized");
        }
        
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob == null) {
            throw new IOException("File not found in GCS: " + objectName);
        }
        return blob.getContent();
    }

    /**
     * Get InputStream from GCS object
     */
    public InputStream getInputStream(String objectName) throws IOException {
        byte[] content = downloadFile(objectName);
        return new ByteArrayInputStream(content);
    }

    /**
     * Check if object exists in GCS
     */
    public boolean exists(String objectName) {
        if (storage == null) {
            return false;
        }
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        return blob != null && blob.exists();
    }

    /**
     * Delete object from GCS
     */
    public boolean deleteFile(String objectName) {
        if (storage == null) {
            return false;
        }
        return storage.delete(BlobId.of(bucketName, objectName));
    }
}
