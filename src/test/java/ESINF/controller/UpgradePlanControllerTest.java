package ESINF.controller;

import ESINF.DTO.UpgradePlanDTO;
import ESINF.model.RailwayStation;
import ESINF.repository.RailwayNetworkRepository;
import Graph.Graph;
import Graph.map.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpgradePlanControllerTest {

    private UpgradePlanController controller;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        controller = new UpgradePlanController();
    }

    @Test
    void testLoadRailwayNetwork() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();

        RailwayNetworkRepository.LoadResult result = controller.loadRailwayNetwork(
                stationsFile.toString(), linesFile.toString());

        assertEquals(5, result.stationsLoaded, "Should load 5 stations");
        assertEquals(6, result.connectionsLoaded, "Should load 6 connections");
        assertTrue(controller.isDataLoaded(), "Data should be loaded");
    }

    @Test
    void testComputeUpgradePlanAcyclic() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();

        controller.loadRailwayNetwork(stationsFile.toString(), linesFile.toString());
        UpgradePlanDTO result = controller.computeUpgradePlan();

        assertFalse(result.hasCycles(), "Graph should be acyclic");
        assertEquals(5, result.getTopologicalOrder().size(), "Should have 5 stations in order");
    }

    @Test
    void testComputeUpgradePlanWithCycle() throws IOException {
        Path stationsFile = createStationsFile();
        Path linesFile = createCyclicLinesFile();

        controller.loadRailwayNetwork(stationsFile.toString(), linesFile.toString());
        UpgradePlanDTO result = controller.computeUpgradePlan();

        assertTrue(result.hasCycles(), "Graph should have cycles");
        assertFalse(result.getCycles().isEmpty(), "Should detect at least one cycle");
    }

    @Test
    void testComputeUpgradePlanWithoutLoadedData() {
        assertThrows(IllegalStateException.class, () -> controller.computeUpgradePlan(),
                "Should throw exception when no data loaded");
    }

    @Test
    void testIsDataLoaded() throws IOException {
        assertFalse(controller.isDataLoaded(), "Should be false initially");

        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        controller.loadRailwayNetwork(stationsFile.toString(), linesFile.toString());

        assertTrue(controller.isDataLoaded(), "Should be true after loading");
    }

    @Test
    void testGetStatistics() throws IOException {
        UpgradePlanController.NetworkStatistics stats = controller.getStatistics();
        assertFalse(stats.dataLoaded, "Should not be loaded initially");

        Path stationsFile = createStationsFile();
        Path linesFile = createLinesFile();
        controller.loadRailwayNetwork(stationsFile.toString(), linesFile.toString());

        stats = controller.getStatistics();
        assertTrue(stats.dataLoaded, "Should be loaded");
        assertEquals(5, stats.vertexCount, "Should have 5 vertices");
        assertEquals(6, stats.edgeCount, "Should have 6 edges");
    }


    @Test
    void testGetRepository() {
        assertNotNull(controller.getRepository(), "Repository should not be null");
    }

    // Helper methods to create test files
    private Path createStationsFile() throws IOException {
        Path file = tempDir.resolve("stations.csv");
        String content = "Station id,Station,Lat,Lon,CoordX,CoordY\n" +
                "1,StationA,50.1,4.1,100,200\n" +
                "2,StationB,50.2,4.2,110,210\n" +
                "3,StationC,50.3,4.3,120,220\n" +
                "4,StationD,50.4,4.4,130,230\n" +
                "5,StationE,50.5,4.5,140,240\n";
        Files.writeString(file, content);
        return file;
    }

    private Path createLinesFile() throws IOException {
        Path file = tempDir.resolve("lines.csv");
        String content = "departure_stid,arrival_stid,dist,capacity,cost\n" +
                "1,2,10.5,100,50\n" +
                "1,3,15.0,100,50\n" +
                "2,4,20.0,100,50\n" +
                "3,4,12.0,100,50\n" +
                "3,5,18.0,100,50\n" +
                "4,5,8.0,100,50\n";
        Files.writeString(file, content);
        return file;
    }

    private Path createCyclicLinesFile() throws IOException {
        Path file = tempDir.resolve("lines_cyclic.csv");
        String content = "departure_stid,arrival_stid,dist,capacity,cost\n" +
                "1,2,10.5,100,50\n" +
                "2,3,15.0,100,50\n" +
                "3,1,20.0,100,50\n" +
                "3,4,12.0,100,50\n" +
                "4,5,18.0,100,50\n";
        Files.writeString(file, content);
        return file;
    }
}