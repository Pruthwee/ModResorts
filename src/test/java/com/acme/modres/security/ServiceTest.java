package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class ServiceTest {

    private Service service;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        service = new Service();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    void testConstructor() {
        assertNotNull(service);
    }

    @Test
    void testOperationConstant() {
        assertEquals("my-operation", Service.OPERATION);
    }

    @Test
    void testOperation_doesNotThrow() {
        assertDoesNotThrow(() -> service.operation());
    }

    @Test
    void testOperation_printsMessage() {
        service.operation();
        
        String output = outputStream.toString();
        assertTrue(output.contains("Operation is executed"));
    }

    @Test
    void testOperation_multipleInvocations() {
        service.operation();
        service.operation();
        
        String output = outputStream.toString();
        assertTrue(output.contains("Operation is executed"));
    }

    @Test
    void testOperationConstant_notNull() {
        assertNotNull(Service.OPERATION);
    }

    @Test
    void testOperationConstant_notEmpty() {
        assertFalse(Service.OPERATION.isEmpty());
    }

    @Test
    void testOperation_completesSuccessfully() {
        assertDoesNotThrow(() -> {
            service.operation();
        });
    }

    @Test
    void testMultipleServiceInstances() {
        Service service1 = new Service();
        Service service2 = new Service();
        
        assertNotNull(service1);
        assertNotNull(service2);
        assertNotSame(service1, service2);
    }

    @Test
    void testOperation_outputNotEmpty() {
        service.operation();
        
        String output = outputStream.toString();
        assertFalse(output.isEmpty());
    }

    @Test
    void testOperationConstant_hasExpectedValue() {
        assertEquals("my-operation", Service.OPERATION);
    }

    @Test
    void testOperation_withMultipleServices() {
        Service service1 = new Service();
        Service service2 = new Service();
        
        service1.operation();
        service2.operation();
        
        String output = outputStream.toString();
        assertTrue(output.contains("Operation is executed"));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
}
