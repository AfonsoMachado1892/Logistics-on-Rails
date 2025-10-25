package LAPR3.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class SegmentTest {

    @Test
    public void testValidConstructor() {
        Segment seg = new Segment(1, 10, 1, true, 5000, 10000, 2);

        assertEquals(1, seg.getId());
        assertEquals(10, seg.getLineId());
        assertEquals(1, seg.getOrder());
        assertTrue(seg.isElectrified());
        assertEquals(5000, seg.getMaxWeightKgPerMeter(), 0.01);
        assertEquals(10000, seg.getLengthMeters(), 0.01);
        assertEquals(2, seg.getNumberTracks());
    }

    @Test
    public void testGetLengthKilometers() {
        Segment seg = new Segment(1, 10, 1, true, 5000, 12345, 2);
        assertEquals(12.345, seg.getLengthKilometers(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NegativeId() {
        new Segment(-1, 10, 1, true, 5000, 10000, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NegativeLineId() {
        new Segment(1, -10, 1, true, 5000, 10000, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroOrder() {
        new Segment(1, 10, 0, true, 5000, 10000, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroWeight() {
        new Segment(1, 10, 1, true, 0, 10000, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroLength() {
        new Segment(1, 10, 1, true, 5000, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroTracks() {
        new Segment(1, 10, 1, true, 5000, 10000, 0);
    }

    @Test
    public void testCompareTo() {
        Segment seg1 = new Segment(1, 10, 1, true, 5000, 100, 1);
        Segment seg2 = new Segment(2, 10, 2, true, 5000, 100, 1);
        Segment seg3 = new Segment(3, 10, 1, true, 5000, 100, 1); // Ordem igual

        assertTrue(seg1.compareTo(seg2) < 0);
        assertTrue(seg2.compareTo(seg1) > 0);
        assertEquals(0, seg1.compareTo(seg3));
    }

    @Test
    public void testEqualsAndHashCode() {
        Segment seg1 = new Segment(1, 10, 1, true, 5000, 100, 1);
        Segment seg2 = new Segment(1, 20, 2, false, 1000, 200, 2);
        Segment seg3 = new Segment(2, 10, 1, true, 5000, 100, 1);

        assertEquals(seg1, seg2);
        assertNotEquals(seg1, seg3);
        assertEquals(seg1.hashCode(), seg2.hashCode());
        assertNotEquals(seg1.hashCode(), seg3.hashCode());
    }
}
