package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.acme.modres.Constants;

/**
 * DateChecker using modern java.time API instead of legacy Date/SimpleDateFormat
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
    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      
      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
        LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), formatter);
        LocalDate toDate = LocalDate.parse(reservation.getToDate(), formatter);
        
        // Convert java.util.Date to LocalDate for comparison
        LocalDate selectedDate = convertToLocalDate(data.getSelectedDate());
        
        if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
          data.setAvailablility(false);
          break;
        }
      } catch (DateTimeParseException ex) {
        logger.log(Level.WARNING, "Error parsing date for reservation", ex);
      }
    }
    data.setAvailablility(true);
  }
  
  /**
   * Converts java.util.Date to LocalDate
   */
  private LocalDate convertToLocalDate(java.util.Date date) {
    if (date == null) {
      return null;
    }
    return date.toInstant()
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate();
  }
}
