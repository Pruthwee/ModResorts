package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanOperationInfo;

import org.junit.jupiter.api.Test;

class DMBeanUtilsTest {

    @Test
    void testGetOps_withNullOpList() {
        MBeanOperationInfo[] result = DMBeanUtils.getOps(null);
        
        assertNull(result);
    }

    @Test
    void testGetOps_withNullOpMetadataList() {
        OpMetadataList opList = new OpMetadataList();
        opList.setOpMetadatList(null);
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertNull(result);
    }

    @Test
    void testGetOps_withEmptyOpList() {
        OpMetadataList opList = new OpMetadataList();
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertNull(result);
    }

    @Test
    void testGetOps_withSingleOperation() {
        OpMetadataList opList = new OpMetadataList();
        OpMetadata opMetadata = new OpMetadata("testOp", "Test operation", "void", 1);
        opList.add(opMetadata);
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("testOp", result[0].getName());
        assertEquals("Test operation", result[0].getDescription());
    }

    @Test
    void testGetOps_withMultipleOperations() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("op1", "Operation 1", "void", 1));
        opList.add(new OpMetadata("op2", "Operation 2", "String", 2));
        opList.add(new OpMetadata("op3", "Operation 3", "int", 3));
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertNotNull(result);
        assertEquals(3, result.length);
    }

    @Test
    void testGetOps_preservesOperationOrder() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("first", "First op", "void", 1));
        opList.add(new OpMetadata("second", "Second op", "void", 2));
        opList.add(new OpMetadata("third", "Third op", "void", 3));
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertEquals("first", result[0].getName());
        assertEquals("second", result[1].getName());
        assertEquals("third", result[2].getName());
    }

    @Test
    void testGetOps_preservesOperationDetails() {
        OpMetadataList opList = new OpMetadataList();
        OpMetadata opMetadata = new OpMetadata("testMethod", "Test description", "java.lang.String", 5);
        opList.add(opMetadata);
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertEquals("testMethod", result[0].getName());
        assertEquals("Test description", result[0].getDescription());
        assertEquals("java.lang.String", result[0].getReturnType());
        assertEquals(5, result[0].getImpact());
    }

    @Test
    void testGetOps_withDifferentImpactLevels() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("info", "Info operation", "void", 0));
        opList.add(new OpMetadata("action", "Action operation", "void", 1));
        opList.add(new OpMetadata("actionInfo", "Action/Info operation", "void", 2));
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertEquals(0, result[0].getImpact());
        assertEquals(1, result[1].getImpact());
        assertEquals(2, result[2].getImpact());
    }

    @Test
    void testGetOps_withDifferentReturnTypes() {
        OpMetadataList opList = new OpMetadataList();
        opList.add(new OpMetadata("voidOp", "Void operation", "void", 1));
        opList.add(new OpMetadata("stringOp", "String operation", "java.lang.String", 1));
        opList.add(new OpMetadata("intOp", "Int operation", "int", 1));
        
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);
        
        assertEquals("void", result[0].getReturnType());
        assertEquals("java.lang.String", result[1].getReturnType());
        assertEquals("int", result[2].getReturnType());
    }
}
