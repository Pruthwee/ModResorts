package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Boot Application for ModResorts
 * Migrated from traditional WAR deployment to executable JAR with embedded Tomcat
 * 
 * Cloud-native features:
 * - Embedded servlet container (Tomcat)
 * - Externalized configuration via environment variables
 * - Redis-based distributed session management
 * - Google Cloud Storage integration
 * - Google Secret Manager integration
 * - HikariCP connection pooling
 * - UTC timezone standardization
 */
@SpringBootApplication
@ServletComponentScan // Enable scanning for @WebServlet annotations
@EnableRedisHttpSession // Enable Redis-based session management for cloud scalability
public class ModResortsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModResortsApplication.class, args);
    }
}
