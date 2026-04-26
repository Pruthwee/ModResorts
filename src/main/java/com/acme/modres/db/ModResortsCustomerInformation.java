package com.acme.modres.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Migrated from EJB 2.x to Spring Boot Service
 * Uses Spring's dependency injection and proper resource management
 */
@Service
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  @Autowired(required = false)
  private DataSource dataSource;

  /**
   * Get customer information with proper resource management using try-with-resources
   * This ensures all database resources are properly closed even in case of exceptions
   */
  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();
    
    if (dataSource == null) {
      // DataSource not configured, return empty list
      return customerInfo;
    }

    // Use try-with-resources to ensure proper resource cleanup
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      e.printStackTrace();
      // In production, use proper logging framework
      // logger.error("Error retrieving customer information", e);
    }
    
    return customerInfo;
  }
}
