package com.acme.modres;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.DateChecker;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.mbean.reservation.Reservation;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private static InitialContext context;

  private ReservationCheckerData reservationCheckerData;

  // S3 configuration from environment variables (12-factor app principle)
  private static final String S3_BUCKET_NAME = System.getenv("S3_BUCKET_NAME") != null
      ? System.getenv("S3_BUCKET_NAME") : "modresorts-data";

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

      // Use java.time API (UTC) instead of java.util.Date for cloud-safe date parsing
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);

      for (Reservation reservation : reservations) {
        try {
          LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), formatter);
          LocalDate toDate = LocalDate.parse(reservation.getToDate(), formatter);
          LocalDate selectedDate = reservationCheckerData.getSelectedDateAsLocalDate();

          if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
            isAvailible = false;
            break;
          }
        } catch (Exception ex) {
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
   * Exports reservations by reading from classpath and uploading to Amazon S3.
   * Replaces the previous local file system zip approach with S3-backed durable storage.
   * Uses try-with-resources for automatic resource management to prevent resource leaks.
   */
  protected int exportRevervations(String selectedDateStr) {
    String s3Key = "exports/reservations-" + Instant.now().toEpochMilli() + ".zip";

    try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("reservations.json")) {
      if (resourceStream == null) {
        logger.warning("reservations.json not found on classpath");
        return -1;
      }

      byte[] data = resourceStream.readAllBytes();

      // Create a zip in memory and upload to S3 using try-with-resources
      java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
      try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(baos)) {
        java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry("reservations.json");
        zipOut.putNextEntry(zipEntry);
        zipOut.write(data);
        zipOut.closeEntry();
      }

      byte[] zipBytes = baos.toByteArray();

      // Upload to Amazon S3 using AWS SDK v2 with try-with-resources
      try (S3Client s3Client = S3Client.builder().build()) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(S3_BUCKET_NAME)
            .key(s3Key)
            .contentType("application/zip")
            .build();
        s3Client.putObject(putRequest, RequestBody.fromBytes(zipBytes));
        logger.info("Reservations exported to S3: s3://" + S3_BUCKET_NAME + "/" + s3Key);
        return 0;
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return -1;
  }

}
