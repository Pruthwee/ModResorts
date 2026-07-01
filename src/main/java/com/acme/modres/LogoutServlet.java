package com.acme.modres;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
      // WebSphere specific SSO cookie revocation replaced with standard session invalidation
      request.getSession().invalidate();
    }

    response.sendRedirect("login.jsp");
  }
}
