package com.acme.modres.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Cloud-ready ZipValidator that works with InputStreams instead of Files.
 * This allows validation of zip data from any source (S3, memory, etc.)
 * without requiring local file system access.
 */
public class ZipValidator {

  private InputStream inputStream;

  public ZipValidator(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public boolean isValid() throws Throwable {
    if (inputStream == null) {
      return false;
    }
    
    try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
      ZipEntry entry = zipInputStream.getNextEntry();
      // If we can read at least one entry, the zip is valid
      return entry != null;
    } catch (IOException e) {
      return false;
    }
  }

}
