package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationCheckerDataTest {

    @Mock
    private ReservationList reservationList;

    private ReservationCheckerData checkerData;

    @BeforeEach
    void setUp() {
        checkerData = new ReservationCheckerData(reservationList);
    }

    @Test
    void testConstructor_shouldInitializeWithReservationList() {
        // Assert
        assertNotNull(checkerData);
        assertEquals(reservationList, checkerData.getReservationList());
    }

    @Test
    void testConstructor_shouldSetAvailableToTrue() {
        // Assert
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testGetReservationList_shouldReturnReservationList() {
        // Act
        ReservationList result = checkerData.getReservationList();

        // Assert
        assertEquals(reservationList, result);
    }

    @Test
    void testSetSelectedDate_withValidDate_shouldReturnTrue() {
        // Act
        boolean result = checkerData.setSelectedDate("01/15/2024");

        // Assert
        assertTrue(result);
        assertNotNull(checkerData.getSelectedDate());
        assertNotNull(checkerData.getSelectedLocalDate());
    }

    @Test
    void testSetSelectedDate_withInvalidDate_shouldReturnFalse() {
        // Act
        boolean result = checkerData.setSelectedDate("invalid-date");

        // Assert
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withNullDate_shouldReturnFalse() {
        // Act
        boolean result = checkerData.setSelectedDate(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withEmptyDate_shouldReturnFalse() {
        // Act
        boolean result = checkerData.setSelectedDate("");

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetSelectedDate_afterSettingValidDate_shouldReturnDate() {
        // Arrange
        checkerData.setSelectedDate("01/15/2024");

        // Act
        Date result = checkerData.getSelectedDate();

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetSelectedLocalDate_afterSettingValidDate_shouldReturnLocalDate() {
        // Arrange
        checkerData.setSelectedDate("01/15/2024");

        // Act
        LocalDate result = checkerData.getSelectedLocalDate();

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 1, 15), result);
    }

    @Test
    void testGetSelectedDate_beforeSettingDate_shouldReturnNull() {
        // Act
        Date result = checkerData.getSelectedDate();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetSelectedLocalDate_beforeSettingDate_shouldReturnNull() {
        // Act
        LocalDate result = checkerData.getSelectedLocalDate();

        // Assert
        assertNull(result);
    }

    @Test
    void testIsAvailible_initialValue_shouldBeTrue() {
        // Act
        boolean result = checkerData.isAvailible();

        // Assert
        assertTrue(result);
    }

    @Test
    void testSetAvailablility_withTrue_shouldSetTrue() {
        // Act
        checkerData.setAvailablility(true);

        // Assert
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_withFalse_shouldSetFalse() {
        // Act
        checkerData.setAvailablility(false);

        // Assert
        assertFalse(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_multipleTimes_shouldUpdateCorrectly() {
        // Act
        checkerData.setAvailablility(false);
        checkerData.setAvailablility(true);
        checkerData.setAvailablility(false);

        // Assert
        assertFalse(checkerData.isAvailible());
    }

    @Test
    void testSetSelectedDate_withDifferentValidDates_shouldUpdateCorrectly() {
        // Act
        boolean result1 = checkerData.setSelectedDate("01/15/2024");
        LocalDate date1 = checkerData.getSelectedLocalDate();
        
        boolean result2 = checkerData.setSelectedDate("02/20/2024");
        LocalDate date2 = checkerData.getSelectedLocalDate();

        // Assert
        assertTrue(result1);
        assertTrue(result2);
        assertEquals(LocalDate.of(2024, 2, 20), date2);
        assertNotEquals(date1, date2);
    }

    @Test
    void testSetSelectedDate_withLeapYearDate_shouldHandleCorrectly() {
        // Act
        boolean result = checkerData.setSelectedDate("02/29/2024");

        // Assert
        assertTrue(result);
        assertEquals(LocalDate.of(2024, 2, 29), checkerData.getSelectedLocalDate());
    }

    @Test
    void testSetSelectedDate_withInvalidLeapYearDate_shouldReturnFalse() {
        // Act
        boolean result = checkerData.setSelectedDate("02/29/2023");

        // Assert
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withEndOfMonthDates_shouldHandleCorrectly() {
        // Arrange
        String[] dates = {"01/31/2024", "03/31/2024", "12/31/2024"};

        // Act & Assert
        for (String date : dates) {
            boolean result = checkerData.setSelectedDate(date);
            assertTrue(result, "Failed for date: " + date);
        }
    }

    @Test
    void testSetSelectedDate_withInvalidMonthDay_shouldReturnFalse() {
        // Act
        boolean result = checkerData.setSelectedDate("13/32/2024");

        // Assert
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withWrongFormat_shouldReturnFalse() {
        // Act
        boolean result = checkerData.setSelectedDate("2024-01-15");

        // Assert
        assertFalse(result);
    }

    @Test
    void testConstructor_withNullReservationList_shouldAcceptNull() {
        // Act
        ReservationCheckerData data = new ReservationCheckerData(null);

        // Assert
        assertNotNull(data);
        assertNull(data.getReservationList());
    }

    @Test
    void testSetSelectedDate_withPastDate_shouldHandleCorrectly() {
        // Act
        boolean result = checkerData.setSelectedDate("01/01/2000");

        // Assert
        assertTrue(result);
        assertEquals(LocalDate.of(2000, 1, 1), checkerData.getSelectedLocalDate());
    }

    @Test
    void testSetSelectedDate_withFutureDate_shouldHandleCorrectly() {
        // Act
        boolean result = checkerData.setSelectedDate("12/31/2099");

        // Assert
        assertTrue(result);
        assertEquals(LocalDate.of(2099, 12, 31), checkerData.getSelectedLocalDate());
    }

    @Test
    void testGetSelectedDate_shouldReturnSqlDate() {
        // Arrange
        checkerData.setSelectedDate("01/15/2024");

        // Act
        Date result = checkerData.getSelectedDate();

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof java.sql.Date);
    }
}
