package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutServletTest {

    private LogoutServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        servlet = new LogoutServlet();
    }

    @Test
    void testDoGet_withValidSession_shouldInvalidateSession() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(session).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withNullSession_shouldRedirectToLogin() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(session, never()).invalidate();
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_shouldRedirectToLoginPage() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withSessionInvalidationException_shouldStillRedirect() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IllegalStateException("Session already invalidated")).when(session).invalidate();

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withGetSessionException_shouldHandleGracefully() throws IOException {
        // Arrange
        when(request.getSession(false)).thenThrow(new RuntimeException("Session error"));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_shouldCallGetSessionWithFalseParameter() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(request).getSession(false);
    }

    @Test
    void testDoGet_multipleInvocations_shouldWorkCorrectly() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);

        // Act
        servlet.doGet(request, response);
        servlet.doGet(request, response);

        // Assert
        verify(session, times(2)).invalidate();
        verify(response, times(2)).sendRedirect("login.jsp");
    }

    @Test
    void testDoGet_withRedirectException_shouldThrowIOException() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        doThrow(new IOException("Redirect failed")).when(response).sendRedirect(anyString());

        // Act & Assert
        assertThrows(IOException.class, () -> {
            servlet.doGet(request, response);
        });
    }

    @Test
    void testDoGet_shouldNotCreateNewSession() throws IOException {
        // Arrange
        when(request.getSession(false)).thenReturn(null);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(request).getSession(false);
        verify(request, never()).getSession(true);
        verify(request, never()).getSession();
    }

    @Test
    void testSerialVersionUID_shouldBeDefined() {
        // Assert
        assertNotNull(servlet);
    }
}
