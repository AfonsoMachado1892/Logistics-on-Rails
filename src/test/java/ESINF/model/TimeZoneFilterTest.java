package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TimeZoneFilterTest {

    private Station stationCET;
    private Station stationWET;
    private Station stationEET;

    @Before
    public void setUp() {
        // Create mock stations
        stationCET = new Station("Berlin Hbf", 52.52, 13.36, "DE", "Europe/Berlin", "CET", true, true, false);
        // Note: WET/GMT is used in the TimeZoneFilter implementation (toLowerCase) for matching
        stationWET = new Station("Lisbon Oriente", 38.76, -9.09, "PT", "Europe/Lisbon", "WET/GMT", true, true, false);
        stationEET = new Station("Kyiv-Pasazhyrskyi", 50.43, 30.48, "UA", "Europe/Kyiv", "EET", true, true, false);
    }

    @Test
    public void testNoFilterMatches_CET_Station() {
        TimeZoneFilter filter = new TimeZoneFilter(); // No groups
        assertTrue("No filter should match CET", filter.matches(stationCET));
    }

    @Test
    public void testNoFilterMatches_WET_Station() {
        TimeZoneFilter filter = new TimeZoneFilter(); // No groups
        assertTrue("No filter should match WET/GMT", filter.matches(stationWET));
    }

    @Test
    public void testNoFilterMatches_EET_Station() {
        TimeZoneFilter filter = new TimeZoneFilter(); // No groups
        assertTrue("No filter should match EET", filter.matches(stationEET));
    }

    @Test
    public void testNullFilterMatches() {
        TimeZoneFilter filter = new TimeZoneFilter((String) null);
        assertTrue("Null filter should match all", filter.matches(stationCET));
    }

    @Test
    public void testEmptyStringFilterMatches() {
        TimeZoneFilter filter = new TimeZoneFilter("");
        assertTrue("Empty string filter should match all", filter.matches(stationCET));
    }

    @Test
    public void testSingleGroupMatch_Success() {
        TimeZoneFilter filter = new TimeZoneFilter("CET");
        assertTrue(filter.matches(stationCET));
    }

    @Test
    public void testSingleGroupMatch_Failure() {
        TimeZoneFilter filter = new TimeZoneFilter("CET");
        assertFalse(filter.matches(stationWET));
    }

    @Test
    public void testSingleGroupMatchCaseInsensitive_Success() {
        TimeZoneFilter filter = new TimeZoneFilter("cet"); // Lowercase filter
        assertTrue("Filter should be case-insensitive", filter.matches(stationCET));
    }

    @Test
    public void testSingleGroupMatchCaseInsensitive_Failure() {
        TimeZoneFilter filter = new TimeZoneFilter("cet");
        assertFalse("WET should not match CET", filter.matches(stationWET));
    }

    @Test
    public void testMultipleGroupsMatch_CET_Success() {
        TimeZoneFilter filter = new TimeZoneFilter("CET", "EET");
        assertTrue(filter.matches(stationCET));
    }

    @Test
    public void testMultipleGroupsMatch_EET_Success() {
        TimeZoneFilter filter = new TimeZoneFilter("CET", "EET");
        assertTrue(filter.matches(stationEET));
    }

    @Test
    public void testMultipleGroupsMatch_WET_Failure() {
        TimeZoneFilter filter = new TimeZoneFilter("CET", "EET");
        assertFalse(filter.matches(stationWET));
    }

    @Test
    public void testFilterMismatch() {
        TimeZoneFilter filter = new TimeZoneFilter("WET/GMT");
        assertFalse(filter.matches(stationCET));
    }

    @Test
    public void testFilterMatchesWET_WETSuccess() {
        TimeZoneFilter filter = new TimeZoneFilter("WET/GMT");
        assertTrue(filter.matches(stationWET));
    }

    @Test
    public void testToString_ActiveFilter() {
        TimeZoneFilter filter = new TimeZoneFilter("CET", "EET");
        String expected = "TimeZoneFilter{groups=[cet, eet]}";
        assertEquals(expected, filter.toString());
    }

    @Test
    public void testToString_NoFilter() {
        TimeZoneFilter noFilter = new TimeZoneFilter();
        String expected = "TimeZoneFilter{NO FILTERS}";
        assertEquals(expected, noFilter.toString());
    }

    @Test
    public void testHasFilter_ReturnsTrue() {
        TimeZoneFilter filter = new TimeZoneFilter("CET");
        assertTrue(filter.hasFilter());
    }

    @Test
    public void testHasFilter_ReturnsFalse() {
        TimeZoneFilter filter = new TimeZoneFilter();
        assertFalse(filter.hasFilter());
    }
}