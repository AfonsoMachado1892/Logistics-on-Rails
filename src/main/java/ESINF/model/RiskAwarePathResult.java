package ESINF.model;

import ESINF.model.RailwayStation;
import java.util.List;

/**
 * Encapsulates the path, step-by-step costs, total cost, and any detected negative cycles.
 */
public class RiskAwarePathResult {

    private final boolean hasNegativeCycle;
    private final List<RailwayStation> path;
    private final List<Double> pathCosts; // NEW: Stores cumulative cost at each node
    private final double totalCost;
    private final List<RailwayStation> cycleStations;

    /**
     * Constructor
     *
     * @param hasNegativeCycle true if a negative cost cycle was detected
     * @param path             The ordered list of stations representing the path (null if cycle detected)
     * @param pathCosts        The ordered list of cumulative costs for each station (null if cycle detected)
     * @param totalCost        The total accumulated cost/risk of the path
     * @param cycleStations    The list of stations forming the negative cycle (null if no cycle)
     */
    public RiskAwarePathResult(boolean hasNegativeCycle, List<RailwayStation> path, List<Double> pathCosts,
                               double totalCost, List<RailwayStation> cycleStations) {
        this.hasNegativeCycle = hasNegativeCycle;
        this.path = path;
        this.pathCosts = pathCosts;
        this.totalCost = totalCost;
        this.cycleStations = cycleStations;
    }

    public boolean hasNegativeCycle() {
        return hasNegativeCycle;
    }

    public List<RailwayStation> getPath() {
        return path;
    }

    public List<Double> getPathCosts() {
        return pathCosts;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public List<RailwayStation> getCycleStations() {
        return cycleStations;
    }
}