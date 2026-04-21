package com.acme.modres.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud-ready repository using Spring Data JPA with HikariCP connection pooling.
 * This replaces EJB 2.x with Spring Boot patterns for better cloud compatibility.
 * HikariCP provides efficient connection pooling for AWS RDS and other cloud databases.
 */
@Repository
public class ModResortsCustomerInformation {
  private static final String SELECT_CUSTOMERS_QUERY = "SELECT INFO FROM CUSTOMER";

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public ModResortsCustomerInformation(DataSource dataSource) {
    // HikariCP is automatically configured by Spring Boot
    // Connection pooling is handled transparently
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public ArrayList<String> getCustomerInformation() {
    try {
      // Use JdbcTemplate which automatically manages connections from the pool
      List<String> customerInfo = jdbcTemplate.query(
          SELECT_CUSTOMERS_QUERY,
          (rs, rowNum) -> rs.getString("INFO")
      );
      return new ArrayList<>(customerInfo);
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }
}
