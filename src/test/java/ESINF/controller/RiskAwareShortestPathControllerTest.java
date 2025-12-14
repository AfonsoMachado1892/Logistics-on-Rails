package ESINF.controller;

import ESINF.model.RiskAwarePathResult;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import ESINF.service.RailwayNetworkOptimizationService;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class RiskAwareShortestPathControllerTest {

    private RiskAwareShortestPathController controller;
    private StubOptimizationService stubService;

    // --- Inner Stub Class for Service ---
    private static class StubOptimizationService extends RailwayNetworkOptimizationService {

        private RiskAwarePathResult resultToReturn;
        private RuntimeException exceptionToThrow;

        public StubOptimizationService() {
            super(null);
        }

        public void setResultToReturn(RiskAwarePathResult result) {
            this.resultToReturn = result;
            this.exceptionToThrow = null;
        }

        public void setExceptionToThrow(RuntimeException ex) {
            this.exceptionToThrow = ex;
        }

        @Override
        public RiskAwarePathResult getRiskAwareShortestPath(int sourceId, int targetId) {
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            return resultToReturn;
        }
    }

    @Before
    public void setUp() throws Exception {
        RailwayNetworkRepository dummyRepo = new RailwayNetworkRepository();
        controller = new RiskAwareShortestPathController(dummyRepo);
        stubService = new StubOptimizationService();

        // Inject the stub service using Reflection
        Field serviceField = RiskAwareShortestPathController.class.getDeclaredField("optimizationService");
        serviceField.setAccessible(true);
        serviceField.set(controller, stubService);
    }

    @Test
    public void testOutputContainsCalculationHeader() {
        RailwayStation s1 = new RailwayStation(1, "S1", 0, 0, 0, 0);
        RiskAwarePathResult result = new RiskAwarePathResult(false, Collections.singletonList(s1), Collections.singletonList(0.0), 0.0, null);

        stubService.setResultToReturn(result);
        String resultStr = controller.calculateShortestPath(1, 1);

        assertTrue("Output should contain header", resultStr.contains("Calculation Result:"));
    }

    @Test
    public void testOutputContainsFormattedPathSequence() {
        RailwayStation s1 = new RailwayStation(1, "S1", 0, 0, 0, 0);
        RailwayStation s2 = new RailwayStation(2, "S2", 0, 0, 0, 0);
        List<Double> costs = Arrays.asList(0.0, 50.0);

        // Path sequence for 1 -> 2 with cost 50
        RiskAwarePathResult result = new RiskAwarePathResult(false, Arrays.asList(s1, s2), costs, 50.0, null);

        stubService.setResultToReturn(result);
        String resultStr = controller.calculateShortestPath(1, 2);

        // Fixed Assertion: Matches the format (ID, CumulativeCost, ID, CumulativeCost)
        // Checks for both dot (.) and comma (,) decimal separators
        boolean hasPattern = resultStr.contains("(1, 0.00, 2, 50.00)") ||
                resultStr.contains("(1, 0,00, 2, 50,00)");

        assertTrue("Output should contain correct path sequence. Actual: " + resultStr, hasPattern);
    }

    @Test
    public void testOutputContainsTotalCost() {
        RailwayStation s1 = new RailwayStation(1, "S1", 0, 0, 0, 0);
        RiskAwarePathResult result = new RiskAwarePathResult(false, Collections.singletonList(s1), Collections.singletonList(0.0), 123.45, null);

        stubService.setResultToReturn(result);
        String resultStr = controller.calculateShortestPath(1, 1);

        boolean hasCost = resultStr.contains("123.45") || resultStr.contains("123,45");
        assertTrue("Output should contain total cost", hasCost);
    }

    @Test
    public void testOutputFormatsNegativeCycleWarning() {
        RailwayStation s1 = new RailwayStation(1, "CycleNode", 0, 0, 0, 0);
        RiskAwarePathResult result = new RiskAwarePathResult(true, null, null, 0.0, Collections.singletonList(s1));

        stubService.setResultToReturn(result);
        String resultStr = controller.calculateShortestPath(1, 2);

        assertTrue("Output should warn about negative cycle", resultStr.contains("Negative cycle detected"));
    }

    @Test
    public void testOutputFormatsNoPathMessage() {
        RiskAwarePathResult result = new RiskAwarePathResult(false, Collections.emptyList(), Collections.emptyList(), Double.MAX_VALUE, null);

        stubService.setResultToReturn(result);
        String resultStr = controller.calculateShortestPath(1, 2);

        assertTrue("Output should indicate no path exists", resultStr.contains("No path exists"));
    }

    @Test
    public void testOutputHandlesExceptions() {
        stubService.setExceptionToThrow(new IllegalArgumentException("TestError"));

        String resultStr = controller.calculateShortestPath(1, 2);

        // Fixed: Controller outputs "Error calculating path: TestError"
        assertTrue("Output should contain error message", resultStr.contains("Error calculating path: TestError"));
    }
}