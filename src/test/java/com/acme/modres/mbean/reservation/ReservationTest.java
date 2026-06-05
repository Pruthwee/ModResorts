package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void testDefaultConstructor_shouldCreateInstance() {
        // Act
        Reservation reservation = new Reservation();

        // Assert
        assertNotNull(reservation);
    }

    @Test
    void testParameterizedConstructor_shouldSetBothDates() {
        // Arrange
        String fromDate = "01/10/2024";
        String toDate = "01/20/2024";

        // Act
        Reservation reservation = new Reservation(fromDate, toDate);

        // Assert
        assertEquals(fromDate, reservation.getFromDate());
        assertEquals(toDate, reservation.getToDate());
    }

    @Test
    void testSetFromDate_shouldSetFromDate() {
        // Arrange
        Reservation reservation = new Reservation();
        String fromDate = "01/10/2024";

        // Act
        reservation.setFromDate(fromDate);

        // Assert
        assertEquals(fromDate, reservation.getFromDate());
    }

    @Test
    void testSetToDate_shouldSetToDate() {
        // Arrange
        Reservation reservation = new Reservation();
        String toDate = "01/20/2024";

        // Act
        reservation.setToDate(toDate);

        // Assert
        assertEquals(toDate, reservation.getToDate());
    }

    @Test
    void testGetFromDate_withNullValue_shouldReturnNull() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        String result = reservation.getFromDate();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetToDate_withNullValue_shouldReturnNull() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        String result = reservation.getToDate();

        // Assert
        assertNull(result);
    }

    @Test
    void testSetFromDate_withNullValue_shouldSetNull() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        reservation.setFromDate(null);

        // Assert
        assertNull(reservation.getFromDate());
    }

    @Test
    void testSetToDate_withNullValue_shouldSetNull() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        reservation.setToDate(null);

        // Assert
        assertNull(reservation.getToDate());
    }

    @Test
    void testSetFromDate_withEmptyString_shouldSetEmptyString() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        reservation.setFromDate("");

        // Assert
        assertEquals("", reservation.getFromDate());
    }

    @Test
    void testSetToDate_withEmptyString_shouldSetEmptyString() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        reservation.setToDate("");

        // Assert
        assertEquals("", reservation.getToDate());
    }

    @Test
    void testParameterizedConstructor_withNullValues_shouldSetNullValues() {
        // Act
        Reservation reservation = new Reservation(null, null);

        // Assert
        assertNull(reservation.getFromDate());
        assertNull(reservation.getToDate());
    }

    @Test
    void testParameterizedConstructor_withEmptyStrings_shouldSetEmptyStrings() {
        // Act
        Reservation reservation = new Reservation("", "");

        // Assert
        assertEquals("", reservation.getFromDate());
        assertEquals("", reservation.getToDate());
    }

    @Test
    void testSetFromDate_multipleTimes_shouldUpdateValue() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        reservation.setFromDate("01/10/2024");
        reservation.setFromDate("02/10/2024");
        reservation.setFromDate("03/10/2024");

        // Assert
        assertEquals("03/10/2024", reservation.getFromDate());
    }

    @Test
    void testSetToDate_multipleTimes_shouldUpdateValue() {
        // Arrange
        Reservation reservation = new Reservation();

        // Act
        reservation.setToDate("01/20/2024");
        reservation.setToDate("02/20/2024");
        reservation.setToDate("03/20/2024");

        // Assert
        assertEquals("03/20/2024", reservation.getToDate());
    }

    @Test
    void testSetFromDate_withDifferentFormats_shouldAcceptAnyString() {
        // Arrange
        Reservation reservation = new Reservation();
        String[] formats = {
            "01/10/2024",
            "2024-01-10",
            "10-Jan-2024",
            "January 10, 2024"
        };

        // Act & Assert
        for (String format : formats) {
            reservation.setFromDate(format);
            assertEquals(format, reservation.getFromDate());
        }
    }

    @Test
    void testSetToDate_withDifferentFormats_shouldAcceptAnyString() {
        // Arrange
        Reservation reservation = new Reservation();
        String[] formats = {
            "01/20/2024",
            "2024-01-20",
            "20-Jan-2024",
            "January 20, 2024"
        };

        // Act & Assert
        for (String format : formats) {
            reservation.setToDate(format);
            assertEquals(format, reservation.getToDate());
        }
    }

    @Test
    void testParameterizedConstructor_withDifferentDates_shouldSetCorrectly() {
        // Arrange
        String fromDate = "12/25/2023";
        String toDate = "01/05/2024";

        // Act
        Reservation reservation = new Reservation(fromDate, toDate);

        // Assert
        assertEquals(fromDate, reservation.getFromDate());
        assertEquals(toDate, reservation.getToDate());
    }

    @Test
    void testSetFromDate_withLongString_shouldSetCorrectly() {
        // Arrange
        Reservation reservation = new Reservation();
        String longDate = "A".repeat(1000);

        // Act
        reservation.setFromDate(longDate);

        // Assert
        assertEquals(longDate, reservation.getFromDate());
    }

    @Test
    void testSetToDate_withLongString_shouldSetCorrectly() {
        // Arrange
        Reservation reservation = new Reservation();
        String longDate = "B".repeat(1000);

        // Act
        reservation.setToDate(longDate);

        // Assert
        assertEquals(longDate, reservation.getToDate());
    }

    @Test
    void testSetFromDate_withSpecialCharacters_shouldSetCorrectly() {
        // Arrange
        Reservation reservation = new Reservation();
        String specialDate = "01/10/2024 @#$%";

        // Act
        reservation.setFromDate(specialDate);

        // Assert
        assertEquals(specialDate, reservation.getFromDate());
    }

    @Test
    void testSetToDate_withSpecialCharacters_shouldSetCorrectly() {
        // Arrange
        Reservation reservation = new Reservation();
        String specialDate = "01/20/2024 @#$%";

        // Act
        reservation.setToDate(specialDate);

        // Assert
        assertEquals(specialDate, reservation.getToDate());
    }
}
