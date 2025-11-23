package ESINF.controller;

import ESINF.model.*;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for GeographicSearchController class.
 * Tests cover controller initialization, search operations, filtering, and result handling.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class GeographicSearchControllerTest {

    private KDTree2D kdTree;
    private GeographicSearchController controller;
    private List<Station> testStations;

    /**
     * Setup test fixtures: create a KD-Tree with sample stations.
     */
    @Before
    public void setUp() {
        // Create test stations
        testStations = new ArrayList<>();

        testStations.add(new Station("Lisbon Santa Apolonia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "Western European Time", true, true, false));

        testStations.add(new Station("Porto Campanha", 41.14961, -8.58519, "PT",
                "Europe/Lisbon", "Western European Time", true, true, false));

        testStations.add(new Station("Coimbra B", 40.21161, -8.43456, "PT",
                "Europe/Lisbon", "Western European Time", true, false, false));

        testStations.add(new Station("Faro", 37.01933, -7.93044, "PT",
                "Europe/Lisbon", "Western European Time", false, true, false));

        testStations.add(new Station("Barcelona Sants", 41.37906, 2.14014, "ES",
                "Europe/Madrid", "Central European Time", true, true, false));

        testStations.add(new Station("Madrid Atocha", 40.40678, -3.68819, "ES",
                "Europe/Madrid", "Central European Time", true, true, false));

        // Build KD-Tree
        kdTree = new KDTree2D();
        kdTree.buildBalanced(testStations, testStations);

        // Create controller
        controller = new GeographicSearchController(kdTree);
    }

    // ==================== CONSTRUCTOR TESTS ====================

    /**
     * Test constructor with valid KD-Tree creates controller successfully.
     */
    @Test
    public void testConstructor_ValidKDTree_ShouldCreate() {
        GeographicSearchController ctrl = new GeographicSearchController(kdTree);
        assertNotNull(ctrl);
    }

    /**
     * Test constructor with null KD-Tree throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullKDTree_ShouldThrowException() {
        new GeographicSearchController(null);
    }

    /**
     * Test constructor with unbuilt KD-Tree throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_UnbuiltKDTree_ShouldThrowException() {
        KDTree2D emptyTree = new KDTree2D();
        new GeographicSearchController(emptyTree);
    }

    // ==================== SEARCH BY COORDINATES TESTS ====================

    /**
     * Test search with valid coordinates returns non-null result.
     */
    @Test
    public void testSearchByCoordinates_ValidRegion_ShouldReturnResult() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(37.0, 42.0, -10.0, -7.0);
        assertNotNull(result);
    }

    /**
     * Test search finds all stations in Portugal region.
     */
    @Test
    public void testSearchByCoordinates_PortugalRegion_ShouldFindAllPTStations() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);
        assertEquals(4, result.getStationCount());
    }

    /**
     * Test search with small region finds specific station.
     */
    @Test
    public void testSearchByCoordinates_SmallRegion_ShouldFindOneStation() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(38.7, 38.8, -9.2, -9.0);
        assertTrue(result.getStationCount() >= 1);
    }

    /**
     * Test search with no matching stations returns empty result.
     */
    @Test
    public void testSearchByCoordinates_EmptyRegion_ShouldReturnZeroStations() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(50.0, 51.0, 20.0, 21.0);
        assertEquals(0, result.getStationCount());
    }

    /**
     * Test search with global region finds all stations.
     */
    @Test
    public void testSearchByCoordinates_GlobalRegion_ShouldFindAllStations() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(-90.0, 90.0, -180.0, 180.0);
        assertEquals(6, result.getStationCount());
    }

    /**
     * Test search returns list of stations.
     */
    @Test
    public void testSearchByCoordinates_ShouldReturnStationList() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);
        assertNotNull(result.getStations());
    }

    /**
     * Test search execution time is recorded.
     */
    @Test
    public void testSearchByCoordinates_ShouldRecordExecutionTime() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);
        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    // ==================== SEARCH WITH FILTER TESTS ====================

    /**
     * Test search with cities-only filter returns only cities.
     */
    @Test
    public void testSearchWithFilter_CitiesOnly_ShouldReturnOnlyCities() {
        StationFilter filter = StationFilter.citiesOnly();
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        for (Station station : result.getStations()) {
            assertTrue(station.isCity());
        }
    }

    /**
     * Test search with main stations filter returns only main stations.
     */
    @Test
    public void testSearchWithFilter_MainStationsOnly_ShouldReturnOnlyMainStations() {
        StationFilter filter = StationFilter.mainStationsOnly();
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        for (Station station : result.getStations()) {
            assertTrue(station.isMainStation());
        }
    }

    /**
     * Test search with country filter (PT) returns only Portuguese stations.
     */
    @Test
    public void testSearchWithFilter_CountryPT_ShouldReturnOnlyPTStations() {
        StationFilter filter = StationFilter.forCountry("PT");
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, 3.0, filter);

        for (Station station : result.getStations()) {
            assertEquals("PT", station.getCountry());
        }
    }

    /**
     * Test search with country filter (ES) returns only Spanish stations.
     */
    @Test
    public void testSearchWithFilter_CountryES_ShouldReturnOnlyESStations() {
        StationFilter filter = StationFilter.forCountry("ES");
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, 3.0, filter);

        for (Station station : result.getStations()) {
            assertEquals("ES", station.getCountry());
        }
    }

    /**
     * Test search with combined filter (cities in PT).
     */
    @Test
    public void testSearchWithFilter_CitiesInPT_ShouldReturnFilteredStations() {
        StationFilter filter = StationFilter.citiesInCountry("PT");
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        assertTrue(result.getStationCount() >= 2);
    }

    /**
     * Test search with filter verifies all stations match filter.
     */
    @Test
    public void testSearchWithFilter_CitiesInPT_AllStationsShouldMatchFilter() {
        StationFilter filter = StationFilter.citiesInCountry("PT");
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        for (Station station : result.getStations()) {
            assertTrue(station.isCity() && "PT".equals(station.getCountry()));
        }
    }

    /**
     * Test search with strict filter returns fewer results.
     */
    @Test
    public void testSearchWithFilter_StrictFilter_ShouldReturnFewerResults() {
        GeographicSearchController.SearchResult resultNoFilter =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        StationFilter filter = StationFilter.citiesOnly();
        GeographicSearchController.SearchResult resultWithFilter =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        assertTrue(resultWithFilter.getStationCount() <= resultNoFilter.getStationCount());
    }

    /**
     * Test search with no filter returns all stations in region.
     */
    @Test
    public void testSearchWithFilter_NoFilter_ShouldReturnAllStations() {
        StationFilter filter = StationFilter.noFilter();
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        assertEquals(4, result.getStationCount());
    }

    /**
     * Test search with null filter behaves like no filter.
     */
    @Test
    public void testSearchWithFilter_NullFilter_ShouldReturnAllStations() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, null);

        assertEquals(4, result.getStationCount());
    }

    // ==================== SEARCH WITH REGION OBJECT TESTS ====================

    /**
     * Test search with GeographicRegion object returns valid result.
     */
    @Test
    public void testSearchWithRegion_ValidRegion_ShouldReturnResult() {
        GeographicRegion region = new GeographicRegion(36.0, 42.0, -10.0, -6.0);
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(region, null);

        assertNotNull(result);
    }

    /**
     * Test search with region object and filter.
     */
    @Test
    public void testSearchWithRegion_WithFilter_ShouldApplyFilter() {
        GeographicRegion region = new GeographicRegion(36.0, 42.0, -10.0, -6.0);
        StationFilter filter = StationFilter.citiesOnly();
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(region, filter);

        for (Station station : result.getStations()) {
            assertTrue(station.isCity());
        }
    }

    /**
     * Test search with null region throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSearchWithRegion_NullRegion_ShouldThrowException() {
        controller.searchByGeographicArea((GeographicRegion) null, null);
    }

    /**
     * Test search with region object produces same results as coordinates.
     */
    @Test
    public void testSearchWithRegion_SameResultsAsCoordinates() {
        GeographicRegion region = new GeographicRegion(36.0, 42.0, -10.0, -6.0);

        GeographicSearchController.SearchResult result1 =
                controller.searchByGeographicArea(region, null);

        GeographicSearchController.SearchResult result2 =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, null);

        assertEquals(result1.getStationCount(), result2.getStationCount());
    }

    // ==================== SEARCH RESULT TESTS ====================

    /**
     * Test SearchResult contains region information.
     */
    @Test
    public void testSearchResult_ContainsRegion() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertNotNull(result.getRegion());
    }

    /**
     * Test SearchResult region matches search parameters.
     */
    @Test
    public void testSearchResult_RegionMatchesSearchParameters() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        GeographicRegion region = result.getRegion();
        assertEquals(36.0, region.getLatMin(), 0.0001);
    }

    /**
     * Test SearchResult contains filter information.
     */
    @Test
    public void testSearchResult_ContainsFilter() {
        StationFilter filter = StationFilter.citiesOnly();
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        assertNotNull(result.getFilter());
    }

    /**
     * Test SearchResult filter is null when no filter applied.
     */
    @Test
    public void testSearchResult_FilterIsNullWhenNoneApplied() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertNull(result.getFilter());
    }

    /**
     * Test SearchResult hasFilter returns false when no filter.
     */
    @Test
    public void testSearchResult_HasFilterReturnsFalseWhenNoFilter() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertFalse(result.hasFilter());
    }

    /**
     * Test SearchResult hasFilter returns true when filter applied.
     */
    @Test
    public void testSearchResult_HasFilterReturnsTrueWhenFilterApplied() {
        StationFilter filter = StationFilter.citiesOnly();
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        assertTrue(result.hasFilter());
    }

    /**
     * Test SearchResult station count matches list size.
     */
    @Test
    public void testSearchResult_StationCountMatchesListSize() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertEquals(result.getStations().size(), result.getStationCount());
    }

    /**
     * Test SearchResult execution time is positive.
     */
    @Test
    public void testSearchResult_ExecutionTimeIsPositive() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    /**
     * Test SearchResult toString returns non-null.
     */
    @Test
    public void testSearchResult_ToStringReturnsNonNull() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertNotNull(result.toString());
    }

    /**
     * Test SearchResult toString contains region info.
     */
    @Test
    public void testSearchResult_ToStringContainsRegion() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertTrue(result.toString().contains("Region"));
    }

    /**
     * Test SearchResult toString contains station count.
     */
    @Test
    public void testSearchResult_ToStringContainsStationCount() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertTrue(result.toString().contains("stations"));
    }

    /**
     * Test SearchResult toString contains execution time.
     */
    @Test
    public void testSearchResult_ToStringContainsExecutionTime() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertTrue(result.toString().contains("time"));
    }

    // ==================== EDGE CASE TESTS ====================

    /**
     * Test search with inverted coordinates (handled by GeographicRegion validation).
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSearch_InvertedLatitudes_ShouldThrowException() {
        controller.searchByGeographicArea(42.0, 36.0, -10.0, -6.0);
    }

    /**
     * Test search with inverted longitudes (handled by GeographicRegion validation).
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSearch_InvertedLongitudes_ShouldThrowException() {
        controller.searchByGeographicArea(36.0, 42.0, -6.0, -10.0);
    }

    /**
     * Test search with point region (zero area).
     */
    @Test
    public void testSearch_PointRegion_ShouldReturnAtMostOneStation() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(38.71387, 38.71387, -9.122271, -9.122271);

        assertTrue(result.getStationCount() <= 1);
    }

    /**
     * Test search with line region (latitude).
     */
    @Test
    public void testSearch_LineRegionLatitude_ShouldReturnStations() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(40.0, 40.0, -10.0, -6.0);

        assertNotNull(result);
    }

    /**
     * Test search with line region (longitude).
     */
    @Test
    public void testSearch_LineRegionLongitude_ShouldReturnStations() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -8.0, -8.0);

        assertNotNull(result);
    }

    /**
     * Test multiple searches return consistent results.
     */
    @Test
    public void testMultipleSearches_SameParameters_ShouldReturnConsistentResults() {
        GeographicSearchController.SearchResult result1 =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        GeographicSearchController.SearchResult result2 =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0);

        assertEquals(result1.getStationCount(), result2.getStationCount());
    }

    /**
     * Test search with boundary coordinates at Earth limits.
     */
    @Test
    public void testSearch_EarthBoundaries_ShouldHandleCorrectly() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(-90.0, 90.0, -180.0, 180.0);

        assertEquals(6, result.getStationCount());
    }

    /**
     * Test search with very small region returns fast.
     */
    @Test
    public void testSearch_VerySmallRegion_ShouldReturnQuickly() {
        long startTime = System.currentTimeMillis();

        controller.searchByGeographicArea(38.7, 38.8, -9.2, -9.1);

        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;

        assertTrue(elapsed < 1000); // Should complete in less than 1 second
    }

    /**
     * Test search result stations list is not null even when empty.
     */
    @Test
    public void testSearch_EmptyResult_StationsListIsNotNull() {
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(50.0, 51.0, 20.0, 21.0);

        assertNotNull(result.getStations());
    }

    /**
     * Test search with overlapping filter criteria.
     */
    @Test
    public void testSearch_OverlappingCriteria_ShouldApplyAllFilters() {
        StationFilter filter = new StationFilter(true, true, "PT");
        GeographicSearchController.SearchResult result =
                controller.searchByGeographicArea(36.0, 42.0, -10.0, -6.0, filter);

        for (Station station : result.getStations()) {
            assertTrue(station.isCity() && station.isMainStation() && "PT".equals(station.getCountry()));
        }
    }
}