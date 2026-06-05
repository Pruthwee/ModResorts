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
    void testDefaultConstructor() {
        assertNotNull(reservationList);
        assertNotNull(reservationList.getReservations());
    }

    @Test
    void testParameterizedConstructor() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        
        ReservationList list = new ReservationList(reservations);
        
        assertNotNull(list);
        assertEquals(1, list.getReservations().size());
    }

    @Test
    void testGetReservations_initiallyEmpty() {
        List<Reservation> reservations = reservationList.getReservations();
        
        assertNotNull(reservations);
        assertTrue(reservations.isEmpty());
    }

    @Test
    void testAdd_singleReservation() {
        Reservation reservation = new Reservation("08/10/2024", "08/20/2024");
        reservationList.add(reservation);
        
        assertEquals(1, reservationList.getReservations().size());
        assertEquals(reservation, reservationList.getReservations().get(0));
    }

    @Test
    void testAdd_multipleReservations() {
        Reservation res1 = new Reservation("08/10/2024", "08/20/2024");
        Reservation res2 = new Reservation("09/01/2024", "09/10/2024");
        Reservation res3 = new Reservation("10/01/2024", "10/10/2024");
        
        reservationList.add(res1);
        reservationList.add(res2);
        reservationList.add(res3);
        
        assertEquals(3, reservationList.getReservations().size());
    }

    @Test
    void testAdd_preservesOrder() {
        Reservation res1 = new Reservation("08/10/2024", "08/20/2024");
        Reservation res2 = new Reservation("09/01/2024", "09/10/2024");
        Reservation res3 = new Reservation("10/01/2024", "10/10/2024");
        
        reservationList.add(res1);
        reservationList.add(res2);
        reservationList.add(res3);
        
        List<Reservation> reservations = reservationList.getReservations();
        assertEquals(res1, reservations.get(0));
        assertEquals(res2, reservations.get(1));
        assertEquals(res3, reservations.get(2));
    }

    @Test
    void testParameterizedConstructor_withEmptyList() {
        List<Reservation> emptyList = new ArrayList<>();
        ReservationList list = new ReservationList(emptyList);
        
        assertTrue(list.getReservations().isEmpty());
    }

    @Test
    void testParameterizedConstructor_withMultipleReservations() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("08/10/2024", "08/20/2024"));
        reservations.add(new Reservation("09/01/2024", "09/10/2024"));
        
        ReservationList list = new ReservationList(reservations);
        
        assertEquals(2, list.getReservations().size());
    }

    @Test
    void testAdd_withNullReservation() {
        reservationList.add(null);
        
        assertEquals(1, reservationList.getReservations().size());
        assertNull(reservationList.getReservations().get(0));
    }

    @Test
    void testGetReservations_returnsModifiableList() {
        List<Reservation> reservations = reservationList.getReservations();
        Reservation res = new Reservation("08/10/2024", "08/20/2024");
        reservations.add(res);
        
        assertEquals(1, reservationList.getReservations().size());
    }

    @Test
    void testAdd_multipleInvocations() {
        for (int i = 0; i < 10; i++) {
            reservationList.add(new Reservation("08/0" + (i + 1) + "/2024", "08/" + (i + 10) + "/2024"));
        }
        
        assertEquals(10, reservationList.getReservations().size());
    }

    @Test
    void testParameterizedConstructor_preservesOriginalList() {
        List<Reservation> originalList = new ArrayList<>();
        originalList.add(new Reservation("08/10/2024", "08/20/2024"));
        
        ReservationList list = new ReservationList(originalList);
        
        originalList.add(new Reservation("09/01/2024", "09/10/2024"));
        
        // The ReservationList should have the same reference
        assertEquals(2, list.getReservations().size());
    }

    @Test
    void testAdd_afterParameterizedConstructor() {
        List<Reservation> initialList = new ArrayList<>();
        initialList.add(new Reservation("08/10/2024", "08/20/2024"));
        
        ReservationList list = new ReservationList(initialList);
        list.add(new Reservation("09/01/2024", "09/10/2024"));
        
        assertEquals(2, list.getReservations().size());
    }
}
