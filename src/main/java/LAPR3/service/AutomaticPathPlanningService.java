package LAPR3.service;

import ESINF.model.Connection;
import ESINF.model.RailwayStation;
import ESINF.model.RiskAwarePathResult;
import ESINF.service.RailwayNetworkOptimizationService;
import ESINF.repository.RailwayNetworkRepository;
import LAPR3.model.Facility;
import LAPR3.model.Line;
import LAPR3.model.Segment;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.LineRepository;
import LAPR3.repository.SegmentRepository;
import Graph.Graph;
import Graph.map.MapGraph;

import java.util.*;

/**
 * Service for Automatic Path Planning (USLP10)
 * Bridges LAPR3 domain (Facilities, Lines, Segments) with ESINF graph algorithms
 *
 * This service:
 * 1. Converts LAPR3 railway network → ESINF graph structure
 * 2. Uses Bellman-Ford algorithm for shortest path (supports negative costs)
 * 3. Returns path as list of Facility IDs
 * 4. Considers sidings in cost calculation (USLP10 requirement)
 *
 * SIDINGS HANDLING (USLP07/USLP10):
 * - Single-track segments WITH sidings: Allow train crossing → Higher cost (+30%)
 * - Single-track segments WITHOUT sidings: Must cross at stations → Normal cost
 * - Double-track segments: Free crossing → Lower cost (-15%)
 *
 * The cost calculation penalizes single-track lines with sidings because they
 * require complex crossing coordination, which is handled by CrossingResolutionService
 * in the dispatch phase.
 *
 * Part of USLP10 - Automatic Path Calculation Mode
 */
public class AutomaticPathPlanningService {

    private final FacilityRepository facilityRepository;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;

    // ESINF components
    private RailwayNetworkRepository railwayRepo;
    private RailwayNetworkOptimizationService optimizationService;

    // Mapping between LAPR3 and ESINF domains
    private Map<Integer, RailwayStation> facilityToStation;
    private Map<RailwayStation, Integer> stationToFacility;

    public AutomaticPathPlanningService(FacilityRepository facilityRepository,
                                        LineRepository lineRepository,
                                        SegmentRepository segmentRepository) {
        this.facilityRepository = facilityRepository;
        this.lineRepository = lineRepository;
        this.segmentRepository = segmentRepository;
        this.facilityToStation = new HashMap<>();
        this.stationToFacility = new HashMap<>();
    }

    /**
     * Calculates the optimal path between two facilities using cost-based algorithm
     *
     * @param startFacilityId origin facility ID
     * @param endFacilityId destination facility ID
     * @return PathCalculationResult with ordered list of facility IDs or error
     */
    public PathCalculationResult calculateOptimalPath(int startFacilityId, int endFacilityId) {
        try {
            // Step 1: Build ESINF graph from LAPR3 data
            buildRailwayNetwork();

            // Step 2: Get corresponding railway stations
            RailwayStation startStation = facilityToStation.get(startFacilityId);
            RailwayStation endStation = facilityToStation.get(endFacilityId);

            if (startStation == null || endStation == null) {
                return PathCalculationResult.error("Invalid facility IDs: start or end not found");
            }

            // Step 3: Calculate shortest path using Bellman-Ford
            RiskAwarePathResult result = optimizationService.getRiskAwareShortestPath(
                    startStation.getStationId(),
                    endStation.getStationId()
            );

            // Step 4: Check for negative cycles
            if (result.hasNegativeCycle()) {
                List<String> cycleNames = new ArrayList<>();
                for (RailwayStation station : result.getCycleStations()) {
                    Integer facId = stationToFacility.get(station);
                    if (facId != null) {
                        Facility fac = facilityRepository.findById(facId).orElse(null);
                        cycleNames.add(fac != null ? fac.getName() : "Unknown");
                    }
                }
                return PathCalculationResult.negativeCycle(cycleNames);
            }

            // Step 5: Convert path back to facility IDs
            List<Integer> facilityPath = new ArrayList<>();
            List<Double> costs = new ArrayList<>();

            if (result.getPath() != null) {
                for (int i = 0; i < result.getPath().size(); i++) {
                    RailwayStation station = result.getPath().get(i);
                    Integer facId = stationToFacility.get(station);
                    if (facId != null) {
                        facilityPath.add(facId);
                        if (i < result.getPathCosts().size()) {
                            costs.add(result.getPathCosts().get(i));
                        }
                    }
                }
            }

            if (facilityPath.isEmpty()) {
                return PathCalculationResult.noPath("No path found between facilities");
            }

            return PathCalculationResult.success(facilityPath, costs, result.getTotalCost());

        } catch (Exception e) {
            return PathCalculationResult.error("Error calculating path: " + e.getMessage());
        }
    }

