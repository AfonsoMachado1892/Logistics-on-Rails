package LAPR3.UI.menu;

import ESINF.controller.ProximitySearchController;
import ESINF.controller.StationQueryController;
import ESINF.model.KDTree2D;
import ESINF.model.Station;
import ESINF.model.TimeZoneFilter;
import LAPR3.UI.utils.InputValidator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;


public class ProximitySearchMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final StationQueryController stationController;
    private ProximitySearchController proximityController;

    public ProximitySearchMenu(Scanner scanner, InputValidator validator,
                               StationQueryController stationController) {
        this.scanner = scanner;
        this.validator = validator;
        this.stationController = stationController;
    }

    public void show() {
        // Check if data is loaded and 2D-Tree is built
        if (!stationController.isDataLoaded()) {
            System.out.println("\n Station data not loaded!");
            System.out.println("Please load station data first (Main Menu -> Option 10 -> Option 1)");
            return;
        }

        // Get KDTree from station controller (assumes KDTree is built in KDTreeMenu)
        KDTree2D kdTree = stationController.getKDTree();
        if (kdTree == null || kdTree.getRoot() == null) {
            System.out.println("\n 2D-Tree not built!");
            System.out.println("Please build 2D-Tree first (Main Menu -> Option 11 -> Option 1)");
            return;
        }

        // Initialize proximity search controller
        this.proximityController = new ProximitySearchController(kdTree);

        boolean back = false;

        while (!back) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 4);

            switch (choice) {
                case 1 -> searchNearestN();
                case 2 -> searchNearestNWithFilter();
                case 3 -> viewComplexityAnalysis();
                case 4 -> viewAvailableTimeZones();
                case 0 -> back = true;
                default -> System.out.println("Invalid option!");
            }

            if (!back) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n╔═════════════════════════════════════════════════╗");
        System.out.println("║   USEI09 - PROXIMITY SEARCH (NEAREST-N)         ║");
        System.out.println("╚═════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌───────────────────────────────────────────────────┐");
        System.out.println("│  1. Search N Nearest Stations                     │");
        System.out.println("│  2. Search N Nearest Stations with Filter         │");
        System.out.println("│  3. View Complexity Analysis                      │");
        System.out.println("│  4. View Available Time Zone Groups               │");
        System.out.println("├───────────────────────────────────────────────────┤");
        System.out.println("│  0. Back to Main Menu                             │");
        System.out.println("└───────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void searchNearestN() {
        System.out.println("\n╔══ SEARCH NEAREST N STATIONS ══╗");
        System.out.println();

        System.out.println("Enter target coordinates:");
        double lat = validator.readDouble("  Latitude [-90, 90]: ", -90, 90);
        double lon = validator.readDouble("  Longitude [-180, 180]: ", -180, 180);
        int n = validator.readInt("  Number of stations (N) [1-1000]: ", 1, 1000);

        System.out.println("\n Searching for " + n + " nearest station(s)...");

        try {
            ProximitySearchController.SearchResult result =
                    proximityController.searchNearest(lat, lon, n, null);

            displaySearchResult(result);
            offerToSaveResults(result.getStations(), "nearest_n");

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private void searchNearestNWithFilter() {
        System.out.println("\n╔══ SEARCH NEAREST N WITH FILTER ══╗");
        System.out.println();

        System.out.println("Enter target coordinates:");
        double lat = validator.readDouble("  Latitude [-90, 90]: ", -90, 90);
        double lon = validator.readDouble("  Longitude [-180, 180]: ", -180, 180);
        int n = validator.readInt("  Number of stations (N) [1-1000]: ", 1, 1000);

        // Get available time zone groups to show user
        viewAvailableTimeZones();

        System.out.println("\nEnter time zone groups separated by commas (e.g., CET,WET):");
        String input = validator.readString("Time Zone Groups: ");
        String[] groups = input.split(",");

        // Clean up input
        for (int i = 0; i < groups.length; i++) {
            groups[i] = groups[i].trim();
        }

        TimeZoneFilter filter = new TimeZoneFilter(groups);

        System.out.println("\n Searching for " + n + " nearest station(s) with filter: " + filter);

        try {
            ProximitySearchController.SearchResult result =
                    proximityController.searchNearest(lat, lon, n, filter);

            displaySearchResult(result);
            offerToSaveResults(result.getStations(), "nearest_n_filtered");

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private void viewComplexityAnalysis() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       TEMPORAL COMPLEXITY ANALYSIS (USEI09)           ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("ALGORITHM: k-Nearest Neighbors (k-NN) on 2D-Tree with Filtering");
        System.out.println("─".repeat(60));
        System.out.println();

        // Best / Average Case
        System.out.println("  BEST / AVERAGE CASE: O(log n)");
        System.out.println("   When:    N is small, and the Filter matches local stations.");
        System.out.println("   Example: Finding the 5 nearest stations in Paris (no filter).");
        System.out.println("   Why:     The algorithm finds valid neighbors quickly. The distance");
        System.out.println("            to the N-th neighbor becomes the pruning threshold,");
        System.out.println("            allowing huge sections of the tree to be discarded.");
        System.out.println();

        // Worst Case
        System.out.println("  WORST CASE: O(n * log N)");
        System.out.println("   When:    1. Strict Filtering (e.g., searching for 'Asia/Tokyo'");
        System.out.println("               stations while targeting coordinates in Portugal).");
        System.out.println("            2. Large N (Requesting N ≈ total stations).");
        System.out.println("   Why:     The pruning check always returns true (cannot shrink");
        System.out.println("            threshold), forcing recursion to visit every node.");
        System.out.println();

        System.out.println("─".repeat(60));

        // Component Costs
        System.out.println(" COMPONENT COSTS:");
        System.out.println("    Tree Traversal:     O(log n) [depth] to O(n) [full scan]");
        System.out.println("    Priority Queue Ops: O(log N) [insert/poll]");
        System.out.println("    Distance Calc:      O(1)");
        System.out.println("    Filter Check:       O(1)");
        System.out.println();
        System.out.println("   Where:");
        System.out.println("     n = Total stations (~64,000)");
        System.out.println("     N = Desired neighbors (k)");
        System.out.println();

        System.out.println("─".repeat(60));

        // Speedup
        System.out.println(" SPEEDUP vs LINEAR SCAN:");
        System.out.println("   , Linear Scan: Always O(n) iterations.");
        System.out.println("    2D-Tree:     Varies from O(log n) to O(n).");
        System.out.println("     -> Typically 100x-500x faster for small N (e.g., N=10)");
        System.out.println("        unless filters force a full traversal.");
        System.out.println();
        System.out.println("═".repeat(60));
    }

    private void viewAvailableTimeZones() {
        System.out.println("\n═══ AVAILABLE TIME ZONE GROUPS ═══");
        Set<String> tzGroups = stationController.getAllTimeZoneGroups();

        int count = 0;
        for (String tzGroup : tzGroups) {
            System.out.print(String.format("%-15s", tzGroup));
            if (++count % 5 == 0) System.out.println();
        }
        if (count % 5 != 0) System.out.println();
    }

    // --- Helper Methods ---

    private void displaySearchResult(ProximitySearchController.SearchResult result) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("SEARCH RESULTS");
        System.out.println("═".repeat(80));
        System.out.println(result.toString()); // Uses the toString we implemented in Controller
        System.out.println("═".repeat(80));

        if (result.getStationCount() > 0) {
            // Group by Country for summary
            Map<String, Long> byCountry = result.getStations().stream()
                    .collect(Collectors.groupingBy(Station::getCountry, Collectors.counting()));

            System.out.println("\nSummary by Country:");
            byCountry.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .forEach(entry ->
                            System.out.println("  " + entry.getKey() + ": " + entry.getValue()));
        }
    }

    private void offerToSaveResults(List<Station> results, String suffix) {
        boolean save = validator.readConfirmation("\nSave ALL " + results.size() + " stations to a log file?");
        if (save) {
            saveResultsToFile(results, suffix);
        }
    }

    /**
     * Saves all station results to a timestamped CSV file in the Logs directory.
     * This method ensures all stations, regardless of the console print limit, are logged.
     */
    private void saveResultsToFile(List<Station> results, String suffix) {
        try {
            // Create a timestamped filename
            String filename = "Logs/proximity_" + suffix + "_" + System.currentTimeMillis() + ".csv";

            try (FileWriter writer = new FileWriter(filename)) {
                // Header
                writer.write("station,latitude,longitude,country,timeZone,timeZoneGroup,isCity,isMainStation,isAirport\n");

                // Data
                for (Station s : results) {
                    // Escape commas and quotes for CSV integrity
                    String name = s.getName().contains(",") ? "\"" + s.getName().replace("\"", "\"\"") + "\"" : s.getName();

                    writer.write(String.format("%s,%.5f,%.5f,%s,%s,%s,%b,%b,%b\n",
                            name,
                            s.getLatitude(),
                            s.getLongitude(),
                            s.getCountry(),
                            s.getTimeZone(),
                            s.getTimeZoneGroup(),
                            s.isCity(),
                            s.isMainStation(),
                            s.isAirport()));
                }
            }

            System.out.println("\n ALL results saved to: " + filename);

        } catch (IOException e) {
            System.err.println(" Error saving file: " + e.getMessage());
        }
    }
}