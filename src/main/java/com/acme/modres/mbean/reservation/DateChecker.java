package com.acme.modres.mbean.reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.acme.modres.Constants;

/**
 * DateChecker with UTC timezone standardization for cloud deployment
 * Eliminates local timezone dependencies
 */
public class DateChecker implements Runnable {
  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    // Use UTC timezone for consistent date handling across cloud regions
    SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATA_FORMAT);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      Date selectedDate = data.getSelectedDate();

      try {
        Date fromDate = dateFormat.parse(reservation.getFromDate());
        Date toDate = dateFormat.parse(reservation.getToDate());
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
