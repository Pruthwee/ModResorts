package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanOperationInfo;

import org.junit.jupiter.api.Test;

class DMBeanUtilsTest {

    @Test
    void testGetOps_withNullOpList_shouldReturnNull() {
        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetOps_withNullOpMetadataList_shouldReturnNull() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        opList.setOpMetadatList(null);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetOps_withEmptyOpMetadataList_shouldReturnNull() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        opList.setOpMetadatList(new ArrayList<>());

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetOps_withSingleOperation_shouldReturnSingleOpInfo() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        List<OpMetadata> metadataList = new ArrayList<>();
        
        OpMetadata metadata = new OpMetadata();
        metadata.setName("testOperation");
        metadata.setDescription("Test operation description");
        metadata.setType("java.lang.String");
        metadata.setImpact(MBeanOperationInfo.ACTION);
        
        metadataList.add(metadata);
        opList.setOpMetadatList(metadataList);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("testOperation", result[0].getName());
        assertEquals("Test operation description", result[0].getDescription());
        assertEquals("java.lang.String", result[0].getReturnType());
        assertEquals(MBeanOperationInfo.ACTION, result[0].getImpact());
    }

    @Test
    void testGetOps_withMultipleOperations_shouldReturnAllOpInfos() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        List<OpMetadata> metadataList = new ArrayList<>();
        
        OpMetadata metadata1 = new OpMetadata();
        metadata1.setName("operation1");
        metadata1.setDescription("Description 1");
        metadata1.setType("void");
        metadata1.setImpact(MBeanOperationInfo.ACTION);
        
        OpMetadata metadata2 = new OpMetadata();
        metadata2.setName("operation2");
        metadata2.setDescription("Description 2");
        metadata2.setType("int");
        metadata2.setImpact(MBeanOperationInfo.INFO);
        
        metadataList.add(metadata1);
        metadataList.add(metadata2);
        opList.setOpMetadatList(metadataList);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("operation1", result[0].getName());
        assertEquals("operation2", result[1].getName());
    }

    @Test
    void testGetOps_withDifferentImpactTypes_shouldHandleCorrectly() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        List<OpMetadata> metadataList = new ArrayList<>();
        
        OpMetadata metadata1 = new OpMetadata();
        metadata1.setName("actionOp");
        metadata1.setDescription("Action operation");
        metadata1.setType("void");
        metadata1.setImpact(MBeanOperationInfo.ACTION);
        
        OpMetadata metadata2 = new OpMetadata();
        metadata2.setName("infoOp");
        metadata2.setDescription("Info operation");
        metadata2.setType("String");
        metadata2.setImpact(MBeanOperationInfo.INFO);
        
        OpMetadata metadata3 = new OpMetadata();
        metadata3.setName("actionInfoOp");
        metadata3.setDescription("Action Info operation");
        metadata3.setType("boolean");
        metadata3.setImpact(MBeanOperationInfo.ACTION_INFO);
        
        metadataList.add(metadata1);
        metadataList.add(metadata2);
        metadataList.add(metadata3);
        opList.setOpMetadatList(metadataList);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(MBeanOperationInfo.ACTION, result[0].getImpact());
        assertEquals(MBeanOperationInfo.INFO, result[1].getImpact());
        assertEquals(MBeanOperationInfo.ACTION_INFO, result[2].getImpact());
    }

    @Test
    void testGetOps_withDifferentReturnTypes_shouldHandleCorrectly() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        List<OpMetadata> metadataList = new ArrayList<>();
        
        String[] returnTypes = {"void", "int", "String", "boolean", "java.util.List"};
        
        for (String returnType : returnTypes) {
            OpMetadata metadata = new OpMetadata();
            metadata.setName("op_" + returnType);
            metadata.setDescription("Operation returning " + returnType);
            metadata.setType(returnType);
            metadata.setImpact(MBeanOperationInfo.INFO);
            metadataList.add(metadata);
        }
        
        opList.setOpMetadatList(metadataList);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.length);
        for (int i = 0; i < returnTypes.length; i++) {
            assertEquals(returnTypes[i], result[i].getReturnType());
        }
    }

    @Test
    void testGetOps_withEmptyStrings_shouldHandleCorrectly() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        List<OpMetadata> metadataList = new ArrayList<>();
        
        OpMetadata metadata = new OpMetadata();
        metadata.setName("");
        metadata.setDescription("");
        metadata.setType("");
        metadata.setImpact(MBeanOperationInfo.UNKNOWN);
        
        metadataList.add(metadata);
        opList.setOpMetadatList(metadataList);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0].getName());
        assertEquals("", result[0].getDescription());
    }

    @Test
    void testGetOps_withLargeNumberOfOperations_shouldHandleCorrectly() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        List<OpMetadata> metadataList = new ArrayList<>();
        
        int numOperations = 100;
        for (int i = 0; i < numOperations; i++) {
            OpMetadata metadata = new OpMetadata();
            metadata.setName("operation" + i);
            metadata.setDescription("Description " + i);
            metadata.setType("void");
            metadata.setImpact(MBeanOperationInfo.ACTION);
            metadataList.add(metadata);
        }
        
        opList.setOpMetadatList(metadataList);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNotNull(result);
        assertEquals(numOperations, result.length);
    }

    @Test
    void testGetOps_shouldPreserveOperationOrder() {
        // Arrange
        OpMetadataList opList = new OpMetadataList();
        List<OpMetadata> metadataList = new ArrayList<>();
        
        String[] names = {"first", "second", "third", "fourth"};
        for (String name : names) {
            OpMetadata metadata = new OpMetadata();
            metadata.setName(name);
            metadata.setDescription("Description for " + name);
            metadata.setType("void");
            metadata.setImpact(MBeanOperationInfo.ACTION);
            metadataList.add(metadata);
        }
        
        opList.setOpMetadatList(metadataList);

        // Act
        MBeanOperationInfo[] result = DMBeanUtils.getOps(opList);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.length);
        for (int i = 0; i < names.length; i++) {
            assertEquals(names[i], result[i].getName());
        }
    }
}
