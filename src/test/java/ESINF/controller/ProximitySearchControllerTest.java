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
    private Station stationA;
    private Station stationB;

    @Before
    void setUp() {
        // 1. Initialize the Tree
        realTree = new KDTree2D();

        // 2. Create Dummy Stations
        // Station A: Near (10, 10)
        stationA = new Station("Station A", 10.0, 10.0, "PT", "Europe/Lisbon", "WET", true, true, false);
        // Station B: Far (20, 20)
        stationB = new Station("Station B", 20.0, 20.0, "ES", "Europe/Madrid", "CET", true, true, false);

        // 3. Build the tree so it is valid for the Controller
        List<Station> stations = new ArrayList<>();
        stations.add(stationA);
        stations.add(stationB);

        // We use the build method to populate the root (satisfying the "must be built" check)
        realTree.buildBalanced(stations, stations);
    }

    @Test
    void testControllerCreationSuccess() {
        ProximitySearchController controller = new ProximitySearchController(realTree);
        assertNotNull(controller, "Controller should be created successfully with a built tree");
    }

    @Test
    void testControllerCreationNullTree() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProximitySearchController(null);
        });
        assertEquals("KDTree cannot be null", exception.getMessage());
    }

    @Test
    void testControllerCreationUnbuiltTree() {
        // Create a tree but don't build it (root remains null)
        KDTree2D emptyTree = new KDTree2D();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            new ProximitySearchController(emptyTree);
        });
        assertEquals("KDTree must be built before searching", exception.getMessage());
    }

    @Test
    void testSearchNearestReturnsCorrectWrapperObject() {
        ProximitySearchController controller = new ProximitySearchController(realTree);

        double targetLat = 10.0;
        double targetLon = 10.0;
        int N = 1;
        TimeZoneFilter filter = new TimeZoneFilter("WET");

        // Execute Search
        ProximitySearchController.SearchResult result = controller.searchNearest(targetLat, targetLon, N, filter);

        // Verify the wrapper object contains the inputs we sent
        assertNotNull(result, "Result object should not be null");
        assertEquals(targetLat, result.getTargetLat(), 0.0001);
        assertEquals(targetLon, result.getTargetLon(), 0.0001);
        assertEquals(N, result.getN());
        assertEquals(filter, result.getFilter());

        // Verify execution time is recorded (should be >= 0)
        assertTrue(result.getExecutionTimeMs() >= 0, "Execution time should be recorded");
    }

    @Test
    void testSearchNearestIntegration() {
        // This tests that the controller correctly delegates to the tree
        // and returns the station we expect.
        ProximitySearchController controller = new ProximitySearchController(realTree);

        // Search exactly at Station A's coordinates
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);

        // We expect 1 station found, and it must be Station A
        assertEquals(1, result.getStationCount());
        assertEquals("Station A", result.getStations().get(0).getName());
    }

    @Test
    void testSearchResultToString() {
        // Validates the toString method used for console output
        ProximitySearchController controller = new ProximitySearchController(realTree);
        ProximitySearchController.SearchResult result = controller.searchNearest(10.0, 10.0, 1, null);

        String output = result.toString();

        assertTrue(output.contains("Target: (10.00000, 10.00000)"));
        assertTrue(output.contains("N: 1"));
        assertTrue(output.contains("Results: 1 stations"));
    }
}