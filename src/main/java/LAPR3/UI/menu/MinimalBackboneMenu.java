package LAPR3.UI.menu;

import ESINF.controller.MinimalBackboneController;
import LAPR3.UI.utils.InputValidator;

import java.util.Scanner;

/**
 * Menu for USEI12 - Minimal Backbone Network
 * Provides interface for computing and visualizing MST of railway network
 *
 * NOTE: Data loading is done via USEI11 menu (shared repository)
 * Based on Sprint 3 requirements
 */
public class MinimalBackboneMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final MinimalBackboneController controller;

    public MinimalBackboneMenu(Scanner scanner,
                               InputValidator validator,
                               MinimalBackboneController controller) {
        this.scanner = scanner;
        this.validator = validator;
        this.controller = controller;
    }

    public void show() {
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = validator.readInt("Select option: ", 0, 6);

            switch (choice) {
                case 1 -> computeMST();
                case 2 -> computeAndExportDOT();
                case 3 -> computeAndSaveFiles();
                case 4 -> viewComparisonStats();
                case 5 -> analyzeComponents();
                case 6 -> viewHelp();
                case 0 -> {
                    System.out.println("Returning to main menu...");
                    running = false;
                }
                default -> System.out.println("Invalid option!");
            }

            if (running && choice != 0) {
                pressEnterToContinue();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  USEI12 - MINIMAL BACKBONE NETWORK (MST)                  │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  Kruskal's Algorithm for Railway Network Optimization     │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
        System.out.println();

        // Show data status
        if (controller.isDataLoaded()) {
            System.out.println("✓ Railway network data loaded");
            System.out.println("  Stations: " + controller.getStationCount());
            System.out.println("  Connections: " + controller.getConnectionCount());
        } else {
            System.out.println("⚠ No railway network data loaded!");
            System.out.println("  Load data via USEI11 (Option 16) first:");
            System.out.println("  Main Menu → 16. Directed Line Upgrade Plan → 1. Import Network");
        }

        System.out.println();
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  1. Compute MST (Basic)                                   │");
        System.out.println("│  2. Compute MST + Export DOT                              │");
        System.out.println("│  3. Compute MST + Save Files (DOT + SVG)                  │");
        System.out.println("│  4. View Network Comparison (Original vs MST)             │");
        System.out.println("│  5. Analyze Connected Components                          │");
        System.out.println("│  6. Help & Information                                    │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  0. Return to Main Menu                                   │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    /**
     * Option 1: Compute MST and display basic results
     */
    private void computeMST() {
        System.out.println("\n╔══ COMPUTE MINIMUM SPANNING TREE ══╗");
        System.out.println();

        if (!checkDataLoaded()) {
            return;
        }

        try {
            System.out.println("Computing MST using Kruskal's algorithm...");
            long startTime = System.currentTimeMillis();

            String result = controller.computeMST();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            System.out.println("\n" + result);
            System.out.println("\n⏱ Computation time: " + duration + " ms");
            System.out.println("✓ MST computed successfully!");

        } catch (Exception e) {
            System.out.println("\n✗ Error computing MST: " + e.getMessage());
        }
    }

    /**
     * Option 2: Compute MST and export to DOT format
     */
    private void computeAndExportDOT() {
        System.out.println("\n╔══ COMPUTE MST + EXPORT DOT ══╗");
        System.out.println();

        if (!checkDataLoaded()) {
            return;
        }

        System.out.print("Display DOT content? (y/n): ");
        boolean displayContent = scanner.nextLine().trim().equalsIgnoreCase("y");

        try {
            System.out.println("\nComputing MST and exporting to DOT format...");
            long startTime = System.currentTimeMillis();

            String result = controller.computeAndExportDOT();

            long endTime = System.currentTimeMillis();

            System.out.println("\n✓ MST computed and exported!");
            System.out.println("⏱ Time: " + (endTime - startTime) + " ms");

            if (displayContent) {
                System.out.println("\n" + result);
            } else {
                System.out.println("\n" + result.split("\n")[0]); // Just summary
            }

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }

    /**
     * Option 3: Compute MST and save DOT + SVG files
     */
    private void computeAndSaveFiles() {
        System.out.println("\n╔══ COMPUTE MST + SAVE FILES ══╗");
        System.out.println();

        if (!checkDataLoaded()) {
            return;
        }

        // Use fixed filename to avoid file accumulation
        String filename = "belgian_railway_mst";

        System.out.println("Files will be saved as:");
        System.out.println("  • output/usei12/" + filename + ".dot");
        System.out.println("  • output/usei12/" + filename + ".svg");
        System.out.println();
        System.out.println("(Previous files will be overwritten)");
        System.out.println();

        try {
            System.out.println("Computing MST and generating files...");

            long startTime = System.currentTimeMillis();

            String result = controller.computeAndSaveFiles(filename);

            long endTime = System.currentTimeMillis();

            System.out.println(result);
            System.out.println("\n⏱ Total time: " + (endTime - startTime) + " ms");

            // Check if GraphViz is available
            if (result.contains("SVG file:")) {
                System.out.println("\n✓ All files generated successfully!");
                System.out.println("  You can open the SVG file to visualize the MST.");
            } else if (result.contains("Not generated")) {
                System.out.println("\n⚠ DOT file generated, but SVG generation failed.");
                System.out.println("  Install GraphViz or use online viewer:");
                System.out.println("  https://dreampuf.github.io/GraphvizOnline/");
            }

        } catch (Exception e) {
            System.out.println("\n✗ Error generating files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Option 4: View comparison between original network and MST
     */
    private void viewComparisonStats() {
        System.out.println("\n╔══ NETWORK COMPARISON STATISTICS ══╗");
        System.out.println();

        if (!checkDataLoaded()) {
            return;
        }

        try {
            System.out.println("Computing comparison statistics...");
            String stats = controller.getComparisonStats();

            System.out.println("\n" + stats);

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }

    /**
     * Option 5: Analyze connected components
     */
    private void analyzeComponents() {
        System.out.println("\n╔══ CONNECTED COMPONENTS ANALYSIS ══╗");
        System.out.println();

        if (!checkDataLoaded()) {
            return;
        }

        try {
            System.out.println("Analyzing network connectivity...");
            String analysis = controller.analyzeComponents();

            System.out.println("\n" + analysis);

            // Additional information based on result
            if (analysis.contains("components: 1")) {
                System.out.println("\n✓ Network is fully connected!");
                System.out.println("  All stations can reach each other.");
            } else {
                System.out.println("\n⚠ Network has multiple disconnected components!");
                System.out.println("  Some stations are isolated from others.");
                System.out.println("  Result is MSF (Minimum Spanning Forest).");
            }

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }

    /**
     * Option 6: Display help and information
     */
    private void viewHelp() {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║  USEI12 - MINIMAL BACKBONE NETWORK - HELP                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("📖 WHAT IS MST?");
        System.out.println("   Minimum Spanning Tree (MST) is a subset of edges that:");
        System.out.println("   • Connects all stations");
        System.out.println("   • Has minimum total distance/cost");
        System.out.println("   • Contains no cycles");
        System.out.println("   • Has exactly V-1 edges (V = number of stations)");
        System.out.println();

        System.out.println("🔧 ALGORITHM: Kruskal's Algorithm");
        System.out.println("   Based on slides 102-107:");
        System.out.println("   1. Sort all edges by weight (distance)");
        System.out.println("   2. Add edges in order, skipping those that create cycles");
        System.out.println("   3. Stop when we have V-1 edges");
        System.out.println("   Complexity: O(E log E)");
        System.out.println();

        System.out.println("📊 OUTPUT FILES:");
        System.out.println("   • DOT file: GraphViz format (text-based graph description)");
        System.out.println("   • SVG file: Visual representation (requires GraphViz)");
        System.out.println("   Location: output/usei12/");
        System.out.println();

        System.out.println("🔍 CONNECTED COMPONENTS:");
        System.out.println("   If network has disconnected parts:");
        System.out.println("   • Result is MSF (Minimum Spanning Forest)");
        System.out.println("   • Multiple separate trees");
        System.out.println("   • Edges = V - k (k = number of components)");
        System.out.println();

        System.out.println("💡 USE CASES:");
        System.out.println("   • Identify essential railway infrastructure");
        System.out.println("   • Estimate baseline maintenance costs");
        System.out.println("   • Plan network expansion");
        System.out.println("   • Detect isolated stations");
        System.out.println();

        System.out.println("⚙️ GRAPHVIZ INSTALLATION:");
        System.out.println("   Ubuntu/Debian: sudo apt-get install graphviz");
        System.out.println("   macOS:         brew install graphviz");
        System.out.println("   Windows:       Download from graphviz.org");
        System.out.println("   Online viewer: dreampuf.github.io/GraphvizOnline/");
        System.out.println();

        System.out.println("📚 REFERENCES:");
        System.out.println("   • Slides 100-107: MST Theory and Kruskal's Algorithm");
        System.out.println("   • USEI12 Documentation: README_USEI12.md");
        System.out.println("   • Complexity Analysis: USEI12_Complexity_Analysis.md");
    }

    /**
     * Checks if railway network data is loaded
     */
    private boolean checkDataLoaded() {
        if (!controller.isDataLoaded()) {
            System.out.println("✗ No railway network data loaded!");
            System.out.println();
            System.out.println("Please load data first via USEI11:");
            System.out.println("  1. Return to Main Menu (press 0)");
            System.out.println("  2. Select option 16 (Directed Line Upgrade Plan)");
            System.out.println("  3. Select option 1 (Import Belgian Railway Network)");
            System.out.println("  4. Return here and compute MST");
            System.out.println();
            return false;
        }
        return true;
    }

    /**
     * Waits for user to press Enter
     */
    private void pressEnterToContinue() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}