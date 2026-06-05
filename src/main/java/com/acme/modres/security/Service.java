package com.acme.modres.security;

public class Service {
  public static final String OPERATION = "my-operation";

  public void operation() {
    // SecurityManager has been deprecated for removal in Java 17 and removed in Java 21
    // Removed SecurityManager usage as it's no longer available
    // Modern applications should use alternative security mechanisms like:
    // - Spring Security
    // - Java Security Manager alternatives
    // - Application-level permission checks
    
    System.out.println("Operation is executed");
  }
}
