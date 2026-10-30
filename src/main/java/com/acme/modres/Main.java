package com.acme.modres;

import java.io.File;
import java.nio.file.Files;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.jasper.servlet.JasperInitializer;

/**
 * Entry point of the executable jar: starts an embedded Tomcat that serves the
 * same web application the WAR held (WebContent/, WEB-INF/web.xml, the servlets
 * and filters, login.jsp) at the same context path and port as before.
 *
 * Settings (environment): PORT (default 9080), CONTEXT_PATH (default /resorts),
 * WEB_CONTENT_DIR (default: the WebContent folder in the working directory, or
 * the one next to the jar).
 */
public class Main {

  private static final int DEFAULT_PORT = 9080;
  private static final String DEFAULT_CONTEXT_PATH = "/resorts";
  private static final String WEB_CONTENT_FOLDER = "WebContent";

  // Servlets declared only by @WebServlet (the WAR scan found them in WEB-INF/classes)
  @SuppressWarnings("unchecked")
  private static final Class<? extends HttpServlet>[] ANNOTATED_SERVLETS = new Class[] {
      WelcomeServlet.class, UpperServlet.class, LogoutServlet.class, WeatherServlet.class,
      AvailabilityCheckerServlet.class };

  public static void main(String[] args) throws Exception {
    Tomcat tomcat = new Tomcat();
    tomcat.setBaseDir(Files.createTempDirectory("modresorts-tomcat").toString());
    tomcat.setPort(readPort());
    tomcat.getConnector();

    File webContent = findWebContent();
    Context context = tomcat.addWebapp(readContextPath(), webContent.getAbsolutePath());
    context.addServletContainerInitializer(new JasperInitializer(), null);

    for (Class<? extends HttpServlet> servletClass : ANNOTATED_SERVLETS) {
      WebServlet annotation = servletClass.getAnnotation(WebServlet.class);
      if (annotation == null) {
        continue;
      }
      Tomcat.addServlet(context, servletClass.getName(), servletClass.getName());
      String[] patterns = annotation.value().length > 0 ? annotation.value() : annotation.urlPatterns();
      for (String pattern : patterns) {
        context.addServletMappingDecoded(pattern, servletClass.getName());
      }
    }

    tomcat.start();
    tomcat.getServer().await();
  }

  private static int readPort() {
    String port = System.getenv("PORT");
    if (port == null || port.trim().isEmpty()) {
      return DEFAULT_PORT;
    }
    return Integer.parseInt(port.trim());
  }

  private static String readContextPath() {
    String path = System.getenv("CONTEXT_PATH");
    if (path == null || path.trim().isEmpty()) {
      return DEFAULT_CONTEXT_PATH;
    }
    return path.trim();
  }

  private static File findWebContent() {
    String configured = System.getenv("WEB_CONTENT_DIR");
    if (configured != null && !configured.trim().isEmpty()) {
      return new File(configured.trim());
    }
    File inWorkingDir = new File(WEB_CONTENT_FOLDER);
    if (inWorkingDir.isDirectory()) {
      return inWorkingDir;
    }
    String firstClassPathEntry = System.getProperty("java.class.path", "").split(File.pathSeparator)[0];
    File parent = new File(firstClassPathEntry).getAbsoluteFile().getParentFile();
    File besideJar = new File(parent, WEB_CONTENT_FOLDER);
    if (besideJar.isDirectory()) {
      return besideJar;
    }
    throw new IllegalStateException(
        "Web content folder not found: set WEB_CONTENT_DIR or run next to the " + WEB_CONTENT_FOLDER + " folder");
  }
}
