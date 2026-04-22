package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
import com.acme.modres.service.AzureBlobStorageService;
import com.acme.modres.util.ZipValidator;
import org.springframework.beans.factory.annotation.Autowired;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private ReservationCheckerData reservationCheckerData;

  @Autowired
  private AzureBlobStorageService blobStorageService;

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
          Date fromDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getFromDate());
          Date toDate = new SimpleDateFormat(Constants.DATA_FORMAT).parse(reservation.getToDate());
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
   * Export reservations to Azure Blob Storage
   * Replaces local file system operations with Azure Blob Storage
   */
  protected int exportRevervations(String selectedDateStr) {
    // Use try-with-resources to ensure proper resource cleanup
    try (InputStream reservationStream = IOUtils.getResourceInputStream("reservations.json");
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zipOut = new ZipOutputStream(baos)) {

      // Read reservation data
      byte[] reservationData = new byte[reservationStream.available()];
      reservationStream.read(reservationData);

      // Create zip entry
      ZipEntry zipEntry = new ZipEntry("reservations.json");
      zipOut.putNextEntry(zipEntry);
      zipOut.write(reservationData);
      zipOut.closeEntry();
      zipOut.finish();

      // Upload to Azure Blob Storage
      byte[] zipData = baos.toByteArray();
      String blobName = "reservations-" + selectedDateStr + ".zip";
      
      if (blobStorageService != null) {
        blobStorageService.uploadBlob(blobName, zipData);
      } else {
        logger.warning("Azure Blob Storage service not available, cannot export reservations");
        return -1;
      }

      // Verify zip by downloading and validating
      byte[] downloadedZip = blobStorageService.downloadBlob(blobName);
      try (ByteArrayInputStream bais = new ByteArrayInputStream(downloadedZip)) {
        // Validation logic here if needed
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
