package ESINF.controller;

import ESINF.model.RiskAwarePathResult;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import ESINF.service.RailwayNetworkOptimizationService;

import java.util.Collections;
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

            // Check for Negative Cycle using Getter
            if (result.hasNegativeCycle()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Negative cycle detected! \n");
                sb.append("Stations involved in cycle: ");

                // Use Getter for cycle stations
                List<RailwayStation> cycle = result.getCycleStations();
                if (cycle != null) {
                    for (RailwayStation s : cycle) {
                        sb.append(s.getName()).append(" -> ");
                    }
                    if (sb.length() > 4) {
                        sb.setLength(sb.length() - 4); // Remove last arrow
                    }
                }
                return sb.toString();
            }

            if (result.getPath() == null || result.getPath().isEmpty()) {
                return "No path exists between the selected stations.";
            }

            //  Format Success Output using Getters
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Shortest Path Found (Total Cost: %.2f)\n", result.getTotalCost()));
            sb.append("Path: ");

            List<RailwayStation> path = result.getPath();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).getName());
                if (i < path.size() - 1) {
                    sb.append(" -> ");
                }
            }
            return sb.toString();

        } catch (Exception e) {
            return "Error calculating path: " + e.getMessage();
        }
    }
}