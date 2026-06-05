package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpMetadataTest {

    private OpMetadata opMetadata;

    @BeforeEach
    void setUp() {
        opMetadata = new OpMetadata();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(opMetadata);
    }

    @Test
    void testParameterizedConstructor() {
        OpMetadata op = new OpMetadata("testOp", "Test operation", "void", 1);
        
        assertEquals("testOp", op.getName());
        assertEquals("Test operation", op.getDescription());
        assertEquals("void", op.getType());
        assertEquals(1, op.getImpact());
    }

    @Test
    void testSetName() {
        opMetadata.setName("operationName");
        
        assertEquals("operationName", opMetadata.getName());
    }

    @Test
    void testSetDescription() {
        opMetadata.setDescription("Operation description");
        
        assertEquals("Operation description", opMetadata.getDescription());
    }

    @Test
    void testSetType() {
        opMetadata.setType("java.lang.String");
        
        assertEquals("java.lang.String", opMetadata.getType());
    }

    @Test
    void testSetImpact() {
        opMetadata.setImpact(2);
        
        assertEquals(2, opMetadata.getImpact());
    }

    @Test
    void testGetName_defaultValue() {
        assertNull(opMetadata.getName());
    }

    @Test
    void testGetDescription_defaultValue() {
        assertNull(opMetadata.getDescription());
    }

    @Test
    void testGetType_defaultValue() {
        assertNull(opMetadata.getType());
    }

    @Test
    void testGetImpact_defaultValue() {
        assertEquals(0, opMetadata.getImpact());
    }

    @Test
    void testSetName_withNull() {
        opMetadata.setName(null);
        
        assertNull(opMetadata.getName());
    }

    @Test
    void testSetDescription_withNull() {
        opMetadata.setDescription(null);
        
        assertNull(opMetadata.getDescription());
    }

    @Test
    void testSetType_withNull() {
        opMetadata.setType(null);
        
        assertNull(opMetadata.getType());
    }

    @Test
    void testSetName_withEmptyString() {
        opMetadata.setName("");
        
        assertEquals("", opMetadata.getName());
    }

    @Test
    void testSetDescription_withEmptyString() {
        opMetadata.setDescription("");
        
        assertEquals("", opMetadata.getDescription());
    }

    @Test
    void testSetType_withEmptyString() {
        opMetadata.setType("");
        
        assertEquals("", opMetadata.getType());
    }

    @Test
    void testSetImpact_withNegativeValue() {
        opMetadata.setImpact(-1);
        
        assertEquals(-1, opMetadata.getImpact());
    }

    @Test
    void testSetImpact_withLargeValue() {
        opMetadata.setImpact(999);
        
        assertEquals(999, opMetadata.getImpact());
    }

    @Test
    void testParameterizedConstructor_withNullValues() {
        OpMetadata op = new OpMetadata(null, null, null, 0);
        
        assertNull(op.getName());
        assertNull(op.getDescription());
        assertNull(op.getType());
        assertEquals(0, op.getImpact());
    }

    @Test
    void testSettersAndGetters_chainedCalls() {
        opMetadata.setName("op1");
        opMetadata.setDescription("desc1");
        opMetadata.setType("type1");
        opMetadata.setImpact(5);
        
        assertEquals("op1", opMetadata.getName());
        assertEquals("desc1", opMetadata.getDescription());
        assertEquals("type1", opMetadata.getType());
        assertEquals(5, opMetadata.getImpact());
    }
}
