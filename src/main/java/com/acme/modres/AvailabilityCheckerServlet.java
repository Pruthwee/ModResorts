package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.DateChecker;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.mbean.reservation.Reservation;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.exception.AzureException;

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

      for (Reservation reservation : reservations) {
        try {
          DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT).withZone(ZoneOffset.UTC);
          Date fromDate = Date.from(LocalDate.parse(reservation.getFromDate(), dtf).atStartOfDay(ZoneOffset.UTC).toInstant());
          Date toDate = Date.from(LocalDate.parse(reservation.getToDate(), dtf).atStartOfDay(ZoneOffset.UTC).toInstant());
          Date selectedDate = reservationCheckerData.getSelectedDate();

          if (selectedDate.after(fromDate) && selectedDate.before(toDate)) {
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

  protected int exportRevervations(String selectedDateStr) {
    byte[] fileData = IOUtils.getBytesFromRelativePath("reservations.json");
    String blobName = "reservations.zip";

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
         ZipOutputStream zipOut = new ZipOutputStream(baos)) {

      ZipEntry zipEntry = new ZipEntry("reservations.json");
      zipOut.putNextEntry(zipEntry);

      byte[] bytes = new byte[1024];
      int length;
      while ((length = bis.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }

      zipOut.closeEntry();

      byte[] zipData = baos.toByteArray();

      // Upload to Azure Blob Storage
      BlobContainerClient containerClient = getBlobContainerClient();
      BlobClient blobClient = containerClient.getBlobClient(blobName);
      blobClient.upload(new ByteArrayInputStream(zipData), zipData.length, true);

      return 0;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AzureException e) {
      e.printStackTrace();
    }
    return -1;
  }

  private static BlobContainerClient getBlobContainerClient() {
    String storageEndpoint = System.getenv("AZURE_STORAGE_ENDPOINT");
    if (storageEndpoint == null || storageEndpoint.trim().length() == 0) {
      throw new IllegalStateException("AZURE_STORAGE_ENDPOINT is not set");
    }
    BlobServiceClient serviceClient = new BlobServiceClientBuilder()
        .endpoint(storageEndpoint)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
    return serviceClient.getBlobContainerClient("modresorts");
  }

}