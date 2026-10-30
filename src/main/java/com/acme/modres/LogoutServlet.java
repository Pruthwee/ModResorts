package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;

@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    try {
      Class<?> helperClass = Class.forName("com.ibm.websphere.security.WSSecurityHelper");
      Method revokeMethod = helperClass.getMethod("revokeSSOCookies", HttpServletRequest.class, HttpServletResponse.class);
      revokeMethod.invoke(null, request, response);
    } catch (ClassNotFoundException e) {
      // WebSphere not available - skip SSO cookie revocation
    } catch (Exception e) {
      System.err.println("[ERROR] Error logging out");
      e.printStackTrace();
    }

    response.sendRedirect("login.jsp");
  }
}
