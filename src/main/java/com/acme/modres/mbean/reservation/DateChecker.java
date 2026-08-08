package com.acme.modres.mbean.reservation;

import com.acme.modres.Constants;
import com.acme.modres.scheduling.AzureServiceBusScheduler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

public class DateChecker implements Runnable {
  ReservationCheckerData data;
  List<Reservation> reservations;
  private final AzureServiceBusScheduler scheduler = new AzureServiceBusScheduler();

  public DateChecker(ReservationCheckerData data) {
    this.data = data;
    this.reservations = data.getReservationList().getReservations();
    scheduler.scheduleAvailabilityCheck("date-checker-init", OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1));
  }

  public void run() {
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
  }
}
