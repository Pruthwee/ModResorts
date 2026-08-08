package com.acme.modres.mbean;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.storage.AzureBlobStorageService;
import com.acme.modres.util.JsonInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class IOUtils {
  private static final AzureBlobStorageService BLOB_STORAGE_SERVICE = new AzureBlobStorageService();

  private IOUtils() {
  }

  public static File getFileFromRelativePath(String path) {
    try (InputStream blobStream = BLOB_STORAGE_SERVICE.openBlobStream(path)) {
      File file = File.createTempFile("modresorts-", "-" + path);
      file.deleteOnExit();
      Files.copy(blobStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return file;
    } catch (Exception blobException) {
      try (InputStream classpathStream = IOUtils.class.getClassLoader().getResourceAsStream(path)) {
        if (classpathStream == null) {
          throw new IllegalStateException("Resource not found in Azure Blob Storage or classpath: " + path, blobException);
        }
        File file = File.createTempFile("modresorts-", "-" + path);
        file.deleteOnExit();
        Files.copy(classpathStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return file;
      } catch (IOException e) {
        throw new IllegalStateException("Unable to load resource: " + path, e);
      }
    }
  }

  public static InputStream getResourceStream(String path) {
    try {
      return BLOB_STORAGE_SERVICE.openBlobStream(path);
    } catch (Exception blobException) {
      InputStream classpathStream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      if (classpathStream == null) {
        throw new IllegalStateException("Resource not found in Azure Blob Storage or classpath: " + path, blobException);
      }
      return classpathStream;
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream stream = getResourceStream("ops.json");
        JsonInputStream is = new JsonInputStream(createTempFile(stream, "ops.json"))) {
      return (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream stream = getResourceStream("reservations.json");
        JsonInputStream is = new JsonInputStream(createTempFile(stream, "reservations.json"))) {
      return (ReservationList) is.parseJsonAs(ReservationList.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static File createTempFile(InputStream stream, String name) throws IOException {
    File file = File.createTempFile("modresorts-", "-" + name);
    file.deleteOnExit();
    Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return file;
  }

  public static InputStream toInputStream(byte[] content) {
    return new ByteArrayInputStream(content);
  }
}
