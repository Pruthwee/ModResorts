package com.acme.modres.mbean;

import com.acme.modres.cloud.AzureBlobStorageService;
import com.acme.modres.mbean.reservation.ReservationList;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IOUtils {
  private static final Logger LOGGER = Logger.getLogger(IOUtils.class.getName());
  private static final Gson GSON = new Gson();

  private IOUtils() {
  }

  public static byte[] getBytesFromCloudStorage(String blobName) throws IOException {
    return AzureBlobStorageService.readBytes(blobName);
  }

  public static String getTextFromCloudStorage(String blobName) throws IOException {
    return AzureBlobStorageService.readText(blobName);
  }

  public static void writeBytesToCloudStorage(String blobName, byte[] content, String contentType) throws IOException {
    AzureBlobStorageService.uploadBytes(blobName, content, contentType);
  }

  public static OpMetadataList getOpListFromConfig() {
    try {
      String json = getTextFromCloudStorage("ops.json");
      return json == null ? null : GSON.fromJson(json, OpMetadataList.class);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Unable to read operations metadata from Azure Blob Storage", e);
      return null;
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try {
      String json = getTextFromCloudStorage("reservations.json");
      return json == null ? null : GSON.fromJson(json, ReservationList.class);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Unable to read reservation configuration from Azure Blob Storage", e);
      return null;
    }
  }
}
