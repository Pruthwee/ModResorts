package com.acme.modres.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipValidator extends ZipFile {

  private File file;

  public ZipValidator(File file) throws ZipException, IOException {
    super(file);
    this.file = file;
  }

  public ZipValidator(InputStream inputStream) throws IOException {
    super(File.createTempFile("zipvalidator", ".zip"));
  }

  public boolean isValid() throws Throwable {
    if (file != null && file.exists()) {
      try (ZipValidator zipFile = new ZipValidator(file)) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        if (!entries.hasMoreElements()) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isValid(InputStream inputStream) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(inputStream)) {
      ZipEntry entry = zis.getNextEntry();
      return entry != null;
    }
  }
}
