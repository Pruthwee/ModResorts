package com.acme.modres.mbean;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Utility class for I/O operations.
 * File operations have been migrated to Amazon S3 for cloud-native durable storage.
 * Local temporary file creation has been eliminated; classpath resources are read directly.
 */
public final class IOUtils {

  private static final Logger logger = Logger.getLogger(IOUtils.class.getName());

  // S3 configuration sourced from environment variables (12-factor app principle)
  private static final String S3_BUCKET_NAME = System.getenv("S3_BUCKET_NAME") != null
      ? System.getenv("S3_BUCKET_NAME") : "modresorts-data";

  /**
   * Reads a resource from the classpath as a byte array.
   * Replaces the previous pattern of writing classpath resources to local temp files.
   */
  public static byte[] readResourceAsBytes(String path) {
    // Use try-with-resources to ensure the stream is always closed (prevents resource leaks)
    try (InputStream initialStream = IOUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (initialStream == null) {
        logger.warning("Resource not found on classpath: " + path);
        return null;
      }
      return initialStream.readAllBytes();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Uploads content to Amazon S3 for durable, cloud-native storage.
   * Replaces local FileOutputStream write operations.
   */
  public static void writeToS3(String s3Key, byte[] content, String contentType) {
    // Use try-with-resources for S3Client to prevent resource leaks
    try (S3Client s3Client = S3Client.builder().build()) {
      PutObjectRequest putRequest = PutObjectRequest.builder()
          .bucket(S3_BUCKET_NAME)
          .key(s3Key)
          .contentType(contentType)
          .build();
      s3Client.putObject(putRequest, RequestBody.fromBytes(content));
      logger.info("Successfully wrote to S3: s3://" + S3_BUCKET_NAME + "/" + s3Key);
    } catch (Exception e) {
      logger.severe("Failed to write to S3 key: " + s3Key + " - " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Reads content from Amazon S3.
   * Provides cloud-native durable read operations.
   */
  public static byte[] readFromS3(String s3Key) {
    // Use try-with-resources for both S3Client and ResponseInputStream
    try (S3Client s3Client = S3Client.builder().build()) {
      GetObjectRequest getRequest = GetObjectRequest.builder()
          .bucket(S3_BUCKET_NAME)
          .key(s3Key)
          .build();
      try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getRequest)) {
        return s3Object.readAllBytes();
      }
    } catch (NoSuchKeyException e) {
      logger.warning("S3 key not found: " + s3Key);
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static OpMetadataList getOpListFromConfig() {
    // Read directly from classpath using try-with-resources (no temp file needed)
    try (InputStream is = IOUtils.class.getClassLoader().getResourceAsStream("ops.json")) {
      if (is == null) {
        logger.warning("ops.json not found on classpath");
        return new OpMetadataList();
      }
      OpMetadataList opList = (OpMetadataList) JsonInputStream.parseJsonFromStream(is, OpMetadataList.class);
      return opList != null ? opList : new OpMetadataList();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static ReservationList getReservationListFromConfig() {
    // Read directly from classpath using try-with-resources (no temp file needed)
    try (InputStream is = IOUtils.class.getClassLoader().getResourceAsStream("reservations.json")) {
      if (is == null) {
        logger.warning("reservations.json not found on classpath");
        return new ReservationList();
      }
      ReservationList reservationList = (ReservationList) JsonInputStream.parseJsonFromStream(is, ReservationList.class);
      return reservationList != null ? reservationList : new ReservationList();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
