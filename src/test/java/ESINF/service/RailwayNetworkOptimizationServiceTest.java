package ESINF.service;

import ESINF.model.RiskAwarePathResult;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import Graph.Graph;
import Graph.map.MapGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RailwayNetworkOptimizationServiceTest {

    private StubRailwayNetworkRepository repository;
    private RailwayNetworkOptimizationService service;
    private RailwayStation s1, s2, s3, s4;

    // --- Inner Stub Class to replace the real Repository ---
    private static class StubRailwayNetworkRepository extends RailwayNetworkRepository {
        private final MapGraph<RailwayStation, Double> testGraph = new MapGraph<>(true);
        private final Map<Integer, RailwayStation> testStations = new HashMap<>();
        private boolean isLoaded = true;

        @Override
        public Graph<RailwayStation, Double> getCostGraph() {
            return testGraph;
        }

        @Override
        public RailwayStation getStationById(int id) {
            return testStations.get(id);
        }

        @Override
        public boolean isDataLoaded() {
            return isLoaded;
        }

        public void setLoaded(boolean loaded) { this.isLoaded = loaded; }

        public void addStation(RailwayStation s) {
            testGraph.addVertex(s);
            testStations.put(s.getStationId(), s);
        }

        public void addEdge(RailwayStation a, RailwayStation b, double weight) {
            testGraph.addEdge(a, b, weight);
        }
    }

    @Before
    public void setUp() {
        repository = new StubRailwayNetworkRepository();
        service = new RailwayNetworkOptimizationService(repository);

        s1 = new RailwayStation(1, "Station1", 0, 0, 0, 0);
        s2 = new RailwayStation(2, "Station2", 0, 0, 0, 0);
        s3 = new RailwayStation(3, "Station3", 0, 0, 0, 0);
        s4 = new RailwayStation(4, "Station4", 0, 0, 0, 0);
    }

    // --- SCENARIO 1: Negative Edges (Beneficial Path) ---

    @Test
    public void getRiskAwareShortestPath() {
        // test NegativeEdgeOptimization Calculates CorrectCost
        repository.addStation(s1);
        repository.addStation(s2);
        repository.addStation(s3);

        repository.addEdge(s1, s2, 10.0);
        repository.addEdge(s1, s3, 5.0);
        repository.addEdge(s3, s2, -2.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);

        assertEquals("Total cost should correspond to the path with negative edge (5 - 2 = 3)",
                3.0, result.getTotalCost(), 0.001);
    }

    @Test
    public void testNegativeEdgeOptimizationReturnsCorrectPathList() {
        repository.addStation(s1);
        repository.addStation(s2);
        repository.addStation(s3);

        repository.addEdge(s1, s2, 10.0);
        repository.addEdge(s1, s3, 5.0);
        repository.addEdge(s3, s2, -2.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);
        List<RailwayStation> expectedPath = Arrays.asList(s1, s3, s2);

        // Cumulative costs: S1(0.0) -> S3(5.0) -> S2(5.0 - 2.0 = 3.0)
        List<Double> expectedCosts = Arrays.asList(0.0, 5.0, 3.0);

        assertEquals("Path should be [s1, s3, s2]", expectedPath, result.getPath());

        // FIX: Verify pathCosts are correct
        assertEquals("Path costs should match cumulative weights", expectedCosts, result.getPathCosts());
    }

    @Test
    public void testNegativeEdgeOptimizationDoesNotReportCycle() {
        repository.addStation(s1);
        repository.addStation(s2);
        repository.addStation(s3);

        repository.addEdge(s1, s2, 10.0);
        repository.addEdge(s1, s3, 5.0);
        repository.addEdge(s3, s2, -2.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);

        assertFalse("Should not report negative cycle for valid negative edges", result.hasNegativeCycle());
    }

    // --- SCENARIO 2: Negative Cycle Detection ---

    @Test
    public void testNegativeCycleIsDetected() {
        repository.addStation(s1);
        repository.addStation(s2);
        repository.addStation(s3);

        repository.addEdge(s1, s2, 1.0);
        repository.addEdge(s2, s3, -5.0);
        repository.addEdge(s3, s1, 2.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);

        assertTrue("Should detect the negative cycle", result.hasNegativeCycle());
    }

    @Test
    public void testNegativeCycleReturnsCycleStations() {
        repository.addStation(s1);
        repository.addStation(s2);
        repository.addStation(s3);

        repository.addEdge(s1, s2, 1.0);
        repository.addEdge(s2, s3, -5.0);
        repository.addEdge(s3, s1, 2.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);

        assertNotNull("Should return the list of stations involved in the cycle", result.getCycleStations());
    }

    // --- SCENARIO 3: Disconnected Graph ---

    @Test
    public void testUnreachableDestinationReturnsEmptyPath() {
        repository.addStation(s1);
        repository.addStation(s4);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 4);

        assertTrue("Path list should be empty for disconnected stations", result.getPath().isEmpty());
        assertTrue("Path costs should be empty for disconnected stations", result.getPathCosts().isEmpty());
    }

    // --- SCENARIO 4: Validations ---

    @Test(expected = IllegalStateException.class)
    public void testThrowsExceptionWhenDataNotLoaded() {
        repository.setLoaded(false);
        service.getRiskAwareShortestPath(1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidSourceId() {
        repository.addStation(s2);
        service.getRiskAwareShortestPath(999, 2);
    }

    // --- SCENARIO 5: Indirect Path is Cheaper (Positive Weights) ---
    // Direct: S1 -> S2 (Cost 20.0)
    // Indirect: S1 -> S3 (5.0) -> S4 (5.0) -> S2 (5.0) = Total 15.0
    // Algorithm should choose the indirect path.

    @Test
    public void getRiskAwareShortestPath_CalculatesCorrectCostForIndirectCheaperPath() {
        repository.addStation(s1); repository.addStation(s2);
        repository.addStation(s3); repository.addStation(s4);

        repository.addEdge(s1, s2, 20.0);
        repository.addEdge(s1, s3, 5.0);
        repository.addEdge(s3, s4, 5.0);
        repository.addEdge(s4, s2, 5.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);

        assertEquals(15.0, result.getTotalCost(), 0.001);
    }

    @Test
    public void getRiskAwareShortestPath_ReturnsCorrectPathSequenceForIndirectCheaperPath() {
        repository.addStation(s1); repository.addStation(s2);
        repository.addStation(s3); repository.addStation(s4);

        repository.addEdge(s1, s2, 20.0);
        repository.addEdge(s1, s3, 5.0);
        repository.addEdge(s3, s4, 5.0);
        repository.addEdge(s4, s2, 5.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);
        List<RailwayStation> expectedPath = Arrays.asList(s1, s3, s4, s2);

        // Cumulative: 0 -> 5 -> 10 -> 15
        List<Double> expectedCosts = Arrays.asList(0.0, 5.0, 10.0, 15.0);

        assertEquals(expectedPath, result.getPath());
        assertEquals(expectedCosts, result.getPathCosts());
    }

    // --- SCENARIO 6: Massive Negative Bonus ---
    // Direct: S1 -> S2 (Cost 10.0)
    // Indirect: S1 -> S3 (Cost 100.0) -> S2 (Cost -95.0) = Total 5.0
    // The algorithm must accept the high initial cost (100) to get the huge bonus (-95).

    @Test
    public void getRiskAwareShortestPath_CalculatesCostWithMassiveNegativeBonus() {
        repository.addStation(s1); repository.addStation(s2); repository.addStation(s3);

        repository.addEdge(s1, s2, 10.0);
        repository.addEdge(s1, s3, 100.0);
        repository.addEdge(s3, s2, -95.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);

        assertEquals(5.0, result.getTotalCost(), 0.001);
    }

    @Test
    public void getRiskAwareShortestPath_ReturnsPathWithMassiveNegativeBonus() {
        repository.addStation(s1); repository.addStation(s2); repository.addStation(s3);

        repository.addEdge(s1, s2, 10.0);
        repository.addEdge(s1, s3, 100.0);
        repository.addEdge(s3, s2, -95.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);
        List<RailwayStation> expected = Arrays.asList(s1, s3, s2);

        assertEquals(expected, result.getPath());
    }

    // --- SCENARIO 7: Zero Cost Edges ---
    // Direct: S1 -> S2 (Cost 15.0)
    // Indirect: S1 -> S3 (Cost 10.0) -> S2 (Cost 0.0) = Total 10.0
    // Zero cost edges should be handled correctly as valid weights.

    @Test
    public void getRiskAwareShortestPath_HandlesZeroCostEdgesCorrectly() {
        repository.addStation(s1); repository.addStation(s2); repository.addStation(s3);

        repository.addEdge(s1, s2, 15.0);
        repository.addEdge(s1, s3, 10.0);
        repository.addEdge(s3, s2, 0.0);

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 2);

        assertEquals(10.0, result.getTotalCost(), 0.001);
    }

    // --- SCENARIO 8: Large Grid with Shortcut ---
    // Grid structure (Weights are generally 10.0)
    // S1 -> S2 -> S3
    // |           |
    // v           v
    // S4 -> S5 -> S6
    //
    // Standard path: S1->S2->S3->S6 (Cost 30)
    // Shortcut path: S1->S4->S5->S2->S3->S6 (Using a negative edge S5->S2 of -50)
    //
    // FIX: We removed S2->S5 to prevent a negative cycle (S2->S5->S2) which would
    // cost 10 - 50 = -40. Without S2->S5, it's a valid DAG-like shortcut.

    @Test
    public void getRiskAwareShortestPath_FindsComplexPathInGridWithNegativeShortcut() {
        // Create 6 stations
        RailwayStation s5 = new RailwayStation(5, "S5", 0,0,0,0);
        RailwayStation s6 = new RailwayStation(6, "S6", 0,0,0,0);

        repository.addStation(s1); repository.addStation(s2); repository.addStation(s3);
        repository.addStation(s4); repository.addStation(s5); repository.addStation(s6);

        // Horizontal edges (Top Row)
        repository.addEdge(s1, s2, 10.0);
        repository.addEdge(s2, s3, 10.0);

        // Horizontal edges (Bottom Row)
        repository.addEdge(s4, s5, 10.0);
        repository.addEdge(s5, s6, 10.0);

        // Vertical edges (Downwards)
        repository.addEdge(s1, s4, 10.0);
        // repository.addEdge(s2, s5, 10.0); // REMOVED to avoid negative cycle (10 - 50 = -40)
        repository.addEdge(s3, s6, 10.0);

        // *** The Special Negative Shortcut (Upwards) ***
        // S5 -> S2 with huge bonus
        repository.addEdge(s5, s2, -50.0);

        // Path analysis:
        // 1. Normal (S1->S2->S3->S6): 10 + 10 + 10 = 30.0
        // 2. Via Shortcut (S1->S4->S5->S2->S3->S6):
        //    10 (S1->S4) + 10 (S4->S5) - 50 (S5->S2) + 10 (S2->S3) + 10 (S3->S6)
        //    Total: 10 + 10 - 50 + 10 + 10 = -10.0

        RiskAwarePathResult result = service.getRiskAwareShortestPath(1, 6);

        // Verify we didn't hit a cycle
        assertFalse("Should not detect a cycle", result.hasNegativeCycle());

        // Verify path cost
        assertEquals(-10.0, result.getTotalCost(), 0.001);

        // Verify sequence
        List<RailwayStation> expectedPath = Arrays.asList(s1, s4, s5, s2, s3, s6);
        assertEquals(expectedPath, result.getPath());
    }
}