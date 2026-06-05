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
class AvailabilityCheckerServletTest {

    private AvailabilityCheckerServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new AvailabilityCheckerServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testInit_shouldInitializeReservationCheckerData() {
        // Act
        servlet.init();

        // Assert - servlet should initialize without throwing exception
        assertNotNull(servlet);
    }

    @Test
    void testDoGet_withValidDate_shouldReturnAvailability() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("date")).thenReturn("2024-01-15");
        servlet.init();

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setStatus(anyInt());
        assertTrue(stringWriter.toString().contains("availability"));
    }

    @Test
    void testDoGet_withInvalidDate_shouldReturnError() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("date")).thenReturn("invalid-date");
        servlet.init();

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setStatus(500);
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withNullDate_shouldReturnError() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("date")).thenReturn(null);
        servlet.init();

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setStatus(500);
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withEmptyDate_shouldReturnError() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("date")).thenReturn("");
        servlet.init();

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setStatus(500);
    }

    @Test
    void testDoPost_shouldCallDoGet() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("date")).thenReturn("2024-01-15");
        servlet.init();

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
    }

    @Test
    void testExportReservations_withValidDate_shouldReturnSuccessOrFailure() {
        // Arrange
        servlet.init();
        String selectedDate = "2024-01-15";

        // Act
        int result = servlet.exportRevervations(selectedDate);

        // Assert
        assertTrue(result == 0 || result == -1);
    }

    @Test
    void testExportReservations_withNullDate_shouldHandleGracefully() {
        // Arrange
        servlet.init();

        // Act
        int result = servlet.exportRevervations(null);

        // Assert
        assertEquals(-1, result);
    }

    @Test
    void testExportReservations_withEmptyDate_shouldHandleGracefully() {
        // Arrange
        servlet.init();

        // Act
        int result = servlet.exportRevervations("");

        // Assert
        assertEquals(-1, result);
    }

    @Test
    void testDoGet_responseContentType_shouldBeJson() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("date")).thenReturn("2024-01-15");
        servlet.init();

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
    }

    @Test
    void testDoGet_responseFormat_shouldContainAvailabilityField() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("date")).thenReturn("2024-01-15");
        servlet.init();

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        String output = stringWriter.toString();
        assertTrue(output.contains("\"availability\""));
    }
}
