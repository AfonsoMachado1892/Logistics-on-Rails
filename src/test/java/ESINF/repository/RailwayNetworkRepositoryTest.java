package ESINF.repository;

import ESINF.model.RailwayStation;
import Graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RailwayNetworkRepositoryTest {

    private RailwayNetworkRepository repository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repository = new RailwayNetworkRepository();
    }

    @Test
    void testLoadFromCSVValid() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();

        RailwayNetworkRepository.LoadResult result = repository.loadFromCSV(
                stationsFile.toString(), linesFile.toString());

        assertEquals(3, result.stationsLoaded, "Should load 3 stations");
        assertEquals(2, result.connectionsLoaded, "Should load 2 connections");
        assertTrue(repository.isDataLoaded(), "Data should be loaded");
    }

    @Test
    void testLoadFromCSVInvalidStation() throws IOException {
        Path stationsFile = createInvalidStationsFile();
        Path linesFile = createLinesFile();

        RailwayNetworkRepository.LoadResult result = repository.loadFromCSV(
                stationsFile.toString(), linesFile.toString());

        assertEquals(2, result.stationsLoaded, "Should load only valid stations");
    }

    @Test
    void testLoadFromCSVNonExistentFile() {
        assertThrows(IOException.class,
                () -> repository.loadFromCSV("nonexistent.csv", "nonexistent.csv"),
                "Should throw exception for non-existent file");
    }

    @Test
    void testGetGraph() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        Graph<RailwayStation, Double> graph = repository.getGraph();

        assertNotNull(graph, "Graph should not be null");
        assertTrue(graph.isDirected(), "Graph should be directed");
        assertEquals(3, graph.numVertices(), "Should have 3 vertices");
        assertEquals(2, graph.numEdges(), "Should have 2 edges");
    }

    @Test
    void testIsDataLoadedInitially() {
        assertFalse(repository.isDataLoaded(), "Should be false initially");
    }

    @Test
    void testIsDataLoadedAfterLoad() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        assertTrue(repository.isDataLoaded(), "Should be true after loading");
    }

    @Test
    void testGetVertexCount() throws IOException {
        assertEquals(0, repository.getVertexCount(), "Should be 0 initially");

        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        assertEquals(3, repository.getVertexCount(), "Should be 3 after loading");
    }

    @Test
    void testGetEdgeCount() throws IOException {
        assertEquals(0, repository.getEdgeCount(), "Should be 0 initially");

        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        assertEquals(2, repository.getEdgeCount(), "Should be 2 after loading");
    }

    @Test
    void testGetStationById() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        RailwayStation station = repository.getStationById(1);
        assertNotNull(station, "Should find station with ID 1");
        assertEquals("Porto", station.getName(), "Station name should be Porto");

        assertNull(repository.getStationById(999), "Should return null for non-existent ID");
    }

    @Test
    void testGetStationByName() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        RailwayStation station = repository.getStationByName("Porto");
        assertNotNull(station, "Should find station by name");
        assertEquals(1, station.getStationId(), "Station ID should be 1");

        assertNull(repository.getStationByName("NonExistent"), "Should return null for non-existent name");
    }

    @Test
    void testGetAllStationNames() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        Set<String> names = repository.getAllStationNames();
        assertEquals(3, names.size(), "Should have 3 station names");
        assertTrue(names.contains("Porto"), "Should contain Porto");
        assertTrue(names.contains("Braga"), "Should contain Braga");
        assertTrue(names.contains("Lisboa"), "Should contain Lisboa");
    }

    @Test
    void testGetAllStationIds() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        Set<Integer> ids = repository.getAllStationIds();
        assertEquals(3, ids.size(), "Should have 3 station IDs");
        assertTrue(ids.contains(1), "Should contain ID 1");
        assertTrue(ids.contains(2), "Should contain ID 2");
        assertTrue(ids.contains(3), "Should contain ID 3");
    }

    @Test
    void testGetRailNetworkWithDistanceAsWeight() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        repository.loadFromCSV(stationsFile.toString(), linesFile.toString());

        Graph<RailwayStation, Double> undirectedGraph = repository.getRailNetworkWithDistanceAsWeight();

        assertNotNull(undirectedGraph, "Undirected graph should not be null");
        assertFalse(undirectedGraph.isDirected(), "Graph should be undirected");
        assertEquals(3, undirectedGraph.numVertices(), "Should have 3 vertices");
    }

    @Test
    void testGetRailNetworkWithDistanceAsWeightNoData() {
        Graph<RailwayStation, Double> undirectedGraph = repository.getRailNetworkWithDistanceAsWeight();

        assertNotNull(undirectedGraph, "Should return empty graph");
        assertEquals(0, undirectedGraph.numVertices(), "Should have 0 vertices");
    }

    @Test
    void testLoadFromCSVWithCommaDecimal() throws IOException {
        Path stationsFile = createStationsFileCommaDecimal();
        Path linesFile = createLinesFileCommaDecimal();

        RailwayNetworkRepository.LoadResult result = repository.loadFromCSV(
                stationsFile.toString(), linesFile.toString());

        assertTrue(result.stationsLoaded > 0, "Should parse comma decimals");
        assertTrue(result.connectionsLoaded > 0, "Should parse comma decimals in connections");
    }

    @Test
    void testLoadFromCSVMissingConnection() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFileWithMissingStation();

        RailwayNetworkRepository.LoadResult result = repository.loadFromCSV(
                stationsFile.toString(), linesFile.toString());

        assertEquals(1, result.connectionsLoaded, "Should load only valid connections");
    }

    @Test
    void testLoadResultToString() {
        RailwayNetworkRepository.LoadResult result = new RailwayNetworkRepository.LoadResult(5, 10);
        String str = result.toString();

        assertTrue(str.contains("5"), "Should contain stations count");
        assertTrue(str.contains("10"), "Should contain connections count");
    }

    // Helper methods
    private Path createStationsFile() throws IOException {
        Path file = tempDir.resolve("stations.csv");
        String content = "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                "1,Porto,41.1579,-8.6291,100.0,200.0\n" +
                "2,Braga,41.5454,-8.4265,110.0,210.0\n" +
                "3,Lisboa,38.7223,-9.1393,120.0,220.0\n";
        Files.writeString(file, content);
        return file;
    }

    private Path createLinesFile() throws IOException {
        Path file = tempDir.resolve("lines.csv");
        String content = "departure_stid,arrival_stid,dist,capacity,cost\n" +
                "1,2,50.0,100,25.0\n" +
                "2,3,300.0,150,75.0\n";
        Files.writeString(file, content);
        return file;
    }

    private Path createInvalidStationsFile() throws IOException {
        Path file = tempDir.resolve("stations_invalid.csv");
        String content = "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                "1,Porto,41.1579,-8.6291,100.0,200.0\n" +
                "invalid,Braga,41.5454,-8.4265,110.0,210.0\n" +
                "3,Lisboa,38.7223,-9.1393,120.0,220.0\n";
        Files.writeString(file, content);
        return file;
    }

    private Path createStationsFileCommaDecimal() throws IOException {
        Path file = tempDir.resolve("stations_comma.csv");
        String content = "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                "1,Porto,41,1579,-8,6291,100,0,200,0\n";
        Files.writeString(file, content);
        return file;
    }

    private Path createLinesFileCommaDecimal() throws IOException {
        Path file = tempDir.resolve("lines_comma.csv");
        String content = "departure_stid,arrival_stid,dist,capacity,cost\n" +
                "1,1,50,5,100,25,0\n";
        Files.writeString(file, content);
        return file;
    }

    private Path createLinesFileWithMissingStation() throws IOException {
        Path file = tempDir.resolve("lines_missing.csv");
        String content = "departure_stid,arrival_stid,dist,capacity,cost\n" +
                "1,2,50.0,100,25.0\n" +
                "2,999,300.0,150,75.0\n";
        Files.writeString(file, content);
        return file;
    }
}
