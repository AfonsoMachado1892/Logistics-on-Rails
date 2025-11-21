package ESINF.controller;

import ESINF.model.KDTree2D;
import ESINF.model.Station;
import ESINF.model.TimeZoneFilter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class ProximitySearchControllerTest {

    private KDTree2D realTree;
    private KDTree2D emptyTree;
    private Station stationA;
    private Station stationB;
    private List<Station> stations;

    @Before
    public void setUp() {
        // 1. Create Dummy Stations
        // Station A: Near (10, 10) - WET
        stationA = new Station("Station A", 10.0, 10.0, "PT", "Europe/Lisbon", "WET", true, true, false);
        // Station B: Far (20, 20) - CET
        stationB = new Station("Station B", 20.0, 20.0, "ES", "Europe/Madrid", "CET", true, true, false);

        // 2. Built Tree
        stations = new ArrayList<>();
        stations.add(stationA);
        stations.add(stationB);

        realTree = new KDTree2D();
        realTree.buildBalanced(stations, stations);

        // 3. Unbuilt Tree
        emptyTree = new KDTree2D();
    }

    @Test
    public void testControllerCreationSuccess() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        assertNotNull(controller);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testControllerCreationNullTree_ThrowsException() {
        new ProximitySearchController(null);
    }

    @Test
    public void testControllerCreationNullTreeMessage() {
        try {
            new ProximitySearchController(null);
            fail("Expected IllegalArgumentException was not thrown.");
        } catch (IllegalArgumentException e) {
            assertEquals("KDTree cannot be null", e.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testControllerCreationUnbuiltTree_ThrowsException() {
        new ProximitySearchController(emptyTree);
    }

    @Test
    public void testControllerCreationUnbuiltTreeMessage() {
        try {
            new ProximitySearchController(emptyTree);
            fail("Expected IllegalStateException was not thrown.");
        } catch (IllegalStateException e) {
            assertEquals("KDTree must be built before searching", e.getMessage());
        }
    }

    @Test
    public void testSearchResult_TargetLat_IsCorrect() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        double targetLat = 10.0;
        ProximitySearchController.SearchResult result = controller.searchNearest(targetLat, 10.0, 1, null);
        assertEquals(targetLat, result.getTargetLat(), 0.0001);
    }

    @Test
    public void testSearchResult_TargetLon_IsCorrect() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        double targetLon = 10.0;
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, targetLon, 1, null);
        assertEquals(targetLon, result.getTargetLon(), 0.0001);
    }

    @Test
    public void testSearchResult_N_IsCorrect() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        int N = 1;
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, N, null);
        assertEquals(N, result.getN());
    }

    @Test
    public void testSearchResult_Filter_IsCorrect() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        TimeZoneFilter filter = new TimeZoneFilter("WET");
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, filter);
        assertEquals(filter, result.getFilter());
    }

    @Test
    public void testSearchResult_ExecutionTime_IsRecorded() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);
        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    @Test
    public void testSearchNearestIntegration_CorrectCount() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);
        assertEquals(1, result.getStationCount());
    }

    @Test
    public void testSearchNearestIntegration_CorrectStation() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);
        assertEquals("Station A", result.getStations().get(0).getName());
    }

    @Test
    public void testSearchNearestIntegration_WithFilter() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        TimeZoneFilter filter = new TimeZoneFilter("CET");
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, filter);

        assertEquals("Station B", result.getStations().get(0).getName());
    }

    @Test
    public void testSearchResultToString_ContainsTarget() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);
        String output = result.toString();
        assertTrue(output.contains("Target: (10,00000, 10,00000)"));
    }

    @Test
    public void testSearchResultToString_ContainsN() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);
        String output = result.toString();
        assertTrue(output.contains("N: 1"));
    }

    @Test
    public void testSearchResultToString_ContainsResultsCount() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);
        String output = result.toString();
        assertTrue(output.contains("Results: 1 stations"));
    }
}