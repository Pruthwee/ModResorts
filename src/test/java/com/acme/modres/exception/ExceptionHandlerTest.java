package com.acme.modres.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.logging.Logger;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ExceptionHandlerTest {

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleException_withNullException_throwsServletException() {
        String errorMsg = "Test error message";
        
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMsg, logger);
        });
        
        assertEquals(errorMsg, exception.getMessage());
    }

    @Test
    void testHandleException_withException_throwsServletException() {
        Exception cause = new RuntimeException("Cause exception");
        String errorMsg = "Test error message";
        
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(cause, errorMsg, logger);
        });
        
        assertEquals(errorMsg, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testHandleException_withNullException_logsError() {
        String errorMsg = "Test error message";
        
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMsg, logger);
        });
        
        verify(logger).severe(errorMsg);
    }

    @Test
    void testHandleException_withException_logsError() {
        Exception cause = new RuntimeException("Cause exception");
        String errorMsg = "Test error message";
        
        assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(cause, errorMsg, logger);
        });
        
        verify(logger).log(any(), eq(errorMsg), eq(cause));
    }

    @Test
    void testHandleException_withEmptyMessage() {
        String errorMsg = "";
        
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMsg, logger);
        });
        
        assertEquals(errorMsg, exception.getMessage());
    }

    @Test
    void testHandleException_withLongMessage() {
        String errorMsg = "This is a very long error message that contains a lot of details about what went wrong";
        
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(null, errorMsg, logger);
        });
        
        assertEquals(errorMsg, exception.getMessage());
    }

    @Test
    void testHandleException_withIOException() {
        Exception cause = new java.io.IOException("IO error");
        String errorMsg = "IO operation failed";
        
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(cause, errorMsg, logger);
        });
        
        assertEquals(errorMsg, exception.getMessage());
        assertTrue(exception.getCause() instanceof java.io.IOException);
    }

    @Test
    void testHandleException_withSQLException() {
        Exception cause = new java.sql.SQLException("SQL error");
        String errorMsg = "Database operation failed";
        
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(cause, errorMsg, logger);
        });
        
        assertEquals(errorMsg, exception.getMessage());
        assertTrue(exception.getCause() instanceof java.sql.SQLException);
    }

    @Test
    void testHandleException_preservesCauseChain() {
        Exception rootCause = new IllegalArgumentException("Root cause");
        Exception cause = new RuntimeException("Wrapper", rootCause);
        String errorMsg = "Operation failed";
        
        ServletException exception = assertThrows(ServletException.class, () -> {
            ExceptionHandler.handleException(cause, errorMsg, logger);
        });
        
        assertEquals(cause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}
