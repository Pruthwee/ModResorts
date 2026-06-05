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

class WelcomeServletTest {

    private WelcomeServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        servlet = new WelcomeServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_setsContentType() throws ServletException, IOException {
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/plain");
    }

    @Test
    void testDoGet_writesEnjoyMessage() throws ServletException, IOException {
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("Enjoy!"));
    }

    @Test
    void testDoGet_outputNotEmpty() throws ServletException, IOException {
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertFalse(output.isEmpty());
    }

    @Test
    void testDoGet_outputContainsSpace() throws ServletException, IOException {
        servlet.doGet(request, response);
        
        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.trim().equals("Enjoy!"));
    }

    @Test
    void testDoGet_multipleInvocations() throws ServletException, IOException {
        servlet.doGet(request, response);
        servlet.doGet(request, response);
        
        verify(response, times(2)).setContentType("text/plain");
    }

    @Test
    void testDoGet_doesNotThrowException() {
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_writesToPrintWriter() throws ServletException, IOException {
        servlet.doGet(request, response);
        
        verify(response).getWriter();
    }
}
