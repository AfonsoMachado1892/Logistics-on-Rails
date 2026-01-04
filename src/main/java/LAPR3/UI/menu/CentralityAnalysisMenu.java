package LAPR3.UI.menu;

import ESINF.controller.CentralityController;
import ESINF.model.HubCentralityResult;
import ESINF.repository.RailwayNetworkRepository;
import LAPR3.UI.utils.InputValidator;
import LAPR3.UI.utils.TableFormatter;

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
            int choice = validator.readInt("Enter your choice: ", 0, 2);

            if (choice == 0) {
                return;
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
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
            System.out.println("⚠ No railway network data loaded.");
            System.out.println("  Please import railway network data via USEI11 (Option 16 in Main Menu) first.");
        }

        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  1. Compute Centrality Analysis                              │");
        System.out.println("│  2. View Complexity Analysis                                 │");
        System.out.println("│  0. Back to Main Menu                                        │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> computeCentralityAnalysis();
            case 2 -> viewComplexityAnalysis();
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
            System.out.println("\nResultados da Análise de Centralidade (Normalizados e Ordenados por HubScore Descendente):");

            String[] headers = {
                    "STID",
                    "Station Name",
                    "Degree",
                    "Strength",
                    "Betweenness",
                    "Harmonic Closeness",
                    "HubScore"
            };

            List<String[]> data = results.stream()
                    .map(r -> new String[]{
                            String.valueOf(r.getStid()),
                            r.getStname(),
                            String.format("%.0f", r.getDegree()),
                            String.format("%.4f", r.getStrengthNorm()),
                            String.format("%.4f", r.getBetweennessNorm()),
                            String.format("%.4f", r.getHarmonicClosenessNorm()),
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
            System.out.println("Please import railway network data via USEI11 (Option 16 in Main Menu) first.");
            return false;
        }
        return true;
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}