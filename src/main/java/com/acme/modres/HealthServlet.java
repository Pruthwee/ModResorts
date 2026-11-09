package com.acme.modres;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_OK);

    JsonObject health = new JsonObject();
    health.addProperty("status", "UP");
    health.addProperty("application", "ModResorts");

    PrintWriter out = response.getWriter();
    out.print(new Gson().toJson(health));
    out.flush();
  }
}
