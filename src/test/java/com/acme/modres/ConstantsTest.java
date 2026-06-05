package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void testBarcelonaConstant_shouldBeCorrectValue() {
        // Assert
        assertEquals("Barcelona", Constants.BARCELONA);
    }

    @Test
    void testCorkConstant_shouldBeCorrectValue() {
        // Assert
        assertEquals("Cork", Constants.CORK);
    }

    @Test
    void testMiamiConstant_shouldBeCorrectValue() {
        // Assert
        assertEquals("Miami", Constants.MIAMI);
    }

    @Test
    void testSanFranciscoConstant_shouldBeCorrectValue() {
        // Assert
        assertEquals("San_Francisco", Constants.SAN_FRANCISCO);
    }

    @Test
    void testParisConstant_shouldBeCorrectValue() {
        // Assert
        assertEquals("Paris", Constants.PARIS);
    }

    @Test
    void testLasVegasConstant_shouldBeCorrectValue() {
        // Assert
        assertEquals("Las_Vegas", Constants.LAS_VEGAS);
    }

    @Test
    void testSupportedCities_shouldContainAllCities() {
        // Assert
        assertNotNull(Constants.SUPPORTED_CITIES);
        assertEquals(6, Constants.SUPPORTED_CITIES.length);
        assertArrayEquals(
            new String[] { "Paris", "Las_Vegas", "San_Francisco", "Miami", "Cork", "Barcelona" },
            Constants.SUPPORTED_CITIES
        );
    }

    @Test
    void testSupportedCities_shouldContainParis() {
        // Assert
        boolean containsParis = false;
        for (String city : Constants.SUPPORTED_CITIES) {
            if (city.equals(Constants.PARIS)) {
                containsParis = true;
                break;
            }
        }
        assertTrue(containsParis);
    }

    @Test
    void testBarcelonaWeatherFile_shouldBeCorrectValue() {
        // Assert
        assertEquals("barcelona.json", Constants.BACELONA_WEATHER_FILE);
    }

    @Test
    void testCorkWeatherFile_shouldBeCorrectValue() {
        // Assert
        assertEquals("cork.json", Constants.CORK_WEATHER_FILE);
    }

    @Test
    void testLasVegasWeatherFile_shouldBeCorrectValue() {
        // Assert
        assertEquals("nv.json", Constants.LAS_VEGAS_WEATHER_FILE);
    }

    @Test
    void testMiamiWeatherFile_shouldBeCorrectValue() {
        // Assert
        assertEquals("miami.json", Constants.MIAMI_WEATHER_FILE);
    }

    @Test
    void testParisWeatherFile_shouldBeCorrectValue() {
        // Assert
        assertEquals("paris.json", Constants.PARIS_WEATHER_FILE);
    }

    @Test
    void testSanFranciscoWeatherFile_shouldBeCorrectValue() {
        // Assert
        assertEquals("sanfran.json", Constants.SAN_FRANCESCO_WEATHER_FILE);
    }

    @Test
    void testWundergroundApiPrefix_shouldBeCorrectValue() {
        // Assert
        assertEquals("http://api.wunderground.com/api/", Constants.WUNDERGROUND_API_PREFIX);
        assertTrue(Constants.WUNDERGROUND_API_PREFIX.startsWith("http://"));
    }

    @Test
    void testWundergroundApiPart_shouldBeCorrectValue() {
        // Assert
        assertEquals("/forecast/geolookup/conditions/q/", Constants.WUNDERGROUND_API_PART);
        assertTrue(Constants.WUNDERGROUND_API_PART.startsWith("/"));
    }

    @Test
    void testDataFormat_shouldBeCorrectValue() {
        // Assert
        assertEquals("MM/dd/yyyy", Constants.DATA_FORMAT);
    }

    @Test
    void testDataFormat_shouldBeValidDatePattern() {
        // Assert
        assertNotNull(Constants.DATA_FORMAT);
        assertTrue(Constants.DATA_FORMAT.contains("MM"));
        assertTrue(Constants.DATA_FORMAT.contains("dd"));
        assertTrue(Constants.DATA_FORMAT.contains("yyyy"));
    }

    @Test
    void testAllWeatherFiles_shouldHaveJsonExtension() {
        // Assert
        assertTrue(Constants.BACELONA_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.CORK_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.LAS_VEGAS_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.MIAMI_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.PARIS_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.SAN_FRANCESCO_WEATHER_FILE.endsWith(".json"));
    }

    @Test
    void testSupportedCities_shouldNotBeEmpty() {
        // Assert
        assertNotNull(Constants.SUPPORTED_CITIES);
        assertTrue(Constants.SUPPORTED_CITIES.length > 0);
    }

    @Test
    void testSupportedCities_shouldNotContainNullValues() {
        // Assert
        for (String city : Constants.SUPPORTED_CITIES) {
            assertNotNull(city);
            assertFalse(city.isEmpty());
        }
    }
}
