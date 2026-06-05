package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(reservation);
    }

    @Test
    void testParameterizedConstructor() {
        Reservation res = new Reservation("08/10/2024", "08/20/2024");
        
        assertEquals("08/10/2024", res.getFromDate());
        assertEquals("08/20/2024", res.getToDate());
    }

    @Test
    void testSetFromDate() {
        reservation.setFromDate("08/10/2024");
        
        assertEquals("08/10/2024", reservation.getFromDate());
    }

    @Test
    void testSetToDate() {
        reservation.setToDate("08/20/2024");
        
        assertEquals("08/20/2024", reservation.getToDate());
    }

    @Test
    void testGetFromDate_defaultValue() {
        assertNull(reservation.getFromDate());
    }

    @Test
    void testGetToDate_defaultValue() {
        assertNull(reservation.getToDate());
    }

    @Test
    void testSetFromDate_withNull() {
        reservation.setFromDate(null);
        
        assertNull(reservation.getFromDate());
    }

    @Test
    void testSetToDate_withNull() {
        reservation.setToDate(null);
        
        assertNull(reservation.getToDate());
    }

    @Test
    void testSetFromDate_withEmptyString() {
        reservation.setFromDate("");
        
        assertEquals("", reservation.getFromDate());
    }

    @Test
    void testSetToDate_withEmptyString() {
        reservation.setToDate("");
        
        assertEquals("", reservation.getToDate());
    }

    @Test
    void testParameterizedConstructor_withNullValues() {
        Reservation res = new Reservation(null, null);
        
        assertNull(res.getFromDate());
        assertNull(res.getToDate());
    }

    @Test
    void testSettersAndGetters_chainedCalls() {
        reservation.setFromDate("08/01/2024");
        reservation.setToDate("08/31/2024");
        
        assertEquals("08/01/2024", reservation.getFromDate());
        assertEquals("08/31/2024", reservation.getToDate());
    }

    @Test
    void testSetFromDate_withDifferentFormats() {
        reservation.setFromDate("2024-08-10");
        
        assertEquals("2024-08-10", reservation.getFromDate());
    }

    @Test
    void testSetToDate_withDifferentFormats() {
        reservation.setToDate("2024-08-20");
        
        assertEquals("2024-08-20", reservation.getToDate());
    }

    @Test
    void testSetFromDate_multipleInvocations() {
        reservation.setFromDate("08/10/2024");
        reservation.setFromDate("08/15/2024");
        
        assertEquals("08/15/2024", reservation.getFromDate());
    }

    @Test
    void testSetToDate_multipleInvocations() {
        reservation.setToDate("08/20/2024");
        reservation.setToDate("08/25/2024");
        
        assertEquals("08/25/2024", reservation.getToDate());
    }

    @Test
    void testParameterizedConstructor_withSameDate() {
        Reservation res = new Reservation("08/15/2024", "08/15/2024");
        
        assertEquals("08/15/2024", res.getFromDate());
        assertEquals("08/15/2024", res.getToDate());
    }

    @Test
    void testSetFromDate_withSpecialCharacters() {
        reservation.setFromDate("08/10/2024 12:00:00");
        
        assertEquals("08/10/2024 12:00:00", reservation.getFromDate());
    }

    @Test
    void testSetToDate_withSpecialCharacters() {
        reservation.setToDate("08/20/2024 23:59:59");
        
        assertEquals("08/20/2024 23:59:59", reservation.getToDate());
    }
}
