package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/resorts/upper")
public class UpperServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");

    String originalStr = request.getParameter("input");
    if (originalStr == null) {
      originalStr = "";
    }

    String newStr = originalStr.toUpperCase();
    try {
      Class<?> responseUtilsClass = Class.forName("com.ibm.websphere.servlet.response.ResponseUtils");
      Method encodeMethod = responseUtilsClass.getMethod("encodeDataString", String.class);
      newStr = (String) encodeMethod.invoke(null, newStr);
    } catch (ClassNotFoundException e) {
      // WebSphere not available - use the string as-is
    } catch (Exception e) {
      // Use the string as-is if encoding fails
    }

    PrintWriter out = response.getWriter();
    out.print("<br/><b>upper case input " + newStr + "</b>");
  }
}
