package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TimeZoneFilterTest {

    private Station stationCET;
    private Station stationWET;
    private Station stationEET;

    @Before
    void setUp() {
        // Create mock stations
        stationCET = new Station("Berlin Hbf", 52.52, 13.36, "DE", "Europe/Berlin", "CET", true, true, false);
        stationWET = new Station("Lisbon Oriente", 38.76, -9.09, "PT", "Europe/Lisbon", "WET/GMT", true, true, false);
        stationEET = new Station("Kyiv-Pasazhyrskyi", 50.43, 30.48, "UA", "Europe/Kyiv", "EET", true, true, false);
    }

    @Test
    void testNoFilterMatchesAll() {
        TimeZoneFilter filter = new TimeZoneFilter(); // No groups
        assertTrue(filter.matches(stationCET), "No filter should match CET");
        assertTrue(filter.matches(stationWET), "No filter should match WET/GMT");
        assertTrue(filter.matches(stationEET), "No filter should match EET");
    }

    @Test
    void testNullFilterMatchesAll() {
        TimeZoneFilter filter = new TimeZoneFilter((String) null);
        assertTrue(filter.matches(stationCET), "Null filter should match all");
    }

    @Test
    void testEmptyStringFilterMatchesAll() {
        TimeZoneFilter filter = new TimeZoneFilter("");
        assertTrue(filter.matches(stationCET), "Empty string filter should match all");
    }

    @Test
    void testSingleGroupMatch() {
        TimeZoneFilter filter = new TimeZoneFilter("CET");
        assertTrue(filter.matches(stationCET));
        assertFalse(filter.matches(stationWET));
    }

    @Test
    void testSingleGroupMatchCaseInsensitive() {
        TimeZoneFilter filter = new TimeZoneFilter("cet"); // Lowercase
        assertTrue(filter.matches(stationCET), "Filter should be case-insensitive");
        assertFalse(filter.matches(stationWET));
    }

    @Test
    void testMultipleGroupsMatch() {
        TimeZoneFilter filter = new TimeZoneFilter("CET", "EET");
        assertTrue(filter.matches(stationCET));
        assertTrue(filter.matches(stationEET));
        assertFalse(filter.matches(stationWET));
    }

    @Test
    void testFilterMismatch() {
        TimeZoneFilter filter = new TimeZoneFilter("WET/GMT");
        assertTrue(filter.matches(stationWET));
        assertFalse(filter.matches(stationCET));
    }

    @Test
    void testToString() {
        TimeZoneFilter filter = new TimeZoneFilter("CET", "EET");
        String expected = "TimeZoneFilter{groups=[cet, eet]}";
        assertEquals(expected, filter.toString());

        TimeZoneFilter noFilter = new TimeZoneFilter();
        expected = "TimeZoneFilter{NO FILTERS}";
        assertEquals(expected, noFilter.toString());
    }
}