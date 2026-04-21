package com.acme.modres;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.Reservation;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.service.S3StorageService;

/**
 * Servlet for checking resort availability.
 * Updated to use java.time API instead of java.util.Date for cloud-native time handling.
 * File operations migrated to S3 for cloud storage.
 */
@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());
  
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);

  private ReservationCheckerData reservationCheckerData;
  
  @Autowired
  private S3StorageService s3StorageService;

  @Override
  public void init() {
    // load reserved dates
    this.reservationCheckerData = new ReservationCheckerData(IOUtils.getReservationListFromConfig());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    String methodName = "doGet";
    logger.entering(AvailabilityCheckerServlet.class.getName(), methodName);
    int statusCode = 200;

    String selectedDateStr = request.getParameter("date");
    boolean parsedDate = reservationCheckerData.setSelectedDate(selectedDateStr);
    if (!parsedDate || reservationCheckerData.getReservationList() == null) {
      statusCode = 500;
      reservationCheckerData.setAvailablility(false);
    } else {
      List<Reservation> reservations = reservationCheckerData.getReservationList().getReservations();
      boolean isAvailible = true;

      for (Reservation reservation : reservations) {
        try {
          LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), DATE_FORMATTER);
          LocalDate toDate = LocalDate.parse(reservation.getToDate(), DATE_FORMATTER);
          LocalDate selectedDate = reservationCheckerData.getSelectedDate();

          if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
            isAvailible = false;
            break;
          }
        } catch (DateTimeParseException ex) {
          logger.severe("Failed to parse date: " + ex.getMessage());
          ex.printStackTrace();
        }
      }

      reservationCheckerData.setAvailablility(isAvailible);

      // Adjust the status code based on availability
      if (!isAvailible) {
        statusCode = 201;
      }
    }

    // Send the response
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    out.print("{\"availability\": \"" + String.valueOf(reservationCheckerData.isAvailible()) + "\"}");
    response.setStatus(statusCode);
  }

  /**
   * Returns the weather information for a given city
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    doGet(request, response);
  }

  /**
   * Export reservations to S3 instead of local file system.
   * This ensures data durability in cloud environments.
   */
  protected int exportRevervations(String selectedDateStr) {
    try {
      // Load reservation data from classpath
      InputStream reservationStream = IOUtils.getResourceAsStream("reservations.json");
      byte[] reservationData = reservationStream.readAllBytes();
      reservationStream.close();
      
      // Upload to S3 instead of writing to local file system
      String s3Key = "exports/reservations-" + selectedDateStr + ".json";
      
      if (s3StorageService != null) {
        s3StorageService.uploadToS3(s3Key, reservationData);
        logger.info("Successfully exported reservations to S3: " + s3Key);
        return 0;
      } else {
        logger.warning("S3StorageService not available. Skipping export.");
        return -1;
      }
    } catch (Exception e) {
      logger.severe("Failed to export reservations: " + e.getMessage());
      e.printStackTrace();
      return -1;
    }
  }
}
