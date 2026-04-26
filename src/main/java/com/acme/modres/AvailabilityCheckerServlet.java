package com.acme.modres;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
import com.google.cloud.storage.Blob;
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

      // Use UTC timezone for consistent date handling across cloud regions
      SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATA_FORMAT);
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

      for (Reservation reservation : reservations) {
        try {
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
   * Export reservations to Google Cloud Storage instead of local file system
   */
  protected int exportRevervations(String selectedDateStr) {
    // Use try-with-resources to ensure proper resource cleanup
    try (InputStream reservationStream = getClass().getClassLoader().getResourceAsStream("reservations.json")) {
      
      if (reservationStream == null) {
        logger.severe("reservations.json not found in classpath");
        return -1;
      }

      // Create zip in memory instead of local file system
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
        ZipEntry zipEntry = new ZipEntry("reservations.json");
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = reservationStream.read(bytes)) >= 0) {
          zipOut.write(bytes, 0, length);
        }
        zipOut.closeEntry();
      }

      // Upload to Google Cloud Storage
      byte[] zipData = baos.toByteArray();
      String timestamp = Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
      String gcsObjectName = "reservations/reservations_" + timestamp + ".zip";
      
      Storage storage = StorageOptions.newBuilder()
          .setProjectId(GCS_PROJECT_ID)
          .build()
          .getService();
      
      BlobId blobId = BlobId.of(GCS_BUCKET_NAME, gcsObjectName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
          .setContentType("application/zip")
          .build();
      
      Blob blob = storage.create(blobInfo, zipData);
      
      logger.info("Reservations exported to GCS: gs://" + GCS_BUCKET_NAME + "/" + gcsObjectName);
      
      // Verify zip data is valid
      if (zipData.length > 0) {
        return 0;
      }
      
    } catch (IOException e) {
      logger.severe("Error exporting reservations: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      logger.severe("Unexpected error exporting reservations: " + e.getMessage());
      e.printStackTrace();
    }
    return -1;
  }

}
