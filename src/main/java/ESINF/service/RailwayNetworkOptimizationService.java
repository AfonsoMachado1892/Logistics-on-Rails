package ESINF.service;

import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import Graph.Edge;
import Graph.Graph;
import ESINF.model.RiskAwarePathResult;

import java.util.*;

/**
 * Service for Railway Network Optimization
 * Implements USEI15: Risk-Aware Shortest Paths using Bellman-Ford
 */
public class RailwayNetworkOptimizationService {

    private final RailwayNetworkRepository repository;

    public RailwayNetworkOptimizationService(RailwayNetworkRepository repository) {
        this.repository = repository;
    }

    /**
     * USEI15 - Risk-Aware Shortest Paths
     * Computes the shortest path using the Bellman-Ford algorithm on the COST graph.
     */
    public RiskAwarePathResult getRiskAwareShortestPath(int sourceStationId, int targetStationId) {
        if (!repository.isDataLoaded()) {
            throw new IllegalStateException("Railway network data is not loaded.");
        }

        RailwayStation source = repository.getStationById(sourceStationId);
        RailwayStation target = repository.getStationById(targetStationId);

        if (source == null || target == null) {
            throw new IllegalArgumentException("Invalid station IDs provided.");
        }

        // This ensures we use the 'cost' column (which can be negative) as weights
        Graph<RailwayStation, Double> graph = repository.getCostGraph();

        int numVertices = graph.numVertices();

        // Initialization
        Map<RailwayStation, Double> dist = new HashMap<>();
        Map<RailwayStation, RailwayStation> predecessor = new HashMap<>();

        for (RailwayStation v : graph.vertices()) {
            dist.put(v, Double.MAX_VALUE);
            predecessor.put(v, null);
        }
        dist.put(source, 0.0);

        // Relaxation (V - 1 times)
        for (int i = 1; i < numVertices; i++) {
            boolean changed = false;
            for (Edge<RailwayStation, Double> edge : graph.edges()) {
                if (relax(edge, dist, predecessor)) {
                    changed = true;
                }
            }
            if (!changed) break; // Optimization
        }

        // Negative Cycle Detection
        for (Edge<RailwayStation, Double> edge : graph.edges()) {
            RailwayStation u = edge.getVOrig();
            RailwayStation v = edge.getVDest();
            double weight = edge.getWeight();

            if (dist.get(u) != Double.MAX_VALUE && dist.get(u) + weight < dist.get(v)) {
                // Negative cycle detected
                return new RiskAwarePathResult(true, null, 0.0, identifyCycle(v, predecessor));
            }
        }

        // Construct Path
        if (dist.get(target) == Double.MAX_VALUE) {
            return new RiskAwarePathResult(false, Collections.emptyList(), Double.MAX_VALUE, null);
        }

        LinkedList<RailwayStation> path = new LinkedList<>();
        RailwayStation current = target;
        while (current != null) {
            path.addFirst(current);
            current = predecessor.get(current);
        }

        return new RiskAwarePathResult(false, path, dist.get(target), null);
    }

    private boolean relax(Edge<RailwayStation, Double> edge,
                          Map<RailwayStation, Double> dist,
                          Map<RailwayStation, RailwayStation> predecessor) {
        RailwayStation u = edge.getVOrig();
        RailwayStation v = edge.getVDest();
        double weight = edge.getWeight();

        if (dist.get(u) != Double.MAX_VALUE && dist.get(u) + weight < dist.get(v)) {
            dist.put(v, dist.get(u) + weight);
            predecessor.put(v, u);
            return true;
        }
        return false;
    }

    private List<RailwayStation> identifyCycle(RailwayStation startNode, Map<RailwayStation, RailwayStation> pred) {
        List<RailwayStation> cycle = new ArrayList<>();
        RailwayStation curr = startNode;

        // Move back V times to ensure we are inside the loop
        for (int i = 0; i < pred.size(); i++) {
            curr = pred.get(curr);
        }

        RailwayStation cycleStart = curr;
        do {
            cycle.add(curr);
            curr = pred.get(curr);
        } while (curr != cycleStart && curr != null);
        cycle.add(cycleStart);

        return cycle;
    }
}