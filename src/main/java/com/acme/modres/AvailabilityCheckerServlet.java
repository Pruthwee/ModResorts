package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.DateChecker;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.mbean.reservation.Reservation;

import com.acme.modres.util.BlobStorageHelper;
import com.acme.modres.util.ZipValidator;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private static InitialContext context;

  private ReservationCheckerData reservationCheckerData;

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

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT).withZone(ZoneOffset.UTC);
      for (Reservation reservation : reservations) {
        try {
          Date fromDate = Date.from(LocalDate.parse(reservation.getFromDate(), formatter).atStartOfDay(ZoneOffset.UTC).toInstant());
          Date toDate = Date.from(LocalDate.parse(reservation.getToDate(), formatter).atStartOfDay(ZoneOffset.UTC).toInstant());
          Date selectedDate = reservationCheckerData.getSelectedDate();

          if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
            isAvailible = false;
            break;
          }
        } catch (DateTimeException ex) {
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

  protected int exportRevervations(String selectedDateStr) {
    String blobName = "reservations.zip";
    String storageEndpoint = System.getenv("AZURE_STORAGE_ENDPOINT");
    String containerName = System.getenv("AZURE_STORAGE_CONTAINER");

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try {
      File fileToZip = IOUtils.getFileFromRelativePath("reservations.json");
      File zipFile = new File(System.getProperty("java.io.tmpdir"), blobName);
      try (FileOutputStream fos = new FileOutputStream(zipFile);
          ZipOutputStream zipOut = new ZipOutputStream(fos);
          FileInputStream fis = new FileInputStream(fileToZip)) {

        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
          zipOut.write(bytes, 0, length);
        }
        zipOut.closeEntry();
      }

      // Read the zip into buffer for upload
      try (FileInputStream fis = new FileInputStream(zipFile)) {
        byte[] data = new byte[(int) zipFile.length()];
        fis.read(data);
        buffer.write(data);
      }

      // Upload to Azure Blob Storage
      if (storageEndpoint != null && containerName != null) {
        BlobContainerClient containerClient = BlobStorageHelper.getContainerClient(storageEndpoint, containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(new ByteArrayInputStream(buffer.toByteArray()), buffer.size(), true);
      }

      // verify zip
      ZipValidator zipValidator = new ZipValidator(zipFile);
      if (zipValidator.isValid()) {
        return 0;
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return -1;
  }

}