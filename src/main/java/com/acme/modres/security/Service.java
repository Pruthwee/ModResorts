package com.acme.modres.security;

/**
 * Service class demonstrating operation execution.
 * Note: SecurityManager has been deprecated and removed in Java 21.
 * Security checks should be implemented using modern security frameworks.
 */
public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager has been deprecated for removal in Java 17 and removed in Java 21
    // Modern applications should use security frameworks like Spring Security
    // or implement custom authorization mechanisms instead
    
    // If security checks are needed, implement them using:
    // - Spring Security annotations (@PreAuthorize, @Secured, etc.)
    // - Custom authorization logic
    // - Role-based access control (RBAC)
    
    System.out.println("Operation is executed");
  }
}
