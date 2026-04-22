package com.acme.modres.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GCP Cloud Services Configuration
 * Provides beans for Google Cloud Storage and other GCP services
 */
@Configuration
public class GcpConfiguration {

    @Value("${gcp.project.id:default-project}")
    private String projectId;

    @Bean
    public Storage googleCloudStorage() {
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();
    }
}
