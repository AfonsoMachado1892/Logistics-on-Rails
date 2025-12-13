package ESINF.model;

import ESINF.model.RailwayStation;
import java.util.List;

/**
 * Encapsulates the path, total cost, and any detected negative cycles.
 */
public class RiskAwarePathResult {

    private final boolean hasNegativeCycle;
    private final List<RailwayStation> path;
    private final double totalCost;
    private final List<RailwayStation> cycleStations;

    /**
     * Constructor
     *
     * @param hasNegativeCycle true if a negative cost cycle was detected
     * @param path             The ordered list of stations representing the path (null if cycle detected)
     * @param totalCost        The total accumulated cost/risk of the path
     * @param cycleStations    The list of stations forming the negative cycle (null if no cycle)
     */
    public RiskAwarePathResult(boolean hasNegativeCycle, List<RailwayStation> path,
                               double totalCost, List<RailwayStation> cycleStations) {
        this.hasNegativeCycle = hasNegativeCycle;
        this.path = path;
        this.totalCost = totalCost;
        this.cycleStations = cycleStations;
    }

    public boolean hasNegativeCycle() {
        return hasNegativeCycle;
    }

    public List<RailwayStation> getPath() {
        return path;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public List<RailwayStation> getCycleStations() {
        return cycleStations;
    }
}