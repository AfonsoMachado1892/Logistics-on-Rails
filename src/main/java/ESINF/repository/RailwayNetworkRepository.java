package ESINF.repository;

import ESINF.model.RailwayStation;
import Graph.Graph;
import Graph.map.MapGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Repository for managing Belgian Railway Network data
 * Loads stations and connections from separate CSV files
 */
public class RailwayNetworkRepository {

    private Graph<RailwayStation, Double> graph;
    private Map<Integer, RailwayStation> stationById;
    private Map<String, RailwayStation> stationByName;
    private boolean dataLoaded;

    public RailwayNetworkRepository() {
        this.graph = new MapGraph<>(true); // Directed graph
        this.stationById = new HashMap<>();
        this.stationByName = new HashMap<>();
        this.dataLoaded = false;
    }

    /**
     * Loads complete railway network from two CSV files
     *
     * @param stationsPath path to stations.csv (Station id, Station, Lat, Lon, CoordX, CoordY)
     * @param linesPath path to lines.csv (departure_stid, arrival_stid, dist, capacity, cost)
     * @return LoadResult with statistics
     */
    public LoadResult loadFromCSV(String stationsPath, String linesPath) throws IOException {
        graph = new MapGraph<>(true); // Reset graph
        stationById.clear();
        stationByName.clear();

        // Step 1: Load stations
        int stationsLoaded = loadStations(stationsPath);

        // Step 2: Load connections
        int connectionsLoaded = loadConnections(linesPath);

        dataLoaded = stationsLoaded > 0 && connectionsLoaded > 0;

        return new LoadResult(stationsLoaded, connectionsLoaded);
    }

    /**
     * Loads stations from stations.csv
     * Format: Station id,Station,Lat,Lon,CoordX,CoordY
     */
    private int loadStations(String filePath) throws IOException {
        int loaded = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                try {
                    String[] parts = line.split(",");

                    if (parts.length < 6) {
                        continue; // Skip invalid lines
                    }

                    int stationId = Integer.parseInt(parts[0].trim());
                    String stationName = parts[1].trim();
                    double lat = parseDouble(parts[2].trim());
                    double lon = parseDouble(parts[3].trim());
                    double coordX = parseDouble(parts[4].trim());
                    double coordY = parseDouble(parts[5].trim());

                    // Create station
                    RailwayStation station = new RailwayStation(
                            stationId, stationName, lat, lon, coordX, coordY
                    );

                    // Add to maps and graph
                    stationById.put(stationId, station);
                    stationByName.put(stationName, station);
                    graph.addVertex(station);

                    loaded++;

                } catch (Exception e) {
                    // Skip lines with parsing errors
                    System.err.println("Warning: Could not parse station line: " + line);
                }
            }
        }

        return loaded;
    }

    /**
     * Loads connections from lines.csv
     * Format: departure_stid,arrival_stid,dist,capacity,cost
     */
    private int loadConnections(String filePath) throws IOException {
        int loaded = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                try {
                    String[] parts = line.split(",");

                    if (parts.length < 5) {
                        continue; // Skip invalid lines
                    }

                    int departureId = Integer.parseInt(parts[0].trim());
                    int arrivalId = Integer.parseInt(parts[1].trim());
                    double distance = parseDouble(parts[2].trim());
                    // int capacity = Integer.parseInt(parts[3].trim()); // Not used for USEI11
                    // double cost = parseDouble(parts[4].trim()); // Not used for USEI11

                    // Get stations
                    RailwayStation fromStation = stationById.get(departureId);
                    RailwayStation toStation = stationById.get(arrivalId);

                    if (fromStation != null && toStation != null) {
                        // Add directed edge with distance as weight
                        graph.addEdge(fromStation, toStation, distance);
                        loaded++;
                    } else {
                        System.err.println("Warning: Station not found for connection: "
                                + departureId + " -> " + arrivalId);
                    }

                } catch (Exception e) {
                    // Skip lines with parsing errors
                    System.err.println("Warning: Could not parse connection line: " + line);
                }
            }
        }

        return loaded;
    }

    /**
     * Parses double value, handling both comma and dot as decimal separator
     */
    private double parseDouble(String value) {
        return Double.parseDouble(value.replace(",", "."));
    }

    /**
     * Gets the railway network graph
     */
    public Graph<RailwayStation, Double> getGraph() {
        return graph;
    }

    /**
     * Checks if data is loaded
     */
    public boolean isDataLoaded() {
        return dataLoaded && graph.numVertices() > 0;
    }

    /**
     * Gets number of stations (vertices)
     */
    public int getVertexCount() {
        return graph.numVertices();
    }

    /**
     * Gets number of connections (edges)
     */
    public int getEdgeCount() {
        return graph.numEdges();
    }

    /**
     * Gets a station by ID
     */
    public RailwayStation getStationById(int stationId) {
        return stationById.get(stationId);
    }

    /**
     * Gets a station by name
     */
    public RailwayStation getStationByName(String name) {
        return stationByName.get(name);
    }

    /**
     * Gets all station names
     */
    public Set<String> getAllStationNames() {
        return new HashSet<>(stationByName.keySet());
    }

    /**
     * Gets all station IDs
     */
    public Set<Integer> getAllStationIds() {
        return new HashSet<>(stationById.keySet());
    }

    /**
     * Result class for load operation
     */
    public static class LoadResult {
        public final int stationsLoaded;
        public final int connectionsLoaded;

        public LoadResult(int stationsLoaded, int connectionsLoaded) {
            this.stationsLoaded = stationsLoaded;
            this.connectionsLoaded = connectionsLoaded;
        }

        @Override
        public String toString() {
            return String.format("✓ Loaded %d stations and %d connections",
                    stationsLoaded, connectionsLoaded);
        }
    }
}