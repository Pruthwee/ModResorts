package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UpperServletTest {

    private UpperServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        servlet = new UpperServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_withValidInput() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("hello");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO"));
    }

    @Test
    void testDoGet_withNullInput() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        writer.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    void testDoGet_withEmptyInput() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        writer.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    void testDoGet_convertsToUpperCase() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test");
        
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("TEST"));
    }

    @Test
    void testDoGet_withMixedCase() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("TeSt");
        
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("TEST"));
    }

    @Test
    void testDoGet_withNumbers() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test123");
        
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("TEST123"));
    }

    @Test
    void testDoGet_withSpecialCharacters() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test@123");
        
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("TEST"));
    }

    @Test
    void testDoGet_setsContentType() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test");
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_outputContainsBoldTag() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test");
        
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("<b>"));
        assertTrue(output.contains("</b>"));
    }

    @Test
    void testDoGet_outputContainsBreakTag() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("test");
        
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("<br/>"));
    }

    @Test
    void testDoGet_withLongInput() throws ServletException, IOException {
        String longInput = "this is a very long input string for testing";
        when(request.getParameter("input")).thenReturn(longInput);
        
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains(longInput.toUpperCase()));
    }

    @Test
    void testDoGet_multipleInvocations() throws ServletException, IOException {
        when(request.getParameter("input")).thenReturn("first");
        servlet.doGet(request, response);
        
        when(request.getParameter("input")).thenReturn("second");
        servlet.doGet(request, response);
        
        verify(response, times(2)).setContentType("text/html");
    }
}
