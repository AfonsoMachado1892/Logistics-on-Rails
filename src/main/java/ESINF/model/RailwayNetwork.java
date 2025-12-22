package ESINF.model;

import java.util.*;

/**
 * MODEL - Railway Network Graph
 *
 * Represents the railway network as a weighted directed graph.
 * Stores stations (vertices) and connections (edges) with their capacities.
 *
 *
 * @author Diogo Oliveira
 * @version 1.0
 */
public class RailwayNetwork{

    /**
     * Represents a connection between two stations
     */
    // Graph data structures
    private final Map<Integer, RailwayStation> stations;              // stationId -> Station
    private final Map<Integer, List<Connection>> adjacencyList;       // stationId -> connections
    private final Map<String, Connection> connectionMap;              // "fromId-toId" -> Connection

    /**
     * Creates an empty railway network
     */
    public RailwayNetwork() {
        this.stations = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.connectionMap = new HashMap<>();
    }

    /**
     * Adds a station to the network
     *
     * @param station the station to add
     * @return true if added, false if already exists
     */
    public boolean addStation(RailwayStation station) {
        if (station == null || stations.containsKey(station.getStationId())) {
            return false;
        }

        stations.put(station.getStationId(), station);
        adjacencyList.putIfAbsent(station.getStationId(), new ArrayList<>());
        return true;
    }

    /**
     * Adds a connection between two stations
     *
     * @param connection the connection to add
     * @return true if added successfully
     * @throws IllegalArgumentException if stations don't exist
     */
    public boolean addConnection(Connection connection) {
        if (connection == null) {
            return false;
        }

        int fromId = connection.getFrom().getStationId();
        int toId = connection.getTo().getStationId();

        if (!stations.containsKey(fromId) || !stations.containsKey(toId)) {
            throw new IllegalArgumentException("Both stations must exist in the network");
        }

        String key = fromId + "-" + toId;
        if (connectionMap.containsKey(key)) {
            return false; // Already exists
        }

        adjacencyList.get(fromId).add(connection);
        connectionMap.put(key, connection);
        return true;
    }

    /**
     * Gets a station by its ID
     *
     * @param stationId the station ID
     * @return the station, or null if not found
     */
    public RailwayStation getStation(int stationId) {
        return stations.get(stationId);
    }

    /**
     * Gets all outgoing connections from a station
     *
     * @param stationId the station ID
     * @return list of connections, empty if station not found
     */
    public List<Connection> getConnections(int stationId) {
        return adjacencyList.getOrDefault(stationId, Collections.emptyList());
    }

    /**
     * Gets a specific connection between two stations
     *
     * @param fromId source station ID
     * @param toId destination station ID
     * @return the connection, or null if not found
     */
    public Connection getConnection(int fromId, int toId) {
        return connectionMap.get(fromId + "-" + toId);
    }

    /**
     * Gets all stations in the network
     *
     * @return collection of all stations
     */
    public Collection<RailwayStation> getAllStations() {
        return stations.values();
    }

    /**
     * Gets all connections in the network
     *
     * @return collection of all connections
     */
    public Collection<Connection> getAllConnections() {
        return connectionMap.values();
    }

    /**
     * Gets the number of stations in the network
     *
     * @return number of stations
     */
    public int getStationCount() {
        return stations.size();
    }

    /**
     * Gets the number of connections in the network
     *
     * @return number of connections
     */
    public int getConnectionCount() {
        return connectionMap.size();
    }

    /**
     * Builds a capacity matrix for max flow algorithms
     *
     * @return 2D array where [i][j] is capacity from station i to j
     */
    public double[][] buildCapacityMatrix() {
        int n = stations.size();
        double[][] matrix = new double[n][n];

        // Create index mapping
        Map<Integer, Integer> stationIdToIndex = new HashMap<>();
        int index = 0;
        for (Integer stationId : stations.keySet()) {
            stationIdToIndex.put(stationId, index++);
        }

        // Fill matrix
        for (Connection conn : connectionMap.values()) {
            int fromIdx = stationIdToIndex.get(conn.getFrom().getStationId());
            int toIdx = stationIdToIndex.get(conn.getTo().getStationId());
            matrix[fromIdx][toIdx] = conn.getCapacity();
        }

        return matrix;
    }

    /**
     * Builds a cost matrix for shortest path algorithms
     *
     * @return 2D array where [i][j] is cost from station i to j
     */
    public double[][] buildCostMatrix() {
        int n = stations.size();
        double[][] matrix = new double[n][n];

        // Initialize with infinity
        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], Double.POSITIVE_INFINITY);
            matrix[i][i] = 0; // Distance to self is 0
        }

        // Create index mapping
        Map<Integer, Integer> stationIdToIndex = new HashMap<>();
        int index = 0;
        for (Integer stationId : stations.keySet()) {
            stationIdToIndex.put(stationId, index++);
        }

        // Fill matrix
        for (Connection conn : connectionMap.values()) {
            int fromIdx = stationIdToIndex.get(conn.getFrom().getStationId());
            int toIdx = stationIdToIndex.get(conn.getTo().getStationId());
            matrix[fromIdx][toIdx] = conn.getCost();
        }

        return matrix;
    }

    
}