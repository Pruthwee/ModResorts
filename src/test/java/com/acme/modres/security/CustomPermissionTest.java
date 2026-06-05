package com.acme.modres.security;

import static org.junit.jupiter.api.Assertions.*;

import java.security.Permission;

import org.junit.jupiter.api.Test;

class CustomPermissionTest {

    @Test
    void testConstructor_withName() {
        CustomPermission permission = new CustomPermission("testPermission");
        
        assertNotNull(permission);
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testConstructor_withNameAndActions() {
        CustomPermission permission = new CustomPermission("testPermission", "read,write");
        
        assertNotNull(permission);
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testGetName() {
        CustomPermission permission = new CustomPermission("myPermission");
        
        assertEquals("myPermission", permission.getName());
    }

    @Test
    void testConstructor_withEmptyName() {
        CustomPermission permission = new CustomPermission("");
        
        assertNotNull(permission);
        assertEquals("", permission.getName());
    }

    @Test
    void testConstructor_withNullActions() {
        CustomPermission permission = new CustomPermission("testPermission", null);
        
        assertNotNull(permission);
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testConstructor_withEmptyActions() {
        CustomPermission permission = new CustomPermission("testPermission", "");
        
        assertNotNull(permission);
        assertEquals("testPermission", permission.getName());
    }

    @Test
    void testInstanceOfBasicPermission() {
        CustomPermission permission = new CustomPermission("testPermission");
        
        assertTrue(permission instanceof java.security.BasicPermission);
    }

    @Test
    void testInstanceOfPermission() {
        CustomPermission permission = new CustomPermission("testPermission");
        
        assertTrue(permission instanceof Permission);
    }

    @Test
    void testConstructor_withDifferentNames() {
        CustomPermission perm1 = new CustomPermission("permission1");
        CustomPermission perm2 = new CustomPermission("permission2");
        
        assertNotEquals(perm1.getName(), perm2.getName());
    }

    @Test
    void testConstructor_withSpecialCharactersInName() {
        CustomPermission permission = new CustomPermission("test.permission.*");
        
        assertEquals("test.permission.*", permission.getName());
    }

    @Test
    void testConstructor_withWildcardName() {
        CustomPermission permission = new CustomPermission("*");
        
        assertEquals("*", permission.getName());
    }

    @Test
    void testConstructor_withDottedName() {
        CustomPermission permission = new CustomPermission("com.acme.permission");
        
        assertEquals("com.acme.permission", permission.getName());
    }

    @Test
    void testConstructor_withMultipleActions() {
        CustomPermission permission = new CustomPermission("testPermission", "read,write,execute");
        
        assertNotNull(permission);
    }

    @Test
    void testEquals_samePermission() {
        CustomPermission perm1 = new CustomPermission("testPermission");
        CustomPermission perm2 = new CustomPermission("testPermission");
        
        assertEquals(perm1, perm2);
    }

    @Test
    void testHashCode_samePermission() {
        CustomPermission perm1 = new CustomPermission("testPermission");
        CustomPermission perm2 = new CustomPermission("testPermission");
        
        assertEquals(perm1.hashCode(), perm2.hashCode());
    }
}
