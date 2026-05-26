import java.io.ByteArrayInputStream;
import java.util.Date;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import javax.servlet.http.HttpServletResponse;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.DateChecker;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.mbean.reservation.Reservation;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private static InitialContext context;

  private transient BlobClient blobClient;
    this.reservationCheckerData = new ReservationCheckerData(IOUtils.getReservationListFromConfig());

    // Initialize Azure Blob Storage client for exporting reservations
    String blobUrl = System.getenv("RESERVATIONS_ZIP_BLOB_URL");
    if (blobUrl != null && !blobUrl.isEmpty()) {
      this.blobClient = new BlobClientBuilder()
          .endpoint(blobUrl)
          .credential(new DefaultAzureCredentialBuilder().build())
          .buildClient();
    }
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
      // Upload to Azure Blob Storage
      BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();
      try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes)) {
        blockBlobClient.upload(bais, zipBytes.length, true);
      }

      // verify zip in-memory
      ZipValidator zipValidator = new ZipValidator(new java.io.ByteArrayInputStream(zipBytes));
      if (zipValidator.isValid()) {
        return 0;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return -1;

}