package com.acme.modres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

@SpringBootApplication
@ServletComponentScan
public class ModResortsApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    System.setProperty("server.port", System.getenv().getOrDefault("PORT", "8080"));
    SpringApplication.run(ModResortsApplication.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(ModResortsApplication.class);
  }

  @Bean
  public WebServerFactoryCustomizer<org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory> webServerFactoryCustomizer() {
    return factory -> factory.setPort(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));
  }

  @Bean
  public FilterRegistrationBean<Filter> firstFilterRegistration() {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new FirstFilter());
    registration.addUrlPatterns("/welcome");
    registration.setOrder(1);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<Filter> secondFilterRegistration() {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new SecondFilter());
    registration.addUrlPatterns("/welcome");
    registration.setOrder(2);
    return registration;
  }
}
