package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/resorts/upper")
public class UpperServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(UpperServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");

    String originalStr = request.getParameter("input");
    if (originalStr == null) {
      originalStr = "";
    }

    String newStr = originalStr.toUpperCase();
    
    // Try to use WebSphere ResponseUtils if available, otherwise use standard encoding
    try {
      Class<?> responseUtilsClass = Class.forName("com.ibm.websphere.servlet.response.ResponseUtils");
      java.lang.reflect.Method encodeMethod = responseUtilsClass.getMethod("encodeDataString", String.class);
      newStr = (String) encodeMethod.invoke(null, newStr);
    } catch (ClassNotFoundException e) {
      // WebSphere runtime not available - use standard HTML encoding
      logger.log(Level.FINE, "WebSphere ResponseUtils not available, using standard encoding");
      newStr = htmlEncode(newStr);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Error using WebSphere ResponseUtils, falling back to standard encoding", e);
      newStr = htmlEncode(newStr);
    }

    PrintWriter out = response.getWriter();
    out.print("<br/><b>upper case input " + newStr + "</b>");
  }
  
  /**
   * Simple HTML encoding to prevent XSS attacks
   */
  private String htmlEncode(String input) {
    if (input == null) {
      return null;
    }
    return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
  }
}
