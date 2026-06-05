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
        servlet = new WelcomeServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testDoGet_shouldReturnEnjoyMessage() throws ServletException, IOException {
        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        verify(response).setContentType("text/plain");
        String output = stringWriter.toString();
        assertTrue(output.contains("Enjoy!"));
    }

    @Test
    void testDoGet_shouldSetContentTypeToTextPlain() throws ServletException, IOException {
        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
    }

    @Test
    void testDoGet_shouldCallGetWriter() throws ServletException, IOException {
        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).getWriter();
    }

    @Test
    void testDoGet_outputShouldContainEnjoy() throws ServletException, IOException {
        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("Enjoy!"));
    }

    @Test
    void testDoGet_multipleInvocations_shouldWorkCorrectly() throws ServletException, IOException {
        // Act
        servlet.doGet(request, response);
        servlet.doGet(request, response);

        // Assert
        verify(response, times(2)).setContentType("text/plain");
        verify(response, times(2)).getWriter();
    }

    @Test
    void testDoGet_shouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoGet_outputShouldEndWithNewline() throws ServletException, IOException {
        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.endsWith("\n") || output.endsWith("\r\n"));
    }

    @Test
    void testSerialVersionUID_shouldBeDefined() {
        // Assert
        assertNotNull(servlet);
    }

    @Test
    void testDoGet_shouldProduceNonEmptyOutput() throws ServletException, IOException {
        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertFalse(output.isEmpty());
    }

    @Test
    void testDoGet_withNullRequest_shouldThrowException() throws IOException {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            servlet.doGet(null, response);
        });
    }
}
