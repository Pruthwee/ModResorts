package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple health check endpoint for container orchestration systems.
 */
@WebServlet("/health")
public class HealthCheckServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    // Minimal health response indicating the application is up
    PrintWriter out = response.getWriter();
    out.print("{\"status\":\"UP\"}");
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
