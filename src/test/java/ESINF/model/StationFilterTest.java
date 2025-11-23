package ESINF.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for StationFilter class.
 * Tests cover filter creation, matching logic, factory methods, and edge cases.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class StationFilterTest {

    private Station cityMainStationPT;
    private Station cityNonMainStationES;
    private Station nonCityMainStationPT;
    private Station nonCityNonMainStationES;

    /**
     * Setup test fixtures: create sample stations with different attributes.
     */
    @Before
    public void setUp() {
        // City, Main Station, Portugal
        cityMainStationPT = new Station(
                "Lisbon Santa Apolonia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "Western European Time", true, true, false
        );

        // City, Non-Main Station, Spain
        cityNonMainStationES = new Station(
                "Barcelona Sants", 41.3791, 2.1401, "ES",
                "Europe/Madrid", "Central European Time", true, false, false
        );

        // Non-City, Main Station, Portugal
        nonCityMainStationPT = new Station(
                "Entroncamento", 39.4667, -8.4667, "PT",
                "Europe/Lisbon", "Western European Time", false, true, false
        );

        // Non-City, Non-Main Station, Spain
        nonCityNonMainStationES = new Station(
                "Medina del Campo", 41.3083, -4.9167, "ES",
                "Europe/Madrid", "Central European Time", false, false, false
        );
    }

    // ==================== CONSTRUCTOR TESTS ====================

    /**
     * Test constructor with all null values creates a valid filter.
     */
    @Test
    public void testConstructor_AllNull_ShouldCreate() {
        StationFilter filter = new StationFilter(null, null, null);
        assertNotNull(filter);
    }

    /**
     * Test constructor with all parameters set.
     */
    @Test
    public void testConstructor_AllParametersSet_ShouldCreate() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertNotNull(filter);
    }

    /**
     * Test constructor with only isCity set.
     */
    @Test
    public void testConstructor_OnlyIsCity_ShouldCreate() {
        StationFilter filter = new StationFilter(true, null, null);
        assertNotNull(filter);
    }

    /**
     * Test constructor with only isMainStation set.
     */
    @Test
    public void testConstructor_OnlyIsMainStation_ShouldCreate() {
        StationFilter filter = new StationFilter(null, true, null);
        assertNotNull(filter);
    }

    /**
     * Test constructor with only country set.
     */
    @Test
    public void testConstructor_OnlyCountry_ShouldCreate() {
        StationFilter filter = new StationFilter(null, null, "PT");
        assertNotNull(filter);
    }

    /**
     * Test that "all" country is treated as null (no filter).
     */
    @Test
    public void testConstructor_CountryAll_ShouldTreatAsNull() {
        StationFilter filter = new StationFilter(null, null, "all");
        assertNull(filter.getCountry());
    }

    /**
     * Test that "ALL" (uppercase) country is treated as null.
     */
    @Test
    public void testConstructor_CountryALLUppercase_ShouldTreatAsNull() {
        StationFilter filter = new StationFilter(null, null, "ALL");
        assertNull(filter.getCountry());
    }

    // ==================== FACTORY METHOD TESTS ====================

    /**
     * Test noFilter factory method creates filter with no criteria.
     */
    @Test
    public void testNoFilter_ShouldHaveNoCriteria() {
        StationFilter filter = StationFilter.noFilter();
        assertFalse(filter.hasFilters());
    }

    /**
     * Test citiesOnly factory method has isCity set to true.
     */
    @Test
    public void testCitiesOnly_ShouldHaveIsCityTrue() {
        StationFilter filter = StationFilter.citiesOnly();
        assertEquals(Boolean.TRUE, filter.getIsCity());
    }

    /**
     * Test nonCitiesOnly factory method has isCity set to false.
     */
    @Test
    public void testNonCitiesOnly_ShouldHaveIsCityFalse() {
        StationFilter filter = StationFilter.nonCitiesOnly();
        assertEquals(Boolean.FALSE, filter.getIsCity());
    }

    /**
     * Test mainStationsOnly factory method has isMainStation set to true.
     */
    @Test
    public void testMainStationsOnly_ShouldHaveIsMainStationTrue() {
        StationFilter filter = StationFilter.mainStationsOnly();
        assertEquals(Boolean.TRUE, filter.getIsMainStation());
    }

    /**
     * Test nonMainStationsOnly factory method has isMainStation set to false.
     */
    @Test
    public void testNonMainStationsOnly_ShouldHaveIsMainStationFalse() {
        StationFilter filter = StationFilter.nonMainStationsOnly();
        assertEquals(Boolean.FALSE, filter.getIsMainStation());
    }

    /**
     * Test forCountry factory method sets correct country.
     */
    @Test
    public void testForCountry_ShouldSetCountry() {
        StationFilter filter = StationFilter.forCountry("PT");
        assertEquals("PT", filter.getCountry());
    }

    /**
     * Test citiesInCountry factory method has both isCity and country set.
     */
    @Test
    public void testCitiesInCountry_ShouldHaveIsCityTrue() {
        StationFilter filter = StationFilter.citiesInCountry("ES");
        assertEquals(Boolean.TRUE, filter.getIsCity());
    }

    /**
     * Test citiesInCountry factory method sets country correctly.
     */
    @Test
    public void testCitiesInCountry_ShouldSetCountry() {
        StationFilter filter = StationFilter.citiesInCountry("ES");
        assertEquals("ES", filter.getCountry());
    }

    /**
     * Test mainStationsInCountry factory method has isMainStation set.
     */
    @Test
    public void testMainStationsInCountry_ShouldHaveIsMainStationTrue() {
        StationFilter filter = StationFilter.mainStationsInCountry("PT");
        assertEquals(Boolean.TRUE, filter.getIsMainStation());
    }

    /**
     * Test mainStationsInCountry factory method sets country correctly.
     */
    @Test
    public void testMainStationsInCountry_ShouldSetCountry() {
        StationFilter filter = StationFilter.mainStationsInCountry("PT");
        assertEquals("PT", filter.getCountry());
    }

    // ==================== MATCHES METHOD TESTS ====================

    /**
     * Test that null station does not match any filter.
     */
    @Test
    public void testMatches_NullStation_ShouldReturnFalse() {
        StationFilter filter = StationFilter.noFilter();
        assertFalse(filter.matches(null));
    }

    /**
     * Test that no filter matches any station.
     */
    @Test
    public void testMatches_NoFilter_ShouldMatchAnyStation() {
        StationFilter filter = StationFilter.noFilter();
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test citiesOnly filter matches city station.
     */
    @Test
    public void testMatches_CitiesOnly_ShouldMatchCity() {
        StationFilter filter = StationFilter.citiesOnly();
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test citiesOnly filter rejects non-city station.
     */
    @Test
    public void testMatches_CitiesOnly_ShouldRejectNonCity() {
        StationFilter filter = StationFilter.citiesOnly();
        assertFalse(filter.matches(nonCityMainStationPT));
    }

    /**
     * Test nonCitiesOnly filter matches non-city station.
     */
    @Test
    public void testMatches_NonCitiesOnly_ShouldMatchNonCity() {
        StationFilter filter = StationFilter.nonCitiesOnly();
        assertTrue(filter.matches(nonCityMainStationPT));
    }

    /**
     * Test nonCitiesOnly filter rejects city station.
     */
    @Test
    public void testMatches_NonCitiesOnly_ShouldRejectCity() {
        StationFilter filter = StationFilter.nonCitiesOnly();
        assertFalse(filter.matches(cityMainStationPT));
    }

    /**
     * Test mainStationsOnly filter matches main station.
     */
    @Test
    public void testMatches_MainStationsOnly_ShouldMatchMainStation() {
        StationFilter filter = StationFilter.mainStationsOnly();
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test mainStationsOnly filter rejects non-main station.
     */
    @Test
    public void testMatches_MainStationsOnly_ShouldRejectNonMainStation() {
        StationFilter filter = StationFilter.mainStationsOnly();
        assertFalse(filter.matches(cityNonMainStationES));
    }

    /**
     * Test nonMainStationsOnly filter matches non-main station.
     */
    @Test
    public void testMatches_NonMainStationsOnly_ShouldMatchNonMainStation() {
        StationFilter filter = StationFilter.nonMainStationsOnly();
        assertTrue(filter.matches(cityNonMainStationES));
    }

    /**
     * Test nonMainStationsOnly filter rejects main station.
     */
    @Test
    public void testMatches_NonMainStationsOnly_ShouldRejectMainStation() {
        StationFilter filter = StationFilter.nonMainStationsOnly();
        assertFalse(filter.matches(cityMainStationPT));
    }

    /**
     * Test country filter matches correct country.
     */
    @Test
    public void testMatches_CountryFilter_ShouldMatchCorrectCountry() {
        StationFilter filter = StationFilter.forCountry("PT");
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test country filter rejects different country.
     */
    @Test
    public void testMatches_CountryFilter_ShouldRejectDifferentCountry() {
        StationFilter filter = StationFilter.forCountry("PT");
        assertFalse(filter.matches(cityNonMainStationES));
    }

    /**
     * Test country filter is case-insensitive (lowercase).
     */
    @Test
    public void testMatches_CountryFilterCaseInsensitive_LowercaseFilter() {
        StationFilter filter = StationFilter.forCountry("pt");
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test country filter is case-insensitive (mixed case).
     */
    @Test
    public void testMatches_CountryFilterCaseInsensitive_MixedCase() {
        StationFilter filter = StationFilter.forCountry("Pt");
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test combined filter: cities in Portugal matches correctly.
     */
    @Test
    public void testMatches_CitiesInCountry_ShouldMatchCityInPT() {
        StationFilter filter = StationFilter.citiesInCountry("PT");
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test combined filter: cities in Portugal rejects city in Spain.
     */
    @Test
    public void testMatches_CitiesInCountry_ShouldRejectCityInES() {
        StationFilter filter = StationFilter.citiesInCountry("PT");
        assertFalse(filter.matches(cityNonMainStationES));
    }

    /**
     * Test combined filter: cities in Portugal rejects non-city in Portugal.
     */
    @Test
    public void testMatches_CitiesInCountry_ShouldRejectNonCityInPT() {
        StationFilter filter = StationFilter.citiesInCountry("PT");
        assertFalse(filter.matches(nonCityMainStationPT));
    }

    /**
     * Test combined filter: main stations in Portugal matches correctly.
     */
    @Test
    public void testMatches_MainStationsInCountry_ShouldMatchMainInPT() {
        StationFilter filter = StationFilter.mainStationsInCountry("PT");
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test combined filter: main stations in Portugal rejects non-main in PT.
     */
    @Test
    public void testMatches_MainStationsInCountry_ShouldRejectNonMainInPT() {
        StationFilter filter = StationFilter.mainStationsInCountry("PT");
        assertFalse(filter.matches(nonCityNonMainStationES));
    }

    /**
     * Test triple combined filter: city, main station, Portugal.
     */
    @Test
    public void testMatches_TripleCombined_ShouldMatchAll() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertTrue(filter.matches(cityMainStationPT));
    }

    /**
     * Test triple combined filter rejects when isCity doesn't match.
     */
    @Test
    public void testMatches_TripleCombined_ShouldRejectWhenIsCityFails() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertFalse(filter.matches(nonCityMainStationPT));
    }


    /**
     * Test triple combined filter rejects when country doesn't match.
     */
    @Test
    public void testMatches_TripleCombined_ShouldRejectWhenCountryFails() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertFalse(filter.matches(cityNonMainStationES));
    }

    // ==================== HAS FILTERS METHOD TESTS ====================

    /**
     * Test hasFilters returns false for no filter.
     */
    @Test
    public void testHasFilters_NoFilter_ShouldReturnFalse() {
        StationFilter filter = StationFilter.noFilter();
        assertFalse(filter.hasFilters());
    }

    /**
     * Test hasFilters returns true when isCity is set.
     */
    @Test
    public void testHasFilters_IsCitySet_ShouldReturnTrue() {
        StationFilter filter = StationFilter.citiesOnly();
        assertTrue(filter.hasFilters());
    }

    /**
     * Test hasFilters returns true when isMainStation is set.
     */
    @Test
    public void testHasFilters_IsMainStationSet_ShouldReturnTrue() {
        StationFilter filter = StationFilter.mainStationsOnly();
        assertTrue(filter.hasFilters());
    }

    /**
     * Test hasFilters returns true when country is set.
     */
    @Test
    public void testHasFilters_CountrySet_ShouldReturnTrue() {
        StationFilter filter = StationFilter.forCountry("PT");
        assertTrue(filter.hasFilters());
    }

    /**
     * Test hasFilters returns true for combined filters.
     */
    @Test
    public void testHasFilters_CombinedFilters_ShouldReturnTrue() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertTrue(filter.hasFilters());
    }

    // ==================== GET ACTIVE FILTER COUNT TESTS ====================

    /**
     * Test getActiveFilterCount returns 0 for no filter.
     */
    @Test
    public void testGetActiveFilterCount_NoFilter_ShouldReturnZero() {
        StationFilter filter = StationFilter.noFilter();
        assertEquals(0, filter.getActiveFilterCount());
    }

    /**
     * Test getActiveFilterCount returns 1 when only isCity is set.
     */
    @Test
    public void testGetActiveFilterCount_OnlyIsCity_ShouldReturnOne() {
        StationFilter filter = StationFilter.citiesOnly();
        assertEquals(1, filter.getActiveFilterCount());
    }

    /**
     * Test getActiveFilterCount returns 1 when only isMainStation is set.
     */
    @Test
    public void testGetActiveFilterCount_OnlyIsMainStation_ShouldReturnOne() {
        StationFilter filter = StationFilter.mainStationsOnly();
        assertEquals(1, filter.getActiveFilterCount());
    }

    /**
     * Test getActiveFilterCount returns 1 when only country is set.
     */
    @Test
    public void testGetActiveFilterCount_OnlyCountry_ShouldReturnOne() {
        StationFilter filter = StationFilter.forCountry("PT");
        assertEquals(1, filter.getActiveFilterCount());
    }

    /**
     * Test getActiveFilterCount returns 2 for two filters.
     */
    @Test
    public void testGetActiveFilterCount_TwoFilters_ShouldReturnTwo() {
        StationFilter filter = StationFilter.citiesInCountry("PT");
        assertEquals(2, filter.getActiveFilterCount());
    }

    /**
     * Test getActiveFilterCount returns 3 for three filters.
     */
    @Test
    public void testGetActiveFilterCount_ThreeFilters_ShouldReturnThree() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertEquals(3, filter.getActiveFilterCount());
    }

    // ==================== GETTER TESTS ====================

    /**
     * Test getIsCity returns null when not set.
     */
    @Test
    public void testGetIsCity_NotSet_ShouldReturnNull() {
        StationFilter filter = new StationFilter(null, true, "PT");
        assertNull(filter.getIsCity());
    }

    /**
     * Test getIsCity returns correct value when set to true.
     */
    @Test
    public void testGetIsCity_SetTrue_ShouldReturnTrue() {
        StationFilter filter = new StationFilter(true, null, null);
        assertEquals(Boolean.TRUE, filter.getIsCity());
    }

    /**
     * Test getIsCity returns correct value when set to false.
     */
    @Test
    public void testGetIsCity_SetFalse_ShouldReturnFalse() {
        StationFilter filter = new StationFilter(false, null, null);
        assertEquals(Boolean.FALSE, filter.getIsCity());
    }

    /**
     * Test getIsMainStation returns null when not set.
     */
    @Test
    public void testGetIsMainStation_NotSet_ShouldReturnNull() {
        StationFilter filter = new StationFilter(true, null, "PT");
        assertNull(filter.getIsMainStation());
    }

    /**
     * Test getIsMainStation returns correct value when set to true.
     */
    @Test
    public void testGetIsMainStation_SetTrue_ShouldReturnTrue() {
        StationFilter filter = new StationFilter(null, true, null);
        assertEquals(Boolean.TRUE, filter.getIsMainStation());
    }

    /**
     * Test getIsMainStation returns correct value when set to false.
     */
    @Test
    public void testGetIsMainStation_SetFalse_ShouldReturnFalse() {
        StationFilter filter = new StationFilter(null, false, null);
        assertEquals(Boolean.FALSE, filter.getIsMainStation());
    }

    /**
     * Test getCountry returns null when not set.
     */
    @Test
    public void testGetCountry_NotSet_ShouldReturnNull() {
        StationFilter filter = new StationFilter(true, true, null);
        assertNull(filter.getCountry());
    }

    /**
     * Test getCountry returns correct value when set.
     */
    @Test
    public void testGetCountry_Set_ShouldReturnValue() {
        StationFilter filter = new StationFilter(null, null, "PT");
        assertEquals("PT", filter.getCountry());
    }

    // ==================== EQUALS AND HASHCODE TESTS ====================

    /**
     * Test equals with identical filters.
     */
    @Test
    public void testEquals_IdenticalFilters_ShouldReturnTrue() {
        StationFilter filter1 = new StationFilter(true, true, "PT");
        StationFilter filter2 = new StationFilter(true, true, "PT");
        assertTrue(filter1.equals(filter2));
    }

    /**
     * Test equals with same reference.
     */
    @Test
    public void testEquals_SameReference_ShouldReturnTrue() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertTrue(filter.equals(filter));
    }

    /**
     * Test equals with different isCity.
     */
    @Test
    public void testEquals_DifferentIsCity_ShouldReturnFalse() {
        StationFilter filter1 = new StationFilter(true, true, "PT");
        StationFilter filter2 = new StationFilter(false, true, "PT");
        assertFalse(filter1.equals(filter2));
    }

    /**
     * Test equals with different isMainStation.
     */
    @Test
    public void testEquals_DifferentIsMainStation_ShouldReturnFalse() {
        StationFilter filter1 = new StationFilter(true, true, "PT");
        StationFilter filter2 = new StationFilter(true, false, "PT");
        assertFalse(filter1.equals(filter2));
    }

    /**
     * Test equals with different country.
     */
    @Test
    public void testEquals_DifferentCountry_ShouldReturnFalse() {
        StationFilter filter1 = new StationFilter(true, true, "PT");
        StationFilter filter2 = new StationFilter(true, true, "ES");
        assertFalse(filter1.equals(filter2));
    }

    /**
     * Test equals with null object.
     */
    @Test
    public void testEquals_NullObject_ShouldReturnFalse() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertFalse(filter.equals(null));
    }

    /**
     * Test equals with different class.
     */
    @Test
    public void testEquals_DifferentClass_ShouldReturnFalse() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertFalse(filter.equals("Not a StationFilter"));
    }

    /**
     * Test hashCode consistency for equal objects.
     */
    @Test
    public void testHashCode_EqualObjects_ShouldHaveSameHashCode() {
        StationFilter filter1 = new StationFilter(true, true, "PT");
        StationFilter filter2 = new StationFilter(true, true, "PT");
        assertEquals(filter1.hashCode(), filter2.hashCode());
    }

    /**
     * Test hashCode consistency for same object.
     */
    @Test
    public void testHashCode_SameObject_ShouldReturnSameHashCode() {
        StationFilter filter = new StationFilter(true, true, "PT");
        int hash1 = filter.hashCode();
        int hash2 = filter.hashCode();
        assertEquals(hash1, hash2);
    }

    // ==================== TO STRING TESTS ====================

    /**
     * Test toString returns non-null string.
     */
    @Test
    public void testToString_ShouldReturnNonNull() {
        StationFilter filter = new StationFilter(true, true, "PT");
        assertNotNull(filter.toString());
    }

    /**
     * Test toString for no filter mentions no filters.
     */
    @Test
    public void testToString_NoFilter_ShouldIndicateNoFilters() {
        StationFilter filter = StationFilter.noFilter();
        assertTrue(filter.toString().contains("NO FILTERS"));
    }

    /**
     * Test toString contains isCity when set.
     */
    @Test
    public void testToString_IsCitySet_ShouldContainIsCity() {
        StationFilter filter = StationFilter.citiesOnly();
        assertTrue(filter.toString().contains("isCity"));
    }

    /**
     * Test toString contains isMainStation when set.
     */
    @Test
    public void testToString_IsMainStationSet_ShouldContainIsMainStation() {
        StationFilter filter = StationFilter.mainStationsOnly();
        assertTrue(filter.toString().contains("isMainStation"));
    }

    /**
     * Test toString contains country when set.
     */
    @Test
    public void testToString_CountrySet_ShouldContainCountry() {
        StationFilter filter = StationFilter.forCountry("PT");
        assertTrue(filter.toString().contains("country"));
    }
}