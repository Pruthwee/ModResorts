package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import javax.management.MBeanOperationInfo;

import org.junit.jupiter.api.Test;

class OpMetadataTest {

    @Test
    void testDefaultConstructor_shouldCreateInstance() {
        // Act
        OpMetadata metadata = new OpMetadata();

        // Assert
        assertNotNull(metadata);
    }

    @Test
    void testParameterizedConstructor_shouldSetAllFields() {
        // Arrange
        String name = "testOperation";
        String description = "Test description";
        String type = "java.lang.String";
        int impact = MBeanOperationInfo.ACTION;

        // Act
        OpMetadata metadata = new OpMetadata(name, description, type, impact);

        // Assert
        assertEquals(name, metadata.getName());
        assertEquals(description, metadata.getDescription());
        assertEquals(type, metadata.getType());
        assertEquals(impact, metadata.getImpact());
    }

    @Test
    void testSetName_shouldSetName() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        String name = "operationName";

        // Act
        metadata.setName(name);

        // Assert
        assertEquals(name, metadata.getName());
    }

    @Test
    void testSetDescription_shouldSetDescription() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        String description = "Operation description";

        // Act
        metadata.setDescription(description);

        // Assert
        assertEquals(description, metadata.getDescription());
    }

    @Test
    void testSetType_shouldSetType() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        String type = "void";

        // Act
        metadata.setType(type);

        // Assert
        assertEquals(type, metadata.getType());
    }

    @Test
    void testSetImpact_shouldSetImpact() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        int impact = MBeanOperationInfo.INFO;

        // Act
        metadata.setImpact(impact);

        // Assert
        assertEquals(impact, metadata.getImpact());
    }

    @Test
    void testGetName_withNullName_shouldReturnNull() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        String result = metadata.getName();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetDescription_withNullDescription_shouldReturnNull() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        String result = metadata.getDescription();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetType_withNullType_shouldReturnNull() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        String result = metadata.getType();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetImpact_withDefaultValue_shouldReturnZero() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        int result = metadata.getImpact();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void testSetName_withEmptyString_shouldSetEmptyString() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        metadata.setName("");

        // Assert
        assertEquals("", metadata.getName());
    }

    @Test
    void testSetDescription_withEmptyString_shouldSetEmptyString() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        metadata.setDescription("");

        // Assert
        assertEquals("", metadata.getDescription());
    }

    @Test
    void testSetType_withEmptyString_shouldSetEmptyString() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        metadata.setType("");

        // Assert
        assertEquals("", metadata.getType());
    }

    @Test
    void testSetImpact_withDifferentValues_shouldSetCorrectly() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        int[] impacts = {
            MBeanOperationInfo.ACTION,
            MBeanOperationInfo.INFO,
            MBeanOperationInfo.ACTION_INFO,
            MBeanOperationInfo.UNKNOWN
        };

        // Act & Assert
        for (int impact : impacts) {
            metadata.setImpact(impact);
            assertEquals(impact, metadata.getImpact());
        }
    }

    @Test
    void testParameterizedConstructor_withNullValues_shouldSetNullValues() {
        // Act
        OpMetadata metadata = new OpMetadata(null, null, null, 0);

        // Assert
        assertNull(metadata.getName());
        assertNull(metadata.getDescription());
        assertNull(metadata.getType());
        assertEquals(0, metadata.getImpact());
    }

    @Test
    void testSetName_withLongString_shouldSetCorrectly() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        String longName = "A".repeat(1000);

        // Act
        metadata.setName(longName);

        // Assert
        assertEquals(longName, metadata.getName());
    }

    @Test
    void testSetDescription_withLongString_shouldSetCorrectly() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        String longDescription = "B".repeat(1000);

        // Act
        metadata.setDescription(longDescription);

        // Assert
        assertEquals(longDescription, metadata.getDescription());
    }

    @Test
    void testSetType_withComplexType_shouldSetCorrectly() {
        // Arrange
        OpMetadata metadata = new OpMetadata();
        String complexType = "java.util.List<java.lang.String>";

        // Act
        metadata.setType(complexType);

        // Assert
        assertEquals(complexType, metadata.getType());
    }

    @Test
    void testMultipleSettersOnSameInstance_shouldUpdateCorrectly() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        metadata.setName("name1");
        metadata.setName("name2");
        metadata.setDescription("desc1");
        metadata.setDescription("desc2");

        // Assert
        assertEquals("name2", metadata.getName());
        assertEquals("desc2", metadata.getDescription());
    }

    @Test
    void testSetImpact_withNegativeValue_shouldSetNegativeValue() {
        // Arrange
        OpMetadata metadata = new OpMetadata();

        // Act
        metadata.setImpact(-1);

        // Assert
        assertEquals(-1, metadata.getImpact());
    }
}
