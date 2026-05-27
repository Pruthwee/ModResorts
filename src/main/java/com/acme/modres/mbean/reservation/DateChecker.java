package com.acme.modres.mbean.reservation;

import java.text.ParseException;
import java.util.concurrent.CompletableFuture;
    CompletableFuture.runAsync(() -> {
      boolean available = true;
      for (int i = 0; i < reservations.size(); i++) {
        Reservation reservation = reservations.get(i);
        Date selectedDate = data.getSelectedDate();

        try {
          Date fromDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getFromDate());
          Date toDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getToDate());
          if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
            available = false;
            break;
          }
        } catch (ParseException ex) {
          ex.printStackTrace();
        }
      }
      data.setAvailablility(available);
    });

        try {
          Date fromDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getFromDate());
          Date toDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getToDate());
          if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
            available = false;
            break;
          }
        } catch (ParseException ex) {
          ex.printStackTrace();
        }
      }
      data.setAvailablility(available);
    });
public class DateChecker implements Runnable {
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
        Date fromDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getFromDate());
        Date toDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getToDate());
        if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
          data.setAvailablility(false);
          break;
        }
      } catch (ParseException ex) {
        ex.printStackTrace();
      }
    }
    data.setAvailablility(true);
  }
}
