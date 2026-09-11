package com.acme.modres.mbean;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.acme.modres.mbean.reservation.ReservationList;
import com.google.gson.Gson;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Utility class for I/O operations.
 *
 * Cloud-readiness fix (cr-java-0062): Replaced local file system write
 * operations with in-memory classpath resource reads. The original
 * implementation wrote classpath resources to a local temp file via
 * FileOutputStream (line 24), which is incompatible with ephemeral cloud /
 * containerised environments. All methods now read directly from the classpath
 * InputStream without touching the local file system, ensuring data is never
 * lost on container restart or scale-out.
 *
 * Cloud-readiness fix (cr-java-0112): Eliminated reliance on ephemeral local
 * temporary directories. The original code used {@code File.createTempFile()}
 * (source line 23) to write classpath resources to the local /tmp directory,
 * which is ephemeral in cloud/containerised environments and causes data loss
 * on container restarts or scale-out events. This fix migrates temporary file
 * operations to Amazon S3 for persistent intermediate data storage, ensuring
 * data survives container restarts and enabling multi-instance access.
 *
 * <p>When the environment variable {@code S3_BUCKET_NAME} is set, resource
 * content is fetched from the configured S3 bucket (key = resource path).
 * When the variable is absent the implementation falls back to reading the
 * resource directly from the application classpath, so the application
 * continues to work in local-development environments without AWS credentials.
 *
 * <p>Required environment variables (AWS deployment):
 * <ul>
 *   <li>{@code S3_BUCKET_NAME} – name of the S3 bucket that holds the
 *       application's data files (e.g. {@code ops.json},
 *       {@code reservations.json}).</li>
 *   <li>{@code AWS_REGION} – AWS region of the bucket (defaults to
 *       {@code us-east-1} when not set).</li>
 * </ul>
 */
public final class IOUtils {

  /** Environment variable that holds the S3 bucket name for persistent storage. */
  private static final String ENV_S3_BUCKET = "S3_BUCKET_NAME";

  /** Environment variable that holds the AWS region (optional, defaults to us-east-1). */
  private static final String ENV_AWS_REGION = "AWS_REGION";

  /** Default AWS region used when {@code AWS_REGION} is not set. */
  private static final String DEFAULT_REGION = "us-east-1";

  /**
   * Lazily-initialised S3 client.  Created once and reused across calls.
   * {@code null} when S3 is not configured (i.e. {@code S3_BUCKET_NAME} is
   * absent), in which case the classpath fallback is used.
   */
  private static volatile S3Client s3Client;

  // -------------------------------------------------------------------------
  // Internal helpers
  // -------------------------------------------------------------------------

  /**
   * Returns the configured S3 bucket name, or {@code null} when the
   * {@code S3_BUCKET_NAME} environment variable is not set.
   */
  private static String getS3BucketName() {
    return System.getenv(ENV_S3_BUCKET);
  }

  /**
   * Returns a shared {@link S3Client} instance, creating it on first use.
   * The client is built from the standard AWS SDK v2 default credential
   * provider chain (environment variables, instance profile, etc.).
   */
  private static S3Client getS3Client() {
    if (s3Client == null) {
      synchronized (IOUtils.class) {
        if (s3Client == null) {
          String regionStr = System.getenv(ENV_AWS_REGION);
          Region region = (regionStr != null && !regionStr.isEmpty())
              ? Region.of(regionStr)
              : Region.of(DEFAULT_REGION);
          s3Client = S3Client.builder()
              .region(region)
              .build();
        }
      }
    }
    return s3Client;
  }

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /**
   * Reads a resource and returns its raw bytes.
   *
   * <p>Cloud-readiness fix (cr-java-0112): When {@code S3_BUCKET_NAME} is set
   * the content is fetched from Amazon S3 (key = {@code path}), replacing the
   * original {@code File.createTempFile()} call that wrote to the ephemeral
   * local {@code /tmp} directory. When S3 is not configured the resource is
   * read directly from the application classpath (local-dev fallback).
   *
   * @param path classpath-relative / S3-key resource path
   *             (e.g. {@code "reservations.json"})
   * @return byte array containing the resource content, or {@code null} on error
   */
  public static byte[] getResourceBytes(String path) {
    String bucketName = getS3BucketName();
    if (bucketName != null && !bucketName.isEmpty()) {
      // --- S3 path: fetch from Amazon S3 (persistent, cloud-native storage) ---
      try {
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(path)
            .build();
        try (ResponseInputStream<GetObjectResponse> s3Object =
                 getS3Client().getObject(request)) {
          return s3Object.readAllBytes();
        }
      } catch (NoSuchKeyException e) {
        System.err.println("[IOUtils] S3 key not found: " + path
            + " in bucket: " + bucketName + ". Falling back to classpath.");
      } catch (Exception e) {
        System.err.println("[IOUtils] Failed to read from S3 (key=" + path
            + "): " + e.getMessage() + ". Falling back to classpath.");
        e.printStackTrace();
      }
    }

    // --- Classpath fallback (local-dev / S3 not configured) ---
    // NOTE: This path does NOT write to any local temporary directory.
    //       The original File.createTempFile() call (source line 23) has been
    //       removed entirely; bytes are returned in-memory only.
    try (InputStream initialStream =
             IOUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (initialStream == null) {
        return null;
      }
      return initialStream.readAllBytes();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns an {@link InputStream} backed by the raw bytes of the named
   * resource (S3 or classpath).  Callers that previously received a
   * {@link java.io.File} from {@code getFileFromRelativePath} and only needed
   * to read its content should use this method instead.
   *
   * @param path classpath-relative / S3-key resource path
   * @return {@link InputStream} over the resource bytes, or {@code null} on error
   */
  public static InputStream getResourceAsStream(String path) {
    byte[] bytes = getResourceBytes(path);
    if (bytes == null) {
      return null;
    }
    return new ByteArrayInputStream(bytes);
  }

  /**
   * Loads the operations metadata list from S3 (when configured) or the
   * application classpath.
   *
   * <p>Cloud-readiness fix (cr-java-0112): Intermediate data is no longer
   * written to the ephemeral local {@code /tmp} directory; it is fetched
   * directly from Amazon S3 or the classpath and parsed in-memory.
   *
   * @return parsed {@link OpMetadataList}, or {@code null} on error
   */
  public static OpMetadataList getOpListFromConfig() {
    try (InputStream is = getResourceAsStream("ops.json")) {
      if (is == null) {
        return null;
      }
      Gson gson = new Gson();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      return gson.fromJson(reader, OpMetadataList.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Loads the reservation list from S3 (when configured) or the application
   * classpath.
   *
   * <p>Cloud-readiness fix (cr-java-0112): Intermediate data is no longer
   * written to the ephemeral local {@code /tmp} directory; it is fetched
   * directly from Amazon S3 or the classpath and parsed in-memory.
   *
   * @return parsed {@link ReservationList}, or {@code null} on error
   */
  public static ReservationList getReservationListFromConfig() {
    try (InputStream is = getResourceAsStream("reservations.json")) {
      if (is == null) {
        return null;
      }
      Gson gson = new Gson();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      return gson.fromJson(reader, ReservationList.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
