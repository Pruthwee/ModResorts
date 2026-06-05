package com.acme.modres.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonInputStreamTest {

    @TempDir
    File tempDir;

    @Test
    void testConstructor_withValidFile() throws IOException {
        File testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        
        assertDoesNotThrow(() -> {
            JsonInputStream jis = new JsonInputStream(testFile);
            jis.close();
        });
    }

    @Test
    void testConstructor_withNonExistentFile() {
        File nonExistentFile = new File(tempDir, "nonexistent.json");
        
        assertThrows(FileNotFoundException.class, () -> {
            new JsonInputStream(nonExistentFile);
        });
    }

    @Test
    void testParseJsonAs_withNonExistentFile() throws IOException {
        File testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        
        JsonInputStream jis = new JsonInputStream(testFile);
        Object result = jis.parseJsonAs(String.class);
        jis.close();
        
        // Result may be null for empty file
        assertDoesNotThrow(() -> jis.parseJsonAs(String.class));
    }

    @Test
    void testParseJsonAs_withNullClass() throws IOException {
        File testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        
        JsonInputStream jis = new JsonInputStream(testFile);
        
        assertDoesNotThrow(() -> {
            jis.parseJsonAs(null);
            jis.close();
        });
    }

    @Test
    void testClose_afterConstruction() throws IOException {
        File testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        
        JsonInputStream jis = new JsonInputStream(testFile);
        
        assertDoesNotThrow(() -> jis.close());
    }

    @Test
    void testParseJsonAs_withEmptyFile() throws IOException {
        File testFile = new File(tempDir, "empty.json");
        testFile.createNewFile();
        
        JsonInputStream jis = new JsonInputStream(testFile);
        Object result = jis.parseJsonAs(Object.class);
        jis.close();
        
        // Empty file should return null
        assertNull(result);
    }

    @Test
    void testConstructor_extendsFileInputStream() throws IOException {
        File testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        
        JsonInputStream jis = new JsonInputStream(testFile);
        
        assertTrue(jis instanceof java.io.FileInputStream);
        jis.close();
    }

    @Test
    void testParseJsonAs_multipleInvocations() throws IOException {
        File testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        
        JsonInputStream jis = new JsonInputStream(testFile);
        jis.parseJsonAs(String.class);
        jis.parseJsonAs(String.class);
        jis.close();
        
        assertDoesNotThrow(() -> jis.parseJsonAs(String.class));
    }

    @Test
    void testConstructor_withDirectory() {
        File directory = tempDir;
        
        assertThrows(FileNotFoundException.class, () -> {
            new JsonInputStream(directory);
        });
    }

    @Test
    void testParseJsonAs_withDifferentClasses() throws IOException {
        File testFile = new File(tempDir, "test.json");
        testFile.createNewFile();
        
        JsonInputStream jis = new JsonInputStream(testFile);
        
        assertDoesNotThrow(() -> {
            jis.parseJsonAs(String.class);
            jis.parseJsonAs(Integer.class);
            jis.parseJsonAs(Object.class);
            jis.close();
        });
    }
}
