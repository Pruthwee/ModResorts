package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

/**
 * Spring Boot Application for ModResorts
 * Migrated from WAR packaging to executable JAR with embedded Tomcat
 */
@SpringBootApplication
@ServletComponentScan
@EnableRedisHttpSession
public class ModResortsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModResortsApplication.class, args);
    }

    /**
     * Azure Blob Storage client for file operations
     * Uses Managed Identity for authentication
     */
    @Bean
    public BlobServiceClient blobServiceClient() {
        String storageAccountUrl = System.getenv("AZURE_STORAGE_ACCOUNT_URL");
        if (storageAccountUrl == null || storageAccountUrl.isEmpty()) {
            storageAccountUrl = "https://" + System.getenv("AZURE_STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net";
        }
        
        return new BlobServiceClientBuilder()
                .endpoint(storageAccountUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    /**
     * Azure Key Vault client for secrets management
     * Uses Managed Identity for authentication
     */
    @Bean
    public SecretClient secretClient() {
        String keyVaultUrl = System.getenv("AZURE_KEYVAULT_URL");
        if (keyVaultUrl == null || keyVaultUrl.isEmpty()) {
            keyVaultUrl = "https://" + System.getenv("AZURE_KEYVAULT_NAME") + ".vault.azure.net";
        }
        
        return new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }
}
