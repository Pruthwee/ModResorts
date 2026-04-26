package com.acme.modres.mbean.reservation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.acme.modres.Constants;

/**
 * ReservationCheckerData with UTC timezone standardization for cloud deployment
 * Eliminates local timezone dependencies
 */
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
      // Use UTC timezone for consistent date handling across cloud regions
      SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATA_FORMAT);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      selectedDate = dateFormat.parse(dateStr);
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
