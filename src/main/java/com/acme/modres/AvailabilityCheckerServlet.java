package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.mbean.reservation.Reservation;
import com.acme.modres.util.ZipValidator;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Cloud-ready servlet that uses java.time API for date handling and AWS S3 for file storage.
 * Eliminates dependencies on local file system and timezone-specific date handling.
 */
@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);

  private static InitialContext context;

  private ReservationCheckerData reservationCheckerData;
  private S3Client s3Client;
  private String s3BucketName;

  @Override
  public void init() {
    // load reserved dates
    this.reservationCheckerData = new ReservationCheckerData(IOUtils.getReservationListFromConfig());
    
    // Initialize S3 client for cloud storage
    String awsRegion = System.getenv("AWS_REGION");
    if (awsRegion == null || awsRegion.isEmpty()) {
      awsRegion = "us-east-1"; // default region
    }
    this.s3Client = S3Client.builder()
        .region(Region.of(awsRegion))
        .build();
    
    // Get S3 bucket name from environment variable
    this.s3BucketName = System.getenv("S3_BUCKET_NAME");
    if (this.s3BucketName == null || this.s3BucketName.isEmpty()) {
      logger.warning("S3_BUCKET_NAME environment variable not set. File export will be disabled.");
    }
  }

  @Override
  public void destroy() {
    if (s3Client != null) {
      s3Client.close();
    }
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
   * Exports reservations to AWS S3 instead of local file system.
   * This ensures data durability and availability in cloud environments.
   */
  protected int exportRevervations(String selectedDateStr) {
    if (s3BucketName == null || s3BucketName.isEmpty()) {
      logger.severe("Cannot export reservations: S3_BUCKET_NAME not configured");
      return -1;
    }

    try {
      // Read reservations.json from classpath
      InputStream reservationsStream = IOUtils.getResourceAsStream("reservations.json");
      if (reservationsStream == null) {
        logger.severe("reservations.json not found in classpath");
        return -1;
      }

      // Create zip in memory instead of local file system
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zipOut = new ZipOutputStream(baos);

      ZipEntry zipEntry = new ZipEntry("reservations.json");
      zipOut.putNextEntry(zipEntry);

      byte[] bytes = new byte[1024];
      int length;
      while ((length = reservationsStream.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }
      reservationsStream.close();

      zipOut.close();

      // Upload to S3
      byte[] zipBytes = baos.toByteArray();
      String s3Key = "reservations/reservations-" + selectedDateStr + ".zip";
      
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(s3BucketName)
          .key(s3Key)
          .contentType("application/zip")
          .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(zipBytes));
      
      logger.info("Successfully exported reservations to S3: s3://" + s3BucketName + "/" + s3Key);

      // Verify zip validity
      ZipValidator zipValidator = new ZipValidator(new ByteArrayInputStream(zipBytes));
      if (zipValidator.isValid()) {
        return 0;
      }
    } catch (IOException e) {
      logger.severe("Error exporting reservations: " + e.getMessage());
      e.printStackTrace();
    } catch (Throwable e) {
      logger.severe("Unexpected error exporting reservations: " + e.getMessage());
      e.printStackTrace();
    }
    return -1;
  }

}
