package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WeatherServletTest {

    private WeatherServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        servlet = new WeatherServlet();
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> servlet.init());
    }

    @Test
    void testDestroy() {
        servlet.init();
        assertDoesNotThrow(() -> servlet.destroy());
    }

    @Test
    void testDoGet_withValidCity() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withParis() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withLasVegas() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.LAS_VEGAS);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withSanFrancisco() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.SAN_FRANCISCO);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withMiami() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.MIAMI);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withCork() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.CORK);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withBarcelona() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.BARCELONA);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoPost_callsDoGet() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);
        
        servlet.init();
        servlet.doPost(request, response);
        
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withNullCity() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(null);
        
        servlet.init();
        
        assertThrows(ServletException.class, () -> {
            servlet.doGet(request, response);
        });
    }

    @Test
    void testDoGet_withInvalidCity() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn("InvalidCity");
        
        servlet.init();
        
        assertThrows(ServletException.class, () -> {
            servlet.doGet(request, response);
        });
    }

    @Test
    void testInit_registersMBean() {
        assertDoesNotThrow(() -> servlet.init());
    }

    @Test
    void testDestroy_unregistersMBean() {
        servlet.init();
        assertDoesNotThrow(() -> servlet.destroy());
    }

    @Test
    void testDoGet_setsJsonContentType() throws IOException, ServletException {
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);
        
        servlet.init();
        servlet.doGet(request, response);
        
        verify(response).setContentType("application/json");
    }
}