    /**
     * Builds the ESINF railway network graph from LAPR3 data
     * Converts Facilities → RailwayStations
     * Converts Lines/Segments → Connections with cost calculation
     */
    private void buildRailwayNetwork() {
        // Clear mappings
        facilityToStation.clear();
        stationToFacility.clear();

        // Create cost graph (directed)
        Graph<RailwayStation, Double> costGraph = new MapGraph<>(true);

        // Step 1: Convert all facilities to railway stations
        List<Facility> facilities = facilityRepository.findAll();
        for (Facility facility : facilities) {
            // Create RailwayStation (using facility ID as station ID)
            RailwayStation station = new RailwayStation(
                    facility.getId(),
                    facility.getName(),
                    0.0, // latitude (not used in path calculation)
                    0.0, // longitude (not used in path calculation)
                    0.0, // coordX
                    0.0  // coordY
            );

            facilityToStation.put(facility.getId(), station);
            stationToFacility.put(station, facility.getId());
            costGraph.addVertex(station);
        }

        // Step 2: Convert all lines to connections with calculated costs
        List<Line> lines = lineRepository.findAll();
        for (Line line : lines) {
            // Get segments for this line
            List<Segment> segments = segmentRepository.findByLineId(line.getId());

            if (segments.isEmpty()) {
                continue;
            }

            // Calculate total distance (in km)
            double totalDistance = segments.stream()
                    .mapToDouble(Segment::getLengthMeters)
                    .sum() / 1000.0;

            // Calculate cost based on multiple factors
            double cost = calculateLineCost(segments, totalDistance);

            // Add bidirectional edges (lines work in both directions)
            RailwayStation startStation = facilityToStation.get(line.getStartFacilityId());
            RailwayStation endStation = facilityToStation.get(line.getEndFacilityId());

            if (startStation != null && endStation != null) {
                // Forward direction
                costGraph.addEdge(startStation, endStation, cost);
                // Reverse direction (same cost)
                costGraph.addEdge(endStation, startStation, cost);
            }
        }

        // Step 3: Create custom repository with our built graph
        railwayRepo = new CustomRailwayNetworkRepository(costGraph, facilityToStation);

        // Step 4: Create optimization service
        optimizationService = new RailwayNetworkOptimizationService(railwayRepo);
    }

    /**
     * Calculates cost for a line based on its segments characteristics
     *
     * Cost factors:
     * - Base: Distance (km)
     * - Electrification bonus: -10% if fully electrified
     * - Double track bonus: -15% if all segments are double track
     * - Single track penalty: +30% if any segment is single track with sidings (crossing complexity)
     * - Weight limit penalty: +20% if average weight limit < 7000 kg/m
     *
     * @param segments list of segments in the line
     * @param distanceKm total distance in kilometers
     * @return calculated cost
     */
    private double calculateLineCost(List<Segment> segments, double distanceKm) {
        double cost = distanceKm;

        // Factor 1: Electrification bonus (-10%)
        boolean allElectrified = segments.stream().allMatch(Segment::isElectrified);
        if (allElectrified) {
            cost *= 0.9;
        }

        // Factor 2: Track type
        boolean allDoubleTrack = segments.stream().allMatch(s -> s.getNumberTracks() >= 2);
        boolean hasSingleTrackWithSiding = segments.stream()
                .anyMatch(s -> s.getNumberTracks() == 1 && s.getSidingPosition() != null);

        if (allDoubleTrack) {
            // Double track bonus: -15%
            cost *= 0.85;
        } else if (hasSingleTrackWithSiding) {
            // Single track with siding penalty: +30% (crossing complexity)
            cost *= 1.3;
        }

        // Factor 3: Weight limit penalty
        double avgWeightLimit = segments.stream()
                .mapToDouble(Segment::getMaxWeightKgPerMeter)
                .average()
                .orElse(8000.0);

        if (avgWeightLimit < 7000) {
            cost *= 1.2; // +20% penalty for low weight capacity
        }

        return cost;
    }

