package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UpperServlet migrated from WebSphere-specific ResponseUtils to standard Java encoding
 * Uses cloud-native compatible encoding methods
 */
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
    // Replace WebSphere-specific ResponseUtils with standard URL encoding
    newStr = encodeForHTML(newStr);

    PrintWriter out = response.getWriter();
    out.print("<br/><b>upper case input " + newStr + "</b>");
  }
  
  /**
   * HTML-safe encoding to prevent XSS attacks
   * Replaces WebSphere ResponseUtils.encodeDataString
   */
  private String encodeForHTML(String input) {
    if (input == null) {
      return "";
    }
    
    StringBuilder encoded = new StringBuilder();
    for (char c : input.toCharArray()) {
      switch (c) {
        case '<':
          encoded.append("&lt;");
          break;
        case '>':
          encoded.append("&gt;");
          break;
        case '&':
          encoded.append("&amp;");
          break;
        case '"':
          encoded.append("&quot;");
          break;
        case '\'':
          encoded.append("&#x27;");
          break;
        default:
          encoded.append(c);
      }
    }
    return encoded.toString();
  }
}
