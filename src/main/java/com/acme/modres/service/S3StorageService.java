package com.acme.modres.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Service for managing file storage operations using Amazon S3.
 * Replaces local file system operations with cloud-native S3 storage.
 */
@Service
public class S3StorageService {
    
    private static final Logger logger = Logger.getLogger(S3StorageService.class.getName());
    
    @Autowired
    private S3Client s3Client;
    
    private String bucketName = System.getenv().getOrDefault("S3_BUCKET_NAME", "modresorts-storage");
    
    /**
     * Upload data to S3
     * @param key The S3 object key
     * @param data The data to upload
     */
    public void uploadToS3(String key, byte[] data) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
            logger.info("Successfully uploaded file to S3: " + key);
        } catch (Exception e) {
            logger.severe("Failed to upload file to S3: " + key);
            e.printStackTrace();
            throw new RuntimeException("Failed to upload to S3", e);
        }
    }
    
    /**
     * Download data from S3
     * @param key The S3 object key
     * @return InputStream of the object data
     */
    public InputStream downloadFromS3(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            byte[] data = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes()).asByteArray();
            logger.info("Successfully downloaded file from S3: " + key);
            return new ByteArrayInputStream(data);
        } catch (Exception e) {
            logger.severe("Failed to download file from S3: " + key);
            e.printStackTrace();
            throw new RuntimeException("Failed to download from S3", e);
        }
    }
    
    /**
     * Check if an object exists in S3
     * @param key The S3 object key
     * @return true if exists, false otherwise
     */
    public boolean existsInS3(String key) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
