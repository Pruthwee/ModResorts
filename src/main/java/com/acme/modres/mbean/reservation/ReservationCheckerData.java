package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import com.acme.modres.Constants;

/**
 * Migrated from java.util.Date to java.time.LocalDate for better timezone handling
 * and cloud environment compatibility
 */
public class ReservationCheckerData {
  private ReservationList reservations;
  private LocalDate selectedDate;
  private boolean available;

  public ReservationCheckerData(ReservationList reservations) {
    this.reservations = reservations;
    this.available = true;
  }

  public ReservationList getReservationList() {
    return reservations;
  }

  /**
   * @deprecated Use getSelectedDateAsLocalDate() instead
   */
  @Deprecated
  public Date getSelectedDate() {
    // For backward compatibility, convert LocalDate to Date
    if (selectedDate == null) {
      return null;
    }
    return java.sql.Date.valueOf(selectedDate);
  }
  
  public LocalDate getSelectedDateAsLocalDate() {
    return selectedDate;
  }

  public boolean setSelectedDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
      selectedDate = LocalDate.parse(dateStr, formatter);
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
