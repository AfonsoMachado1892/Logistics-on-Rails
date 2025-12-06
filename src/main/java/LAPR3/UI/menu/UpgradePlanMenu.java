package LAPR3.UI.menu;

import ESINF.controller.UpgradePlanController;
import ESINF.DTO.UpgradePlanDTO;
import ESINF.repository.RailwayNetworkRepository;
import LAPR3.UI.utils.InputValidator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Menu for USEI11 - Directed Line Upgrade Plan
 * Railway Network Optimization - Sprint 3
 */
public class UpgradePlanMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final UpgradePlanController controller;

    public UpgradePlanMenu(Scanner scanner, InputValidator validator) {
        this.scanner = scanner;
        this.validator = validator;
        this.controller = new UpgradePlanController();
    }

    public UpgradePlanMenu(Scanner scanner, InputValidator validator,
                           UpgradePlanController controller) {
        this.scanner = scanner;
        this.validator = validator;
        this.controller = controller;
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 4);

            if (choice == 0) {
                return;
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                if (choice != 1) {
                    e.printStackTrace();
                }
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   DIRECTED LINE UPGRADE PLAN (USEI11)                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        if (controller.isDataLoaded()) {
            var stats = controller.getStatistics();
            System.out.println(stats.toString());
        } else {
            System.out.println("⚠ No railway network data loaded. Please import data first.");
        }

        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│  1. Import Belgian Railway Network                          │");
        System.out.println("│  2. Compute Upgrade Plan                                     │");
        System.out.println("│  3. View Complexity Analysis                                 │");
        System.out.println("│  4. Export Results to File                                   │");
        System.out.println("│  0. Back to Main Menu                                        │");
        System.out.println("└──────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> importRailwayNetwork();
            case 2 -> computeUpgradePlan();
            case 3 -> viewComplexityAnalysis();
            case 4 -> exportResults();
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

        System.out.println("\n⏳ Loading railway network...");
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

            var stats = controller.getStatistics();
            System.out.printf("\n📊 Network Statistics:%n");
            System.out.printf("  • Total stations: %d%n", result.stationsLoaded);
            System.out.printf("  • Total connections: %d%n", result.connectionsLoaded);
            System.out.printf("  • Average connections per station: %.2f%n",
                    (double) result.connectionsLoaded / result.stationsLoaded);

        } catch (IOException e) {
            System.err.println("❌ Failed to load files: " + e.getMessage());
            System.out.println("\nPlease ensure:");
            System.out.println("  1. Both files exist at specified paths");
            System.out.println("  2. stations.csv format: Station id,Station,Lat,Lon,CoordX,CoordY");
            System.out.println("  3. lines.csv format: departure_stid,arrival_stid,dist,capacity,cost");
            System.out.println("  4. CSV files use comma (,) as separator");
        }
    }

    private void computeUpgradePlan() {
        System.out.println("\n═══ COMPUTE UPGRADE PLAN ═══");

        if (!ensureDataLoaded()) return;

        System.out.println("\n⏳ Computing upgrade plan...");
        System.out.println("  • Detecting cycles using Colored DFS");
        System.out.println("  • Computing topological order if acyclic");
        System.out.println();

        try {
            long startTime = System.currentTimeMillis();
            UpgradePlanDTO result = controller.computeUpgradePlan();
            long endTime = System.currentTimeMillis();

            displayUpgradePlanResults(result, endTime - startTime);

            // Offer to save results
            if (validator.readConfirmation("\nSave results to file?")) {
                saveResultsToFile(result);
            }

        } catch (Exception e) {
            System.err.println("❌ Error computing upgrade plan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayUpgradePlanResults(UpgradePlanDTO result, long timeMs) {
        System.out.println("═".repeat(70));
        System.out.println("UPGRADE PLAN RESULTS");
        System.out.println("═".repeat(70));
        System.out.printf("Computation time: %.3f seconds%n", timeMs / 1000.0);
        System.out.println();

        if (result.hasCycles()) {
            // Graph contains cycles
            System.out.println("⚠ GRAPH CONTAINS CYCLES");
            System.out.println("─".repeat(70));
            System.out.println("The railway network has circular dependencies.");
            System.out.println("Upgrade cannot proceed until these cycles are resolved.");
            System.out.println();

            List<UpgradePlanDTO.CycleInfo> cycles = result.getCycles();
            System.out.printf("Total cycles detected: %d%n", cycles.size());
            System.out.println();

            int displayLimit = Math.min(cycles.size(), 10);
            for (int i = 0; i < displayLimit; i++) {
                UpgradePlanDTO.CycleInfo cycle = cycles.get(i);
                System.out.printf("Cycle %d:%n", i + 1);
                System.out.println("  Stations: " + String.join(" → ", cycle.getVertices()));
                System.out.println("  Edges:");
                for (String edge : cycle.getEdges()) {
                    System.out.println("    • " + edge);
                }
                System.out.println();
            }

            if (cycles.size() > 10) {
                System.out.printf("... and %d more cycles (save to file to see all)%n",
                        cycles.size() - 10);
            }

        } else {
            // Graph is acyclic - show topological order
            System.out.println("✓ VALID UPGRADE ORDER FOUND");
            System.out.println("─".repeat(70));
            System.out.println("The railway network has no circular dependencies.");
            System.out.println("Stations can be upgraded in the following order:");
            System.out.println();

            List<String> order = result.getTopologicalOrder();
            System.out.printf("Total stations: %d%n", order.size());
            System.out.println();

            // Display first 50 stations
            int displayLimit = Math.min(order.size(), 50);
            for (int i = 0; i < displayLimit; i++) {
                System.out.printf("%3d. %s%n", i + 1, order.get(i));
            }

            if (order.size() > 50) {
                System.out.printf("... and %d more stations (save to file to see complete order)%n",
                        order.size() - 50);
            }
        }

        System.out.println("═".repeat(70));
    }

    private void viewComplexityAnalysis() {
        System.out.println(controller.getComplexityAnalysis());
    }

    private void exportResults() {
        System.out.println("\n═══ EXPORT RESULTS ═══");

        if (!ensureDataLoaded()) return;

        System.out.println("\n⏳ Computing upgrade plan for export...");

        try {
            UpgradePlanDTO result = controller.computeUpgradePlan();
            saveResultsToFile(result);

        } catch (Exception e) {
            System.err.println("❌ Error exporting results: " + e.getMessage());
        }
    }

    private void saveResultsToFile(UpgradePlanDTO result) {
        try {
            String defaultFileName = "upgrade_plan_" + System.currentTimeMillis() + ".txt";
            String defaultPath = "Logs/" + defaultFileName;

            System.out.println("Enter file path to save results:");
            System.out.println("[Press ENTER to use default: " + defaultPath + "]");
            System.out.print("> ");

            String customPath = scanner.nextLine().trim();
            String finalPath;

            if (customPath.isEmpty()) {
                finalPath = defaultPath;
            } else {
                java.io.File userPath = new java.io.File(customPath);
                if (userPath.isDirectory()) {
                    finalPath = new java.io.File(userPath, defaultFileName).getPath();
                    System.out.println("ℹ (Target is a directory. Saving as: " + finalPath + ")");
                } else {
                    finalPath = customPath;
                }
            }

            // Write results to file
            try (FileWriter writer = new FileWriter(finalPath)) {
                writer.write("═══════════════════════════════════════════════════════════════\n");
                writer.write("  DIRECTED LINE UPGRADE PLAN RESULTS (USEI11)\n");
                writer.write("═══════════════════════════════════════════════════════════════\n\n");

                if (result.hasCycles()) {
                    writer.write("⚠ GRAPH CONTAINS CYCLES\n\n");
                    writer.write("Total cycles detected: " + result.getCycles().size() + "\n\n");

                    for (int i = 0; i < result.getCycles().size(); i++) {
                        UpgradePlanDTO.CycleInfo cycle = result.getCycles().get(i);
                        writer.write(String.format("Cycle %d:\n", i + 1));
                        writer.write("  Stations: " + String.join(" → ", cycle.getVertices()) + "\n");
                        writer.write("  Edges:\n");
                        for (String edge : cycle.getEdges()) {
                            writer.write("    • " + edge + "\n");
                        }
                        writer.write("\n");
                    }
                } else {
                    writer.write("✓ VALID UPGRADE ORDER FOUND\n\n");
                    writer.write("Total stations: " + result.getTopologicalOrder().size() + "\n\n");
                    writer.write("Upgrade Order:\n");
                    writer.write("─".repeat(60) + "\n");

                    List<String> order = result.getTopologicalOrder();
                    for (int i = 0; i < order.size(); i++) {
                        writer.write(String.format("%4d. %s\n", i + 1, order.get(i)));
                    }
                }
            }

            System.out.println("✓ Results saved to: " + finalPath);

        } catch (IOException e) {
            System.err.println("❌ Error saving file: " + e.getMessage());
        }
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