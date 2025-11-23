package ESINF.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for GeographicRegion class.
 * Tests cover validation, boundary conditions, geometric calculations, and edge cases.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class GeographicRegionTest {

    // ==================== CONSTRUCTOR VALIDATION TESTS ====================

    /**
     * Test that a valid region with standard coordinates is created successfully.
     */
    @Test
    public void testConstructor_ValidRegion_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertNotNull(region);
    }

    /**
     * Test that latMin below -90 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LatMinBelowMinimum_ShouldThrowException() {
        new GeographicRegion(-91.0, 42.0, -9.0, -6.0);
    }

    /**
     * Test that latMin above 90 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LatMinAboveMaximum_ShouldThrowException() {
        new GeographicRegion(91.0, 42.0, -9.0, -6.0);
    }

    /**
     * Test that latMax below -90 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LatMaxBelowMinimum_ShouldThrowException() {
        new GeographicRegion(38.0, -91.0, -9.0, -6.0);
    }

    /**
     * Test that latMax above 90 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LatMaxAboveMaximum_ShouldThrowException() {
        new GeographicRegion(38.0, 91.0, -9.0, -6.0);
    }

    /**
     * Test that lonMin below -180 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LonMinBelowMinimum_ShouldThrowException() {
        new GeographicRegion(38.0, 42.0, -181.0, -6.0);
    }

    /**
     * Test that lonMin above 180 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LonMinAboveMaximum_ShouldThrowException() {
        new GeographicRegion(38.0, 42.0, 181.0, -6.0);
    }

    /**
     * Test that lonMax below -180 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LonMaxBelowMinimum_ShouldThrowException() {
        new GeographicRegion(38.0, 42.0, -9.0, -181.0);
    }

    /**
     * Test that lonMax above 180 throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LonMaxAboveMaximum_ShouldThrowException() {
        new GeographicRegion(38.0, 42.0, -9.0, 181.0);
    }

    /**
     * Test that latMin greater than latMax throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LatMinGreaterThanLatMax_ShouldThrowException() {
        new GeographicRegion(45.0, 40.0, -9.0, -6.0);
    }

    /**
     * Test that lonMin greater than lonMax throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_LonMinGreaterThanLonMax_ShouldThrowException() {
        new GeographicRegion(38.0, 42.0, -5.0, -10.0);
    }

    /**
     * Test that a region with equal latitudes (line) is valid.
     */
    @Test
    public void testConstructor_EqualLatitudes_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -9.0, -6.0);
        assertNotNull(region);
    }

    /**
     * Test that a region with equal longitudes (line) is valid.
     */
    @Test
    public void testConstructor_EqualLongitudes_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -8.0, -8.0);
        assertNotNull(region);
    }

    /**
     * Test that a point region (all coordinates equal) is valid.
     */
    @Test
    public void testConstructor_PointRegion_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        assertNotNull(region);
    }

    /**
     * Test boundary value: latitude at exact minimum (-90).
     */
    @Test
    public void testConstructor_LatitudeAtMinimumBoundary_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(-90.0, -89.0, -9.0, -6.0);
        assertNotNull(region);
    }

    /**
     * Test boundary value: latitude at exact maximum (90).
     */
    @Test
    public void testConstructor_LatitudeAtMaximumBoundary_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(89.0, 90.0, -9.0, -6.0);
        assertNotNull(region);
    }

    /**
     * Test boundary value: longitude at exact minimum (-180).
     */
    @Test
    public void testConstructor_LongitudeAtMinimumBoundary_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -180.0, -179.0);
        assertNotNull(region);
    }

    /**
     * Test boundary value: longitude at exact maximum (180).
     */
    @Test
    public void testConstructor_LongitudeAtMaximumBoundary_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, 179.0, 180.0);
        assertNotNull(region);
    }

    /**
     * Test global region covering entire Earth.
     */
    @Test
    public void testConstructor_GlobalRegion_ShouldCreate() {
        GeographicRegion region = new GeographicRegion(-90.0, 90.0, -180.0, 180.0);
        assertNotNull(region);
    }

    // ==================== CONTAINS METHOD TESTS ====================

    /**
     * Test that a point inside the region returns true.
     */
    @Test
    public void testContains_PointInsideRegion_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.contains(40.0, -8.0));
    }

    /**
     * Test that a point outside the region (north) returns false.
     */
    @Test
    public void testContains_PointOutsideNorth_ShouldReturnFalse() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertFalse(region.contains(43.0, -8.0));
    }

    /**
     * Test that a point outside the region (south) returns false.
     */
    @Test
    public void testContains_PointOutsideSouth_ShouldReturnFalse() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertFalse(region.contains(37.0, -8.0));
    }

    /**
     * Test that a point outside the region (east) returns false.
     */
    @Test
    public void testContains_PointOutsideEast_ShouldReturnFalse() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertFalse(region.contains(40.0, -5.0));
    }

    /**
     * Test that a point outside the region (west) returns false.
     */
    @Test
    public void testContains_PointOutsideWest_ShouldReturnFalse() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertFalse(region.contains(40.0, -10.0));
    }

    /**
     * Test that a point on the minimum latitude boundary is included.
     */
    @Test
    public void testContains_PointOnMinLatBoundary_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.contains(38.0, -8.0));
    }

    /**
     * Test that a point on the maximum latitude boundary is included.
     */
    @Test
    public void testContains_PointOnMaxLatBoundary_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.contains(42.0, -8.0));
    }

    /**
     * Test that a point on the minimum longitude boundary is included.
     */
    @Test
    public void testContains_PointOnMinLonBoundary_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.contains(40.0, -9.0));
    }

    /**
     * Test that a point on the maximum longitude boundary is included.
     */
    @Test
    public void testContains_PointOnMaxLonBoundary_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.contains(40.0, -6.0));
    }

    /**
     * Test that a corner point (min lat, min lon) is included.
     */
    @Test
    public void testContains_CornerMinLatMinLon_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.contains(38.0, -9.0));
    }

    /**
     * Test that a corner point (max lat, max lon) is included.
     */
    @Test
    public void testContains_CornerMaxLatMaxLon_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.contains(42.0, -6.0));
    }

    /**
     * Test contains with a point region (single point).
     */
    @Test
    public void testContains_PointRegionExactMatch_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        assertTrue(region.contains(40.0, -8.0));
    }

    /**
     * Test contains with a point region but different coordinates.
     */
    @Test
    public void testContains_PointRegionNoMatch_ShouldReturnFalse() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        assertFalse(region.contains(40.1, -8.0));
    }

    // ==================== IS VALID METHOD TESTS ====================

    /**
     * Test that a properly constructed region is valid.
     */
    @Test
    public void testIsValid_ProperRegion_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.isValid());
    }

    /**
     * Test that a point region is considered valid.
     */
    @Test
    public void testIsValid_PointRegion_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        assertTrue(region.isValid());
    }

    /**
     * Test that a line region (latitude) is valid.
     */
    @Test
    public void testIsValid_LineRegionLatitude_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -9.0, -6.0);
        assertTrue(region.isValid());
    }

    /**
     * Test that a line region (longitude) is valid.
     */
    @Test
    public void testIsValid_LineRegionLongitude_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -8.0, -8.0);
        assertTrue(region.isValid());
    }

    // ==================== AREA CALCULATION TESTS ====================

    /**
     * Test area calculation for a standard rectangular region.
     */
    @Test
    public void testGetAreaSquareDegrees_StandardRegion_ShouldReturnCorrectArea() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(12.0, region.getAreaSquareDegrees(), 0.0001);
    }

    /**
     * Test area calculation for a point region (should be zero).
     */
    @Test
    public void testGetAreaSquareDegrees_PointRegion_ShouldReturnZero() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        assertEquals(0.0, region.getAreaSquareDegrees(), 0.0001);
    }

    /**
     * Test area calculation for a line region (latitude).
     */
    @Test
    public void testGetAreaSquareDegrees_LineRegionLatitude_ShouldReturnZero() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -9.0, -6.0);
        assertEquals(0.0, region.getAreaSquareDegrees(), 0.0001);
    }

    /**
     * Test area calculation for a line region (longitude).
     */
    @Test
    public void testGetAreaSquareDegrees_LineRegionLongitude_ShouldReturnZero() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -8.0, -8.0);
        assertEquals(0.0, region.getAreaSquareDegrees(), 0.0001);
    }

    /**
     * Test area calculation for a 1x1 degree region.
     */
    @Test
    public void testGetAreaSquareDegrees_OneByOneRegion_ShouldReturnOne() {
        GeographicRegion region = new GeographicRegion(40.0, 41.0, -8.0, -7.0);
        assertEquals(1.0, region.getAreaSquareDegrees(), 0.0001);
    }

    /**
     * Test area calculation for global region.
     */
    @Test
    public void testGetAreaSquareDegrees_GlobalRegion_ShouldReturnMaxArea() {
        GeographicRegion region = new GeographicRegion(-90.0, 90.0, -180.0, 180.0);
        assertEquals(64800.0, region.getAreaSquareDegrees(), 0.0001);
    }

    // ==================== CENTER CALCULATION TESTS ====================

    /**
     * Test center calculation for a standard region.
     */
    @Test
    public void testGetCenter_StandardRegion_ShouldReturnCenter() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        double[] center = region.getCenter();
        assertEquals(40.0, center[0], 0.0001);
    }

    /**
     * Test center longitude calculation for a standard region.
     */
    @Test
    public void testGetCenter_StandardRegion_ShouldReturnCenterLongitude() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        double[] center = region.getCenter();
        assertEquals(-7.5, center[1], 0.0001);
    }

    /**
     * Test center of a point region.
     */
    @Test
    public void testGetCenter_PointRegion_ShouldReturnSamePoint() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        double[] center = region.getCenter();
        assertEquals(40.0, center[0], 0.0001);
    }

    /**
     * Test center array length.
     */
    @Test
    public void testGetCenter_ShouldReturnArrayOfLengthTwo() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        double[] center = region.getCenter();
        assertEquals(2, center.length);
    }

    /**
     * Test center of global region should be (0, 0).
     */
    @Test
    public void testGetCenter_GlobalRegion_ShouldReturnZeroZero() {
        GeographicRegion region = new GeographicRegion(-90.0, 90.0, -180.0, 180.0);
        double[] center = region.getCenter();
        assertEquals(0.0, center[0], 0.0001);
    }

    // ==================== WIDTH AND HEIGHT TESTS ====================

    /**
     * Test width calculation for a standard region.
     */
    @Test
    public void testGetWidthDegrees_StandardRegion_ShouldReturnCorrectWidth() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(3.0, region.getWidthDegrees(), 0.0001);
    }

    /**
     * Test height calculation for a standard region.
     */
    @Test
    public void testGetHeightDegrees_StandardRegion_ShouldReturnCorrectHeight() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(4.0, region.getHeightDegrees(), 0.0001);
    }

    /**
     * Test width of a point region should be zero.
     */
    @Test
    public void testGetWidthDegrees_PointRegion_ShouldReturnZero() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        assertEquals(0.0, region.getWidthDegrees(), 0.0001);
    }

    /**
     * Test height of a point region should be zero.
     */
    @Test
    public void testGetHeightDegrees_PointRegion_ShouldReturnZero() {
        GeographicRegion region = new GeographicRegion(40.0, 40.0, -8.0, -8.0);
        assertEquals(0.0, region.getHeightDegrees(), 0.0001);
    }

    /**
     * Test width of global region.
     */
    @Test
    public void testGetWidthDegrees_GlobalRegion_ShouldReturn360() {
        GeographicRegion region = new GeographicRegion(-90.0, 90.0, -180.0, 180.0);
        assertEquals(360.0, region.getWidthDegrees(), 0.0001);
    }

    /**
     * Test height of global region.
     */
    @Test
    public void testGetHeightDegrees_GlobalRegion_ShouldReturn180() {
        GeographicRegion region = new GeographicRegion(-90.0, 90.0, -180.0, 180.0);
        assertEquals(180.0, region.getHeightDegrees(), 0.0001);
    }

    // ==================== GETTER TESTS ====================

    /**
     * Test getLatMin returns correct value.
     */
    @Test
    public void testGetLatMin_ShouldReturnCorrectValue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(38.0, region.getLatMin(), 0.0001);
    }

    /**
     * Test getLatMax returns correct value.
     */
    @Test
    public void testGetLatMax_ShouldReturnCorrectValue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(42.0, region.getLatMax(), 0.0001);
    }

    /**
     * Test getLonMin returns correct value.
     */
    @Test
    public void testGetLonMin_ShouldReturnCorrectValue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(-9.0, region.getLonMin(), 0.0001);
    }

    /**
     * Test getLonMax returns correct value.
     */
    @Test
    public void testGetLonMax_ShouldReturnCorrectValue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(-6.0, region.getLonMax(), 0.0001);
    }

    // ==================== EQUALS AND HASHCODE TESTS ====================

    /**
     * Test equals with identical regions.
     */
    @Test
    public void testEquals_IdenticalRegions_ShouldReturnTrue() {
        GeographicRegion region1 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        GeographicRegion region2 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region1.equals(region2));
    }

    /**
     * Test equals with same object reference.
     */
    @Test
    public void testEquals_SameReference_ShouldReturnTrue() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.equals(region));
    }

    /**
     * Test equals with different latMin.
     */
    @Test
    public void testEquals_DifferentLatMin_ShouldReturnFalse() {
        GeographicRegion region1 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        GeographicRegion region2 = new GeographicRegion(37.0, 42.0, -9.0, -6.0);
        assertFalse(region1.equals(region2));
    }

    /**
     * Test equals with different latMax.
     */
    @Test
    public void testEquals_DifferentLatMax_ShouldReturnFalse() {
        GeographicRegion region1 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        GeographicRegion region2 = new GeographicRegion(38.0, 43.0, -9.0, -6.0);
        assertFalse(region1.equals(region2));
    }

    /**
     * Test equals with different lonMin.
     */
    @Test
    public void testEquals_DifferentLonMin_ShouldReturnFalse() {
        GeographicRegion region1 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        GeographicRegion region2 = new GeographicRegion(38.0, 42.0, -10.0, -6.0);
        assertFalse(region1.equals(region2));
    }

    /**
     * Test equals with different lonMax.
     */
    @Test
    public void testEquals_DifferentLonMax_ShouldReturnFalse() {
        GeographicRegion region1 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        GeographicRegion region2 = new GeographicRegion(38.0, 42.0, -9.0, -5.0);
        assertFalse(region1.equals(region2));
    }

    /**
     * Test equals with null object.
     */
    @Test
    public void testEquals_NullObject_ShouldReturnFalse() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertFalse(region.equals(null));
    }

    /**
     * Test equals with different class object.
     */
    @Test
    public void testEquals_DifferentClass_ShouldReturnFalse() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertFalse(region.equals("Not a GeographicRegion"));
    }

    /**
     * Test hashCode consistency: equal objects have same hashCode.
     */
    @Test
    public void testHashCode_EqualObjects_ShouldHaveSameHashCode() {
        GeographicRegion region1 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        GeographicRegion region2 = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertEquals(region1.hashCode(), region2.hashCode());
    }

    /**
     * Test hashCode consistency: same object returns same hashCode.
     */
    @Test
    public void testHashCode_SameObject_ShouldReturnSameHashCode() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        int hash1 = region.hashCode();
        int hash2 = region.hashCode();
        assertEquals(hash1, hash2);
    }

    // ==================== TO STRING TEST ====================

    /**
     * Test toString returns a non-null string.
     */
    @Test
    public void testToString_ShouldReturnNonNull() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertNotNull(region.toString());
    }

    /**
     * Test toString contains latitude information.
     */
    @Test
    public void testToString_ShouldContainLatitude() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.toString().contains("lat"));
    }

    /**
     * Test toString contains longitude information.
     */
    @Test
    public void testToString_ShouldContainLongitude() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.toString().contains("lon"));
    }

    /**
     * Test toString contains area information.
     */
    @Test
    public void testToString_ShouldContainArea() {
        GeographicRegion region = new GeographicRegion(38.0, 42.0, -9.0, -6.0);
        assertTrue(region.toString().contains("area"));
    }
}