package ESINF.model;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class RiskAwarePathResultTest {

    private RailwayStation createStation(int id) {
        return new RailwayStation(id, "Station" + id, 0, 0, 0, 0);
    }

    @Test
    public void testHasNegativeCycle() {
        // Updated constructor: added null for pathCosts
        RiskAwarePathResult result = new RiskAwarePathResult(true, null, null, 0.0, null);
        assertTrue("hasNegativeCycle() should return true", result.hasNegativeCycle());
    }

    @Test
    public void testGetPathAndCosts() {
        List<RailwayStation> path = Arrays.asList(createStation(1), createStation(2));
        List<Double> costs = Arrays.asList(0.0, 10.0);

        // Updated constructor
        RiskAwarePathResult result = new RiskAwarePathResult(false, path, costs, 10.0, null);

        assertEquals("getPath() should return the correct list", path, result.getPath());
        assertEquals("getPathCosts() should return the correct list", costs, result.getPathCosts());
    }

    @Test
    public void testGetTotalCost() {
        RiskAwarePathResult result = new RiskAwarePathResult(false, null, null, 123.45, null);
        assertEquals("getTotalCost() should return the correct value", 123.45, result.getTotalCost(), 0.001);
    }

    @Test
    public void testGetCycleStations() {
        List<RailwayStation> cycle = Arrays.asList(createStation(1), createStation(2), createStation(1));
        RiskAwarePathResult result = new RiskAwarePathResult(true, null, null, 0.0, cycle);

        assertEquals("getCycleStations() should return the correct cycle", cycle, result.getCycleStations());
    }
}