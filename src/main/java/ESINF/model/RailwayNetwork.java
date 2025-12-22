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

  


}