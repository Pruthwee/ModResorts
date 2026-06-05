package com.acme.modres.mbean;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpMetadataListTest {

    private OpMetadataList opMetadataList;

    @BeforeEach
    void setUp() {
        opMetadataList = new OpMetadataList();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(opMetadataList);
        assertNotNull(opMetadataList.getOpMetadatList());
    }

    @Test
    void testGetOpMetadatList_initiallyEmpty() {
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testAdd_singleElement() {
        OpMetadata opMetadata = new OpMetadata("op1", "Operation 1", "void", 1);
        opMetadataList.add(opMetadata);
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertEquals(opMetadata, opMetadataList.getOpMetadatList().get(0));
    }

    @Test
    void testAdd_multipleElements() {
        OpMetadata op1 = new OpMetadata("op1", "Operation 1", "void", 1);
        OpMetadata op2 = new OpMetadata("op2", "Operation 2", "String", 2);
        OpMetadata op3 = new OpMetadata("op3", "Operation 3", "int", 3);
        
        opMetadataList.add(op1);
        opMetadataList.add(op2);
        opMetadataList.add(op3);
        
        assertEquals(3, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAdd_preservesOrder() {
        OpMetadata op1 = new OpMetadata("first", "First", "void", 1);
        OpMetadata op2 = new OpMetadata("second", "Second", "void", 2);
        OpMetadata op3 = new OpMetadata("third", "Third", "void", 3);
        
        opMetadataList.add(op1);
        opMetadataList.add(op2);
        opMetadataList.add(op3);
        
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        assertEquals("first", list.get(0).getName());
        assertEquals("second", list.get(1).getName());
        assertEquals("third", list.get(2).getName());
    }

    @Test
    void testSetOpMetadatList() {
        List<OpMetadata> newList = new ArrayList<>();
        newList.add(new OpMetadata("op1", "Operation 1", "void", 1));
        newList.add(new OpMetadata("op2", "Operation 2", "String", 2));
        
        opMetadataList.setOpMetadatList(newList);
        
        assertEquals(2, opMetadataList.getOpMetadatList().size());
        assertEquals(newList, opMetadataList.getOpMetadatList());
    }

    @Test
    void testSetOpMetadatList_withEmptyList() {
        List<OpMetadata> emptyList = new ArrayList<>();
        opMetadataList.setOpMetadatList(emptyList);
        
        assertTrue(opMetadataList.getOpMetadatList().isEmpty());
    }

    @Test
    void testSetOpMetadatList_withNull() {
        opMetadataList.setOpMetadatList(null);
        
        assertNull(opMetadataList.getOpMetadatList());
    }

    @Test
    void testAdd_afterSetOpMetadatList() {
        List<OpMetadata> newList = new ArrayList<>();
        newList.add(new OpMetadata("op1", "Operation 1", "void", 1));
        opMetadataList.setOpMetadatList(newList);
        
        OpMetadata op2 = new OpMetadata("op2", "Operation 2", "String", 2);
        opMetadataList.add(op2);
        
        assertEquals(2, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAdd_withNullElement() {
        opMetadataList.add(null);
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
        assertNull(opMetadataList.getOpMetadatList().get(0));
    }

    @Test
    void testGetOpMetadatList_returnsModifiableList() {
        List<OpMetadata> list = opMetadataList.getOpMetadatList();
        OpMetadata op = new OpMetadata("op1", "Operation 1", "void", 1);
        list.add(op);
        
        assertEquals(1, opMetadataList.getOpMetadatList().size());
    }

    @Test
    void testAdd_multipleInvocations() {
        for (int i = 0; i < 10; i++) {
            opMetadataList.add(new OpMetadata("op" + i, "Operation " + i, "void", i));
        }
        
        assertEquals(10, opMetadataList.getOpMetadatList().size());
    }
}
