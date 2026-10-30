package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

import com.acme.modres.Constants;

public class DateChecker implements Runnable {
  // Same calendar-date format as Constants.DATA_FORMAT; single letters also accept unpadded dates
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
      .ofPattern(Constants.DATA_FORMAT.replace("MM", "M").replace("dd", "d"));

  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      Date selectedDate = data.getSelectedDate();

      try {
        Date fromDate = Date.from(LocalDate.parse(reservation.getFromDate(), DATE_FORMATTER)
            .atStartOfDay(ZoneOffset.UTC).toInstant());
        Date toDate = Date.from(LocalDate.parse(reservation.getToDate(), DATE_FORMATTER)
            .atStartOfDay(ZoneOffset.UTC).toInstant());
        if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
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
