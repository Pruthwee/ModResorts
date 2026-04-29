package com.acme.modres.db;

import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Customer information service.
 * Migrated from EJB 2.x (@Singleton/@Startup) to a Spring Boot @Service component,
 * enabling cloud-native deployment on AWS without a heavyweight EJB container.
 * Uses Spring-managed DataSource (backed by HikariCP connection pool) for AWS RDS connectivity.
 */
@Service
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  // Spring-injected DataSource (configured via application.properties / environment variables)
  // Supports AWS RDS via HikariCP connection pooling
  private final DataSource dataSource;

  public ModResortsCustomerInformation(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();

    // Use try-with-resources to ensure Connection, PreparedStatement, and ResultSet
    // are automatically closed, preventing resource leaks in containerized AWS environments
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return customerInfo;
  }
}
