package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class DefaultWeatherDataTest {

    @Test
    void testConstructor_withValidCity_shouldCreateInstance() {
        // Act
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.PARIS);

        // Assert
        assertNotNull(weatherData);
        assertEquals(Constants.PARIS, weatherData.getCity());
    }

    @Test
    void testConstructor_withNullCity_shouldThrowException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData(null);
        });
    }

    @Test
    void testConstructor_withUnsupportedCity_shouldThrowException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("InvalidCity");
        });
    }

    @Test
    void testConstructor_withEmptyCity_shouldThrowException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            new DefaultWeatherData("");
        });
    }

    @Test
    void testGetCity_withParis_shouldReturnParis() {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.PARIS);

        // Act
        String city = weatherData.getCity();

        // Assert
        assertEquals(Constants.PARIS, city);
    }

    @Test
    void testGetCity_withLasVegas_shouldReturnLasVegas() {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.LAS_VEGAS);

        // Act
        String city = weatherData.getCity();

        // Assert
        assertEquals(Constants.LAS_VEGAS, city);
    }

    @Test
    void testGetCity_withSanFrancisco_shouldReturnSanFrancisco() {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.SAN_FRANCISCO);

        // Act
        String city = weatherData.getCity();

        // Assert
        assertEquals(Constants.SAN_FRANCISCO, city);
    }

    @Test
    void testGetCity_withMiami_shouldReturnMiami() {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.MIAMI);

        // Act
        String city = weatherData.getCity();

        // Assert
        assertEquals(Constants.MIAMI, city);
    }

    @Test
    void testGetCity_withCork_shouldReturnCork() {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.CORK);

        // Act
        String city = weatherData.getCity();

        // Assert
        assertEquals(Constants.CORK, city);
    }

    @Test
    void testGetCity_withBarcelona_shouldReturnBarcelona() {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.BARCELONA);

        // Act
        String city = weatherData.getCity();

        // Assert
        assertEquals(Constants.BARCELONA, city);
    }

    @Test
    void testGetDefaultWeatherData_withParis_shouldReturnData() throws IOException {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.PARIS);

        // Act
        String data = weatherData.getDefaultWeatherData();

        // Assert
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void testGetDefaultWeatherData_withLasVegas_shouldReturnData() throws IOException {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.LAS_VEGAS);

        // Act
        String data = weatherData.getDefaultWeatherData();

        // Assert
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void testGetDefaultWeatherData_withSanFrancisco_shouldReturnData() throws IOException {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.SAN_FRANCISCO);

        // Act
        String data = weatherData.getDefaultWeatherData();

        // Assert
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void testGetDefaultWeatherData_withMiami_shouldReturnData() throws IOException {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.MIAMI);

        // Act
        String data = weatherData.getDefaultWeatherData();

        // Assert
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void testGetDefaultWeatherData_withCork_shouldReturnData() throws IOException {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.CORK);

        // Act
        String data = weatherData.getDefaultWeatherData();

        // Assert
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void testGetDefaultWeatherData_withBarcelona_shouldReturnData() throws IOException {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.BARCELONA);

        // Act
        String data = weatherData.getDefaultWeatherData();

        // Assert
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void testConstructor_withAllSupportedCities_shouldSucceed() {
        // Act & Assert
        for (String city : Constants.SUPPORTED_CITIES) {
            DefaultWeatherData weatherData = new DefaultWeatherData(city);
            assertNotNull(weatherData);
            assertEquals(city, weatherData.getCity());
        }
    }

    @Test
    void testGetDefaultWeatherData_shouldReturnValidJsonFormat() throws IOException {
        // Arrange
        DefaultWeatherData weatherData = new DefaultWeatherData(Constants.PARIS);

        // Act
        String data = weatherData.getDefaultWeatherData();

        // Assert
        assertNotNull(data);
        assertTrue(data.contains("{") || data.contains("["));
    }
}
