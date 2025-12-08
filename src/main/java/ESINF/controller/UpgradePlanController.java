package ESINF.controller;

import ESINF.DTO.UpgradePlanDTO;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import ESINF.service.TopologicalSortService;
import ESINF.service.CycleDetectionService;
import Graph.Graph;
import Graph.map.MapGraph;

import java.io.IOException;
import java.util.*;

/**
 * Controller for USEI11 - Directed Line Upgrade Plan
 * Manages the computation of upgrade ordering based on track dependencies
 */
public class UpgradePlanController {

    private final RailwayNetworkRepository networkRepository;
    private final TopologicalSortService topologicalService;
    private final CycleDetectionService cycleDetectionService;

    public UpgradePlanController() {
        this.networkRepository = new RailwayNetworkRepository();
        this.topologicalService = new TopologicalSortService();
        this.cycleDetectionService = new CycleDetectionService();
    }

    /**
     * Loads the Belgian railway network from CSV files
     * @param stationsPath path to stations.csv
     * @param linesPath path to lines.csv
     * @return LoadResult with statistics
     */
    public RailwayNetworkRepository.LoadResult loadRailwayNetwork(String stationsPath,
                                                                  String linesPath) throws IOException {
        return networkRepository.loadFromCSV(stationsPath, linesPath);
    }

    /**
     * Checks if data is loaded
     */
    public boolean isDataLoaded() {
        return networkRepository.isDataLoaded();
    }

    /**
     * Computes the upgrade plan for all stations
     * @return UpgradePlanDTO containing either topological order or cycle info
     */
    public UpgradePlanDTO computeUpgradePlan() {
        if (!networkRepository.isDataLoaded()) {
            throw new IllegalStateException("No railway network data loaded");
        }

        Graph<RailwayStation, Double> graph = networkRepository.getGraph();

        // First, detect if there are cycles
        List<List<RailwayStation>> cycles = cycleDetectionService.findAllCycles(graph);

        if (!cycles.isEmpty()) {
            // Graph has cycles - return cycle information
            return buildCycleDTO(cycles, graph);
        } else {
            // Graph is acyclic - compute topological order
            LinkedList<RailwayStation> topOrder = topologicalService.topologicalSort(graph);
            return buildTopologicalDTO(topOrder);
        }
    }

    /**
     * Gets network statistics
     */
    public NetworkStatistics getStatistics() {
        return new NetworkStatistics(
                networkRepository.getVertexCount(),
                networkRepository.getEdgeCount(),
                networkRepository.isDataLoaded()
        );
    }

    /**
     * Gets complexity analysis information
     */
    public String getComplexityAnalysis() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║          TEMPORAL COMPLEXITY ANALYSIS (USEI11)               ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");

        sb.append("ALGORITHM: Topological Sort (DFS-based)\n");
        sb.append("─".repeat(60)).append("\n");
        sb.append("• Time Complexity: O(V + E)\n");
        sb.append("  - V = number of vertices (stations)\n");
        sb.append("  - E = number of edges (track connections)\n");
        sb.append("  - Each vertex visited exactly once\n");
        sb.append("  - Each edge examined exactly once\n\n");

        sb.append("ALGORITHM: Cycle Detection (Colored DFS)\n");
        sb.append("─".repeat(60)).append("\n");
        sb.append("• Time Complexity: O(V + E)\n");
        sb.append("  - Uses DFS with color marking (white/gray/black)\n");
        sb.append("  - Gray node encountered → cycle detected\n");
        sb.append("  - Early termination possible when cycle found\n\n");

        if (isDataLoaded()) {
            NetworkStatistics stats = getStatistics();
            sb.append("CURRENT GRAPH:\n");
            sb.append("─".repeat(60)).append("\n");
            sb.append(String.format("• Vertices (V): %d stations\n", stats.vertexCount));
            sb.append(String.format("• Edges (E): %d connections\n", stats.edgeCount));
            sb.append(String.format("• Expected Operations: %d (V + E)\n",
                    stats.vertexCount + stats.edgeCount));
        }

        return sb.toString();
    }

    /**
     * Builds DTO for cycle information
     */
    private UpgradePlanDTO buildCycleDTO(List<List<RailwayStation>> cycles,
                                         Graph<RailwayStation, Double> graph) {
        List<UpgradePlanDTO.CycleInfo> cycleInfos = new ArrayList<>();

        for (List<RailwayStation> cycle : cycles) {
            List<String> vertices = new ArrayList<>();
            List<String> edges = new ArrayList<>();

            for (int i = 0; i < cycle.size(); i++) {
                RailwayStation current = cycle.get(i);
                vertices.add(current.getName());

                if (i < cycle.size() - 1) {
                    RailwayStation next = cycle.get(i + 1);
                    edges.add(current.getName() + " → " + next.getName());
                } else {
                    // Close the cycle
                    RailwayStation first = cycle.get(0);
                    edges.add(current.getName() + " → " + first.getName());
                }
            }

            cycleInfos.add(new UpgradePlanDTO.CycleInfo(vertices, edges));
        }

        return UpgradePlanDTO.withCycles(cycleInfos);  // ← FACTORY METHOD
    }

    /**
     * Builds DTO for topological order
     */
    private UpgradePlanDTO buildTopologicalDTO(LinkedList<RailwayStation> topOrder) {
        List<String> stationNames = new ArrayList<>();
        for (RailwayStation station : topOrder) {
            stationNames.add(station.getName());
        }
        return UpgradePlanDTO.withTopologicalOrder(stationNames);  // ← FACTORY METHOD
    }

    public RailwayNetworkRepository getRepository() {
        return networkRepository;
    }

    /**
     * Inner class for network statistics
     */
    public static class NetworkStatistics {
        public final int vertexCount;
        public final int edgeCount;
        public final boolean dataLoaded;

        public NetworkStatistics(int vertexCount, int edgeCount, boolean dataLoaded) {
            this.vertexCount = vertexCount;
            this.edgeCount = edgeCount;
            this.dataLoaded = dataLoaded;
        }

        @Override
        public String toString() {
            if (!dataLoaded) {
                return "⚠ No data loaded";
            }
            return String.format("📊 Network: %d stations, %d connections",
                    vertexCount, edgeCount);
        }
    }
}