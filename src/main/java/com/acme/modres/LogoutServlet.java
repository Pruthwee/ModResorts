package com.acme.modres;

import javax.servlet.annotation.WebServlet;


@WebServlet({ "/logout" })
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request,
    try {
    response.sendRedirect("login.jsp");
  }
}
