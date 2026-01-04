package LAPR3.service;

import LAPR3.model.Facility;
import LAPR3.model.Line;
import LAPR3.model.Segment;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.LineRepository;
import LAPR3.repository.SegmentRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for automatic path calculation using Bellman-Ford algorithm
 * Supports negative edge weights (penalties/bonuses)
 * Part of USLP10 - Automatic Path Planning with ESINF US15
 */
public class AutomaticPathPlanningService {
    private final FacilityRepository facilityRepository;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;

    public AutomaticPathPlanningService(FacilityRepository facilityRepository,
                                        LineRepository lineRepository,
                                        SegmentRepository segmentRepository) {
        this.facilityRepository = facilityRepository;
        this.lineRepository = lineRepository;
        this.segmentRepository = segmentRepository;
    }

    /**
     * Calculate optimal path between two facilities using Bellman-Ford
     * Handles negative costs (bonuses) and detects negative cycles
     */
    public PathCalculationResult calculateOptimalPath(int startFacilityId, int endFacilityId) {
        // Validate facilities exist
        if (facilityRepository.findById(startFacilityId).isEmpty()) {
            System.err.println("Start facility not found: " + startFacilityId);
            return new PathCalculationResult();
        }

        if (facilityRepository.findById(endFacilityId).isEmpty()) {
            System.err.println("End facility not found: " + endFacilityId);
            return new PathCalculationResult();
        }

        // Build graph
        Map<Integer, List<Edge>> graph = buildGraph();

        if (graph.isEmpty()) {
            System.err.println("Graph is empty - no lines loaded");
            return new PathCalculationResult();
        }

        // Get all facility IDs in the graph
        Set<Integer> allFacilities = new HashSet<>(graph.keySet());
        for (List<Edge> edges : graph.values()) {
            for (Edge edge : edges) {
                allFacilities.add(edge.to);
            }
        }

        // Verify start and end are in graph
        if (!allFacilities.contains(startFacilityId)) {
            System.err.println("Start facility not connected to network: " + startFacilityId);
            return new PathCalculationResult();
        }

        if (!allFacilities.contains(endFacilityId)) {
            System.err.println("End facility not connected to network: " + endFacilityId);
            return new PathCalculationResult();
        }

        // Initialize distances - CRITICAL FIX
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> predecessors = new HashMap<>();

        // Initialize ALL facilities to infinity
        for (Integer facility : allFacilities) {
            distances.put(facility, Double.POSITIVE_INFINITY);
        }

        // Start facility has distance 0
        distances.put(startFacilityId, 0.0);

        // Bellman-Ford algorithm
        int n = allFacilities.size();

        // Relax edges (n-1) times
        for (int i = 0; i < n - 1; i++) {
            boolean changed = false;

            for (Map.Entry<Integer, List<Edge>> entry : graph.entrySet()) {
                int from = entry.getKey();

                // CRITICAL FIX: Check if distance exists and is not infinity
                Double fromDist = distances.get(from);
                if (fromDist == null || fromDist == Double.POSITIVE_INFINITY) {
                    continue;
                }

                for (Edge edge : entry.getValue()) {
                    // CRITICAL FIX: Ensure 'to' node has a distance
                    Double toDist = distances.get(edge.to);
                    if (toDist == null) {
                        distances.put(edge.to, Double.POSITIVE_INFINITY);
                        toDist = Double.POSITIVE_INFINITY;
                    }

                    double newDist = fromDist + edge.cost;

                    if (newDist < toDist) {
                        distances.put(edge.to, newDist);
                        predecessors.put(edge.to, from);
                        changed = true;
                    }
                }
            }

            if (!changed) {
                break; // Early termination if no changes
            }
        }

        // Check for negative cycles
        List<Integer> negativeCycleNodes = detectNegativeCycle(graph, distances);
        if (!negativeCycleNodes.isEmpty()) {
            return new PathCalculationResult(negativeCycleNodes);
        }

        // Check if destination is reachable
        Double endDist = distances.get(endFacilityId);
        if (endDist == null || endDist == Double.POSITIVE_INFINITY) {
            System.err.println("No path found from " + startFacilityId + " to " + endFacilityId);
            return new PathCalculationResult(); // No path found
        }

        // Reconstruct path
        List<Integer> path = reconstructPath(predecessors, startFacilityId, endFacilityId);

        if (path.isEmpty()) {
            System.err.println("Failed to reconstruct path");
            return new PathCalculationResult();
        }

        // Calculate total distance
        double totalDistance = calculateTotalDistance(path);

        return new PathCalculationResult(path, totalDistance, endDist);
    }

