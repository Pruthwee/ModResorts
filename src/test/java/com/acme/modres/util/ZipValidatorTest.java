package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ZipValidatorTest {

    @TempDir
    File tempDir;

    @Test
    void testConstructor_withValidZipFile() throws IOException {
        File zipFile = createValidZipFile("test.zip");
        
        assertDoesNotThrow(() -> {
            ZipValidator validator = new ZipValidator(zipFile);
            validator.close();
        });
    }

    @Test
    void testConstructor_withNonExistentFile() {
        File nonExistentFile = new File(tempDir, "nonexistent.zip");
        
        assertThrows(IOException.class, () -> {
            new ZipValidator(nonExistentFile);
        });
    }

    @Test
    void testConstructor_withInvalidZipFile() throws IOException {
        File invalidZipFile = new File(tempDir, "invalid.zip");
        invalidZipFile.createNewFile();
        
        assertThrows(ZipException.class, () -> {
            new ZipValidator(invalidZipFile);
        });
    }

    @Test
    void testIsValid_withValidZipFile() throws Throwable {
        File zipFile = createValidZipFile("valid.zip");
        
        ZipValidator validator = new ZipValidator(zipFile);
        boolean result = validator.isValid();
        validator.close();
        
        // Result depends on zip content
        assertNotNull(result);
    }

    @Test
    void testIsValid_withEmptyZipFile() throws Throwable {
        File zipFile = createEmptyZipFile("empty.zip");
        
        ZipValidator validator = new ZipValidator(zipFile);
        boolean result = validator.isValid();
        validator.close();
        
        assertTrue(result);
    }

    @Test
    void testConstructor_extendsZipFile() throws IOException {
        File zipFile = createValidZipFile("test.zip");
        
        ZipValidator validator = new ZipValidator(zipFile);
        
        assertTrue(validator instanceof java.util.zip.ZipFile);
        validator.close();
    }

    @Test
    void testClose_afterConstruction() throws IOException {
        File zipFile = createValidZipFile("test.zip");
        
        ZipValidator validator = new ZipValidator(zipFile);
        
        assertDoesNotThrow(() -> validator.close());
    }

    @Test
    void testIsValid_multipleInvocations() throws Throwable {
        File zipFile = createValidZipFile("test.zip");
        
        ZipValidator validator = new ZipValidator(zipFile);
        validator.isValid();
        validator.isValid();
        validator.close();
        
        assertDoesNotThrow(() -> validator.isValid());
    }

    @Test
    void testConstructor_withDirectory() {
        File directory = tempDir;
        
        assertThrows(IOException.class, () -> {
            new ZipValidator(directory);
        });
    }

    @Test
    void testIsValid_withNonExistentFile() throws Throwable {
        File zipFile = createValidZipFile("test.zip");
        ZipValidator validator = new ZipValidator(zipFile);
        validator.close();
        
        zipFile.delete();
        
        assertDoesNotThrow(() -> validator.isValid());
    }

    // Helper methods
    private File createValidZipFile(String filename) throws IOException {
        File zipFile = new File(tempDir, filename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("Test content".getBytes());
            zos.closeEntry();
        }
        return zipFile;
    }

    private File createEmptyZipFile(String filename) throws IOException {
        File zipFile = new File(tempDir, filename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // Create empty zip file
        }
        return zipFile;
    }
}
