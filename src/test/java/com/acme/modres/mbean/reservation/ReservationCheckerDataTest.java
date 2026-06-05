package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationCheckerDataTest {

    private ReservationCheckerData checkerData;
    private ReservationList reservationList;

    @BeforeEach
    void setUp() {
        reservationList = new ReservationList();
        reservationList.add(new Reservation("08/10/2024", "08/20/2024"));
        checkerData = new ReservationCheckerData(reservationList);
    }

    @Test
    void testConstructor() {
        assertNotNull(checkerData);
    }

    @Test
    void testConstructor_withValidReservationList() {
        ReservationList list = new ReservationList();
        ReservationCheckerData data = new ReservationCheckerData(list);
        
        assertNotNull(data);
        assertEquals(list, data.getReservationList());
    }

    @Test
    void testGetReservationList() {
        ReservationList result = checkerData.getReservationList();
        
        assertNotNull(result);
        assertEquals(reservationList, result);
    }

    @Test
    void testGetSelectedDate_defaultValue() {
        assertNull(checkerData.getSelectedDate());
    }

    @Test
    void testSetSelectedDate_withValidDate() {
        boolean result = checkerData.setSelectedDate("08/15/2024");
        
        assertTrue(result);
        assertNotNull(checkerData.getSelectedDate());
    }

    @Test
    void testSetSelectedDate_withInvalidDate() {
        boolean result = checkerData.setSelectedDate("invalid-date");
        
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withNull() {
        boolean result = checkerData.setSelectedDate(null);
        
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withEmptyString() {
        boolean result = checkerData.setSelectedDate("");
        
        assertFalse(result);
    }

    @Test
    void testIsAvailible_defaultValue() {
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_toFalse() {
        checkerData.setAvailablility(false);
        
        assertFalse(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_toTrue() {
        checkerData.setAvailablility(true);
        
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetAvailablility_multipleInvocations() {
        checkerData.setAvailablility(false);
        assertFalse(checkerData.isAvailible());
        
        checkerData.setAvailablility(true);
        assertTrue(checkerData.isAvailible());
    }

    @Test
    void testSetSelectedDate_withDifferentFormats() {
        boolean result1 = checkerData.setSelectedDate("08/15/2024");
        assertTrue(result1);
        
        boolean result2 = checkerData.setSelectedDate("12/31/2024");
        assertTrue(result2);
    }

    @Test
    void testGetSelectedDate_afterSet() {
        checkerData.setSelectedDate("08/15/2024");
        Date selectedDate = checkerData.getSelectedDate();
        
        assertNotNull(selectedDate);
    }

    @Test
    void testConstructor_withNullReservationList() {
        ReservationCheckerData data = new ReservationCheckerData(null);
        
        assertNotNull(data);
        assertNull(data.getReservationList());
    }

    @Test
    void testSetSelectedDate_withLeapYearDate() {
        boolean result = checkerData.setSelectedDate("02/29/2024");
        
        assertTrue(result);
    }

    @Test
    void testSetSelectedDate_withInvalidLeapYearDate() {
        boolean result = checkerData.setSelectedDate("02/29/2023");
        
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withBoundaryDates() {
        assertTrue(checkerData.setSelectedDate("01/01/2024"));
        assertTrue(checkerData.setSelectedDate("12/31/2024"));
    }

    @Test
    void testSetSelectedDate_withInvalidMonth() {
        boolean result = checkerData.setSelectedDate("13/01/2024");
        
        assertFalse(result);
    }

    @Test
    void testSetSelectedDate_withInvalidDay() {
        boolean result = checkerData.setSelectedDate("08/32/2024");
        
        assertFalse(result);
    }

    @Test
    void testIsAvailible_afterMultipleChanges() {
        checkerData.setAvailablility(false);
        checkerData.setAvailablility(true);
        checkerData.setAvailablility(false);
        
        assertFalse(checkerData.isAvailible());
    }
}
