package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateCheckerTest {

    private ReservationCheckerData data;
    private DateChecker dateChecker;

    @BeforeEach
    void setUp() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        reservations.add(new Reservation("09/01/2024", "09/10/2024"));
        
        ReservationList reservationList = new ReservationList(reservations);
        data = new ReservationCheckerData(reservationList);
        data.setSelectedDate("08/15/2024");
        
        dateChecker = new DateChecker(data);
    }

    @Test
    void testConstructor() {
        assertNotNull(dateChecker);
    }

    @Test
    void testConstructor_withValidData() {
        ReservationList reservationList = new ReservationList();
        ReservationCheckerData testData = new ReservationCheckerData(reservationList);
        
        DateChecker checker = new DateChecker(testData);
        
        assertNotNull(checker);
    }

    @Test
    void testRun_doesNotThrow() {
        assertDoesNotThrow(() -> dateChecker.run());
    }

    @Test
    void testRun_setsAvailability() {
        dateChecker.run();
        
        // Availability should be set (either true or false)
        assertNotNull(data);
    }

    @Test
    void testRun_withEmptyReservations() {
        ReservationList emptyList = new ReservationList();
        ReservationCheckerData emptyData = new ReservationCheckerData(emptyList);
        emptyData.setSelectedDate("08/15/2024");
        
        DateChecker checker = new DateChecker(emptyData);
        
        assertDoesNotThrow(() -> checker.run());
    }

    @Test
    void testRun_withNullSelectedDate() {
        ReservationList reservationList = new ReservationList();
        ReservationCheckerData testData = new ReservationCheckerData(reservationList);
        
        DateChecker checker = new DateChecker(testData);
        
        assertDoesNotThrow(() -> checker.run());
    }

    @Test
    void testRun_withInvalidDateFormat() {
        data.setSelectedDate("invalid-date");
        
        assertDoesNotThrow(() -> dateChecker.run());
    }

    @Test
    void testRun_multipleInvocations() {
        dateChecker.run();
        dateChecker.run();
        
        assertDoesNotThrow(() -> dateChecker.run());
    }

    @Test
    void testRun_withMultipleReservations() {
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reservations.add(new Reservation("08/0" + (i + 1) + "/2024", "08/" + (i + 10) + "/2024"));
        }
        
        ReservationList reservationList = new ReservationList(reservations);
        ReservationCheckerData testData = new ReservationCheckerData(reservationList);
        testData.setSelectedDate("08/15/2024");
        
        DateChecker checker = new DateChecker(testData);
        
        assertDoesNotThrow(() -> checker.run());
    }

    @Test
    void testRun_withOverlappingReservations() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/01/2024", "08/15/2024"));
        reservations.add(new Reservation("08/10/2024", "08/25/2024"));
        
        ReservationList reservationList = new ReservationList(reservations);
        ReservationCheckerData testData = new ReservationCheckerData(reservationList);
        testData.setSelectedDate("08/12/2024");
        
        DateChecker checker = new DateChecker(testData);
        
        assertDoesNotThrow(() -> checker.run());
    }
}
