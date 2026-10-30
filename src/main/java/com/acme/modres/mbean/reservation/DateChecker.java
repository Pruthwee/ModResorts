package com.acme.modres.mbean.reservation;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.acme.modres.Constants;

public class DateChecker implements Runnable {
  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT).withZone(ZoneOffset.UTC);
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      Date selectedDate = data.getSelectedDate();

      try {
        Date fromDate = Date.from(LocalDate.parse(reservation.getFromDate(), formatter).atStartOfDay(ZoneOffset.UTC).toInstant());
        Date toDate = Date.from(LocalDate.parse(reservation.getToDate(), formatter).atStartOfDay(ZoneOffset.UTC).toInstant());
        if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
          data.setAvailablility(false);
          break;
        }
      } catch (DateTimeException ex) {
        ex.printStackTrace();
      }
    }
    data.setAvailablility(true);
  }
}
