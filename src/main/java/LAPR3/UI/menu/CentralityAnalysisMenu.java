package LAPR3.UI.menu;

import ESINF.controller.CentralityController;
import ESINF.model.HubCentralityResult;
import ESINF.repository.RailwayNetworkRepository;
import LAPR3.UI.utils.InputValidator;
import LAPR3.UI.utils.TableFormatter;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Menu para a funcionalidade USEI13: Rail Hub Centrality Analysis.
 */
public class CentralityAnalysisMenu implements Runnable {

    private final CentralityController controller;
    private final Scanner scanner;
    private final InputValidator validator;

    /**
     * Construtor para injeção de dependência.
     * @param scanner Scanner para input do utilizador.
     * @param validator Validador de input.
     * @param networkRepository O repositório da rede ferroviária.
     */
    public CentralityAnalysisMenu(Scanner scanner, InputValidator validator, RailwayNetworkRepository networkRepository) {
        this.scanner = scanner;
        this.validator = validator;
        this.controller = new CentralityController(networkRepository);
    }

    @Override
    public void run() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 3);

            if (choice == 0) {
                return;
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                if (choice != 1) {
                    e.printStackTrace();
                }
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   RAIL HUB CENTRALITY ANALYSIS (USEI13)                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        if (controller.isDataLoaded()) {
            System.out.println("✓ Railway network data loaded");
        } else {
            System.out.println("⚠ No railway network data loaded. Please import data first.");
        }

        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  1. Import Belgian Railway Network                           │");
        System.out.println("│  2. Compute Centrality Analysis                              │");
        System.out.println("│  3. View Complexity Analysis                                 │");
        System.out.println("│  0. Back to Main Menu                                        │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) throws IOException {
        switch (choice) {
            case 1 -> importRailwayNetwork();
            case 2 -> computeCentralityAnalysis();
            case 3 -> viewComplexityAnalysis();
        }
    }

    private void importRailwayNetwork() {
        System.out.println("\n═══ IMPORT BELGIAN RAILWAY NETWORK ═══");
        System.out.println("\nThis requires TWO CSV files:");
        System.out.println("  1. stations.csv - Station data (ID, Name, Lat, Lon, CoordX, CoordY)");
        System.out.println("  2. lines.csv - Connections (departure_stid, arrival_stid, dist, capacity, cost)");
        System.out.println();

        // Get stations file path
        String defaultStationsPath = "Dataset/Sprint3-ESINF/stations.csv";
        System.out.println("Step 1/2: Stations file");
        String stationsPath = validator.readFilePath("Enter stations.csv path", defaultStationsPath);

        // Get lines file path
        String defaultLinesPath = "Dataset/Sprint3-ESINF/lines.csv";
        System.out.println("\nStep 2/2: Lines file");
        String linesPath = validator.readFilePath("Enter lines.csv path", defaultLinesPath);

        System.out.println("\nLoading railway network...");
        System.out.println("  • Reading stations from: " + stationsPath);
        System.out.println("  • Reading connections from: " + linesPath);
        System.out.println("Please wait...");

        try {
            long startTime = System.currentTimeMillis();
            RailwayNetworkRepository.LoadResult result =
                    controller.loadRailwayNetwork(stationsPath, linesPath);
            long endTime = System.currentTimeMillis();

            System.out.println("\n✓ Successfully loaded railway network!");
            System.out.println(result.toString());
            System.out.printf("  • Loading time: %.2f seconds%n", (endTime - startTime) / 1000.0);
            System.out.printf("\nNetwork Statistics:%n");
            System.out.printf("  • Total stations: %d%n", result.stationsLoaded);
            System.out.printf("  • Total connections: %d%n", result.connectionsLoaded);
            if (result.stationsLoaded > 0) {
                System.out.printf("  • Average connections per station: %.2f%n",
                        (double) result.connectionsLoaded / result.stationsLoaded);
            }

        } catch (IOException e) {
            System.err.println("Failed to load files: " + e.getMessage());
            System.out.println("\nPlease ensure:");
            System.out.println("  1. Both files exist at specified paths");
            System.out.println("  2. stations.csv format: Station id,Station,Lat,Lon,CoordX,CoordY");
            System.out.println("  3. lines.csv format: departure_stid,arrival_stid,dist,capacity,cost");
            System.out.println("  4. CSV files use comma (,) as separator");
        }
    }

    private void computeCentralityAnalysis() {
        System.out.println("\n═══ COMPUTE CENTRALITY ANALYSIS ═══");

        if (!ensureDataLoaded()) return;

        System.out.println("\nComputing centrality measures...");
        System.out.println("  • Degree and Strength");
        System.out.println("  • Betweenness Centrality (Brandes Algorithm)");
        System.out.println("  • Harmonic Closeness");
        System.out.println("  • HubScore (composite measure)");
        System.out.println("Please wait, this may take a while...");

        try {
            long startTime = System.currentTimeMillis();
            List<HubCentralityResult> results = controller.analyzeRailHubCentrality();
            long endTime = System.currentTimeMillis();

            if (results.isEmpty()) {
                System.out.println("\n[!] Não foi possível calcular a centralidade da rede.");
                return;
            }

            System.out.println("\n✓ Analysis completed!");
            System.out.printf("  • Computation time: %.2f seconds%n", (endTime - startTime) / 1000.0);
            System.out.println("\nResultados da Análise de Centralidade (Ordenados por HubScore Descendente):");

            // Preparar os resultados formatados
            String[] headers = {"STID", "Station Name", "Degree", "Strength", "Betweenness", "Harmonic Closeness", "HubScore"};
            List<String[]> data = results.stream()
                    .map(r -> new String[]{
                            String.valueOf(r.getStid()),
                            r.getStname(),
                            String.format("%.0f", r.getDegree()),
                            String.format("%.4f", r.getStrength()),
                            String.format("%.4f", r.getBetweenness()),
                            String.format("%.4f", r.getHarmonicCloseness()),
                            String.format("%.4f", r.getHubScore())
                    }).collect(java.util.stream.Collectors.toList());

            TableFormatter formatter = new TableFormatter();
            formatter.printTable(headers, data);

        } catch (Exception e) {
            System.err.println("Error computing centrality analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewComplexityAnalysis() {
        System.out.println("\n═══ COMPLEXITY ANALYSIS ═══");
        System.out.println(controller.getTemporalComplexity());
    }

    private boolean ensureDataLoaded() {
        if (!controller.isDataLoaded()) {
            System.out.println("\n⚠ No railway network data loaded!");
            System.out.println("Please import railway network data first (Option 1).");
            return false;
        }
        return true;
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}