    /**
     * Build graph from railway network
     * IMPROVED: Better error handling and validation
     */
    private Map<Integer, List<Edge>> buildGraph() {
        Map<Integer, List<Edge>> graph = new HashMap<>();

        List<Line> allLines = lineRepository.findAll();

        if (allLines.isEmpty()) {
            System.err.println("No lines found in repository");
            return graph;
        }

        int edgesAdded = 0;

        for (Line line : allLines) {
            List<Segment> segments = segmentRepository.findByLineId(line.getId());

            if (segments.isEmpty()) {
                System.err.println("Warning: Line " + line.getId() + " has no segments");
                continue;
            }

            int start = line.getStartFacilityId();
            int end = line.getEndFacilityId();

            // Validate facilities exist
            if (facilityRepository.findById(start).isEmpty()) {
                System.err.println("Warning: Start facility " + start + " not found for line " + line.getId());
                continue;
            }

            if (facilityRepository.findById(end).isEmpty()) {
                System.err.println("Warning: End facility " + end + " not found for line " + line.getId());
                continue;
            }

            double totalDistance = segments.stream()
                    .mapToDouble(Segment::getLengthMeters)
                    .sum() / 1000.0; // Convert to km

            // Calculate cost (can include penalties/bonuses)
            double cost = calculateEdgeCost(segments, totalDistance);

            // Add bidirectional edges
            graph.computeIfAbsent(start, k -> new ArrayList<>())
                    .add(new Edge(end, cost, totalDistance));
            graph.computeIfAbsent(end, k -> new ArrayList<>())
                    .add(new Edge(start, cost, totalDistance));

            edgesAdded += 2;
        }

        System.out.println("[AutomaticPath] Graph built: " + graph.size() + " nodes, " + edgesAdded + " edges");

        return graph;
    }

    /**
     * Calculate cost for an edge based on distance and segment characteristics
     */
    private double calculateEdgeCost(List<Segment> segments, double distanceKm) {
        double baseCost = distanceKm;

        // Bonuses (negative costs)
        boolean allElectrified = segments.stream().allMatch(Segment::isElectrified);
        boolean allDoubleTrack = segments.stream().allMatch(s -> s.getNumberTracks() >= 2);

        if (allElectrified) {
            baseCost -= distanceKm * 0.1; // 10% bonus for electrified
        }

        if (allDoubleTrack) {
            baseCost -= distanceKm * 0.05; // 5% bonus for double-track
        }

        // Penalties (positive costs)
        boolean hasSingleTrack = segments.stream().anyMatch(s -> s.getNumberTracks() == 1);
        if (hasSingleTrack) {
            baseCost += distanceKm * 0.15; // 15% penalty for single-track
        }

        return baseCost;
    }

    /**
     * Detect negative cycle by running one more iteration
     */
    private List<Integer> detectNegativeCycle(Map<Integer, List<Edge>> graph,
                                              Map<Integer, Double> distances) {
        for (Map.Entry<Integer, List<Edge>> entry : graph.entrySet()) {
            int from = entry.getKey();

            Double fromDist = distances.get(from);
            if (fromDist == null || fromDist == Double.POSITIVE_INFINITY) {
                continue;
            }

            for (Edge edge : entry.getValue()) {
                Double toDist = distances.get(edge.to);
                if (toDist == null) {
                    continue;
                }

                double newDist = fromDist + edge.cost;
                if (newDist < toDist) {
                    // Negative cycle detected
                    return traceCycle(graph, from, edge.to);
                }
            }
        }

        return Collections.emptyList(); // No negative cycle
    }

    /**
     * Trace the negative cycle starting from a node
     */
    private List<Integer> traceCycle(Map<Integer, List<Edge>> graph, int start, int current) {
        List<Integer> cycle = new ArrayList<>();
        cycle.add(start);
        cycle.add(current);

        Set<Integer> visited = new HashSet<>();
        visited.add(start);
        visited.add(current);

        int node = current;
        int maxSteps = 100;

        while (maxSteps-- > 0) {
            List<Edge> edges = graph.get(node);
            if (edges == null || edges.isEmpty()) {
                break;
            }

            node = edges.get(0).to;
            if (visited.contains(node)) {
                cycle.add(node);
                break;
            }

            cycle.add(node);
            visited.add(node);
        }

        return cycle;
    }

