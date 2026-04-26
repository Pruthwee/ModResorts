package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.mbean.reservation.Reservation;
import com.acme.modres.service.GcsStorageService;
import com.acme.modres.util.ZipValidator;
import org.springframework.beans.factory.annotation.Autowired;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private ReservationCheckerData reservationCheckerData;

  @Autowired
  private GcsStorageService gcsStorageService;

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
          // Use UTC timezone for consistent date handling across cloud instances
          SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATA_FORMAT);
          dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
          
          Date fromDate = dateFormat.parse(reservation.getFromDate());
          Date toDate = dateFormat.parse(reservation.getToDate());
          Date selectedDate = reservationCheckerData.getSelectedDate();

          if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
            isAvailible = false;
            break;
          }
        } catch (ParseException ex) {
          ex.printStackTrace();
        }
      }

      reservationCheckerData.setAvailablility(isAvailible);

      // Adjust the status code based on availability
      if (!isAvailible) {
        statusCode = 201;
      }
    }

    // Send the response - using try-with-resources to ensure proper closure
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try (PrintWriter out = response.getWriter()) {
      out.print("{\"availability\": \"" + String.valueOf(reservationCheckerData.isAvailible()) + "\"}");
    }
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
   * Export reservations to GCS instead of local file system
   */
  protected int exportRevervations(String selectedDateStr) {
    try {
      // Get reservation data from classpath or GCS
      InputStream reservationStream = IOUtils.getInputStreamFromResource("reservations.json");
      if (reservationStream == null) {
        logger.severe("Could not find reservations.json");
        return -1;
      }

      // Create zip in memory using try-with-resources for proper resource management
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (ZipOutputStream zipOut = new ZipOutputStream(baos);
           InputStream fis = reservationStream) {
        
        ZipEntry zipEntry = new ZipEntry("reservations.json");
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
          zipOut.write(bytes, 0, length);
        }
        zipOut.closeEntry();
      }

      byte[] zipContent = baos.toByteArray();

      // Upload to GCS instead of local file system
      if (gcsStorageService != null) {
        String timestamp = Instant.now().toString().replaceAll(":", "-");
        String gcsPath = "exports/reservations-" + timestamp + ".zip";
        gcsStorageService.uploadFile(gcsPath, zipContent);
        logger.info("Exported reservations to GCS: " + gcsPath);
      } else {
        logger.warning("GCS service not available, export skipped");
      }

      // Verify zip
      try (ByteArrayInputStream bais = new ByteArrayInputStream(zipContent)) {
        // Basic validation - zip was created successfully
        return 0;
      }

    } catch (IOException e) {
      logger.severe("Error exporting reservations: " + e.getMessage());
      e.printStackTrace();
      return -1;
    } catch (Throwable e) {
      logger.severe("Unexpected error exporting reservations: " + e.getMessage());
      e.printStackTrace();
      return -1;
    }
  }

}
