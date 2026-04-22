package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Spring Boot Application for ModResorts
 * Migrated from WAR packaging to executable JAR with embedded Tomcat
 * Enables cloud-native deployment on AWS ECS, EKS, or Fargate
 */
@SpringBootApplication
@ServletComponentScan
@EnableRedisHttpSession
public class ModResortsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ModResortsApplication.class, args);
  }

  /**
   * Configure HikariCP connection pool for cloud-native database connectivity
   * Configuration is externalized via environment variables
   */
  @Bean
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    
    // Read configuration from environment variables
    String jdbcUrl = System.getenv().getOrDefault("DB_JDBC_URL", 
        "jdbc:postgresql://localhost:5432/modresorts");
    String username = System.getenv().getOrDefault("DB_USERNAME", "modresorts");
    String password = System.getenv().getOrDefault("DB_PASSWORD", "password");
    
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(username);
    config.setPassword(password);
    
    // HikariCP optimal settings for cloud environments
    config.setMaximumPoolSize(Integer.parseInt(
        System.getenv().getOrDefault("DB_POOL_SIZE", "10")));
    config.setMinimumIdle(Integer.parseInt(
        System.getenv().getOrDefault("DB_MIN_IDLE", "2")));
    config.setConnectionTimeout(30000);
    config.setIdleTimeout(600000);
    config.setMaxLifetime(1800000);
    
    return new HikariDataSource(config);
  }
}
