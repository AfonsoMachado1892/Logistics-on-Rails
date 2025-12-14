package ESINF.controller;

import ESINF.model.RiskAwarePathResult;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import ESINF.service.RailwayNetworkOptimizationService;

import java.util.List;

public class RiskAwareShortestPathController {

    private final RailwayNetworkOptimizationService optimizationService;

    public RiskAwareShortestPathController(RailwayNetworkRepository repository) {
        this.optimizationService = new RailwayNetworkOptimizationService(repository);
    }

    /**
     * USEI15 - Calculate Risk-Aware Shortest Path
     */
    public String calculateShortestPath(int sourceId, int targetId) {
        try {
            // Call the service
            RiskAwarePathResult result = optimizationService.getRiskAwareShortestPath(sourceId, targetId);

            StringBuilder sb = new StringBuilder();

            // Check for Negative Cycle
            if (result.hasNegativeCycle()) {
                sb.append("Negative cycle detected!\n");
                sb.append("Stations involved in cycle: ");

                List<RailwayStation> cycle = result.getCycleStations();
                if (cycle != null) {
                    for (RailwayStation s : cycle) {
                        sb.append(s.getName()).append(" (ID:").append(s.getStationId()).append(") -> ");
                    }
                    if (sb.length() > 4) {
                        sb.setLength(sb.length() - 4); // Remove last arrow
                    }
                }
            } else if (result.getPath() == null || result.getPath().isEmpty()) {
                return "No path exists between the selected stations.";
            } else {
                // Success Output with strict formatting
                sb.append("Calculation Result:\n");
                sb.append("Shortest Path (depart stid, cost, ... , target stid)\n");
                sb.append("Path Sequence: (");

                List<RailwayStation> path = result.getPath();
                List<Double> costs = result.getPathCosts();

                for (int i = 0; i < path.size(); i++) {
                    sb.append(path.get(i).getStationId());
                    sb.append(", ");
                    sb.append(String.format("%.2f", costs.get(i)));

                    if (i < path.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(")\n");
                sb.append(String.format("Total Cost to Target: %.2f\n", result.getTotalCost()));
            }
            return sb.toString();

        } catch (Exception e) {
            return "Error calculating path: " + e.getMessage();
        }
    }
}