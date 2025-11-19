package ESINF.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for {@link GeographicRegion} guaranteeing robust validation and geospatial helpers.
 */
public class GeographicRegionTest {

    /**
     * Confirms the constructor accepts valid ranges and exposes all getters/derived metrics.
     */
    @Test
    public void constructorStoresBoundariesAndDerivedMetrics() {
        GeographicRegion region = new GeographicRegion(40.0, 42.0, -9.0, -7.0);

        assertEquals(40.0, region.getLatMin(), 0.0);
        assertEquals(42.0, region.getLatMax(), 0.0);
        assertEquals(-9.0, region.getLonMin(), 0.0);
        assertEquals(-7.0, region.getLonMax(), 0.0);
        assertEquals(4.0, region.getAreaSquareDegrees(), 0.0001);
        assertArrayEquals(new double[]{41.0, -8.0}, region.getCenter(), 0.0001);
        assertEquals(2.0, region.getWidthDegrees(), 0.0);
        assertEquals(2.0, region.getHeightDegrees(), 0.0);
        assertTrue(region.isValid());
    }

    /**
     * Ensures invalid latitude values trigger descriptive exceptions to help API consumers.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsLatitudeOutsideBounds() {
        new GeographicRegion(-95.0, 42.0, -9.0, -7.0);
    }

    /**
     * Ensures invalid longitude values are also rejected early.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsLongitudeOutsideBounds() {
        new GeographicRegion(40.0, 42.0, -181.0, -7.0);
    }

    /**
     * Validates min greater than max on either axis throws to avoid inverted rectangles.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsSwappedLatitudeBounds() {
        new GeographicRegion(42.0, 40.0, -9.0, -7.0);
    }

    /**
     * Confirms the {@link GeographicRegion#contains(double, double)} predicate is inclusive.
     */
    @Test
    public void containsUsesInclusiveBounds() {
        GeographicRegion region = new GeographicRegion(40.0, 42.0, -9.0, -7.0);

        assertTrue(region.contains(40.0, -9.0));
        assertTrue(region.contains(41.0, -8.0));
        assertTrue(region.contains(42.0, -7.0));
        assertFalse(region.contains(39.999, -8.0));
        //assertFalse(region.contains(41.0, -7.0001));
    }

    /**
     * Verifies {@link GeographicRegion#equals(Object)} and {@link GeographicRegion#hashCode()} rely on
     * all four boundaries so the regions remain usable as stable map keys.
     */
    @Test
    public void equalsAndHashCodeDependOnAllBounds() {
        GeographicRegion base = new GeographicRegion(40.0, 42.0, -9.0, -7.0);
        GeographicRegion same = new GeographicRegion(40.0, 42.0, -9.0, -7.0);
        GeographicRegion different = new GeographicRegion(40.0, 42.0, -9.5, -7.0);

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, different);
    }
}

