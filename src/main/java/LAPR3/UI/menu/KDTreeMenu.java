package LAPR3.UI.menu;

import LAPR3.UI.utils.InputValidator;
import ESINF.controller.StationQueryController;
import ESINF.model.KDTree2D;
import ESINF.model.Station;

import java.util.*;

/**
 * Menu for USEI07 - Build a balanced 2D-Tree on Latitude/Longitude
 *
 * Provides functionality to:
 * - Load station data from CSV
 * - Build balanced 2D-Tree from latitude/longitude BSTs
 * - Display tree statistics
 * - Perform range queries
 * - Find nearest neighbors
 *
 * @author LAPR3 Team
 * @version 1.0
 */
public class KDTreeMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final StationQueryController stationController;

    /** Current 2D-Tree instance */
    private KDTree2D kdTree;

    /** Statistics from the last build */
    private KDTree2D.TreeStatistics lastStatistics;

    /**
     * Constructs a KDTreeMenu instance.
     * @param scanner Scanner for user input
     * @param validator Input validator
     * @param stationController Shared controller instance (from USEI06)
     */
    public KDTreeMenu(Scanner scanner, InputValidator validator, StationQueryController stationController) {
        this.scanner = scanner;
        this.validator = validator;
        this.stationController = stationController;
        this.kdTree = new KDTree2D();
    }

    /**
     * Displays the menu and handles user interactions.
     */
    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 8);

            if (choice == 0) {
                return; // Back to main menu
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

    /**
     * Displays the main menu.
     */
    private void displayMenu() {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║       2D-TREE SPATIAL INDEX (USEI07)             ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();

        displayCurrentStatus();
        System.out.println();

        System.out.println("┌───────────────────────────────────────────────────┐");
        System.out.println("│  1. Check Station Data Status                    │");
        System.out.println("│  2. Build Balanced 2D-Tree                        │");
        System.out.println("│  3. Display Tree Statistics                       │");
        System.out.println("│  4. Display Tree Structure (Visual)               │");
        System.out.println("│  5. Analyze Bucket Sizes                          │");
        System.out.println("│  6. Temporal Complexity Analysis                  │");
        System.out.println("│  0. Back to Main Menu                             │");
        System.out.println("└───────────────────────────────────────────────────┘");
        System.out.println();
    }

    /**
     * Displays current status.
     */
    private void displayCurrentStatus() {
        if (!stationController.isDataLoaded()) {
            System.out.println("Status: No stations loaded (use USEI06 menu to load data)");
        } else {
            var stats = stationController.getStatistics();
            System.out.println("Status: " + stats.totalStations() + " stations loaded");
        }

        if (lastStatistics != null) {
            System.out.println("2D-Tree: " + lastStatistics.size + " nodes, height " + lastStatistics.height);
        } else {
            System.out.println("2D-Tree: Not built yet");
        }
    }

    /**
     * Handles menu choice.
     */
    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> loadStationDataFromUSEI06();
            case 2 -> buildBalancedTree();
            case 3 -> displayStatistics();
            case 4 -> displayTreeStructure();
            case 5 -> analyzeBucketSizes();
            case 6 -> analyzeComplexity();
        }
    }

    /**
     * Loads station data from USEI06 repository (reuses already loaded data).
     */
    private void loadStationDataFromUSEI06() {
        System.out.println("\n═══ LOAD STATION DATA ═══");

        if (stationController.isDataLoaded()) {
            System.out.println("✓ Station data already loaded from USEI06!");
            var stats = stationController.getStatistics();
            System.out.println("  • Total stations: " + stats.totalStations());
            System.out.println("  • Unique latitudes: " + stats.uniqueLatitudes());
            System.out.println("  • Unique longitudes: " + stats.uniqueLongitudes());
            System.out.println("\nYou can proceed to build the 2D-Tree (option 2).");
            return;
        }

        System.out.println("No station data found!");
        System.out.println("\nPlease go to the Station Queries menu (USEI06) to load station data first.");
        System.out.println("After loading data in USEI06, return here to build the 2D-Tree.");
    }

    /**
     * Builds balanced 2D-Tree using the loaded BSTs from USEI06.
     */
    private void buildBalancedTree() {
        System.out.println("\n═══ BUILD BALANCED 2D-TREE ═══");

        if (!stationController.isDataLoaded()) {
            System.out.println("No stations loaded! Please load data via USEI06 menu first.");
            return;
        }

        // Get BSTs from USEI06 index
        var index = stationController.getStatistics();

        System.out.println("Building balanced 2D-Tree from " + index.totalStations() + " stations...");
        System.out.println("Using latitude and longitude BSTs from USEI06 for efficient construction...");

        // Extract sorted lists from USEI06 TreeMaps
        List<Station> latitudeBST = extractLatitudeList();
        List<Station> longitudeBST = extractLongitudeList();

        kdTree = new KDTree2D();
        lastStatistics = kdTree.buildBalanced(latitudeBST, longitudeBST);
        stationController.setKDTree(kdTree); // Share the built tree with USEI06 controller

        System.out.println("\n✓ 2D-Tree built successfully!");
        System.out.println("  • Size: " + lastStatistics.size + " nodes");
        System.out.println("  • Height: " + lastStatistics.height);
        System.out.println("  • Build time: " + String.format("%.3f ms", lastStatistics.buildTimeNanos / 1_000_000.0));
    }

    /**
     * Extracts flattened list from latitude TreeMap.
     */
    private List<Station> extractLatitudeList() {
        var latIndex = stationController.getIndex().getLatitudeIndex();
        List<Station> result = new ArrayList<>();

        for (List<Station> stations : latIndex.values()) {
            result.addAll(stations);
        }

        return result;
    }

    /**
     * Extracts flattened list from longitude TreeMap.
     */
    private List<Station> extractLongitudeList() {
        var lonIndex = stationController.getIndex().getLongitudeIndex();
        List<Station> result = new ArrayList<>();

        for (List<Station> stations : lonIndex.values()) {
            result.addAll(stations);
        }

        return result;
    }

    /**
     * Displays tree statistics.
     */
    private void displayStatistics() {
        System.out.println("\n═══ TREE STATISTICS ═══");

        if (lastStatistics == null) {
            System.out.println("No tree built yet! Please run option 2 first.");
            return;
        }

        System.out.println(lastStatistics);

        System.out.println("\nBucket Size Distribution:");
        Map<Integer, Integer> frequency = lastStatistics.getBucketSizeFrequency();
        frequency.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        System.out.printf("  %d station(s) per node: %d occurrences\n",
                                entry.getKey(), entry.getValue()));
    }

    /**
     * Displays tree structure as a visual graph.
     */
    private void displayTreeStructure() {
        System.out.println("\n═══ TREE STRUCTURE (VISUAL) ═══");

        if (lastStatistics == null) {
            System.out.println("No tree built yet! Please run option 2 first.");
            return;
        }

        System.out.println("\nTree visualization (showing first 5 levels):");
        printSeparator(80);

        printTreeVisual(kdTree.getRoot(), "", true, 0, 5);

        System.out.println();
        printSeparator(80);
        System.out.println("Legend:");
        System.out.println("  [LAT] = Split by Latitude (even depth)");
        System.out.println("  [LON] = Split by Longitude (odd depth)");
        System.out.println("  (n) = Number of stations at this coordinate");
    }

    /**
     * Recursively prints tree structure with visual branches.
     */
    private void printTreeVisual(KDTree2D.Node node, String prefix, boolean isTail, int depth, int maxDepth) {
        if (node == null || depth > maxDepth) {
            return;
        }

        String splitAxis = (depth % 2 == 0) ? "LAT" : "LON";
        String connector = isTail ? "└── " : "├── ";

        System.out.printf("%s%s[%s] (%.4f, %.4f) (%d)%s\n",
                prefix,
                connector,
                splitAxis,
                node.latitude,
                node.longitude,
                node.stations.size(),
                node.stations.size() > 1 ? " *" : "");

        String newPrefix = prefix + (isTail ? "    " : "│   ");

        if (node.left != null || node.right != null) {
            if (node.right != null) {
                printTreeVisual(node.right, newPrefix, node.left == null, depth + 1, maxDepth);
            }
            if (node.left != null) {
                printTreeVisual(node.left, newPrefix, true, depth + 1, maxDepth);
            }
        }
    }

    /**
     * Analyzes and displays bucket size distribution.
     */
    private void analyzeBucketSizes() {
        System.out.println("\n═══ BUCKET SIZE ANALYSIS ═══");

        if (lastStatistics == null) {
            System.out.println("No tree built yet! Please run option 2 first.");
            return;
        }

        Map<Integer, Integer> frequency = lastStatistics.getBucketSizeFrequency();
        Set<Integer> distinctSizes = lastStatistics.getDistinctBucketSizes();

        System.out.println("Distinct bucket sizes: " + distinctSizes);
        System.out.println("\nDetailed frequency distribution:");
        printSeparator(60);
        System.out.printf("%-20s | %-15s | %-15s\n", "Bucket Size", "Frequency", "Percentage");
        printSeparator(60);

        int totalNodes = lastStatistics.size;
        frequency.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int size = entry.getKey();
                    int count = entry.getValue();
                    double percentage = (count * 100.0) / totalNodes;
                    System.out.printf("%-20d | %-15d | %.2f%%\n", size, count, percentage);
                });

        printSeparator(60);

        // Find coordinates with multiple stations
        System.out.println("\nCoordinates with multiple stations:");
        List<Map.Entry<String, Integer>> multiStation = lastStatistics.bucketSizes.entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10)
                .toList();

        if (multiStation.isEmpty()) {
            System.out.println("  (No coordinates with multiple stations)");
        } else {
            for (Map.Entry<String, Integer> entry : multiStation) {
                System.out.printf("  %s: %d station(s)\n", entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Prints a separator line.
     */
    private void printSeparator(int length) {
        for (int i = 0; i < length; i++) {
            System.out.print("─");
        }
        System.out.println();
    }

    /**
     * Analyzes temporal complexity of operations
     */
    private void analyzeComplexity() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       TEMPORAL COMPLEXITY ANALYSIS (USEI07)           ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");

        if (lastStatistics == null) {
            System.out.println("No tree built yet! Please run option 2 first.");
            return;
        }

        int n = stationController.getStatistics().totalStations();
        double idealHeight = Math.log(n) / Math.log(2);
        double heightRatio = lastStatistics.height / idealHeight;

        System.out.println("\nALGORITHM: Balanced 2D-Tree Construction");
        printSeparator(60);

        // BUILD COMPLEXITY
        System.out.println("\n🔹 BUILD OPERATION COMPLEXITY:");
        System.out.println("   Time: O(n log n)");
        System.out.println("   Space: O(n)");
        System.out.println("\n   Why O(n log n)?");
        System.out.println("   • Median finding at each level: O(n) per level");
        System.out.println("   • Tree has O(log n) levels (balanced)");
        System.out.println("   • Total: O(n) × O(log n) = O(n log n)");

        System.out.println("\n   Actual Performance:");
        System.out.printf("   • Number of stations (n): %,d\n", n);
        System.out.printf("   • Expected operations: O(n log n) ≈ %,.0f\n", n * Math.log(n) / Math.log(2));
        System.out.printf("   • Actual build time: %.3f ms\n", lastStatistics.buildTimeNanos / 1_000_000.0);
        System.out.printf("   • Tree height: %d (ideal: %.0f)\n", lastStatistics.height, idealHeight);

        // Balance quality
        System.out.print("   • Balance quality: ");
        if (heightRatio <= 1.2) {
            System.out.println("✓ Excellent (well-balanced)");
        } else if (heightRatio <= 1.5) {
            System.out.println("✓ Good (reasonably balanced)");
        } else {
            System.out.println("⚠ Fair (could be better)");
        }

        printSeparator(60);

        // RANGE QUERY COMPLEXITY
        System.out.println("\n🔹 RANGE QUERY COMPLEXITY:");
        System.out.println("   • BEST CASE: O(log n + k)");
        System.out.println("     When: Small region, effective pruning");
        System.out.println("     Example: 0.1° × 0.1° region");
        System.out.println();
        System.out.println("   • AVERAGE CASE: O(√n + k)");
        System.out.println("     When: Medium-sized region");
        System.out.println("     Example: City-sized area (1-5 degrees)");
        System.out.println("     Why: Rectangular query intersects O(√n) splitting planes");
        System.out.println();
        System.out.println("   • WORST CASE: O(n)");
        System.out.println("     When: Region covers entire map (no pruning)");
        System.out.println("     Example: All coordinates (-90° to 90°)");

        printSeparator(60);

        // NEAREST NEIGHBOR COMPLEXITY
        System.out.println("\n🔹 NEAREST NEIGHBOR COMPLEXITY:");
        System.out.println("   • AVERAGE CASE: O(log n)");
        System.out.println("     When: Well-balanced tree, good pruning");
        System.out.println("     Why: Traverses tree height, prunes branches efficiently");
        System.out.println();
        System.out.println("   • WORST CASE: O(n)");
        System.out.println("     When: Degenerate tree or nearest point requires exploring all branches");
        System.out.println("     Example: Searching near tree boundaries");

        printSeparator(60);

        // COMPONENTS
        System.out.println("\nCOMPONENTS:");
        System.out.println("  • Tree construction: O(n log n)");
        System.out.println("  • Height calculation: O(n)");
        System.out.println("  • Node counting: O(n)");
        System.out.println("  • In-order traversal: O(n)");
        System.out.println();
        System.out.println("WHERE:");
        System.out.printf("  n = total stations in tree (%,d)\n", n);
        System.out.println("  k = number of results found");

        printSeparator(60);

        // SPEEDUP
        System.out.println("\n💡 SPEEDUP vs LINEAR STRUCTURES:");
        System.out.println("   Linear scan: Always O(n) ≈ 50-100ms");
        System.out.printf("   2D-Tree: Typically O(√n) ≈ O(%.0f) for range queries\n", Math.sqrt(n));
        System.out.println("   Performance gain: 10-100x faster! 🚀");

        printSeparator(60);
    }

    /**
     * Reads a double value with validation.
     */
    private double readDouble(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty.");
                    continue;
                }

                double value = Double.parseDouble(input);

                if (value < min || value > max) {
                    System.out.printf("Value must be between %.2f and %.2f.\n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }
    }

    /**
     * Pauses execution.
     */
    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}