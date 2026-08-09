package com.acme.modres.mbean.reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.acme.modres.Constants;
import com.acme.modres.cloud.AzureServiceBusScheduler;

public class DateChecker implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(DateChecker.class.getName());

  ReservationCheckerData data;
  List<Reservation> reservations;

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
  }

  public void run() {
    AzureServiceBusScheduler.scheduleAvailabilityCheck(
        "{\"selectedDate\":\"" + data.getSelectedDate() + "\"}",
        OffsetDateTime.now());

    for (int i = 0; i < reservations.size(); i++) {
      Reservation reservation = reservations.get(i);
      Date selectedDate = data.getSelectedDate();

      try {
        Date fromDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getFromDate());
        Date toDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getToDate());
        if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
          data.setAvailablility(false);
          return;
        }
      } catch (ParseException ex) {
        LOGGER.log(Level.WARNING, "Unable to parse reservation date", ex);
      }
    }
    data.setAvailablility(true);
  }
}
