package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.TimeZone;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

import com.azure.core.exception.AzureException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private static InitialContext context;

  private static final String STORAGE_ENDPOINT_ENV = "AZURE_STORAGE_ENDPOINT";
  private static final String STORAGE_CONTAINER_ENV = "AZURE_STORAGE_CONTAINER";
  private static final String EXPORT_BLOB_NAME = "reservations.zip";

  private ReservationCheckerData reservationCheckerData;

  private transient BlobContainerClient exportContainer;

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
          SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATA_FORMAT);
          dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

  protected int exportRevervations(String selectedDateStr) {
    try {
      byte[] reservationsJson = IOUtils.getResourceBytes("reservations.json");
      if (reservationsJson == null) {
        throw new FileNotFoundException("reservations.json");
      }

      // build the zip in memory
      ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
      try (ZipOutputStream zipOut = new ZipOutputStream(zipBytes)) {
        zipOut.putNextEntry(new ZipEntry("reservations.json"));
        zipOut.write(reservationsJson, 0, reservationsJson.length);
        zipOut.closeEntry();
      }
      byte[] zipData = zipBytes.toByteArray();

      // store the zip in Azure Blob Storage instead of the local home directory
      getExportContainer().getBlobClient(EXPORT_BLOB_NAME).upload(new ByteArrayInputStream(zipData), zipData.length,
          true);

      // verify zip
      try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipData))) {
        if (zipIn.getNextEntry() == null) {
          return 0;
        }
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AzureException e) {
      e.printStackTrace();
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return -1;
  }

  /**
   * Builds the Blob Storage container client on first use (Managed Identity /
   * default Azure credential chain).
   */
  private BlobContainerClient getExportContainer() throws IOException {
    if (exportContainer == null) {
      String endpoint = System.getenv(STORAGE_ENDPOINT_ENV);
      String container = System.getenv(STORAGE_CONTAINER_ENV);
      if (endpoint == null || endpoint.trim().isEmpty()) {
        throw new IOException(STORAGE_ENDPOINT_ENV + " is not set");
      }
      if (container == null || container.trim().isEmpty()) {
        throw new IOException(STORAGE_CONTAINER_ENV + " is not set");
      }
      exportContainer = new BlobServiceClientBuilder().endpoint(endpoint.trim())
          .credential(new DefaultAzureCredentialBuilder().build()).buildClient()
          .getBlobContainerClient(container.trim());
    }
    return exportContainer;
  }

}