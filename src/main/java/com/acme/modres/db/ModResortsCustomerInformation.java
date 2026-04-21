package com.acme.modres.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Service for managing customer information.
 * Migrated from EJB 2.x to Spring Service with HikariCP connection pooling.
 * Uses Spring Boot's auto-configured DataSource with HikariCP for cloud-native database access.
 */
@Service
public class ModResortsCustomerInformation {
  
  private static final Logger logger = Logger.getLogger(ModResortsCustomerInformation.class.getName());
  
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  @Autowired(required = false)
  private DataSource dataSource;

  public ArrayList<String> getCustomerInformation() {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    ArrayList<String> customerInfo = new ArrayList<>();

    if (dataSource == null) {
      logger.warning("DataSource not configured. Skipping database query.");
      return customerInfo;
    }

    try {
      // Get a connection from the HikariCP connection pool
      conn = dataSource.getConnection();
      // Create a prepared statement
      stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
      // Execute the query
      rs = stmt.executeQuery();

      // Process the results
      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      logger.severe("Database error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      // Close the result set, statement, and connection
      try {
        if (rs != null)
          rs.close();
        if (stmt != null)
          stmt.close();
        if (conn != null)
          conn.close();
      } catch (SQLException e) {
        logger.severe("Error closing database resources: " + e.getMessage());
        e.printStackTrace();
      }
    }
    return customerInfo;
  }
}
