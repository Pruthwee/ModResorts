package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Logout servlet.
 * Migrated from WebSphere-specific WSSecurityHelper.revokeSSOCookies() to standard
 * javax.servlet HttpSession invalidation, enabling stateless session management
 * compatible with Amazon ElastiCache (Redis) for horizontal scaling in EKS/ECS.
 */
@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(LogoutServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Invalidate the HTTP session - compatible with externalized session stores
      // such as Amazon ElastiCache (Redis) for cloud-native stateless session management
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }

      // Clear any authentication cookies via standard servlet API
      javax.servlet.http.Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (javax.servlet.http.Cookie cookie : cookies) {
          cookie.setValue("");
          cookie.setPath("/");
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        }
      }
    } catch (Exception e) {
      logger.severe("[ERROR] Error logging out: " + e.getMessage());
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
