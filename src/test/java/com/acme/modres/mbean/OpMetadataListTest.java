package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanOperationInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpMetadataListTest {

    private OpMetadataList opMetadataList;

    @BeforeEach
    void setUp() {
        opMetadataList = new OpMetadataList();
    }

    @Test
    void testConstructor_shouldCreateEmptyList() {
        // Assert
        assertNotNull(opMetadataList);
        assertNotNull(opMetadataList.getOpMetadatList());
        assertTrue(opMetadataList.getOpMetadatList().isEmpty());
    }

    @Test
    void testAdd_withSingleMetadata_shouldAddToList() {
        // Arrange
        OpMetadata metadata = new OpMetadata("test", "description", "void", MBeanOperationInfo.ACTION);

        // Act
        opMetadataList.add(metadata);

        // Assert
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertEquals(metadata, opMetadataList.getOpMetadatList().get(0));
    }

    @Test
    void testAdd_withMultipleMetadata_shouldAddAllToList() {
        // Arrange
        OpMetadata metadata1 = new OpMetadata("op1", "desc1", "void", MBeanOperationInfo.ACTION);
        OpMetadata metadata2 = new OpMetadata("op2", "desc2", "String", MBeanOperationInfo.INFO);
        OpMetadata metadata3 = new OpMetadata("op3", "desc3", "int", MBeanOperationInfo.ACTION_INFO);

        // Act
        opMetadataList.add(metadata1);
        opMetadataList.add(metadata2);
        opMetadataList.add(metadata3);

        // Assert
        assertEquals(3, opMetadataList.getOpMetadatList().size());
        assertEquals(metadata1, opMetadataList.getOpMetadatList().get(0));
        assertEquals(metadata2, opMetadataList.getOpMetadatList().get(1));
        assertEquals(metadata3, opMetadataList.getOpMetadatList().get(2));
    }

    @Test
    void testGetOpMetadatList_shouldReturnList() {
        // Act
        List<OpMetadata> result = opMetadataList.getOpMetadatList();

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof List);
    }

    @Test
    void testSetOpMetadatList_withNewList_shouldReplaceList() {
        // Arrange
        List<OpMetadata> newList = new ArrayList<>();
        OpMetadata metadata1 = new OpMetadata("op1", "desc1", "void", MBeanOperationInfo.ACTION);
        OpMetadata metadata2 = new OpMetadata("op2", "desc2", "String", MBeanOperationInfo.INFO);
        newList.add(metadata1);
        newList.add(metadata2);

        // Act
        opMetadataList.setOpMetadatList(newList);

        // Assert
        assertEquals(2, opMetadataList.getOpMetadatList().size());
        assertEquals(newList, opMetadataList.getOpMetadatList());
    }

    @Test
    void testSetOpMetadatList_withEmptyList_shouldSetEmptyList() {
        // Arrange
        opMetadataList.add(new OpMetadata("test", "desc", "void", MBeanOperationInfo.ACTION));
        List<OpMetadata> emptyList = new ArrayList<>();

        // Act
        opMetadataList.setOpMetadatList(emptyList);

        // Assert
        assertTrue(opMetadataList.getOpMetadatList().isEmpty());
    }

    @Test
    void testSetOpMetadatList_withNull_shouldSetNull() {
        // Act
        opMetadataList.setOpMetadatList(null);

        // Assert
        assertNull(opMetadataList.getOpMetadatList());
    }

    @Test
    void testAdd_withNullMetadata_shouldAddNull() {
        // Act
        opMetadataList.add(null);

        // Assert
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertNull(opMetadataList.getOpMetadatList().get(0));
    }

    @Test
    void testAdd_multipleNullMetadata_shouldAddAllNulls() {
        // Act
        opMetadataList.add(null);
        opMetadataList.add(null);
        opMetadataList.add(null);

        // Assert
        assertEquals(3, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testGetOpMetadatList_afterMultipleAdds_shouldReturnAllItems() {
        // Arrange
        int count = 10;
        for (int i = 0; i < count; i++) {
            opMetadataList.add(new OpMetadata("op" + i, "desc" + i, "void", MBeanOperationInfo.ACTION));
        }

        // Act
        List<OpMetadata> result = opMetadataList.getOpMetadatList();

        // Assert
        assertEquals(count, result.size());
    }

    @Test
    void testAdd_shouldPreserveOrder() {
        // Arrange
        OpMetadata metadata1 = new OpMetadata("first", "desc1", "void", MBeanOperationInfo.ACTION);
        OpMetadata metadata2 = new OpMetadata("second", "desc2", "String", MBeanOperationInfo.INFO);
        OpMetadata metadata3 = new OpMetadata("third", "desc3", "int", MBeanOperationInfo.ACTION_INFO);

        // Act
        opMetadataList.add(metadata1);
        opMetadataList.add(metadata2);
        opMetadataList.add(metadata3);

        // Assert
        assertEquals("first", opMetadataList.getOpMetadatList().get(0).getName());
        assertEquals("second", opMetadataList.getOpMetadatList().get(1).getName());
        assertEquals("third", opMetadataList.getOpMetadatList().get(2).getName());
    }

    @Test
    void testSetOpMetadatList_shouldReplaceExistingList() {
        // Arrange
        opMetadataList.add(new OpMetadata("old1", "desc1", "void", MBeanOperationInfo.ACTION));
        opMetadataList.add(new OpMetadata("old2", "desc2", "String", MBeanOperationInfo.INFO));

        List<OpMetadata> newList = new ArrayList<>();
        newList.add(new OpMetadata("new1", "desc1", "int", MBeanOperationInfo.ACTION));

        // Act
        opMetadataList.setOpMetadatList(newList);

        // Assert
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertEquals("new1", opMetadataList.getOpMetadatList().get(0).getName());
    }

    @Test
    void testAdd_withLargeNumberOfItems_shouldHandleCorrectly() {
        // Arrange
        int largeCount = 1000;

        // Act
        for (int i = 0; i < largeCount; i++) {
            opMetadataList.add(new OpMetadata("op" + i, "desc" + i, "void", MBeanOperationInfo.ACTION));
        }

        // Assert
        assertEquals(largeCount, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testGetOpMetadatList_shouldReturnMutableList() {
        // Arrange
        opMetadataList.add(new OpMetadata("test", "desc", "void", MBeanOperationInfo.ACTION));

        // Act
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        list.add(new OpMetadata("new", "desc", "String", MBeanOperationInfo.INFO));

        // Assert
        assertEquals(2, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAdd_withSameMetadataMultipleTimes_shouldAddMultipleTimes() {
        // Arrange
        OpMetadata metadata = new OpMetadata("test", "desc", "void", MBeanOperationInfo.ACTION);

        // Act
        opMetadataList.add(metadata);
        opMetadataList.add(metadata);
        opMetadataList.add(metadata);

        // Assert
        assertEquals(3, opMetadataList.getOpMetadatList().size());
    }
}
