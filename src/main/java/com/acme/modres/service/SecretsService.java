package com.acme.modres.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.logging.Logger;

/**
 * Service for retrieving secrets from AWS Secrets Manager.
 * Replaces hardcoded credentials and API keys with secure cloud-native secret management.
 */
@Service
public class SecretsService {
    
    private static final Logger logger = Logger.getLogger(SecretsService.class.getName());
    
    @Autowired
    private SecretsManagerClient secretsManagerClient;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Retrieve a secret value from AWS Secrets Manager
     * @param secretName The name of the secret
     * @return The secret value as a string
     */
    public String getSecret(String secretName) {
        try {
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            
            GetSecretValueResponse getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);
            String secret = getSecretValueResponse.secretString();
            
            logger.info("Successfully retrieved secret: " + secretName);
            return secret;
        } catch (Exception e) {
            logger.warning("Failed to retrieve secret from AWS Secrets Manager: " + secretName + ". Falling back to environment variable.");
            // Fallback to environment variable if Secrets Manager is not available
            return System.getenv(secretName);
        }
    }
    
    /**
     * Retrieve a specific key from a JSON secret
     * @param secretName The name of the secret
     * @param key The key within the JSON secret
     * @return The value associated with the key
     */
    public String getSecretKey(String secretName, String key) {
        try {
            String secretJson = getSecret(secretName);
            if (secretJson == null) {
                return null;
            }
            
            JsonNode jsonNode = objectMapper.readTree(secretJson);
            JsonNode valueNode = jsonNode.get(key);
            
            return valueNode != null ? valueNode.asText() : null;
        } catch (Exception e) {
            logger.severe("Failed to parse secret JSON for key: " + key);
            e.printStackTrace();
            return null;
        }
    }
}
