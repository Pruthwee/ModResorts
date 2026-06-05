package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppInfoTest {

    private AppInfo appInfo;

    @BeforeEach
    void setUp() {
        appInfo = new AppInfo();
    }

    @Test
    void testConstructor_shouldInitializeMBeanInfo() {
        // Assert
        assertNotNull(appInfo);
        assertNotNull(appInfo.getMBeanInfo());
    }

    @Test
    void testGetMBeanInfo_shouldReturnNonNull() {
        // Act
        MBeanInfo mBeanInfo = appInfo.getMBeanInfo();

        // Assert
        assertNotNull(mBeanInfo);
    }

    @Test
    void testGetMBeanInfo_shouldHaveCorrectClassName() {
        // Act
        MBeanInfo mBeanInfo = appInfo.getMBeanInfo();

        // Assert
        assertEquals("com.acme.modres.mbean.AppInfo", mBeanInfo.getClassName());
    }

    @Test
    void testGetMBeanInfo_shouldHaveDescription() {
        // Act
        MBeanInfo mBeanInfo = appInfo.getMBeanInfo();

        // Assert
        assertNotNull(mBeanInfo.getDescription());
        assertEquals("Configurable App Info", mBeanInfo.getDescription());
    }

    @Test
    void testInvoke_withIncreaseMaxLimit_shouldReturnSuccessMessage() throws MBeanException, ReflectionException {
        // Act
        Object result = appInfo.invoke("increaseMaxLimit", null, null);

        // Assert
        assertNotNull(result);
        assertEquals("Max limit increased", result);
    }

    @Test
    void testInvoke_withResetMaxLimit_shouldReturnSuccessMessage() throws MBeanException, ReflectionException {
        // Act
        Object result = appInfo.invoke("resetMaxLimit", null, null);

        // Assert
        assertNotNull(result);
        assertEquals("Max limit reset", result);
    }

    @Test
    void testInvoke_withUnsupportedOperation_shouldThrowMBeanException() {
        // Act & Assert
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke("unsupportedOperation", null, null);
        });
    }

    @Test
    void testInvoke_withNullActionName_shouldThrowException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            appInfo.invoke(null, null, null);
        });
    }

    @Test
    void testInvoke_withEmptyActionName_shouldThrowMBeanException() {
        // Act & Assert
        assertThrows(MBeanException.class, () -> {
            appInfo.invoke("", null, null);
        });
    }

    @Test
    void testInvoke_withIncreaseMaxLimit_withParameters_shouldSucceed() throws MBeanException, ReflectionException {
        // Act
        Object result = appInfo.invoke("increaseMaxLimit", new Object[]{}, new String[]{});

        // Assert
        assertEquals("Max limit increased", result);
    }

    @Test
    void testInvoke_withResetMaxLimit_withParameters_shouldSucceed() throws MBeanException, ReflectionException {
        // Act
        Object result = appInfo.invoke("resetMaxLimit", new Object[]{}, new String[]{});

        // Assert
        assertEquals("Max limit reset", result);
    }

    @Test
    void testGetAttribute_shouldReturnNull() throws AttributeNotFoundException, MBeanException, ReflectionException {
        // Act
        Object result = appInfo.getAttribute("anyAttribute");

        // Assert
        assertNull(result);
    }

    @Test
    void testSetAttribute_shouldNotThrowException() {
        // Arrange
        Attribute attribute = new Attribute("testAttribute", "testValue");

        // Act & Assert
        assertDoesNotThrow(() -> appInfo.setAttribute(attribute));
    }

    @Test
    void testGetAttributes_shouldReturnNull() {
        // Act
        AttributeList result = appInfo.getAttributes(new String[]{"attr1", "attr2"});

        // Assert
        assertNull(result);
    }

    @Test
    void testSetAttributes_shouldReturnNull() {
        // Arrange
        AttributeList attributes = new AttributeList();
        attributes.add(new Attribute("attr1", "value1"));

        // Act
        AttributeList result = appInfo.setAttributes(attributes);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetMBeanInfo_shouldHaveOperations() {
        // Act
        MBeanInfo mBeanInfo = appInfo.getMBeanInfo();

        // Assert
        assertNotNull(mBeanInfo.getOperations());
    }

    @Test
    void testInvoke_multipleInvocations_shouldWorkCorrectly() throws MBeanException, ReflectionException {
        // Act
        Object result1 = appInfo.invoke("increaseMaxLimit", null, null);
        Object result2 = appInfo.invoke("resetMaxLimit", null, null);
        Object result3 = appInfo.invoke("increaseMaxLimit", null, null);

        // Assert
        assertEquals("Max limit increased", result1);
        assertEquals("Max limit reset", result2);
        assertEquals("Max limit increased", result3);
    }

    @Test
    void testGetAttribute_withNullAttribute_shouldReturnNull() throws AttributeNotFoundException, MBeanException, ReflectionException {
        // Act
        Object result = appInfo.getAttribute(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetAttributes_withEmptyArray_shouldReturnNull() {
        // Act
        AttributeList result = appInfo.getAttributes(new String[]{});

        // Assert
        assertNull(result);
    }

    @Test
    void testSetAttributes_withEmptyList_shouldReturnNull() {
        // Arrange
        AttributeList emptyList = new AttributeList();

        // Act
        AttributeList result = appInfo.setAttributes(emptyList);

        // Assert
        assertNull(result);
    }
}
