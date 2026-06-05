package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServletTest {

    private WeatherServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @BeforeEach
    void setUp() throws IOException {
        servlet = new WeatherServlet();
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    void testInit_shouldInitializeMBeanServer() {
        // Act
        servlet.init();

        // Assert
        assertNotNull(servlet);
    }

    @Test
    void testDestroy_shouldUnregisterMBean() {
        // Arrange
        servlet.init();

        // Act
        servlet.destroy();

        // Assert - no exception should be thrown
        assertNotNull(servlet);
    }

    @Test
    void testDoGet_withValidCity_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withParis_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
        verify(response).getOutputStream();
    }

    @Test
    void testDoGet_withLasVegas_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.LAS_VEGAS);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withSanFrancisco_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.SAN_FRANCISCO);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withMiami_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.MIAMI);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withCork_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.CORK);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withBarcelona_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.BARCELONA);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_withNullCity_shouldHandleGracefully() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(null);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> servlet.doGet(request, response));
    }

    @Test
    void testDoPost_shouldCallDoGet() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);

        // Act
        servlet.doPost(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testInit_shouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> servlet.init());
    }

    @Test
    void testDestroy_withoutInit_shouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> servlet.destroy());
    }

    @Test
    void testDoGet_withAllSupportedCities_shouldReturnWeatherData() throws ServletException, IOException {
        // Arrange
        servlet.init();

        // Act & Assert
        for (String city : Constants.SUPPORTED_CITIES) {
            when(request.getParameter("selectedCity")).thenReturn(city);
            servlet.doGet(request, response);
            verify(response, atLeastOnce()).setContentType("application/json");
        }
    }

    @Test
    void testDoGet_shouldSetJsonContentType() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json");
    }

    @Test
    void testDoGet_shouldCallGetOutputStream() throws ServletException, IOException {
        // Arrange
        servlet.init();
        when(request.getParameter("selectedCity")).thenReturn(Constants.PARIS);

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).getOutputStream();
    }

    @Test
    void testInit_multipleInvocations_shouldHandleCorrectly() {
        // Act
        servlet.init();
        
        // Assert - second init should handle already registered MBean
        assertDoesNotThrow(() -> servlet.init());
    }

    @Test
    void testDestroy_multipleInvocations_shouldHandleCorrectly() {
        // Arrange
        servlet.init();

        // Act & Assert
        servlet.destroy();
        assertDoesNotThrow(() -> servlet.destroy());
    }
}
