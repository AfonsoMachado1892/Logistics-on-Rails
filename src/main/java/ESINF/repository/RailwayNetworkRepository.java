package ESINF.repository;

import ESINF.model.RailwayStation;
import Graph.Edge;
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
    private Graph<RailwayStation, Double> costGraph;
    private boolean dataLoaded;

    // Contadores mantidos como campos para uso em toString() e estado da instância
    private int stationsLoaded;
    private int connectionsLoaded;

    public RailwayNetworkRepository() {
        this.graph = new MapGraph<>(true); // Directed graph
        this.costGraph = new MapGraph<>(true);
        this.stationById = new HashMap<>();
        this.stationByName = new HashMap<>();
        this.dataLoaded = false;
        this.stationsLoaded = 0;
        this.connectionsLoaded = 0;
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
        this.costGraph = new MapGraph<>(true); // [NEW]

        // Step 1: Load stations
        this.stationsLoaded = loadStations(stationsPath);

        // Step 2: Load connections
        this.connectionsLoaded = loadConnections(linesPath);

        dataLoaded = stationsLoaded > 0 && connectionsLoaded > 0;

        return new LoadResult(this.stationsLoaded, this.connectionsLoaded);
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
                    costGraph.addVertex(station);

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
                    //int capacity = Integer.parseInt(parts[3].trim()); // Not used for USEI11

                    // Get stations
                    RailwayStation fromStation = stationById.get(departureId);
                    RailwayStation toStation = stationById.get(arrivalId);

                    if (fromStation != null && toStation != null) {
                        // Add directed edge with distance as weight
                        graph.addEdge(fromStation, toStation, distance);
                        if (parts.length >= 5) {
                            try {
                                double cost = Double.parseDouble(parts[4].trim());
                                costGraph.addEdge(fromStation, toStation, cost);
                            } catch (NumberFormatException e) {
                                System.err.println("Warning: Invalid cost for edge " + departureId + "->" + arrivalId);
                            }
                        }

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
    public Graph<RailwayStation, Double> getCostGraph() {
        return costGraph;
    }

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
     * Gets the railway network graph modeled as an UNDIRECTED graph,
     * using the distance (Double) as the edge weight.
     * This is required for Centrality Analysis (USEI13).
     *
     * @return An UNDIRECTED Graph for SSSP (Shortest Path) based calculations.
     */
    public Graph<RailwayStation, Double> getRailNetworkWithDistanceAsWeight() {
        if (!dataLoaded) {
            return new MapGraph<>(false); // Return empty undirected graph if no data is loaded
        }

        // Cria um novo grafo não direcionado
        MapGraph<RailwayStation, Double> undirectedGraph = new MapGraph<>(false);

        // 1. Adiciona todos os vértices
        for (RailwayStation station : this.graph.vertices()) {
            undirectedGraph.addVertex(station);
        }

        // 2. Adiciona as arestas
        // Usamos iteração com cast seguro para evitar problemas genéricos do compilador.
        for (Object obj : this.graph.edges()) {

            @SuppressWarnings("unchecked")
            Edge<RailwayStation, Double> edge = (Edge<RailwayStation, Double>) obj;

            RailwayStation vOrig = edge.getVOrig();
            RailwayStation vDest = edge.getVDest();
            Double weight = edge.getWeight();

            // Adicionamos apenas o sentido vOrig -> vDest no grafo não direcionado.
            if (undirectedGraph.edge(vOrig, vDest) == null) {
                undirectedGraph.addEdge(vOrig, vDest, weight);
            }
        }

        return undirectedGraph;
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
            return String.format("Loaded %d stations and %d connections",
                    stationsLoaded, connectionsLoaded);
        }

    }

    @Override
    public String toString() {
        return String.format("Loaded %d stations and %d connections",
                stationsLoaded, connectionsLoaded);
    }
}
