package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot main application class for ModResorts.
 *
 * Converts the application from WAR packaging (requiring an external application
 * server such as WebSphere or Tomcat) to an executable JAR with an embedded
 * Tomcat servlet container. This enables self-contained deployment on AWS ECS,
 * EKS, or Fargate without any external application server dependency.
 *
 * The @ServletComponentScan annotation ensures that existing @WebServlet,
 * @WebFilter, and @WebListener annotations (or web.xml-declared servlets/filters)
 * are discovered and registered automatically by the embedded Tomcat container.
 *
 * To run: java -jar modresorts-2.0.0.jar
 */
@SpringBootApplication
@ServletComponentScan(basePackages = "com.acme.modres")
public class ModResortsApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ModResortsApplication.class, args);
    }
}
