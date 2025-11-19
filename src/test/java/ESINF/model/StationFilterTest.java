package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link StationFilter} covering the different factory helpers and predicate logic.
 */
public class StationFilterTest {

    private Station cityMainStationPT;
    private Station ruralStopES;

    @Before
    public void setUp() {
        cityMainStationPT = new Station(
                "Lisboa Oriente",
                38.76792,
                -9.09944,
                "PT",
                "Europe/Lisbon",
                "UTC+0",
                true,
                true,
                false
        );

        ruralStopES = new Station(
                "Ourense-Ce",
                42.33670,
                -7.86388,
                "ES",
                "Europe/Madrid",
                "UTC+1",
                false,
                false,
                false
        );
    }

    /**
     * Verifies that a filter with all three criteria only matches a station that satisfies every flag.
     */
    @Test
    public void matchesReturnsTrueOnlyWhenAllCriteriaAreMet() {
        StationFilter filter = new StationFilter(true, true, "PT");

        assertTrue(filter.matches(cityMainStationPT));
        assertFalse(filter.matches(ruralStopES));
    }

    /**
     * Ensures any mismatch rejects the station so controllers do not deliver partial matches.
     */
    @Test
    public void matchesReturnsFalseIfOneCriterionFails() {
        StationFilter filter = StationFilter.mainStationsInCountry("PT");

        assertFalse("Non-main stations must be filtered out", filter.matches(ruralStopES));

        Station nonMainCity = new Station(
                "Lisboa Entrecampos",
                38.74167,
                -9.14861,
                "PT",
                "Europe/Lisbon",
                "UTC+0",
                true,
                false,
                false
        );
        assertFalse("Main-station filter must reject secondary hubs", filter.matches(nonMainCity));
    }

    /**
     * Confirms the convenience builders generate neutral filters when instructed (e.g., "all" countries).
     */
    @Test
    public void convenienceFactoriesProduceExpectedFilters() {
        StationFilter noFilter = StationFilter.noFilter();
        assertTrue(noFilter.matches(cityMainStationPT));
        assertTrue(noFilter.matches(ruralStopES));
        assertFalse(noFilter.hasFilters());
        assertEquals(0, noFilter.getActiveFilterCount());

        StationFilter citiesOnly = StationFilter.citiesOnly();
        assertTrue(citiesOnly.matches(cityMainStationPT));
        assertFalse(citiesOnly.matches(ruralStopES));
        assertTrue(citiesOnly.hasFilters());
        assertEquals(1, citiesOnly.getActiveFilterCount());

        StationFilter countryAll = new StationFilter(null, null, "ALL");
        assertTrue("Country 'ALL' should behave like no filter", countryAll.matches(ruralStopES));
        assertFalse(countryAll.hasFilters());
        assertEquals(0, countryAll.getActiveFilterCount());
    }

    /**
     * Validates {@link StationFilter#equals(Object)} and {@link StationFilter#hashCode()} to guarantee
     * deterministic caching of filters in higher layers.
     */
    @Test
    public void equalsAndHashCodeDependOnFilterComponents() {
        StationFilter base = new StationFilter(true, null, "PT");
        StationFilter same = new StationFilter(true, null, "PT");
        StationFilter different = new StationFilter(false, null, "PT");

        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, different);
    }
}

