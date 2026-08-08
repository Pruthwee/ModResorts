package com.acme.modres.mbean.reservation;

import com.acme.modres.Constants;
import com.acme.modres.scheduling.AzureServiceBusScheduler;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class ReservationCheckerData {
  private ReservationList reservations;
  private Date selectedDate;
  private boolean available;
  private final AzureServiceBusScheduler scheduler = new AzureServiceBusScheduler();

  public ReservationCheckerData(ReservationList reservations) {
    this.reservations = reservations;
    this.available = true;
    scheduler.scheduleAvailabilityCheck("reservation-checker-created", OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(1));
  }

  public ReservationList getReservationList() {
    return reservations;
  }

  public Date getSelectedDate() {
    return selectedDate;
  }

  public boolean setSelectedDate(String dateStr) {
    try {
      selectedDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(dateStr);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isAvailible() {
    return available;
  }

  public void setAvailablility(boolean available) {
    this.available = available;
  }
}
