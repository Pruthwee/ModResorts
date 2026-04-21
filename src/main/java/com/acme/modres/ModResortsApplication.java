package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Boot application entry point for ModResorts.
 * This enables the application to run as an executable JAR with embedded Tomcat,
 * making it cloud-ready for deployment on AWS ECS, EKS, or Fargate.
 * 
 * Features:
 * - Embedded Tomcat servlet container
 * - Redis-backed session management via Amazon ElastiCache
 * - Servlet component scanning for @WebServlet annotations
 * - Spring Data JPA with HikariCP connection pooling
 */
@SpringBootApplication
@ServletComponentScan
@EnableRedisHttpSession
public class ModResortsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModResortsApplication.class, args);
    }
}
