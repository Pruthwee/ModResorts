package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonInputStreamTest {

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
    void testConstructor_withValidFile_shouldCreateInstance() throws IOException {
        // Arrange
        testFile = new File(tempDir, "test.json");
        testFile.createNewFile();

        // Act
        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Assert
        assertNotNull(inputStream);
        inputStream.close();
    }

    @Test
    void testConstructor_withNonExistentFile_shouldThrowFileNotFoundException() {
        // Arrange
        testFile = new File(tempDir, "nonexistent.json");

        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> {
            new JsonInputStream(testFile);
        });
    }

    @Test
    void testParseJsonAs_withValidJsonFile_shouldReturnObject() throws IOException {
        // Arrange
        testFile = new File(tempDir, "test.json");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{\"name\":\"test\",\"value\":123}");
        }

        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        Object result = inputStream.parseJsonAs(TestData.class);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof TestData);
        inputStream.close();
    }

    @Test
    void testParseJsonAs_withEmptyFile_shouldReturnNull() throws IOException {
        // Arrange
        testFile = new File(tempDir, "empty.json");
        testFile.createNewFile();

        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        Object result = inputStream.parseJsonAs(TestData.class);

        // Assert
        assertNull(result);
        inputStream.close();
    }

    @Test
    void testParseJsonAs_withInvalidJson_shouldReturnNull() throws IOException {
        // Arrange
        testFile = new File(tempDir, "invalid.json");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("invalid json content");
        }

        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        Object result = inputStream.parseJsonAs(TestData.class);

        // Assert
        assertNull(result);
        inputStream.close();
    }

    @Test
    void testParseJsonAs_withNonExistentFile_shouldReturnNull() throws IOException {
        // Arrange
        testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        JsonInputStream inputStream = new JsonInputStream(testFile);
        testFile.delete(); // Delete after creating stream

        // Act
        Object result = inputStream.parseJsonAs(TestData.class);

        // Assert
        assertNull(result);
        inputStream.close();
    }

    @Test
    void testParseJsonAs_withComplexJson_shouldParseCorrectly() throws IOException {
        // Arrange
        testFile = new File(tempDir, "complex.json");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{\"name\":\"test\",\"value\":123,\"nested\":{\"key\":\"value\"}}");
        }

        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        Object result = inputStream.parseJsonAs(TestData.class);

        // Assert
        assertNotNull(result);
        inputStream.close();
    }

    @Test
    void testParseJsonAs_withArrayJson_shouldParseCorrectly() throws IOException {
        // Arrange
        testFile = new File(tempDir, "array.json");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("[{\"name\":\"test1\"},{\"name\":\"test2\"}]");
        }

        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        Object result = inputStream.parseJsonAs(Object[].class);

        // Assert
        assertNotNull(result);
        inputStream.close();
    }

    @Test
    void testClose_shouldCloseStream() throws IOException {
        // Arrange
        testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        inputStream.close();

        // Assert
        assertThrows(IOException.class, () -> {
            inputStream.read();
        });
    }

    @Test
    void testRead_afterClose_shouldThrowIOException() throws IOException {
        // Arrange
        testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        JsonInputStream inputStream = new JsonInputStream(testFile);
        inputStream.close();

        // Act & Assert
        assertThrows(IOException.class, () -> {
            inputStream.read();
        });
    }

    @Test
    void testParseJsonAs_withNullClass_shouldHandleGracefully() throws IOException {
        // Arrange
        testFile = new File(tempDir, "test.json");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("{\"name\":\"test\"}");
        }

        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        Object result = inputStream.parseJsonAs(null);

        // Assert
        assertNull(result);
        inputStream.close();
    }

    @Test
    void testParseJsonAs_withLargeJsonFile_shouldHandleCorrectly() throws IOException {
        // Arrange
        testFile = new File(tempDir, "large.json");
        try (FileWriter writer = new FileWriter(testFile)) {
            StringBuilder json = new StringBuilder("{\"items\":[");
            for (int i = 0; i < 100; i++) {
                if (i > 0) json.append(",");
                json.append("{\"id\":").append(i).append(",\"name\":\"item").append(i).append("\"}");
            }
            json.append("]}");
            writer.write(json.toString());
        }

        JsonInputStream inputStream = new JsonInputStream(testFile);

        // Act
        Object result = inputStream.parseJsonAs(Object.class);

        // Assert
        assertNotNull(result);
        inputStream.close();
    }

    // Helper class for testing
    static class TestData {
        private String name;
        private int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
