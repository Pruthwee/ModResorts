package com.acme.modres.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerTest {

    @Mock
    private Logger logger;

    private String errorMessage;

    @BeforeEach
    void setUp() {
        errorMessage = "Test error message";
    }

    @Test
    void testHandleException_withNullException_shouldThrowServletException() {
        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMessage, logger);
        });

        assertEquals(errorMessage, exception.getMessage());
        verify(logger).severe(errorMessage);
    }

    @Test
    void testHandleException_withException_shouldThrowServletExceptionWithCause() {
        // Arrange
        Exception cause = new RuntimeException("Cause exception");

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(cause, errorMessage, logger);
        });

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        verify(logger).log(eq(Level.SEVERE), eq(errorMessage), eq(cause));
    }

    @Test
    void testHandleException_withNullException_shouldLogSevere() {
        // Act & Assert
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMessage, logger);
        });

        verify(logger).severe(errorMessage);
    }

    @Test
    void testHandleException_withException_shouldLogWithLevel() {
        // Arrange
        Exception cause = new IllegalArgumentException("Invalid argument");

        // Act & Assert
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(cause, errorMessage, logger);
        });

        verify(logger).log(Level.SEVERE, errorMessage, cause);
    }

    @Test
    void testHandleException_withEmptyErrorMessage_shouldThrowWithEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, emptyMessage, logger);
        });

        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testHandleException_withLongErrorMessage_shouldHandleCorrectly() {
        // Arrange
        String longMessage = "A".repeat(1000);

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, longMessage, logger);
        });

        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    void testHandleException_withIOException_shouldWrapInServletException() {
        // Arrange
        Exception ioException = new java.io.IOException("IO error");

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(ioException, errorMessage, logger);
        });

        assertEquals(ioException, exception.getCause());
    }

    @Test
    void testHandleException_withSQLException_shouldWrapInServletException() {
        // Arrange
        Exception sqlException = new java.sql.SQLException("SQL error");

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(sqlException, errorMessage, logger);
        });

        assertEquals(sqlException, exception.getCause());
    }

    @Test
    void testHandleException_withNullPointerException_shouldWrapInServletException() {
        // Arrange
        Exception npe = new NullPointerException("Null pointer");

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(npe, errorMessage, logger);
        });

        assertEquals(npe, exception.getCause());
    }

    @Test
    void testHandleException_shouldAlwaysThrowServletException() {
        // Act & Assert
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMessage, logger);
        });

        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(new Exception(), errorMessage, logger);
        });
    }

    @Test
    void testHandleException_withSpecialCharactersInMessage_shouldHandleCorrectly() {
        // Arrange
        String specialMessage = "Error: <script>alert('xss')</script>";

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, specialMessage, logger);
        });

        assertEquals(specialMessage, exception.getMessage());
    }

    @Test
    void testHandleException_withMultilineMessage_shouldHandleCorrectly() {
        // Arrange
        String multilineMessage = "Line 1\nLine 2\nLine 3";

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, multilineMessage, logger);
        });

        assertEquals(multilineMessage, exception.getMessage());
    }

    @Test
    void testHandleException_withNestedExceptions_shouldPreserveCause() {
        // Arrange
        Exception rootCause = new IllegalStateException("Root cause");
        Exception wrappedException = new RuntimeException("Wrapped", rootCause);

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(wrappedException, errorMessage, logger);
        });

        assertEquals(wrappedException, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}
