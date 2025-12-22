package ESINF.controller;


import ESINF.model.MaxFlowResult;
import ESINF.model.RailwayNetwork;
import ESINF.model.RailwayStation;
import ESINF.model.Connection;
import ESINF.service.MaxFlowCalculator;

import java.io.*;
import java.util.*;

/**
 * CONTROLLER - Railway Network Operations Controller
 *
 * Orchestrates business operations related to the railway network.
 * Handles use cases like:
 * - Loading data from CSV
 * - Calculating max flow between hubs
 * - Validating inputs
 * - Coordinating between models and algorithms
 *
 * This is where BUSINESS LOGIC and USE CASES live.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class RailwayNetworkController {

    private RailwayNetwork network;

    /**
     * Creates a new controller with an empty network
     */
    public RailwayNetworkController() {
        this.network = new RailwayNetwork();
    }

    /**
     * Creates a controller with an existing network
     *
     * @param network the railway network
     */
    public RailwayNetworkController(RailwayNetwork network) {
        this.network = network;
    }

    // ==================== USE CASE: Load Network Data ====================

    /**
     * Loads network data from CSV files
     *
     * @param stationsFile path to stations CSV file
     * @param connectionsFile path to connections CSV file
     * @return true if loaded successfully
     * @throws IOException if file reading fails
     */
    public boolean loadNetworkFromCSV(String stationsFile, String connectionsFile)
            throws IOException {

        System.out.println("Loading railway network data...");

        // Load stations
        int stationsLoaded = loadStations(stationsFile);
        System.out.println("✓ Loaded " + stationsLoaded + " stations");

        // Load connections
        int connectionsLoaded = loadConnections(connectionsFile);
        System.out.println("✓ Loaded " + connectionsLoaded + " connections");

        // Validate network
        if (!network.isValid()) {
            System.err.println("✗ Network validation failed!");
            return false;
        }

        System.out.println("✓ Network validated successfully");
        return true;
    }

    /**
     * Loads stations from a CSV file
     * Format: stationId,name,latitude,longitude,coordX,coordY
     */
    private int loadStations(String filename) throws IOException {
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 6) {
                    System.err.println("Warning: Invalid station line: " + line);
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    double lat = Double.parseDouble(parts[2].trim());
                    double lon = Double.parseDouble(parts[3].trim());
                    double x = Double.parseDouble(parts[4].trim());
                    double y = Double.parseDouble(parts[5].trim());

                    RailwayStation station = new RailwayStation(id, name, lat, lon, x, y);

                    if (network.addStation(station)) {
                        count++;
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Warning: Error parsing station: " + line);
                }
            }
        }

        return count;
    }

    /**
     * Loads connections from a CSV file
     * Format: fromId,toId,distance,capacity,cost
     */
    private int loadConnections(String filename) throws IOException {
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.err.println("Warning: Invalid connection line: " + line);
                    continue;
                }

                try {
                    int fromId = Integer.parseInt(parts[0].trim());
                    int toId = Integer.parseInt(parts[1].trim());
                    double distance = Double.parseDouble(parts[2].trim());
                    double capacity = Double.parseDouble(parts[3].trim());
                    double cost = Double.parseDouble(parts[4].trim());

                    RailwayStation from = network.getStation(fromId);
                    RailwayStation to = network.getStation(toId);

                    if (from == null || to == null) {
                        System.err.println("Warning: Station not found for connection: " + line);
                        continue;
                    }

                    Connection conn =
                            new Connection(from, to, distance, capacity, cost);

                    if (network.addConnection(conn)) {
                        count++;
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Warning: Error parsing connection: " + line);
                }
            }
        }

        return count;
    }

    // ==================== USE CASE: USEI14 - Maximum Flow ====================

    /**
     * USEI14 - Calculates maximum throughput between two hubs
     *
     * @param sourceStationId ID of source station
     * @param sinkStationId ID of sink station
     * @return MaxFlowResult with detailed analysis
     * @throws IllegalArgumentException if stations not found or invalid
     */
    public MaxFlowResult calculateMaximumThroughput(int sourceStationId, int sinkStationId) {

        // Validation
        RailwayStation source = network.getStation(sourceStationId);
        RailwayStation sink = network.getStation(sinkStationId);

        if (source == null) {
            throw new IllegalArgumentException(
                    "Source station not found: " + sourceStationId);
        }

        if (sink == null) {
            throw new IllegalArgumentException(
                    "Sink station not found: " + sinkStationId);
        }

        if (sourceStationId == sinkStationId) {
            throw new IllegalArgumentException(
                    "Source and sink must be different stations");
        }

        // Build matrices for algorithm
        double[][] capacityMatrix = network.buildCapacityMatrix();
        RailwayStation[] stationArray = network.buildStationArray();
        Map<Integer, Integer> indexMap = network.getStationIndexMap();

        int sourceIdx = indexMap.get(sourceStationId);
        int sinkIdx = indexMap.get(sinkStationId);

        // Calculate max flow using algorithm
        return MaxFlowCalculator.calculateMaxFlow(
                capacityMatrix, stationArray, sourceIdx, sinkIdx);
    }

    /**
     * Calculates max flow and identifies bottlenecks
     *
     * @param sourceStationId source hub ID
     * @param sinkStationId sink hub ID
     * @return Map with result and bottlenecks
     */
    public Map<String, Object> analyzeMaximumThroughput(int sourceStationId, int sinkStationId) {

        MaxFlowResult result = calculateMaximumThroughput(sourceStationId, sinkStationId);

        // Find bottlenecks
        double[][] capacityMatrix = network.buildCapacityMatrix();
        RailwayStation[] stationArray = network.buildStationArray();

        List<String> bottlenecks = MaxFlowCalculator.findBottlenecks(
                result, capacityMatrix, stationArray);

        // Package results
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("result", result);
        analysis.put("bottlenecks", bottlenecks);
        analysis.put("summary", result.getSummary());
        analysis.put("hasBottlenecks", !bottlenecks.isEmpty());

        return analysis;
    }

    // ==================== USE CASE: Network Queries ====================

    /**
     * Gets all stations in the network
     *
     * @return list of all stations
     */
    public List<RailwayStation> getAllStations() {
        return new ArrayList<>(network.getAllStations());
    }

    /**
     * Finds a station by name (case-insensitive partial match)
     *
     * @param namePart part of the station name
     * @return list of matching stations
     */
    public List<RailwayStation> findStationsByName(String namePart) {
        if (namePart == null || namePart.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchTerm = namePart.toLowerCase();
        List<RailwayStation> matches = new ArrayList<>();

        for (RailwayStation station : network.getAllStations()) {
            if (station.getName().toLowerCase().contains(searchTerm)) {
                matches.add(station);
            }
        }

        return matches;
    }

    /**
     * Gets all connections from a station
     *
     * @param stationId station ID
     * @return list of connections
     */
    public List<Connection> getConnectionsFrom(int stationId) {
        return network.getConnections(stationId);
    }

    /**
     * Gets network statistics
     *
     * @return map with various statistics
     */
    public Map<String, Object> getNetworkStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalStations", network.getStationCount());
        stats.put("totalConnections", network.getConnectionCount());

        // Calculate total capacity
        double totalCapacity = network.getAllConnections().stream()
                .mapToDouble(Connection::getCapacity)
                .sum();
        stats.put("totalCapacity", totalCapacity);

        // Calculate average capacity
        double avgCapacity = network.getConnectionCount() > 0
                ? totalCapacity / network.getConnectionCount()
                : 0;
        stats.put("averageCapacity", avgCapacity);

        // Find max capacity connection
        Optional<Connection> maxConn = network.getAllConnections().stream()
                .max(Comparator.comparingDouble(Connection::getCapacity));
        maxConn.ifPresent(conn -> stats.put("maxCapacityConnection", conn.toString()));

        return stats;
    }

    // ==================== Utilities ====================

    /**
     * Gets the current network
     *
     * @return the railway network
     */
    public RailwayNetwork getNetwork() {
        return network;
    }

    /**
     * Validates if a path exists between two stations
     *
     * @param fromId source station ID
     * @param toId destination station ID
     * @return true if a path exists
     */
    public boolean hasPath(int fromId, int toId) {
        // Simple BFS to check connectivity
        if (!network.getStation(fromId).equals(network.getStation(fromId)) ||
                network.getStation(toId) == null) {
            return false;
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(fromId);
        visited.add(fromId);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == toId) {
                return true;
            }

            for (Connection conn : network.getConnections(current)) {
                int next = conn.getTo().getStationId();
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.offer(next);
                }
            }
        }

        return false;
    }

    /**
     * Exports network summary to file
     *
     * @param filename output file path
     * @throws IOException if writing fails
     */
    public void exportNetworkSummary(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=== Railway Network Summary ===");
            writer.println();

            Map<String, Object> stats = getNetworkStatistics();
            stats.forEach((key, value) ->
                    writer.println(key + ": " + value));

            writer.println();
            writer.println("=== Stations ===");
            for (RailwayStation station : network.getAllStations()) {
                writer.println(station);
            }

            writer.println();
            writer.println("=== Connections ===");
            for (Connection conn : network.getAllConnections()) {
                writer.println(conn);
            }
        }
    }
}