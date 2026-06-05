package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ZipValidatorTest {

    @TempDir
    File tempDir;

    private File testFile;

    @AfterEach
    void cleanup() {
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void testConstructor_withValidZipFile_shouldCreateInstance() throws IOException {
        // Arrange
        testFile = createValidZipFile("test.zip");

        // Act
        ZipValidator validator = new ZipValidator(testFile);

        // Assert
        assertNotNull(validator);
        validator.close();
    }

    @Test
    void testConstructor_withNonZipFile_shouldThrowZipException() throws IOException {
        // Arrange
        testFile = new File(tempDir, "notazip.txt");
        testFile.createNewFile();

        // Act & Assert
        assertThrows(ZipException.class, () -> {
            new ZipValidator(testFile);
        });
    }

    @Test
    void testConstructor_withNonExistentFile_shouldThrowIOException() {
        // Arrange
        testFile = new File(tempDir, "nonexistent.zip");

        // Act & Assert
        assertThrows(IOException.class, () -> {
            new ZipValidator(testFile);
        });
    }

    @Test
    void testIsValid_withEmptyZipFile_shouldReturnTrue() throws Throwable {
        // Arrange
        testFile = createEmptyZipFile("empty.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act
        boolean result = validator.isValid();

        // Assert
        assertTrue(result);
        validator.close();
    }

    @Test
    void testIsValid_withValidZipFile_shouldReturnFalse() throws Throwable {
        // Arrange
        testFile = createValidZipFile("valid.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act
        boolean result = validator.isValid();

        // Assert
        assertFalse(result);
        validator.close();
    }

    @Test
    void testIsValid_withMultipleEntries_shouldReturnFalse() throws Throwable {
        // Arrange
        testFile = createZipFileWithMultipleEntries("multiple.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act
        boolean result = validator.isValid();

        // Assert
        assertFalse(result);
        validator.close();
    }

    @Test
    void testIsValid_afterDeletingFile_shouldReturnFalse() throws Throwable {
        // Arrange
        testFile = createValidZipFile("test.zip");
        ZipValidator validator = new ZipValidator(testFile);
        validator.close();
        testFile.delete();

        // Act
        boolean result = validator.isValid();

        // Assert
        assertFalse(result);
    }

    @Test
    void testClose_shouldCloseZipFile() throws IOException {
        // Arrange
        testFile = createValidZipFile("test.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act
        validator.close();

        // Assert - accessing entries after close should throw exception
        assertThrows(IllegalStateException.class, () -> {
            validator.entries();
        });
    }

    @Test
    void testGetEntries_withValidZipFile_shouldReturnEntries() throws IOException {
        // Arrange
        testFile = createValidZipFile("test.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act & Assert
        assertNotNull(validator.entries());
        validator.close();
    }

    @Test
    void testGetName_shouldReturnFileName() throws IOException {
        // Arrange
        testFile = createValidZipFile("test.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act
        String name = validator.getName();

        // Assert
        assertTrue(name.contains("test.zip"));
        validator.close();
    }

    @Test
    void testSize_withValidZipFile_shouldReturnSize() throws IOException {
        // Arrange
        testFile = createValidZipFile("test.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act
        int size = validator.size();

        // Assert
        assertTrue(size >= 0);
        validator.close();
    }

    @Test
    void testIsValid_multipleInvocations_shouldWorkCorrectly() throws Throwable {
        // Arrange
        testFile = createEmptyZipFile("test.zip");
        ZipValidator validator = new ZipValidator(testFile);

        // Act
        boolean result1 = validator.isValid();
        boolean result2 = validator.isValid();

        // Assert
        assertTrue(result1);
        assertTrue(result2);
        validator.close();
    }

    // Helper methods to create test zip files

    private File createEmptyZipFile(String filename) throws IOException {
        File zipFile = new File(tempDir, filename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // Create empty zip file
        }
        return zipFile;
    }

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

    private File createZipFileWithMultipleEntries(String filename) throws IOException {
        File zipFile = new File(tempDir, filename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (int i = 1; i <= 3; i++) {
                ZipEntry entry = new ZipEntry("file" + i + ".txt");
                zos.putNextEntry(entry);
                zos.write(("Content " + i).getBytes());
                zos.closeEntry();
            }
        }
        return zipFile;
    }
}
