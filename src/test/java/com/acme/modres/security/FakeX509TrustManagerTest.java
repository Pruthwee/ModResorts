package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FakeX509TrustManagerTest {

    @Test
    void testConstructor_shouldCreateInstance() {
        // Act
        FakeX509TrustManager trustManager = new FakeX509TrustManager();

        // Assert
        assertNotNull(trustManager);
    }

    @Test
    void testInstance_shouldBeOfCorrectType() {
        // Act
        FakeX509TrustManager trustManager = new FakeX509TrustManager();

        // Assert
        assertTrue(trustManager instanceof FakeX509TrustManager);
    }

    @Test
    void testMultipleInstances_shouldCreateIndependentObjects() {
        // Act
        FakeX509TrustManager trustManager1 = new FakeX509TrustManager();
        FakeX509TrustManager trustManager2 = new FakeX509TrustManager();

        // Assert
        assertNotNull(trustManager1);
        assertNotNull(trustManager2);
        assertNotSame(trustManager1, trustManager2);
    }

    @Test
    void testConstructor_shouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> new FakeX509TrustManager());
    }

    @Test
    void testInstance_shouldHaveDefaultToString() {
        // Act
        FakeX509TrustManager trustManager = new FakeX509TrustManager();
        String toString = trustManager.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("FakeX509TrustManager"));
    }

    @Test
    void testInstance_shouldHaveHashCode() {
        // Act
        FakeX509TrustManager trustManager = new FakeX509TrustManager();
        int hashCode = trustManager.hashCode();

        // Assert
        assertNotEquals(0, hashCode);
    }

    @Test
    void testEquals_withSameInstance_shouldReturnTrue() {
        // Arrange
        FakeX509TrustManager trustManager = new FakeX509TrustManager();

        // Act
        boolean result = trustManager.equals(trustManager);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEquals_withDifferentInstance_shouldReturnFalse() {
        // Arrange
        FakeX509TrustManager trustManager1 = new FakeX509TrustManager();
        FakeX509TrustManager trustManager2 = new FakeX509TrustManager();

        // Act
        boolean result = trustManager1.equals(trustManager2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testEquals_withNull_shouldReturnFalse() {
        // Arrange
        FakeX509TrustManager trustManager = new FakeX509TrustManager();

        // Act
        boolean result = trustManager.equals(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetClass_shouldReturnCorrectClass() {
        // Arrange
        FakeX509TrustManager trustManager = new FakeX509TrustManager();

        // Act
        Class<?> clazz = trustManager.getClass();

        // Assert
        assertEquals(FakeX509TrustManager.class, clazz);
    }
}
