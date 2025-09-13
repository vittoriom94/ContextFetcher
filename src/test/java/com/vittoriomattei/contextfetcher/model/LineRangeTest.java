package com.vittoriomattei.contextfetcher.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LineRangeTest {

    @Test
    public void testCreation() {
        LineRange range = new LineRange(1, 10);
        assertEquals(1, range.startLine());
        assertEquals(10, range.endLine());
    }

    @Test
    public void testEquality() {
        LineRange range1 = new LineRange(1, 10);
        LineRange range2 = new LineRange(1, 10);
        LineRange range3 = new LineRange(2, 10);

        assertEquals(range1, range2);
        assertNotEquals(range1, range3);
        assertEquals(range1.hashCode(), range2.hashCode());
    }

    @Test
    public void testToString() {
        LineRange range = new LineRange(5, 15);
        assertEquals("5 - 15", range.toString());
    }

    @Test
    public void testSingleLine() {
        LineRange range = new LineRange(5, 5);
        assertEquals(5, range.startLine());
        assertEquals(5, range.endLine());
    }
}