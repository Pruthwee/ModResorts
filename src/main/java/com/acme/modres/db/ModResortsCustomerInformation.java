package com.acme.modres.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class ModResortsCustomerInformation implements CustomerInformationService {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";
  private final HikariDataSource dataSource;

  public ModResortsCustomerInformation() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(requiredEnv("MODRES_DB_URL"));
    config.setUsername(requiredEnv("MODRES_DB_USERNAME"));
    config.setPassword(requiredEnv("MODRES_DB_PASSWORD"));
    config.setMaximumPoolSize(Integer.parseInt(System.getenv().getOrDefault("MODRES_DB_POOL_SIZE", "5")));
    this.dataSource = new HikariDataSource(config);
  }

  @Override
  public ArrayList<String> getCustomerInformation() {
    ArrayList<String> customerInfo = new ArrayList<>();
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(SELECT_CUSTOMERS_QUERY);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        customerInfo.add(rs.getString("INFO"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return customerInfo;
  }

  @PreDestroy
  public void shutdown() {
    dataSource.close();
  }

  private String requiredEnv(String key) {
    String value = System.getenv(key);
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalStateException("Missing required environment variable: " + key);
    }
    return value;
  }
}
