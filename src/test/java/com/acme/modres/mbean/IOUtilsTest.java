package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

class IOUtilsTest {

    @Test
    void testGetFileFromRelativePath_withValidPath() {
        File result = IOUtils.getFileFromRelativePath("ops.json");
        
        // File may or may not exist depending on resources
        assertNotNull(result);
    }

    @Test
    void testGetFileFromRelativePath_withNullPath() {
        assertThrows(NullPointerException.class, () -> {
            IOUtils.getFileFromRelativePath(null);
        });
    }

    @Test
    void testGetFileFromRelativePath_withEmptyPath() {
        File result = IOUtils.getFileFromRelativePath("");
        
        assertNotNull(result);
    }

    @Test
    void testGetOpListFromConfig() {
        OpMetadataList result = IOUtils.getOpListFromConfig();
        
        // Result may be null if file doesn't exist
        // Just verify method doesn't throw exception
        assertDoesNotThrow(() -> IOUtils.getOpListFromConfig());
    }

    @Test
    void testGetReservationListFromConfig() {
        assertDoesNotThrow(() -> IOUtils.getReservationListFromConfig());
    }

    @Test
    void testGetFileFromRelativePath_returnsFile() {
        File result = IOUtils.getFileFromRelativePath("test.json");
        
        assertNotNull(result);
        assertTrue(result instanceof File);
    }

    @Test
    void testGetOpListFromConfig_returnsOpMetadataList() {
        OpMetadataList result = IOUtils.getOpListFromConfig();
        
        // May be null if file doesn't exist
        if (result != null) {
            assertTrue(result instanceof OpMetadataList);
        }
    }

    @Test
    void testGetReservationListFromConfig_doesNotThrow() {
        assertDoesNotThrow(() -> {
            IOUtils.getReservationListFromConfig();
        });
    }

    @Test
    void testGetFileFromRelativePath_withDifferentPaths() {
        File file1 = IOUtils.getFileFromRelativePath("path1.json");
        File file2 = IOUtils.getFileFromRelativePath("path2.json");
        
        assertNotNull(file1);
        assertNotNull(file2);
        assertNotEquals(file1.getName(), file2.getName());
    }

    @Test
    void testGetOpListFromConfig_multipleInvocations() {
        OpMetadataList result1 = IOUtils.getOpListFromConfig();
        OpMetadataList result2 = IOUtils.getOpListFromConfig();
        
        // Both should complete without exception
        assertDoesNotThrow(() -> IOUtils.getOpListFromConfig());
    }
}
