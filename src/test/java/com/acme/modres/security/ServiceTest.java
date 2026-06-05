package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testOperationConstant_shouldHaveCorrectValue() {
        // Assert
        assertEquals("my-operation", Service.OPERATION);
    }

    @Test
    void testOperation_shouldExecuteWithoutException() {
        // Arrange
        Service service = new Service();

        // Act & Assert
        assertDoesNotThrow(() -> service.operation());
    }

    @Test
    void testOperation_shouldPrintMessage() {
        // Arrange
        Service service = new Service();

        // Act
        service.operation();

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Operation is executed"));
    }

    @Test
    void testOperation_shouldPrintToSystemOut() {
        // Arrange
        Service service = new Service();

        // Act
        service.operation();

        // Assert
        assertFalse(outContent.toString().isEmpty());
    }

    @Test
    void testConstructor_shouldCreateInstance() {
        // Act
        Service service = new Service();

        // Assert
        assertNotNull(service);
    }

    @Test
    void testOperation_multipleInvocations_shouldExecuteEachTime() {
        // Arrange
        Service service = new Service();

        // Act
        service.operation();
        service.operation();
        service.operation();

        // Assert
        String output = outContent.toString();
        int count = output.split("Operation is executed").length - 1;
        assertEquals(3, count);
    }

    @Test
    void testOperationConstant_shouldBePublicStatic() {
        // Assert
        assertNotNull(Service.OPERATION);
        assertEquals("my-operation", Service.OPERATION);
    }

    @Test
    void testOperation_shouldNotThrowSecurityException() {
        // Arrange
        Service service = new Service();

        // Act & Assert
        assertDoesNotThrow(() -> service.operation());
    }

    @Test
    void testMultipleInstances_shouldExecuteIndependently() {
        // Arrange
        Service service1 = new Service();
        Service service2 = new Service();

        // Act
        service1.operation();
        service2.operation();

        // Assert
        String output = outContent.toString();
        int count = output.split("Operation is executed").length - 1;
        assertEquals(2, count);
    }

    @Test
    void testOperation_outputFormat_shouldBeCorrect() {
        // Arrange
        Service service = new Service();

        // Act
        service.operation();

        // Assert
        String output = outContent.toString().trim();
        assertEquals("Operation is executed", output);
    }

    @Test
    void testInstance_shouldBeOfCorrectType() {
        // Act
        Service service = new Service();

        // Assert
        assertTrue(service instanceof Service);
    }

    @Test
    void testConstructor_shouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> new Service());
    }

    @Test
    void testOperationConstant_shouldNotBeNull() {
        // Assert
        assertNotNull(Service.OPERATION);
    }

    @Test
    void testOperationConstant_shouldNotBeEmpty() {
        // Assert
        assertFalse(Service.OPERATION.isEmpty());
    }
}
