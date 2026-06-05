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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
        servlet = new UpperServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_withValidInput_shouldReturnUpperCase() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("hello world");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        verify(response).setContentType("text/html");
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO WORLD"));
        assertTrue(output.contains("<br/><b>upper case input"));
    }

    @Test
    void testDoGet_withNullInput_shouldReturnEmptyString() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn(null);

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        verify(response).setContentType("text/html");
        String output = stringWriter.toString();
        assertTrue(output.contains("<br/><b>upper case input "));
    }

    @Test
    void testDoGet_withEmptyInput_shouldReturnEmptyString() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        verify(response).setContentType("text/html");
        String output = stringWriter.toString();
        assertTrue(output.contains("<br/><b>upper case input "));
    }

    @Test
    void testDoGet_withLowerCaseInput_shouldConvertToUpperCase() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("test");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("TEST"));
        assertFalse(output.contains("test"));
    }

    @Test
    void testDoGet_withMixedCaseInput_shouldConvertToUpperCase() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("TeSt");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("TEST"));
    }

    @Test
    void testDoGet_withSpecialCharacters_shouldHandleCorrectly() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("hello@world!");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO@WORLD!"));
    }

    @Test
    void testDoGet_withHtmlTags_shouldEscapeHtml() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("<script>alert('xss')</script>");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("&lt;") || output.contains("&gt;"));
        assertFalse(output.contains("<script>"));
    }

    @Test
    void testDoGet_withNumbers_shouldReturnSameNumbers() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("123456");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("123456"));
    }

    @Test
    void testDoGet_withWhitespace_shouldPreserveWhitespace() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("hello   world");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("HELLO   WORLD"));
    }

    @Test
    void testDoGet_shouldSetContentTypeToHtml() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("test");

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/html");
    }

    @Test
    void testDoGet_withLongInput_shouldHandleCorrectly() throws ServletException, IOException {
        // Arrange
        String longInput = "a".repeat(1000);
        when(request.getParameter("input")).thenReturn(longInput);

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("A".repeat(1000)));
    }

    @Test
    void testDoGet_withUnicodeCharacters_shouldHandleCorrectly() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("café");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("CAFÉ"));
    }

    @Test
    void testDoGet_outputFormat_shouldContainBoldTags() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("test");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("<b>"));
        assertTrue(output.contains("</b>"));
    }

    @Test
    void testDoGet_outputFormat_shouldContainBreakTag() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("input")).thenReturn("test");

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("<br/>"));
    }
}
