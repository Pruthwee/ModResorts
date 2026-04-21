package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.acme.modres.Constants;

/**
 * Date checker for reservation availability.
 * Updated to use java.time API instead of java.util.Date for cloud-native time handling.
 */
public class DateChecker implements Runnable {
  ReservationCheckerData data;
  List<Reservation> reservations;
  
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      LocalDate selectedDate = data.getSelectedDate();

      try {
        LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), DATE_FORMATTER);
        LocalDate toDate = LocalDate.parse(reservation.getToDate(), DATE_FORMATTER);
        if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
          data.setAvailablility(false);
          break;
        }
      } catch (DateTimeParseException ex) {
        ex.printStackTrace();
      }
    }
    data.setAvailablility(true);
  }
}
