package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * LogoutServlet migrated from WebSphere-specific security to cloud-native session management
 * Uses standard servlet session management compatible with Redis-backed sessions
 */
@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(LogoutServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Invalidate the session (works with Redis-backed sessions via Spring Session)
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
        logger.info("Session invalidated successfully");
      }
      
      // Clear any authentication cookies
      javax.servlet.http.Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (javax.servlet.http.Cookie cookie : cookies) {
          if (cookie.getName().startsWith("JSESSIONID") || 
              cookie.getName().startsWith("SESSION")) {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
          }
        }
      }
      
    } catch (Exception e) {
      logger.severe("[ERROR] Error logging out: " + e.getMessage());
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