    /**
     * Reconstruct path from predecessors map
     */
    private List<Integer> reconstructPath(Map<Integer, Integer> predecessors,
                                          int start, int end) {
        List<Integer> path = new ArrayList<>();
        Integer current = end;

        Set<Integer> visited = new HashSet<>();
        int maxSteps = 1000; // Prevent infinite loops

        while (current != null && !current.equals(start) && maxSteps-- > 0) {
            if (visited.contains(current)) {
                System.err.println("Cycle detected in path reconstruction");
                return Collections.emptyList();
            }

            path.add(current);
            visited.add(current);
            current = predecessors.get(current);
        }

        if (current == null) {
            System.err.println("Path reconstruction failed - current is null");
            return Collections.emptyList();
        }

        if (maxSteps <= 0) {
            System.err.println("Path reconstruction timeout");
            return Collections.emptyList();
        }

        path.add(start);
        Collections.reverse(path);

        return path;
    }

    /**
     * Calculate total distance of a path
     */
    private double calculateTotalDistance(List<Integer> path) {
        double total = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);

            Optional<Line> line = lineRepository.findLineConnecting(from, to);
            if (line.isPresent()) {
                List<Segment> segments = segmentRepository.findByLineId(line.get().getId());
                total += segments.stream()
                        .mapToDouble(Segment::getLengthMeters)
                        .sum() / 1000.0;
            }
        }

        return total;
    }

    /**
     * Get network summary for display
     */
    public NetworkSummary getNetworkSummary() {
        List<Facility> facilities = facilityRepository.findAll();
        List<Line> lines = lineRepository.findAll();
        List<Segment> segments = segmentRepository.findAll();

        double totalDistance = segments.stream()
                .mapToDouble(Segment::getLengthMeters)
                .sum() / 1000.0;

        long electrifiedSegments = segments.stream()
                .filter(Segment::isElectrified)
                .count();

        long doubleTrackSegments = segments.stream()
                .filter(s -> s.getNumberTracks() >= 2)
                .count();

        return new NetworkSummary(
                facilities.size(),
                lines.size(),
                segments.size(),
                totalDistance,
                (int) electrifiedSegments,
                (int) doubleTrackSegments
        );
    }

    // ============================================================================
    // INNER CLASSES
    // ============================================================================

    private static class Edge {
        final int to;
        final double cost;
        final double distanceKm;

        Edge(int to, double cost, double distanceKm) {
            this.to = to;
            this.cost = cost;
            this.distanceKm = distanceKm;
        }
    }

    public static class PathCalculationResult {
        private final boolean success;
        private final List<Integer> path;
        private final double totalDistanceKm;
        private final double totalCost;
        private final boolean hasNegativeCycle;
        private final List<Integer> negativeCycleStations;

        public PathCalculationResult(List<Integer> path, double totalDistanceKm, double totalCost) {
            this.success = true;
            this.path = path;
            this.totalDistanceKm = totalDistanceKm;
            this.totalCost = totalCost;
            this.hasNegativeCycle = false;
            this.negativeCycleStations = Collections.emptyList();
        }

        public PathCalculationResult(List<Integer> negativeCycleStations) {
            this.success = false;
            this.path = Collections.emptyList();
            this.totalDistanceKm = 0;
            this.totalCost = 0;
            this.hasNegativeCycle = true;
            this.negativeCycleStations = negativeCycleStations;
        }

        public PathCalculationResult() {
            this.success = false;
            this.path = Collections.emptyList();
            this.totalDistanceKm = 0;
            this.totalCost = 0;
            this.hasNegativeCycle = false;
            this.negativeCycleStations = Collections.emptyList();
        }

        public boolean isSuccess() { return success; }
        public List<Integer> getPath() { return new ArrayList<>(path); }
        public double getTotalDistanceKm() { return totalDistanceKm; }
        public double getTotalCost() { return totalCost; }
        public boolean hasNegativeCycle() { return hasNegativeCycle; }
        public List<Integer> getNegativeCycleStations() { return new ArrayList<>(negativeCycleStations); }

        public String getNegativeCycleStationsFormatted() {
            if (!hasNegativeCycle || negativeCycleStations.isEmpty()) {
                return "None";
            }
            return negativeCycleStations.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" → "));
        }
    }

    public static class NetworkSummary {
        public final int facilities;
        public final int lines;
        public final int segments;
        public final double totalDistanceKm;
        public final int electrifiedSegments;
        public final int doubleTrackSegments;

        public NetworkSummary(int facilities, int lines, int segments,
                              double totalDistanceKm, int electrifiedSegments,
                              int doubleTrackSegments) {
            this.facilities = facilities;
            this.lines = lines;
            this.segments = segments;
            this.totalDistanceKm = totalDistanceKm;
            this.electrifiedSegments = electrifiedSegments;
            this.doubleTrackSegments = doubleTrackSegments;
        }
    }
}