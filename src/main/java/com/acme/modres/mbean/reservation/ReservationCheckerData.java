package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.acme.modres.Constants;

public class ReservationCheckerData {
  // Same calendar-date format as Constants.DATA_FORMAT; single letters also accept unpadded dates
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
      .ofPattern(Constants.DATA_FORMAT.replace("MM", "M").replace("dd", "d"));

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
      selectedDate = Date.from(LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay(ZoneOffset.UTC).toInstant());
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
