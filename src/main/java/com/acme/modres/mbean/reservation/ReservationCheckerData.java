package com.acme.modres.mbean.reservation;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.acme.modres.Constants;

/**
 * Holds reservation checker state.
 * Migrated from java.util.Date/SimpleDateFormat to java.time API (LocalDate, UTC)
 * to ensure consistent timezone handling across distributed cloud environments and regions.
 */
public class ReservationCheckerData {
  private ReservationList reservations;
  // Use LocalDate instead of java.util.Date for cloud-safe, timezone-consistent date handling
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
   * Returns the selected date as a LocalDate (UTC-normalized).
   */
  public LocalDate getSelectedDateAsLocalDate() {
    return selectedDate;
  }

  /**
   * @deprecated Use {@link #getSelectedDateAsLocalDate()} for cloud-safe UTC date handling.
   * Kept for backward compatibility.
   */
  @Deprecated
  public java.util.Date getSelectedDate() {
    if (selectedDate == null) {
      return null;
    }
    return java.util.Date.from(selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant());
  }

  /**
   * Parses the date string using java.time LocalDate API.
   * Standardizes on UTC to prevent timezone inconsistencies in multi-region cloud deployments.
   */
  public boolean setSelectedDate(String dateStr) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
      selectedDate = LocalDate.parse(dateStr, formatter);
    } catch (DateTimeParseException e) {
      return false;
    } catch (Exception e) {
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
