package com.acme.modres.db;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  // Removing DB connection for ease of demo setup
  // DataSource should be configured via Spring Boot and connection pooling (e.g. HikariCP)
  @Autowired
  private DataSource dataSource;
    ArrayList<String> customerInfo = new ArrayList<>();
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
      rs = stmt.executeQuery();

      // Process the results
      while (rs.next()) {
        String info = rs.getString("INFO");
        customerInfo.add(info);
      }

    } catch (SQLException e) {
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
        e.printStackTrace();
      }
    }
    return customerInfo;
  }
}