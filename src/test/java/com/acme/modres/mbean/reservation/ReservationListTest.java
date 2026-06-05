package com.acme.modres.mbean.reservation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationListTest {

    private ReservationList reservationList;

    @BeforeEach
    void setUp() {
        reservationList = new ReservationList();
    }

    @Test
    void testDefaultConstructor_shouldCreateEmptyList() {
        // Assert
        assertNotNull(reservationList);
        assertNotNull(reservationList.getReservations());
        assertTrue(reservationList.getReservations().isEmpty());
    }

    @Test
    void testParameterizedConstructor_shouldSetReservations() {
        // Arrange
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("01/10/2024", "01/20/2024"));
        reservations.add(new Reservation("02/10/2024", "02/20/2024"));

        // Act
        ReservationList list = new ReservationList(reservations);

        // Assert
        assertNotNull(list.getReservations());
        assertEquals(2, list.getReservations().size());
        assertEquals(reservations, list.getReservations());
    }

    @Test
    void testAdd_withSingleReservation_shouldAddToList() {
        // Arrange
        Reservation reservation = new Reservation("01/10/2024", "01/20/2024");

        // Act
        reservationList.add(reservation);

        // Assert
        assertEquals(1, reservationList.getReservations().size());
        assertEquals(reservation, reservationList.getReservations().get(0));
    }

    @Test
    void testAdd_withMultipleReservations_shouldAddAllToList() {
        // Arrange
        Reservation reservation1 = new Reservation("01/10/2024", "01/20/2024");
        Reservation reservation2 = new Reservation("02/10/2024", "02/20/2024");
        Reservation reservation3 = new Reservation("03/10/2024", "03/20/2024");

        // Act
        reservationList.add(reservation1);
        reservationList.add(reservation2);
        reservationList.add(reservation3);

        // Assert
        assertEquals(3, reservationList.getReservations().size());
        assertEquals(reservation1, reservationList.getReservations().get(0));
        assertEquals(reservation2, reservationList.getReservations().get(1));
        assertEquals(reservation3, reservationList.getReservations().get(2));
    }

    @Test
    void testGetReservations_shouldReturnList() {
        // Act
        List<Reservation> result = reservationList.getReservations();

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof List);
    }

    @Test
    void testAdd_withNullReservation_shouldAddNull() {
        // Act
        reservationList.add(null);

        // Assert
        assertEquals(1, reservationList.getReservations().size());
        assertNull(reservationList.getReservations().get(0));
    }

    @Test
    void testParameterizedConstructor_withEmptyList_shouldSetEmptyList() {
        // Arrange
        List<Reservation> emptyList = new ArrayList<>();

        // Act
        ReservationList list = new ReservationList(emptyList);

        // Assert
        assertTrue(list.getReservations().isEmpty());
    }

    @Test
    void testParameterizedConstructor_withNullList_shouldSetNullList() {
        // Act
        ReservationList list = new ReservationList(null);

        // Assert
        assertNull(list.getReservations());
    }

    @Test
    void testAdd_shouldPreserveOrder() {
        // Arrange
        Reservation reservation1 = new Reservation("01/10/2024", "01/20/2024");
        Reservation reservation2 = new Reservation("02/10/2024", "02/20/2024");
        Reservation reservation3 = new Reservation("03/10/2024", "03/20/2024");

        // Act
        reservationList.add(reservation1);
        reservationList.add(reservation2);
        reservationList.add(reservation3);

        // Assert
        assertEquals("01/10/2024", reservationList.getReservations().get(0).getFromDate());
        assertEquals("02/10/2024", reservationList.getReservations().get(1).getFromDate());
        assertEquals("03/10/2024", reservationList.getReservations().get(2).getFromDate());
    }

    @Test
    void testAdd_withLargeNumberOfReservations_shouldHandleCorrectly() {
        // Arrange
        int count = 1000;

        // Act
        for (int i = 0; i < count; i++) {
            reservationList.add(new Reservation("01/10/2024", "01/20/2024"));
        }

        // Assert
        assertEquals(count, reservationList.getReservations().size());
    }

    @Test
    void testGetReservations_shouldReturnMutableList() {
        // Arrange
        reservationList.add(new Reservation("01/10/2024", "01/20/2024"));

        // Act
        List<Reservation> list = reservationList.getReservations();
        list.add(new Reservation("02/10/2024", "02/20/2024"));

        // Assert
        assertEquals(2, reservationList.getReservations().size());
    }

    @Test
    void testAdd_withSameReservationMultipleTimes_shouldAddMultipleTimes() {
        // Arrange
        Reservation reservation = new Reservation("01/10/2024", "01/20/2024");

        // Act
        reservationList.add(reservation);
        reservationList.add(reservation);
        reservationList.add(reservation);

        // Assert
        assertEquals(3, reservationList.getReservations().size());
    }

    @Test
    void testParameterizedConstructor_withMultipleReservations_shouldSetAll() {
        // Arrange
        List<Reservation> reservations = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            reservations.add(new Reservation("0" + i + "/10/2024", "0" + i + "/20/2024"));
        }

        // Act
        ReservationList list = new ReservationList(reservations);

        // Assert
        assertEquals(5, list.getReservations().size());
    }

    @Test
    void testAdd_withReservationsHavingNullDates_shouldAddCorrectly() {
        // Arrange
        Reservation reservation = new Reservation(null, null);

        // Act
        reservationList.add(reservation);

        // Assert
        assertEquals(1, reservationList.getReservations().size());
        assertNull(reservationList.getReservations().get(0).getFromDate());
        assertNull(reservationList.getReservations().get(0).getToDate());
    }

    @Test
    void testAdd_withReservationsHavingEmptyDates_shouldAddCorrectly() {
        // Arrange
        Reservation reservation = new Reservation("", "");

        // Act
        reservationList.add(reservation);

        // Assert
        assertEquals(1, reservationList.getReservations().size());
        assertEquals("", reservationList.getReservations().get(0).getFromDate());
        assertEquals("", reservationList.getReservations().get(0).getToDate());
    }

    @Test
    void testGetReservations_afterMultipleAdds_shouldReturnAllReservations() {
        // Arrange
        for (int i = 1; i <= 10; i++) {
            reservationList.add(new Reservation("01/" + i + "/2024", "01/" + (i + 10) + "/2024"));
        }

        // Act
        List<Reservation> result = reservationList.getReservations();

        // Assert
        assertEquals(10, result.size());
    }

    @Test
    void testParameterizedConstructor_shouldNotModifyOriginalList() {
        // Arrange
        List<Reservation> originalList = new ArrayList<>();
        originalList.add(new Reservation("01/10/2024", "01/20/2024"));

        // Act
        ReservationList list = new ReservationList(originalList);
        list.add(new Reservation("02/10/2024", "02/20/2024"));

        // Assert
        assertEquals(2, list.getReservations().size());
        assertEquals(2, originalList.size()); // Original list is also modified (same reference)
    }
}
