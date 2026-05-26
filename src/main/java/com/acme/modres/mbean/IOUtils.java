import java.nio.charset.StandardCharsets;
import com.acme.modres.util.JsonInputStream;

public final class IOUtils {
  public static byte[] getResourceBytes(String path) throws IOException {
    try (InputStream initialStream = IOUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (initialStream == null) {
        throw new IOException("Resource not found: " + path);
      }
      return initialStream.readAllBytes();
    }
  }

  public static byte[] getReservationsJsonBytes() throws IOException {
    return getResourceBytes("reservations.json");
  }
    return file;
  }
    try (InputStream is = IOUtils.class.getClassLoader().getResourceAsStream("ops.json");
         JsonInputStream jis = new JsonInputStream(is)) {
      opList = (OpMetadataList) jis.parseJsonAs(OpMetadataList.class);
      e.printStackTrace();
      return null;
    }
  }

    try (InputStream is = IOUtils.class.getClassLoader().getResourceAsStream("reservations.json");
         JsonInputStream jis = new JsonInputStream(is)) {
      reservationList = (ReservationList) jis.parseJsonAs(ReservationList.class);
      return null;
    }
