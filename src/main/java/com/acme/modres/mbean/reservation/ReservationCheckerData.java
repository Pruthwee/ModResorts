package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.acme.modres.Constants;

/**
 * Data holder for reservation checking.
 * Updated to use java.time API instead of java.util.Date for cloud-native time handling.
 */
public class ReservationCheckerData {
  private ReservationList reservations;
  private LocalDate selectedDate;
  private boolean available;
  
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);

  public ReservationCheckerData(ReservationList reservations) {
    this.reservations = reservations;
    this.available = true;
  }

  public ReservationList getReservationList() {
    return reservations;
  }

  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  public boolean setSelectedDate(String dateStr) {
    try {
      selectedDate = LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return false;
    }
    return true;
  }

  public boolean isAvailible() {
    return available;
  }

  public void setAvailablility(boolean available) {
    this.available = available;
  }
}
