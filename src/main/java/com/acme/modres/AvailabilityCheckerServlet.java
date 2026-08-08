package com.acme.modres;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.Reservation;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.scheduling.AzureServiceBusScheduler;
import com.acme.modres.storage.AzureBlobStorageService;
import com.acme.modres.util.ZipValidator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());
  private final AzureBlobStorageService blobStorageService = new AzureBlobStorageService();
  private final AzureServiceBusScheduler scheduler = new AzureServiceBusScheduler();
  private ReservationCheckerData reservationCheckerData;

  @Override
  public void init() {
    this.reservationCheckerData = new ReservationCheckerData(IOUtils.getReservationListFromConfig());
    scheduler.scheduleAvailabilityCheck("reservation-cache-refresh", OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5));
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    logger.entering(AvailabilityCheckerServlet.class.getName(), "doGet");
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
      if (!isAvailible) {
        statusCode = 201;
      }
    }

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    try (PrintWriter out = response.getWriter()) {
      out.print("{\"availability\": \"" + String.valueOf(reservationCheckerData.isAvailible()) + "\"}");
    }
    response.setStatus(statusCode);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  protected int exportRevervations(String selectedDateStr) {
    File fileToZip = IOUtils.getFileFromRelativePath("reservations.json");
    try (InputStream fis = Files.newInputStream(fileToZip.toPath());
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(fos)) {
      ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
      zipOut.putNextEntry(zipEntry);

      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }
      zipOut.closeEntry();
      zipOut.finish();

      byte[] zipBytes = fos.toByteArray();
      String blobUrl = blobStorageService.uploadWithGeneratedName("reservations", zipBytes);
      ZipValidator zipValidator = new ZipValidator(writeValidationFile(zipBytes));
      if (zipValidator.isValid()) {
        logger.info("Reservations exported to Azure Blob Storage: " + blobUrl);
        return 0;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return -1;
  }

  private File writeValidationFile(byte[] zipBytes) throws IOException {
    File validationFile = File.createTempFile("reservations-", ".zip");
    validationFile.deleteOnExit();
    java.nio.file.Files.write(validationFile.toPath(), zipBytes);
    return validationFile;
  }
}
