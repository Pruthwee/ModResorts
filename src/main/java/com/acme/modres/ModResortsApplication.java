package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.acme.modres")
public class ModResortsApplication {
  public static void main(String[] args) {
    SpringApplication.run(ModResortsApplication.class, args);
  }
}
