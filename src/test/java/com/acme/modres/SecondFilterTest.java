package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecondFilterTest {

    private SecondFilter filter;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        filter = new SecondFilter();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testInit_shouldInitializeWithoutException() throws ServletException {
        // Act
        filter.init(filterConfig);

        // Assert - no exception should be thrown
        assertNotNull(filter);
    }

    @Test
    void testDoFilter_withRequestContent_shouldAppendMessage() throws IOException, ServletException {
        // Arrange
        String requestContent = "Hello";
        BufferedReader reader = new BufferedReader(new StringReader(requestContent));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        verify(response).setContentType("text/plain");
        verify(filterChain).doFilter(request, response);
        String output = stringWriter.toString();
        assertTrue(output.contains("Hello to our site!"));
    }

    @Test
    void testDoFilter_withEmptyRequestContent_shouldAppendMessage() throws IOException, ServletException {
        // Arrange
        BufferedReader reader = new BufferedReader(new StringReader(""));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        verify(response).setContentType("text/plain");
        verify(filterChain).doFilter(request, response);
        String output = stringWriter.toString();
        assertTrue(output.contains(" to our site!"));
    }

    @Test
    void testDoFilter_withMultilineContent_shouldConcatenateLines() throws IOException, ServletException {
        // Arrange
        String requestContent = "Line1\nLine2\nLine3";
        BufferedReader reader = new BufferedReader(new StringReader(requestContent));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Line1Line2Line3 to our site!"));
    }

    @Test
    void testDoFilter_shouldSetContentTypeToTextPlain() throws IOException, ServletException {
        // Arrange
        BufferedReader reader = new BufferedReader(new StringReader("Test"));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setContentType("text/plain");
    }

    @Test
    void testDoFilter_shouldCallFilterChain() throws IOException, ServletException {
        // Arrange
        BufferedReader reader = new BufferedReader(new StringReader("Test"));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_withLongContent_shouldHandleCorrectly() throws IOException, ServletException {
        // Arrange
        String longContent = "A".repeat(1000);
        BufferedReader reader = new BufferedReader(new StringReader(longContent));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains(longContent + " to our site!"));
    }

    @Test
    void testDoFilter_withSpecialCharacters_shouldHandleCorrectly() throws IOException, ServletException {
        // Arrange
        String specialContent = "Hello@#$%^&*()";
        BufferedReader reader = new BufferedReader(new StringReader(specialContent));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains(specialContent + " to our site!"));
    }

    @Test
    void testDestroy_shouldExecuteWithoutException() {
        // Act
        filter.destroy();

        // Assert - no exception should be thrown
        assertNotNull(filter);
    }

    @Test
    void testDoFilter_shouldCastRequestAndResponseCorrectly() throws IOException, ServletException {
        // Arrange
        ServletRequest servletRequest = request;
        ServletResponse servletResponse = response;
        BufferedReader reader = new BufferedReader(new StringReader("Test"));
        when(request.getReader()).thenReturn(reader);

        // Act
        filter.doFilter(servletRequest, servletResponse, filterChain);

        // Assert
        verify(response).setContentType("text/plain");
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @Test
    void testDoFilter_withNullReader_shouldThrowException() throws IOException, ServletException {
        // Arrange
        when(request.getReader()).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testDoFilter_multipleInvocations_shouldWorkCorrectly() throws IOException, ServletException {
        // Arrange
        BufferedReader reader1 = new BufferedReader(new StringReader("First"));
        when(request.getReader()).thenReturn(reader1);

        // Act
        filter.doFilter(request, response, filterChain);

        // Reset for second call
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        BufferedReader reader2 = new BufferedReader(new StringReader("Second"));
        when(request.getReader()).thenReturn(reader2);

        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(2)).doFilter(request, response);
    }
}
