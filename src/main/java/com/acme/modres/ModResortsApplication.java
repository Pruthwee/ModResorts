package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Boot Application for ModResorts
 * Migrated from WAR deployment to executable JAR with embedded Tomcat
 * Supports cloud-native deployment on GKE, Cloud Run, or App Engine
 */
@SpringBootApplication
@ServletComponentScan
@EnableRedisHttpSession
public class ModResortsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModResortsApplication.class, args);
    }
}
