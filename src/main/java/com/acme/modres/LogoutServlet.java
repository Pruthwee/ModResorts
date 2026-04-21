package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Servlet for handling user logout.
 * Updated to use standard servlet session management instead of WebSphere-specific APIs.
 * Session state is now managed by Spring Session with Redis for cloud-native distributed sessions.
 */
@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  private static final Logger logger = Logger.getLogger(LogoutServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      // Use standard servlet session invalidation instead of WebSphere-specific API
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
        logger.info("User session invalidated successfully");
      }
    } catch (Exception e) {
      logger.severe("[ERROR] Error logging out: " + e.getMessage());
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
