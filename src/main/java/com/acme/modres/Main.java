package com.acme.modres;

import java.io.File;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;

public class Main {

  public static void main(String[] args) throws Exception {
    int port = 8080;
    String portEnv = System.getenv("PORT");
    if (portEnv != null && portEnv.trim().length() > 0) {
      try {
        port = Integer.parseInt(portEnv);
      } catch (NumberFormatException e) {
        // keep default
      }
    }

    Tomcat tomcat = new Tomcat();
    tomcat.setPort(port);

    String webAppDir = new File("WebContent").getAbsolutePath();
    File docBase = new File(webAppDir);

    // Tomcat reads WEB-INF/web.xml automatically from the docBase
    Context context = tomcat.addWebapp("", docBase.getAbsolutePath());

    tomcat.start();
    tomcat.getServer().await();
  }
}
