package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DateCheckerTest {

    @Mock
    private ReservationCheckerData data;

    @Mock
    private ReservationList reservationList;

    private List<Reservation> reservations;

    @BeforeEach
    void setUp() {
        reservations = new ArrayList<>();
        when(data.getReservationList()).thenReturn(reservationList);
        when(reservationList.getReservations()).thenReturn(reservations);
    }

    @Test
    void testConstructor_shouldInitializeWithData() {
        // Act
        DateChecker dateChecker = new DateChecker(data);

        // Assert
        assertNotNull(dateChecker);
    }

    @Test
    void testRun_withNoReservations_shouldSetAvailableTrue() {
        // Arrange
        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(true);
    }

    @Test
    void testRun_withDateOutsideReservation_shouldSetAvailableTrue() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("01/10/2024");
        reservation.setToDate("01/20/2024");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 25));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(true);
    }

    @Test
    void testRun_withDateInsideReservation_shouldSetAvailableFalse() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("01/10/2024");
        reservation.setToDate("01/20/2024");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(false);
    }

    @Test
    void testRun_withDateBeforeReservation_shouldSetAvailableTrue() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("01/10/2024");
        reservation.setToDate("01/20/2024");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 5));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(true);
    }

    @Test
    void testRun_withDateOnFromDate_shouldSetAvailableTrue() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("01/10/2024");
        reservation.setToDate("01/20/2024");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 10));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(true);
    }

    @Test
    void testRun_withDateOnToDate_shouldSetAvailableTrue() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("01/10/2024");
        reservation.setToDate("01/20/2024");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 20));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(true);
    }

    @Test
    void testRun_withMultipleReservations_firstConflicts_shouldSetAvailableFalse() {
        // Arrange
        Reservation reservation1 = new Reservation();
        reservation1.setFromDate("01/10/2024");
        reservation1.setToDate("01/20/2024");
        
        Reservation reservation2 = new Reservation();
        reservation2.setFromDate("02/10/2024");
        reservation2.setToDate("02/20/2024");
        
        reservations.add(reservation1);
        reservations.add(reservation2);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(false);
    }

    @Test
    void testRun_withMultipleReservations_secondConflicts_shouldSetAvailableFalse() {
        // Arrange
        Reservation reservation1 = new Reservation();
        reservation1.setFromDate("01/10/2024");
        reservation1.setToDate("01/20/2024");
        
        Reservation reservation2 = new Reservation();
        reservation2.setFromDate("02/10/2024");
        reservation2.setToDate("02/20/2024");
        
        reservations.add(reservation1);
        reservations.add(reservation2);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 2, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(false);
    }

    @Test
    void testRun_withMultipleReservations_noConflicts_shouldSetAvailableTrue() {
        // Arrange
        Reservation reservation1 = new Reservation();
        reservation1.setFromDate("01/10/2024");
        reservation1.setToDate("01/20/2024");
        
        Reservation reservation2 = new Reservation();
        reservation2.setFromDate("02/10/2024");
        reservation2.setToDate("02/20/2024");
        
        reservations.add(reservation1);
        reservations.add(reservation2);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 3, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(true);
    }

    @Test
    void testRun_withInvalidDateFormat_shouldHandleException() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("invalid-date");
        reservation.setToDate("01/20/2024");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> dateChecker.run());
    }

    @Test
    void testRun_asRunnable_shouldExecuteCorrectly() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("01/10/2024");
        reservation.setToDate("01/20/2024");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));
        Runnable dateChecker = new DateChecker(data);

        // Act
        dateChecker.run();

        // Assert
        verify(data).setAvailablility(false);
    }

    @Test
    void testRun_withEmptyReservationDates_shouldHandleGracefully() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate("");
        reservation.setToDate("");
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act & Assert
        assertDoesNotThrow(() -> dateChecker.run());
    }

    @Test
    void testRun_withNullReservationDates_shouldHandleGracefully() {
        // Arrange
        Reservation reservation = new Reservation();
        reservation.setFromDate(null);
        reservation.setToDate(null);
        reservations.add(reservation);

        when(data.getSelectedLocalDate()).thenReturn(LocalDate.of(2024, 1, 15));
        DateChecker dateChecker = new DateChecker(data);

        // Act & Assert
        assertDoesNotThrow(() -> dateChecker.run());
    }
}
