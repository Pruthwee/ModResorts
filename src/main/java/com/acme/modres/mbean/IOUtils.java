package com.acme.modres.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.BlobStorageHelper;
import com.acme.modres.util.JsonInputStream;

public final class IOUtils {

  public static File getFileFromRelativePath(String path) {
    InputStream initialStream = null;
    OutputStream outStream = null;
    File file = null;
    try {
      initialStream = IOUtils.class.getClassLoader().getResourceAsStream(path);
      byte[] buffer = new byte[initialStream.available()];
      initialStream.read(buffer);

      String storageEndpoint = System.getenv("AZURE_STORAGE_ENDPOINT");
      String containerName = System.getenv("AZURE_STORAGE_CONTAINER");

      if (storageEndpoint != null && containerName != null) {
        BlobStorageHelper.uploadBlob(storageEndpoint, containerName, path, buffer);
        byte[] downloaded = BlobStorageHelper.downloadBlob(storageEndpoint, containerName, path);
        file = new File(System.getProperty("java.io.tmpdir"), path);
        outStream = new FileOutputStream(file);
        outStream.write(downloaded);
        outStream.close();
      } else {
        file = new File(System.getProperty("java.io.tmpdir"), path);
        outStream = new FileOutputStream(file);
        outStream.write(buffer);
        outStream.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (initialStream != null) {
        try {
          initialStream.close();
        } catch (IOException e) {
        }
      } else if (outStream != null) {
        try {
          outStream.close();
        } catch (IOException e) {
        }
      }
    }

    return file;
  }

  public static OpMetadataList getOpListFromConfig() {
    File file = getFileFromRelativePath("ops.json");
    try (JsonInputStream is = new JsonInputStream(file)) {
      OpMetadataList opList = new OpMetadataList();
      opList = (OpMetadataList) is.parseJsonAs(OpMetadataList.class);
      return opList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ReservationList getReservationListFromConfig() {
    File file = getFileFromRelativePath("reservations.json");
    try (JsonInputStream is = new JsonInputStream(file)) {
      ReservationList reservationList = new ReservationList();
      reservationList = (ReservationList) is.parseJsonAs(ReservationList.class);
      return reservationList;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
