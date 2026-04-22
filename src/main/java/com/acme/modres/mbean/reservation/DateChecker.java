package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

import com.acme.modres.Constants;

/**
 * DateChecker using UTC-based time handling for cloud compatibility
 * Replaced java.util.Date with java.time.LocalDate for better timezone handling
 */
public class DateChecker implements Runnable {
  private static final Logger logger = Logger.getLogger(DateChecker.class.getName());
  
  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
    
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      LocalDate selectedDate = data.getSelectedDateAsLocalDate();

      try {
        LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), formatter);
        LocalDate toDate = LocalDate.parse(reservation.getToDate(), formatter);
        
        if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
          data.setAvailablility(false);
          return;
        }
      } catch (Exception ex) {
        logger.warning("Error parsing date: " + ex.getMessage());
        ex.printStackTrace();
      }
    }
    data.setAvailablility(true);
  }
}
