package ESINF.controller;

import ESINF.model.HubCentralityResult;
import ESINF.repository.RailwayNetworkRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CentralityControllerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private RailwayNetworkRepository repository;
    private CentralityController controller;

    @Before
    public void setUp() {
        repository = new RailwayNetworkRepository();
        controller = new CentralityController(repository);
    }

    @Test
    public void analyzeReturnsEmptyWhenNoDataIsLoaded() {
        // Sem dados carregados o controlador deve devolver lista vazia.
        List<HubCentralityResult> results = controller.analyzeRailHubCentrality();
        assertTrue(results.isEmpty());
    }

    @Test
    public void loadRailwayNetworkReadsCSVAndSetsLoaded() throws IOException {
        // Verifica que o carregamento via CSV e marca o repositório como carregado.
        File stations = createStationsFile();
        File lines = createLinesFile();

        RailwayNetworkRepository.LoadResult result = controller.loadRailwayNetwork(stations.getAbsolutePath(), lines.getAbsolutePath());

        assertEquals(3, result.stationsLoaded);
        assertEquals(2, result.connectionsLoaded);
        assertTrue(controller.isDataLoaded());
    }

    @Test
    public void analyzeCentralityOrdersResultsByHubScore() throws IOException {
        // Grafo A-B-C: estação 2 (B) deve surgir primeiro pelo maior HubScore.
        File stations = createStationsFile();
        File lines = createLinesFile();
        controller.loadRailwayNetwork(stations.getAbsolutePath(), lines.getAbsolutePath());

        List<HubCentralityResult> results = controller.analyzeRailHubCentrality();

        assertEquals(3, results.size());
        assertEquals(2, results.get(0).getStid());
        assertTrue(results.get(0).getHubScore() > results.get(1).getHubScore());
    }

    @Test
    public void temporalComplexityDelegatesToService() {
        // Deve devolver a frase de complexidade definida no serviço.
        String complexity = controller.getTemporalComplexity();
        assertTrue(complexity.contains("O(V * (E + V log V))"));
    }

    private File createStationsFile() throws IOException {
        File file = tempFolder.newFile("stations.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Station id,Station,Lat,Lon,CoordX,CoordY\n");
            writer.write("1,A,0,0,0,0\n");
            writer.write("2,B,0,0,0,0\n");
            writer.write("3,C,0,0,0,0\n");
        }
        return file;
    }

    private File createLinesFile() throws IOException {
        File file = tempFolder.newFile("lines.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("departure_stid,arrival_stid,dist,capacity,cost\n");
            writer.write("1,2,1.0,10,5.0\n");
            writer.write("2,3,1.0,10,5.0\n");
        }
        return file;
    }
}

