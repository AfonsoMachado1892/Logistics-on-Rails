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

   

}