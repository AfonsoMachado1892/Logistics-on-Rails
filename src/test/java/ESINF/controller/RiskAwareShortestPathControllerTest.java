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

import static org.junit.Assert.assertTrue;

public class RiskAwareShortestPathControllerTest {

    private RiskAwareShortestPathController controller;
    private StubOptimizationService stubService;

    // --- Inner Stub Class for Service ---
    // Allows controlling the exact output the controller receives without running the algorithm
    private static class StubOptimizationService extends RailwayNetworkOptimizationService {

        private RiskAwarePathResult resultToReturn;
        private RuntimeException exceptionToThrow;

        public StubOptimizationService() {
            super(null); // Pass null as repo since we override the method using it
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
        // Use a dummy repository for the constructor (it won't be used due to injection)
        RailwayNetworkRepository dummyRepo = new RailwayNetworkRepository();

        controller = new RiskAwareShortestPathController(dummyRepo);
        stubService = new StubOptimizationService();

        // Inject the stub service into the private field 'optimizationService'
        // This forces the controller to use our stub instead of the real service
        Field serviceField = RiskAwareShortestPathController.class.getDeclaredField("optimizationService");
        serviceField.setAccessible(true);
        serviceField.set(controller, stubService);
    }

    @Test
    public void calculateShortestPath() {
        RailwayStation s1 = new RailwayStation(1, "A", 0, 0, 0, 0);
        RailwayStation s2 = new RailwayStation(2, "B", 0, 0, 0, 0);

        RiskAwarePathResult successResult = new RiskAwarePathResult(
                false, Arrays.asList(s1, s2), 10.5, null
        );

        stubService.setResultToReturn(successResult);

        String output = controller.calculateShortestPath(1, 2);

        assertTrue("Output should contain 'Shortest Path Found'", output.contains("Shortest Path Found"));
    }

    @Test
    public void testControllerDisplaysTotalCostInResult() {
        RailwayStation s1 = new RailwayStation(1, "A", 0, 0, 0, 0);

        RiskAwarePathResult successResult = new RiskAwarePathResult(
                false, Collections.singletonList(s1), 50.0, null
        );

        stubService.setResultToReturn(successResult);

        String result = controller.calculateShortestPath(1, 2);

        // Check for 50,00 or 50.00 depending on Locale
        assertTrue("Output should contain the cost", result.contains("50,00") || result.contains("50.00"));
    }

    @Test
    public void testControllerFormatsNegativeCycleWarningCorrectly() {
        RailwayStation s1 = new RailwayStation(1, "CycleNode", 0, 0, 0, 0);

        // Prepare cycle result (hasNegativeCycle = true)
        RiskAwarePathResult cycleResult = new RiskAwarePathResult(
                true, null, 0.0, Collections.singletonList(s1)
        );

        stubService.setResultToReturn(cycleResult);

        String result = controller.calculateShortestPath(1, 2);

        assertTrue("Output should warn about negative cycle", result.contains("Negative cycle detected"));
    }

    @Test
    public void testControllerFormatsNoPathMessageCorrectly() {
        // Prepare empty result (no path)
        RiskAwarePathResult emptyResult = new RiskAwarePathResult(
                false, Collections.emptyList(), Double.MAX_VALUE, null
        );

        stubService.setResultToReturn(emptyResult);

        String result = controller.calculateShortestPath(1, 2);

        assertTrue("Output should indicate no path exists", result.contains("No path exists"));
    }

    @Test
    public void testControllerHandlesServiceExceptions() {
        stubService.setExceptionToThrow(new IllegalArgumentException("Invalid ID"));

        String result = controller.calculateShortestPath(1, 2);

        assertTrue("Output should report an error occurred", result.contains("Error calculating path"));
    }
}