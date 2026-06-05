package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
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
class FirstFilterTest {

    private FirstFilter filter;

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
        filter = new FirstFilter();
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
    void testDoFilter_withUserParameter_shouldWriteWelcomeMessage() throws IOException, ServletException {
        // Arrange
        when(request.getParameter("user")).thenReturn("John");

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        verify(response).setContentType("text/plain");
        verify(filterChain).doFilter(request, response);
        String output = stringWriter.toString();
        assertTrue(output.contains("Welcome John"));
    }

    @Test
    void testDoFilter_withNullUserParameter_shouldUseDefaultUser() throws IOException, ServletException {
        // Arrange
        when(request.getParameter("user")).thenReturn(null);

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        verify(response).setContentType("text/plain");
        verify(filterChain).doFilter(request, response);
        String output = stringWriter.toString();
        assertTrue(output.contains("Welcome defaultUser"));
    }

    @Test
    void testDoFilter_withEmptyUserParameter_shouldUseEmptyString() throws IOException, ServletException {
        // Arrange
        when(request.getParameter("user")).thenReturn("");

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        verify(response).setContentType("text/plain");
        verify(filterChain).doFilter(request, response);
        String output = stringWriter.toString();
        assertTrue(output.contains("Welcome "));
    }

    @Test
    void testDoFilter_shouldSetContentTypeToTextPlain() throws IOException, ServletException {
        // Arrange
        when(request.getParameter("user")).thenReturn("TestUser");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setContentType("text/plain");
    }

    @Test
    void testDoFilter_shouldCallFilterChain() throws IOException, ServletException {
        // Arrange
        when(request.getParameter("user")).thenReturn("TestUser");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_withSpecialCharactersInUser_shouldHandleCorrectly() throws IOException, ServletException {
        // Arrange
        when(request.getParameter("user")).thenReturn("John@Doe#123");

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Welcome John@Doe#123"));
    }

    @Test
    void testDoFilter_withLongUserName_shouldHandleCorrectly() throws IOException, ServletException {
        // Arrange
        String longUserName = "VeryLongUserNameThatExceedsNormalLength123456789";
        when(request.getParameter("user")).thenReturn(longUserName);

        // Act
        filter.doFilter(request, response, filterChain);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Welcome " + longUserName));
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
        when(request.getParameter("user")).thenReturn("TestUser");

        // Act
        filter.doFilter(servletRequest, servletResponse, filterChain);

        // Assert
        verify(response).setContentType("text/plain");
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @Test
    void testDoFilter_multipleInvocations_shouldWorkCorrectly() throws IOException, ServletException {
        // Arrange
        when(request.getParameter("user")).thenReturn("User1");

        // Act
        filter.doFilter(request, response, filterChain);
        
        // Reset for second call
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
        when(request.getParameter("user")).thenReturn("User2");
        
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(2)).doFilter(request, response);
    }
}
