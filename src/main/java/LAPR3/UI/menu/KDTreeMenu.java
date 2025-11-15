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
     */
    public KDTreeMenu(Scanner scanner, InputValidator validator) {
        this.scanner = scanner;
        this.validator = validator;
        this.stationController = new StationQueryController();
        this.kdTree = new KDTree2D();
    }

    /**
     * Displays the menu and handles user interactions.
     */
    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 7);

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
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║       2D-TREE SPATIAL INDEX (USEI07)             ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        displayCurrentStatus();
        System.out.println();

        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Check Station Data Status                   │");
        System.out.println("│  2. Build Balanced 2D-Tree                       │");
        System.out.println("│  3. Display Tree Statistics                      │");
        System.out.println("│  4. Display Tree Structure (In-Order)            │");
        System.out.println("│  5. Range Query (Rectangular Region)             │");
        System.out.println("│  6. Nearest Neighbor Search                      │");
        System.out.println("│  7. Analyze Bucket Sizes                         │");
        System.out.println("│  0. Back to Main Menu                            │");
        System.out.println("└─────────────────────────────────────────────────┘");
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
            case 5 -> performRangeQuery();
            case 6 -> performNearestNeighbor();
            case 7 -> analyzeBucketSizes();
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

        System.out.println("\n✓ 2D-Tree built successfully!");
        System.out.println(lastStatistics);

        // Temporal complexity analysis
        analyzeComplexity();
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
     * Displays tree structure using in-order traversal.
     */
    private void displayTreeStructure() {
        System.out.println("\n═══ TREE STRUCTURE (IN-ORDER TRAVERSAL) ═══");

        if (lastStatistics == null) {
            System.out.println("No tree built yet! Please run option 2 first.");
            return;
        }

        List<KDTree2D.Node> nodes = kdTree.inOrderTraversal();

        System.out.println("Total nodes: " + nodes.size());
        System.out.println("\nFirst 20 nodes:");
        System.out.println("─".repeat(80));

        for (int i = 0; i < Math.min(20, nodes.size()); i++) {
            KDTree2D.Node node = nodes.get(i);
            System.out.printf("%3d. (%.5f, %.5f) - %d station(s): %s\n",
                    i + 1, node.latitude, node.longitude, node.stations.size(),
                    node.stations.stream()
                            .map(Station::getName)
                            .limit(3)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }

        if (nodes.size() > 20) {
            System.out.println("... (" + (nodes.size() - 20) + " more nodes)");
        }
    }

    /**
     * Performs a range query on the 2D-Tree.
     */
    private void performRangeQuery() {
        System.out.println("\n═══ RANGE QUERY ═══");

        if (lastStatistics == null) {
            System.out.println("No tree built yet! Please run option 2 first.");
            return;
        }

        System.out.println("Enter rectangular region coordinates:");

        double minLat = readDouble("Minimum Latitude: ", -90, 90);
        double maxLat = readDouble("Maximum Latitude: ", minLat, 90);
        double minLon = readDouble("Minimum Longitude: ", -180, 180);
        double maxLon = readDouble("Maximum Longitude: ", minLon, 180);

        long startTime = System.nanoTime();
        List<Station> results = kdTree.rangeQuery(minLat, maxLat, minLon, maxLon);
        long endTime = System.nanoTime();

        System.out.println("\n✓ Range query completed in " +
                (endTime - startTime) / 1_000_000.0 + " ms");
        System.out.println("Found " + results.size() + " station(s) in the region:");

        if (results.isEmpty()) {
            System.out.println("  (No stations found in this region)");
        } else {
            for (int i = 0; i < Math.min(20, results.size()); i++) {
                Station s = results.get(i);
                System.out.printf("  %d. %s (%.5f, %.5f) - %s\n",
                        i + 1, s.getName(), s.getLatitude(), s.getLongitude(), s.getCountry());
            }
            if (results.size() > 20) {
                System.out.println("  ... (" + (results.size() - 20) + " more stations)");
            }
        }
    }

    /**
     * Finds the nearest neighbor to a target point.
     */
    private void performNearestNeighbor() {
        System.out.println("\n═══ NEAREST NEIGHBOR SEARCH ═══");

        if (lastStatistics == null) {
            System.out.println("No tree built yet! Please run option 2 first.");
            return;
        }

        System.out.println("Enter target coordinates:");

        double targetLat = readDouble("Latitude: ", -90, 90);
        double targetLon = readDouble("Longitude: ", -180, 180);

        long startTime = System.nanoTime();
        Station nearest = kdTree.nearestNeighbor(targetLat, targetLon);
        long endTime = System.nanoTime();

        System.out.println("\n✓ Nearest neighbor search completed in " +
                (endTime - startTime) / 1_000_000.0 + " ms");

        if (nearest == null) {
            System.out.println("No nearest station found (tree is empty).");
        } else {
            System.out.println("Nearest station:");
            System.out.printf("  Name: %s\n", nearest.getName());
            System.out.printf("  Coordinates: (%.5f, %.5f)\n",
                    nearest.getLatitude(), nearest.getLongitude());
            System.out.printf("  Country: %s\n", nearest.getCountry());
            System.out.printf("  Timezone: %s\n", nearest.getTimeZone());
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
        System.out.println("─".repeat(60));
        System.out.printf("%-20s | %-15s | %-15s\n", "Bucket Size", "Frequency", "Percentage");
        System.out.println("─".repeat(60));

        int totalNodes = lastStatistics.size;
        frequency.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int size = entry.getKey();
                    int count = entry.getValue();
                    double percentage = (count * 100.0) / totalNodes;
                    System.out.printf("%-20d | %-15d | %.2f%%\n", size, count, percentage);
                });

        System.out.println("─".repeat(60));

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
     * Analyzes temporal complexity of the build operation.
     */
    private void analyzeComplexity() {
        System.out.println("\n═══ TEMPORAL COMPLEXITY ANALYSIS ═══");

        int n = stationController.getStatistics().totalStations();
        double expectedComplexity = n * Math.log(n) / Math.log(2);

        System.out.println("Build operation complexity:");
        System.out.printf("  • Number of stations (n): %d\n", n);
        System.out.printf("  • Expected complexity: O(n log n) ≈ %.0f operations\n", expectedComplexity);
        System.out.printf("  • Actual build time: %.3f ms\n", lastStatistics.buildTimeNanos / 1_000_000.0);
        System.out.printf("  • Tree height: %d (ideal balanced: %.0f)\n",
                lastStatistics.height, Math.log(n) / Math.log(2));

        // Height balance check
        double idealHeight = Math.log(n) / Math.log(2);
        double heightRatio = lastStatistics.height / idealHeight;

        if (heightRatio <= 1.2) {
            System.out.println("  • Balance quality: ✓ Excellent (well-balanced)");
        } else if (heightRatio <= 1.5) {
            System.out.println("  • Balance quality: ✓ Good (reasonably balanced)");
        } else {
            System.out.println("  • Balance quality: ⚠ Fair (could be better)");
        }
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