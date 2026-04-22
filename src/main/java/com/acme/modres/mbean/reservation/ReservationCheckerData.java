package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.acme.modres.Constants;

/**
 * ReservationCheckerData using UTC-based time handling for cloud compatibility
 * Supports both legacy Date and modern LocalDate for backward compatibility
 */
public class ReservationCheckerData {
  private ReservationList reservations;
  private LocalDate selectedLocalDate;
  private Date selectedDate; // Keep for backward compatibility
  private boolean available;

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
  
  public LocalDate getSelectedDateAsLocalDate() {
    return selectedLocalDate;
  }

  public boolean setSelectedDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
      selectedLocalDate = LocalDate.parse(dateStr, formatter);
      
      // For backward compatibility, also set the Date object
      // Convert LocalDate to Date (at start of day in UTC)
      selectedDate = java.sql.Date.valueOf(selectedLocalDate);
      
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
