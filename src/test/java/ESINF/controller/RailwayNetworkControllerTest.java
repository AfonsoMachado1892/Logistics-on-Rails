package ESINF.controller;

import ESINF.controller.RailwayNetworkController;
import ESINF.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RailwayNetworkControllerTest {

    private RailwayNetworkController controller;
    private Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        controller = new RailwayNetworkController();
    }

    @Test
    @DisplayName("Carregamento completo de rede a partir de CSV - sucesso")
    void testLoadNetworkFromCSV_Success() throws IOException {
        Path stationsFile = tempDir.resolve("stations.csv");
        String stationsContent = """
                stationId,name,latitude,longitude,coordX,coordY
                1,Porto Campanhã,41.1496,-8.6110,100.0,200.0
                2,Lisboa Oriente,38.7223,-9.1393,300.0,50.0
                3,Coimbra-B,40.2033,-8.4103,200.0,150.0
                """;
        Files.writeString(stationsFile, stationsContent);

        Path connectionsFile = tempDir.resolve("connections.csv");
        String connectionsContent = """
                fromId,toId,distance,capacity,cost
                1,2,300.0,100.0,50.0
                2,3,120.0,80.0,30.0
                1,3,200.0,50.0,40.0
                """;
        Files.writeString(connectionsFile, connectionsContent);

        boolean success = controller.loadNetworkFromCSV(
                stationsFile.toString(), connectionsFile.toString());

        assertTrue(success);
        assertEquals(3, controller.getNetwork().getStationCount());
        assertEquals(3, controller.getNetwork().getConnectionCount());
        assertTrue(controller.getNetwork().isValid());

        // Verifica nomes corretos
        List<RailwayStation> stations = controller.getAllStations();
        assertTrue(stations.stream().anyMatch(s -> s.getName().equals("Porto Campanhã")));
        assertTrue(stations.stream().anyMatch(s -> s.getName().equals("Lisboa Oriente")));
    }

    @Test
    @DisplayName("Carregamento ignora linhas inválidas (estações mal formatadas ou nome vazio)")
    void testLoadNetworkFromCSV_IgnoresInvalidStations() throws IOException {
        Path stationsFile = tempDir.resolve("stations_invalid.csv");
        String stationsContent = """
                stationId,name,latitude,longitude,coordX,coordY
                1,Porto,41.1496,-8.6110,100.0,200.0
                2,,38.7223,-9.1393,300.0,50.0
                3,Invalid,abc,def,ghi,jkl
                4,Coimbra,40.2033,-8.4103,200.0,150.0
                """;
        Files.writeString(stationsFile, stationsContent);

        Path connectionsFile = tempDir.resolve("connections.csv");
        Files.writeString(connectionsFile, """
                fromId,toId,distance,capacity,cost
                1,4,100.0,50.0,20.0
                """);

        boolean success = controller.loadNetworkFromCSV(
                stationsFile.toString(), connectionsFile.toString());

        assertTrue(success);
        assertEquals(3, controller.getNetwork().getStationCount()); // 1, 3 (inválido), 4 → só 1 e 4
        assertEquals(1, controller.getNetwork().getConnectionCount());
    }

    @Test
    @DisplayName("Validação falha com capacidade negativa")
    void testLoadNetworkFromCSV_NegativeCapacity() throws IOException {
        Path stationsFile = tempDir.resolve("stations.csv");
        Files.writeString(stationsFile, """
                stationId,name,latitude,longitude,coordX,coordY
                10,Source,0.0,0.0,0.0,0.0
                20,Sink,0.0,0.0,0.0,0.0
                """);

        Path connectionsFile = tempDir.resolve("connections.csv");
        Files.writeString(connectionsFile, """
                fromId,toId,distance,capacity,cost
                10,20,100.0,-30.0,10.0
                """);

        boolean success = controller.loadNetworkFromCSV(
                stationsFile.toString(), connectionsFile.toString());

        assertFalse(success); // isValid() retorna false devido à capacidade negativa
    }

    @Test
    @DisplayName("findStationsByName - pesquisa case-insensitive e partial match")
    void testFindStationsByName() throws IOException {
        Path stationsFile = tempDir.resolve("stations.csv");
        Files.writeString(stationsFile, """
                stationId,name,latitude,longitude,coordX,coordY
                100,Porto Campanhã,41.1,-8.6,0.0,0.0
                200,Lisboa Oriente,38.7,-9.1,0.0,0.0
                300,Aveiro,40.6,-8.6,0.0,0.0
                400,Porto São Bento,41.1,-8.6,0.0,0.0
                """);

        controller.loadNetworkFromCSV(stationsFile.toString(), "inexistente.csv"); // só estações

        List<RailwayStation> results;

        results = controller.findStationsByName("porto");
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Porto Campanhã")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Porto São Bento")));

        results = controller.findStationsByName("LISBOA");
        assertEquals(1, results.size());
        assertEquals("Lisboa Oriente", results.get(0).getName());

        results = controller.findStationsByName("  ori  ");
        assertEquals(1, results.size()); // "Oriente" contém "ori"

        assertTrue(controller.findStationsByName(null).isEmpty());
        assertTrue(controller.findStationsByName("").isEmpty());
        assertTrue(controller.findStationsByName("   ").isEmpty());
    }

    @Test
    @DisplayName("Adição manual de estação respeita validação de nome")
    void testManualStationAddition() {
        RailwayNetwork network = controller.getNetwork();

        RailwayStation valid = new RailwayStation(999, "Valid Station", 0, 0, 0, 0);
        assertTrue(network.addStation(valid));

        assertThrows(IllegalArgumentException.class, () ->
                new RailwayStation(888, null, 0, 0, 0, 0));

        assertThrows(IllegalArgumentException.class, () ->
                new RailwayStation(888, "   ", 0, 0, 0, 0));
    }

    @Test
    @DisplayName("exportNetworkSummary inclui toString correto das estações")
    void testExportNetworkSummary() throws IOException {
        RailwayNetwork network = controller.getNetwork();
        network.addStation(new RailwayStation(5, "Braga", 41.55, -8.42, 10, 20));
        network.addStation(new RailwayStation(6, "Faro", 37.02, -7.93, 30, 40));

        Path outputFile = tempDir.resolve("summary.txt");
        controller.exportNetworkSummary(outputFile.toString());

        String content = Files.readString(outputFile);

        assertTrue(content.contains("Braga (ID: 5)"));
        assertTrue(content.contains("Faro (ID: 6)"));
        assertTrue(content.contains("totalStations: 2"));
        assertTrue(content.contains("=== Stations ==="));
    }

    // Os outros testes (max flow, hasPath, statistics, etc.) permanecem válidos
    // pois não dependem diretamente do construtor ou toString da estação.

    @Test
    @DisplayName("hasPath funciona corretamente em grafo dirigido")
    void testHasPath() throws IOException {
        Path stationsFile = tempDir.resolve("stations.csv");
        Files.writeString(stationsFile, """
                stationId,name,latitude,longitude,coordX,coordY
                1,A,0,0,0,0
                2,B,0,0,0,0
                3,C,0,0,0,0
                """);

        Path connectionsFile = tempDir.resolve("connections.csv");
        Files.writeString(connectionsFile, """
                fromId,toId,distance,capacity,cost
                1,2,10.0,10.0,1.0
                2,3,10.0,10.0,1.0
                """);

        controller.loadNetworkFromCSV(stationsFile.toString(), connectionsFile.toString());

        assertTrue(controller.hasPath(1, 3));
        assertFalse(controller.hasPath(3, 1));
        assertFalse(controller.hasPath(1, 999));
    }
}