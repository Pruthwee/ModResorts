package com.acme.modres;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = Logger.getLogger(LogoutServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Attempt to use WebSphere-specific logout if available
      // This will only work in WebSphere environments
      try {
        Class<?> wsSecurityHelperClass = Class.forName("com.ibm.websphere.security.WSSecurityHelper");
        java.lang.reflect.Method revokeSSOCookiesMethod = wsSecurityHelperClass.getMethod(
            "revokeSSOCookies", HttpServletRequest.class, HttpServletResponse.class);
        revokeSSOCookiesMethod.invoke(null, request, response);
        logger.log(Level.INFO, "WebSphere SSO cookies revoked successfully");
      } catch (ClassNotFoundException e) {
        // WebSphere runtime not available - use standard servlet logout
        logger.log(Level.INFO, "WebSphere runtime not available, using standard logout");
        performStandardLogout(request, response);
      } catch (Exception e) {
        logger.log(Level.WARNING, "Error during WebSphere logout, falling back to standard logout", e);
        performStandardLogout(request, response);
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out");
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
  
  /**
   * Performs standard servlet-based logout when WebSphere APIs are not available
   */
  private void performStandardLogout(HttpServletRequest request, HttpServletResponse response) {
    try {
      // Invalidate the session
      if (request.getSession(false) != null) {
        request.getSession().invalidate();
      }
      // Logout using standard servlet API
      request.logout();
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error during standard logout", e);
    }
  }
}
