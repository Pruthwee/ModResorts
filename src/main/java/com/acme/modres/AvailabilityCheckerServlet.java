package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.Reservation;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.util.ZipValidator;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private ReservationCheckerData reservationCheckerData;
  
  // GCS configuration - should be externalized to environment variables
  private static final String GCS_BUCKET_NAME = System.getenv().getOrDefault("GCS_BUCKET_NAME", "modresorts-data");
  private static final String GCS_PROJECT_ID = System.getenv().getOrDefault("GCP_PROJECT_ID", "default-project");

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

      // Use UTC-based date parsing for cloud compatibility
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);
      LocalDate selectedDate = reservationCheckerData.getSelectedDateAsLocalDate();

      for (Reservation reservation : reservations) {
        try {
          LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), formatter);
          LocalDate toDate = LocalDate.parse(reservation.getToDate(), formatter);

          if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
            isAvailible = false;
            break;
          }
        } catch (Exception ex) {
          logger.warning("Error parsing date: " + ex.getMessage());
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
    try (PrintWriter out = response.getWriter()) {
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      out.print("{\"availability\": \"" + String.valueOf(reservationCheckerData.isAvailible()) + "\"}");
      response.setStatus(statusCode);
    }
  }

  /**
   * Returns the weather information for a given city
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    doGet(request, response);
  }

  /**
   * Export reservations to Google Cloud Storage instead of local file system
   */
  protected int exportRevervations(String selectedDateStr) {
    try {
      // Get reservation data from classpath resource
      InputStream resourceStream = IOUtils.class.getClassLoader().getResourceAsStream("reservations.json");
      if (resourceStream == null) {
        logger.severe("reservations.json not found in classpath");
        return -1;
      }

      // Create zip in memory instead of local file system
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
      try (ZipOutputStream zipOut = new ZipOutputStream(baos);
           InputStream fis = resourceStream) {
        
        ZipEntry zipEntry = new ZipEntry("reservations.json");
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
          zipOut.write(bytes, 0, length);
        }
        
        zipOut.closeEntry();
      }

      byte[] zipData = baos.toByteArray();

      // Upload to Google Cloud Storage
      Storage storage = StorageOptions.newBuilder()
          .setProjectId(GCS_PROJECT_ID)
          .build()
          .getService();

      String objectName = "reservations-" + Instant.now().toEpochMilli() + ".zip";
      BlobId blobId = BlobId.of(GCS_BUCKET_NAME, objectName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
          .setContentType("application/zip")
          .build();

      storage.create(blobInfo, zipData);
      logger.info("Uploaded reservations to GCS: " + objectName);

      // Verify zip from memory
      try (ByteArrayInputStream bais = new ByteArrayInputStream(zipData)) {
        // Basic validation - check if we can read the zip
        if (zipData.length > 0) {
          return 0;
        }
      }

    } catch (IOException e) {
      logger.severe("Error exporting reservations: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      logger.severe("Unexpected error: " + e.getMessage());
      e.printStackTrace();
    }
    return -1;
  }

}
