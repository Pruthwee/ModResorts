package com.acme.modres.mbean.reservation;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;

import com.acme.modres.Constants;
import com.acme.modres.cloud.AzureServiceBusScheduler;

public class ReservationCheckerData {
  private ReservationList reservations;
  private Date selectedDate;
  private boolean available; // changed from Boolean to boolean

  public ReservationCheckerData(ReservationList reservations) {
    this.reservations = reservations;
    this.available = true;
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
      AzureServiceBusScheduler.scheduleAvailabilityCheck("{\"date\":\"" + dateStr + "\"}", OffsetDateTime.now());
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public boolean isAvailible() {
    return available;
  }

  public void setAvailablility(boolean available) { // fix parameter type
    this.available = available;
  }
}
