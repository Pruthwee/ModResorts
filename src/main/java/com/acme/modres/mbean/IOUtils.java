package com.acme.modres.mbean;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.acme.modres.mbean.reservation.ReservationList;
import com.google.gson.Gson;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Utility class for I/O operations.
 *
 * <p>Local temporary file writes (previously using {@code File.createTempFile} and
 * {@code FileOutputStream}) have been replaced with Amazon S3 object storage to ensure
 * data durability and availability across container restarts and multiple instances.
 *
 * <p>The S3 bucket name and AWS region are resolved from environment variables
 * ({@code S3_BUCKET_NAME}, {@code AWS_REGION}) following 12-factor app principles.
 *
 * <p>Remediation for rule cr-java-0112 (Local Temporary Storage Reliance):
 * Line 23 of the original source used {@code File.createTempFile(path, null)} which
 * writes to the ephemeral local {@code /tmp} directory. This has been replaced with
 * Amazon S3 so that intermediate data survives container lifecycle events and is
 * accessible across all running instances.
 */
public final class IOUtils {

    /**
     * Returns the S3 bucket name from the {@code S3_BUCKET_NAME} environment variable.
     * Falls back to {@code "modresorts-config"} if the variable is not set.
     */
    private static String getS3BucketName() {
        String bucket = System.getenv("S3_BUCKET_NAME");
        return (bucket != null && !bucket.isEmpty()) ? bucket : "modresorts-config";
    }

    /**
     * Returns the AWS region from the {@code AWS_REGION} environment variable.
     * Falls back to {@code "us-east-1"} if the variable is not set.
     */
    private static String getAwsRegion() {
        String region = System.getenv("AWS_REGION");
        return (region != null && !region.isEmpty()) ? region : "us-east-1";
    }

    /**
     * Reads a classpath resource, uploads it to Amazon S3, and returns an
     * {@link InputStream} backed by the S3 object so callers can parse the content.
     *
     * <p>This replaces the original pattern of:
     * <pre>
     *   file = File.createTempFile(path, null);          // line 23 – ephemeral /tmp
     *   outStream = new FileOutputStream(file);           // line 24
     *   outStream.write(buffer);                          // line 25
     * </pre>
     * with a cloud-native Amazon S3 write/read cycle, eliminating reliance on the
     * ephemeral local temporary directory.
     *
     * @param path classpath-relative resource name (e.g. {@code "reservations.json"})
     * @return an {@link InputStream} for the resource content, or {@code null} on error
     */
    public static InputStream getInputStreamFromResource(String path) {
        InputStream initialStream = null;
        try {
            initialStream = IOUtils.class.getClassLoader().getResourceAsStream(path);
            if (initialStream == null) {
                System.err.println("IOUtils: classpath resource not found: " + path);
                return null;
            }

            // Read all bytes from the classpath resource
            byte[] buffer = readAllBytes(initialStream);
            initialStream.close();
            initialStream = null;

            // Upload the resource bytes to Amazon S3
            // This replaces: file = File.createTempFile(path, null);
            //                outStream = new FileOutputStream(file);
            //                outStream.write(buffer);
            String s3Key = "config/" + path;
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(getAwsRegion()))
                    .build();

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(getS3BucketName())
                    .key(s3Key)
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromBytes(buffer));

            // Retrieve the object back from S3 as a stream for the caller to parse
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(getS3BucketName())
                    .key(s3Key)
                    .build();
            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getRequest);
            // s3Client is intentionally kept open; the caller is responsible for closing the stream.
            return s3Stream;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (initialStream != null) {
                try {
                    initialStream.close();
                } catch (IOException e) {
                    // ignore – best-effort cleanup
                }
            }
        }
    }

    /**
     * Reads all bytes from the given {@link InputStream} into a byte array.
     *
     * @param is the input stream to drain
     * @return byte array containing all bytes read from the stream
     * @throws IOException if an I/O error occurs
     */
    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int bytesRead;
        while ((bytesRead = is.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    /**
     * Loads the operations metadata list from the {@code ops.json} classpath resource
     * via Amazon S3 intermediate storage.
     *
     * @return the parsed {@link OpMetadataList}, or {@code null} on error
     */
    public static OpMetadataList getOpListFromConfig() {
        try (InputStream is = getInputStreamFromResource("ops.json")) {
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
     * Loads the reservation list from the {@code reservations.json} classpath resource
     * via Amazon S3 intermediate storage.
     *
     * @return the parsed {@link ReservationList}, or {@code null} on error
     */
    public static ReservationList getReservationListFromConfig() {
        try (InputStream is = getInputStreamFromResource("reservations.json")) {
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
