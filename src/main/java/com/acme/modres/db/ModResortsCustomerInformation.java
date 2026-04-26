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
 * Migrated from EJB Singleton to Spring Service
 * Uses HikariCP connection pooling (configured via Spring Boot)
 */
@Service
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  @Autowired(required = false)
  private DataSource dataSource;

  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();
    
    if (dataSource == null) {
      System.err.println("DataSource not configured");
      return customerInfo;
    }

    // Use try-with-resources for automatic resource management
    // Connection is returned to HikariCP pool automatically
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      // Process the results
      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      System.err.println("Database error: " + e.getMessage());
      e.printStackTrace();
    }
    
    return customerInfo;
  }
}
