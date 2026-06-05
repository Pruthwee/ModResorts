package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CustomPermissionTest {

    @Test
    void testConstructor_withName_shouldCreatePermission() {
        // Arrange
        String name = "testPermission";

        // Act
        CustomPermission permission = new CustomPermission(name);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testConstructor_withNameAndActions_shouldCreatePermission() {
        // Arrange
        String name = "testPermission";
        String actions = "read,write";

        // Act
        CustomPermission permission = new CustomPermission(name, actions);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testGetName_shouldReturnPermissionName() {
        // Arrange
        String name = "myPermission";
        CustomPermission permission = new CustomPermission(name);

        // Act
        String result = permission.getName();

        // Assert
        assertEquals(name, result);
    }

    @Test
    void testConstructor_withEmptyName_shouldCreatePermission() {
        // Arrange
        String name = "";

        // Act
        CustomPermission permission = new CustomPermission(name);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testConstructor_withNullActions_shouldCreatePermission() {
        // Arrange
        String name = "testPermission";

        // Act
        CustomPermission permission = new CustomPermission(name, null);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testConstructor_withEmptyActions_shouldCreatePermission() {
        // Arrange
        String name = "testPermission";
        String actions = "";

        // Act
        CustomPermission permission = new CustomPermission(name, actions);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testConstructor_withDifferentNames_shouldCreateDifferentPermissions() {
        // Arrange
        String name1 = "permission1";
        String name2 = "permission2";

        // Act
        CustomPermission permission1 = new CustomPermission(name1);
        CustomPermission permission2 = new CustomPermission(name2);

        // Assert
        assertNotEquals(permission1.getName(), permission2.getName());
    }

    @Test
    void testConstructor_withLongName_shouldCreatePermission() {
        // Arrange
        String longName = "A".repeat(1000);

        // Act
        CustomPermission permission = new CustomPermission(longName);

        // Assert
        assertNotNull(permission);
        assertEquals(longName, permission.getName());
    }

    @Test
    void testConstructor_withSpecialCharactersInName_shouldCreatePermission() {
        // Arrange
        String name = "permission.with.dots";

        // Act
        CustomPermission permission = new CustomPermission(name);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testConstructor_withWildcardName_shouldCreatePermission() {
        // Arrange
        String name = "permission.*";

        // Act
        CustomPermission permission = new CustomPermission(name);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testConstructor_withMultipleActions_shouldCreatePermission() {
        // Arrange
        String name = "testPermission";
        String actions = "read,write,execute";

        // Act
        CustomPermission permission = new CustomPermission(name, actions);

        // Assert
        assertNotNull(permission);
        assertEquals(name, permission.getName());
    }

    @Test
    void testImplies_withSamePermission_shouldReturnTrue() {
        // Arrange
        CustomPermission permission1 = new CustomPermission("test");
        CustomPermission permission2 = new CustomPermission("test");

        // Act
        boolean result = permission1.implies(permission2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testImplies_withDifferentPermission_shouldReturnFalse() {
        // Arrange
        CustomPermission permission1 = new CustomPermission("test1");
        CustomPermission permission2 = new CustomPermission("test2");

        // Act
        boolean result = permission1.implies(permission2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testImplies_withWildcardPermission_shouldReturnTrue() {
        // Arrange
        CustomPermission permission1 = new CustomPermission("test.*");
        CustomPermission permission2 = new CustomPermission("test.read");

        // Act
        boolean result = permission1.implies(permission2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEquals_withSamePermission_shouldReturnTrue() {
        // Arrange
        CustomPermission permission1 = new CustomPermission("test");
        CustomPermission permission2 = new CustomPermission("test");

        // Act
        boolean result = permission1.equals(permission2);

        // Assert
        assertTrue(result);
    }

    @Test
    void testEquals_withDifferentPermission_shouldReturnFalse() {
        // Arrange
        CustomPermission permission1 = new CustomPermission("test1");
        CustomPermission permission2 = new CustomPermission("test2");

        // Act
        boolean result = permission1.equals(permission2);

        // Assert
        assertFalse(result);
    }

    @Test
    void testHashCode_withSamePermission_shouldReturnSameHashCode() {
        // Arrange
        CustomPermission permission1 = new CustomPermission("test");
        CustomPermission permission2 = new CustomPermission("test");

        // Act & Assert
        assertEquals(permission1.hashCode(), permission2.hashCode());
    }

    @Test
    void testGetActions_shouldReturnEmptyString() {
        // Arrange
        CustomPermission permission = new CustomPermission("test");

        // Act
        String actions = permission.getActions();

        // Assert
        assertEquals("", actions);
    }
}
