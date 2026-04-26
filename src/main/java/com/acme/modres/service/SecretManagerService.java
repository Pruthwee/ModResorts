package com.acme.modres.service;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Service for accessing secrets from Google Secret Manager
 */
@Service
public class SecretManagerService {

    @Value("${gcs.project.id:#{null}}")
    private String projectId;

    private SecretManagerServiceClient client;

    @PostConstruct
    public void init() {
        try {
            client = SecretManagerServiceClient.create();
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize Secret Manager client. Using fallback mode.");
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Access a secret from Google Secret Manager
     * @param secretId The secret ID
     * @param version The version (default: "latest")
     * @return The secret value as a string
     */
    public String accessSecret(String secretId, String version) {
        if (client == null || projectId == null || projectId.isEmpty()) {
            System.err.println("Secret Manager not available, returning null for secret: " + secretId);
            return null;
        }

        try {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, version);
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            System.err.println("Error accessing secret " + secretId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Access the latest version of a secret
     */
    public String accessSecret(String secretId) {
        return accessSecret(secretId, "latest");
    }
}
