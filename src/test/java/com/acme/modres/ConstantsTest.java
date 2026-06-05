package com.acme.modres;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void testBarcelonaConstant() {
        assertEquals("Barcelona", Constants.BARCELONA);
    }

    @Test
    void testCorkConstant() {
        assertEquals("Cork", Constants.CORK);
    }

    @Test
    void testMiamiConstant() {
        assertEquals("Miami", Constants.MIAMI);
    }

    @Test
    void testSanFranciscoConstant() {
        assertEquals("San_Francisco", Constants.SAN_FRANCISCO);
    }

    @Test
    void testParisConstant() {
        assertEquals("Paris", Constants.PARIS);
    }

    @Test
    void testLasVegasConstant() {
        assertEquals("Las_Vegas", Constants.LAS_VEGAS);
    }

    @Test
    void testSupportedCitiesArray() {
        assertNotNull(Constants.SUPPORTED_CITIES);
        assertEquals(6, Constants.SUPPORTED_CITIES.length);
    }

    @Test
    void testSupportedCitiesContainsParis() {
        boolean containsParis = false;
        for (String city : Constants.SUPPORTED_CITIES) {
            if (Constants.PARIS.equals(city)) {
                containsParis = true;
                break;
            }
        }
        assertTrue(containsParis);
    }

    @Test
    void testSupportedCitiesContainsLasVegas() {
        boolean containsLasVegas = false;
        for (String city : Constants.SUPPORTED_CITIES) {
            if (Constants.LAS_VEGAS.equals(city)) {
                containsLasVegas = true;
                break;
            }
        }
        assertTrue(containsLasVegas);
    }

    @Test
    void testBarcelonaWeatherFile() {
        assertEquals("barcelona.json", Constants.BACELONA_WEATHER_FILE);
    }

    @Test
    void testCorkWeatherFile() {
        assertEquals("cork.json", Constants.CORK_WEATHER_FILE);
    }

    @Test
    void testLasVegasWeatherFile() {
        assertEquals("nv.json", Constants.LAS_VEGAS_WEATHER_FILE);
    }

    @Test
    void testMiamiWeatherFile() {
        assertEquals("miami.json", Constants.MIAMI_WEATHER_FILE);
    }

    @Test
    void testParisWeatherFile() {
        assertEquals("paris.json", Constants.PARIS_WEATHER_FILE);
    }

    @Test
    void testSanFranciscoWeatherFile() {
        assertEquals("sanfran.json", Constants.SAN_FRANCESCO_WEATHER_FILE);
    }

    @Test
    void testWundergroundApiPrefix() {
        assertEquals("http://api.wunderground.com/api/", Constants.WUNDERGROUND_API_PREFIX);
        assertTrue(Constants.WUNDERGROUND_API_PREFIX.startsWith("http://"));
    }

    @Test
    void testWundergroundApiPart() {
        assertEquals("/forecast/geolookup/conditions/q/", Constants.WUNDERGROUND_API_PART);
        assertTrue(Constants.WUNDERGROUND_API_PART.startsWith("/"));
    }

    @Test
    void testDataFormat() {
        assertEquals("MM/dd/yyyy", Constants.DATA_FORMAT);
    }

    @Test
    void testDataFormatPattern() {
        String format = Constants.DATA_FORMAT;
        assertTrue(format.contains("MM"));
        assertTrue(format.contains("dd"));
        assertTrue(format.contains("yyyy"));
    }

    @Test
    void testAllWeatherFilesHaveJsonExtension() {
        assertTrue(Constants.BACELONA_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.CORK_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.LAS_VEGAS_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.MIAMI_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.PARIS_WEATHER_FILE.endsWith(".json"));
        assertTrue(Constants.SAN_FRANCESCO_WEATHER_FILE.endsWith(".json"));
    }
}
