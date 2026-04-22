package com.acme.modres.mbean;

import java.io.IOException;
import java.io.InputStream;

import com.acme.modres.mbean.reservation.ReservationList;
import com.acme.modres.util.JsonInputStream;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public final class IOUtils {

  private static S3Client s3Client;
  private static String s3BucketName;
  private static boolean useS3;

  static {
    // Initialize S3 client if environment variables are set
    String awsRegion = System.getenv("AWS_REGION");
    s3BucketName = System.getenv("S3_BUCKET_NAME");
    
    if (awsRegion != null && s3BucketName != null) {
      s3Client = S3Client.builder()
          .region(Region.of(awsRegion))
          .build();
      useS3 = true;
    } else {
      useS3 = false;
    }
  }

  /**
   * Get input stream from S3 or classpath resource
   * This eliminates the need for temporary files
   */
  public static InputStream getResourceStream(String path) throws IOException {
    if (useS3) {
      // Try to load from S3 first
      try {
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(s3BucketName)
            .key("config/" + path)
            .build();
        
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getRequest);
        return s3Object;
      } catch (Exception e) {
        System.err.println("Failed to load from S3, falling back to classpath: " + e.getMessage());
      }
    }
    
    // Fall back to classpath resource
    InputStream stream = IOUtils.class.getClassLoader().getResourceAsStream(path);
    if (stream == null) {
      throw new IOException("Resource not found: " + path);
    }
    return stream;
  }

  public static OpMetadataList getOpListFromConfig() {
    try (InputStream is = getResourceStream("ops.json");
         JsonInputStream jis = new JsonInputStream(is)) {
      OpMetadataList opList = (OpMetadataList) jis.parseJsonAs(OpMetadataList.class);
      return opList;
    } catch (IOException e) {
      e.printStackTrace();
      return new OpMetadataList(); // Return empty default
    }
  }

  public static ReservationList getReservationListFromConfig() {
    try (InputStream is = getResourceStream("reservations.json");
         JsonInputStream jis = new JsonInputStream(is)) {
      ReservationList reservationList = (ReservationList) jis.parseJsonAs(ReservationList.class);
      return reservationList;
    } catch (IOException e) {
      e.printStackTrace();
      return new ReservationList(); // Return empty default
    }
  }

}