    /**
     * Gets a summary of the network structure
     */
    public NetworkSummary getNetworkSummary() {
        List<Facility> facilities = facilityRepository.findAll();
        List<Line> lines = lineRepository.findAll();

        int totalSegments = 0;
        double totalDistance = 0.0;
        int electrifiedSegments = 0;
        int doubleTrackSegments = 0;

        for (Line line : lines) {
            List<Segment> segments = segmentRepository.findByLineId(line.getId());
            totalSegments += segments.size();

            for (Segment seg : segments) {
                totalDistance += seg.getLengthMeters() / 1000.0;
                if (seg.isElectrified()) electrifiedSegments++;
                if (seg.getNumberTracks() >= 2) doubleTrackSegments++;
            }
        }

        return new NetworkSummary(
                facilities.size(),
                lines.size(),
                totalSegments,
                totalDistance,
                electrifiedSegments,
                doubleTrackSegments
        );
    }

    /**
     * Result of path calculation
     */
    public static class PathCalculationResult {
        private final boolean success;
        private final List<Integer> facilityIds;
        private final List<Double> cumulativeCosts;
        private final double totalCost;
        private final String errorMessage;
        private final boolean hasNegativeCycle;
        private final List<String> cycleInfo;

        private PathCalculationResult(boolean success, List<Integer> facilityIds,
                                      List<Double> cumulativeCosts, double totalCost,
                                      String errorMessage, boolean hasNegativeCycle,
                                      List<String> cycleInfo) {
            this.success = success;
            this.facilityIds = facilityIds != null ? facilityIds : Collections.emptyList();
            this.cumulativeCosts = cumulativeCosts != null ? cumulativeCosts : Collections.emptyList();
            this.totalCost = totalCost;
            this.errorMessage = errorMessage;
            this.hasNegativeCycle = hasNegativeCycle;
            this.cycleInfo = cycleInfo;
        }

        public static PathCalculationResult success(List<Integer> path, List<Double> costs, double totalCost) {
            return new PathCalculationResult(true, path, costs, totalCost, null, false, null);
        }

        public static PathCalculationResult error(String message) {
            return new PathCalculationResult(false, null, null, 0.0, message, false, null);
        }

        public static PathCalculationResult noPath(String message) {
            return new PathCalculationResult(false, Collections.emptyList(), null, Double.MAX_VALUE, message, false, null);
        }

        public static PathCalculationResult negativeCycle(List<String> cycleInfo) {
            return new PathCalculationResult(false, null, null, 0.0,
                    "Negative cost cycle detected", true, cycleInfo);
        }

        public boolean isSuccess() { return success; }
        public List<Integer> getFacilityIds() { return facilityIds; }
        public List<Double> getCumulativeCosts() { return cumulativeCosts; }
        public double getTotalCost() { return totalCost; }
        public String getErrorMessage() { return errorMessage; }
        public boolean hasNegativeCycle() { return hasNegativeCycle; }
        public List<String> getCycleInfo() { return cycleInfo; }
    }

    /**
     * Summary of network structure
     */
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

    /**
     * Custom repository that wraps our built graph
     * This is needed because RailwayNetworkOptimizationService expects a repository
     */
    private static class CustomRailwayNetworkRepository extends RailwayNetworkRepository {
        private final Graph<RailwayStation, Double> costGraph;
        private final Map<Integer, RailwayStation> stationMap;

        public CustomRailwayNetworkRepository(Graph<RailwayStation, Double> costGraph,
                                              Map<Integer, RailwayStation> stationMap) {
            super();
            this.costGraph = costGraph;
            this.stationMap = stationMap;
        }

        @Override
        public boolean isDataLoaded() {
            return true;
        }

        @Override
        public RailwayStation getStationById(int stationId) {
            return stationMap.get(stationId);
        }

        @Override
        public Graph<RailwayStation, Double> getCostGraph() {
            return costGraph;
        }
    }
}