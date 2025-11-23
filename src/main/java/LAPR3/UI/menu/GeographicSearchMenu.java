package LAPR3.UI.menu;

import ESINF.controller.GeographicSearchController;
import ESINF.controller.StationQueryController;
import ESINF.model.*;
import LAPR3.UI.utils.InputValidator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * USEI08 - Geographic Area Search Menu
 * Provides interface for searching stations by geographic region with optional filters

 */
public class GeographicSearchMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final StationQueryController stationController;
    private GeographicSearchController geoController;

    public GeographicSearchMenu(Scanner scanner, InputValidator validator,
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

        // Get KDTree from controller (assumes KDTree is built in KDTreeMenu)
        KDTree2D kdTree = getKDTreeFromController();
        if (kdTree == null || kdTree.getRoot() == null) {
            System.out.println("\n 2D-Tree not built!");
            System.out.println("Please build 2D-Tree first (Main Menu -> Option 11 -> Option 1)");
            return;
        }

        // Initialize geographic search controller
        this.geoController = new GeographicSearchController(kdTree);

        boolean back = false;

        while (!back) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 6);

            switch (choice) {
                case 1 -> searchByCustomRegion();
                case 2 -> searchByPredefinedRegion();
                case 3 -> searchWithFilters();
                case 4 -> runSampleQueries();
                case 5 -> analyzeRegionDensity();
                case 6 -> showComplexityInfo();
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
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║   USEI08 - GEOGRAPHIC AREA SEARCH                 ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌───────────────────────────────────────────────────┐");
        System.out.println("│  1. Search by Custom Geographic Region            │");
        System.out.println("│  2. Search by Predefined Region                   │");
        System.out.println("│  3. Search with Filters                           │");
        System.out.println("│  4. Run Sample Queries (Demo)                     │");
        System.out.println("│  5. Analyze Region Density                        │");
        System.out.println("│  6. View Complexity Analysis                      │");
        System.out.println("├───────────────────────────────────────────────────┤");
        System.out.println("│  0. Back to Main Menu                             │");
        System.out.println("└───────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void searchByCustomRegion() {
        System.out.println("\n╔══ CUSTOM GEOGRAPHIC REGION SEARCH ══╗");
        System.out.println();

        System.out.println("Enter geographic bounds:");

        double latMin = validator.readDouble("  Minimum Latitude [-90, 90]: ", -90, 90);
        double latMax = validator.readDouble("  Maximum Latitude [-90, 90]: ", latMin, 90);
        double lonMin = validator.readDouble("  Minimum Longitude [-180, 180]: ", -180, 180);
        double lonMax = validator.readDouble("  Maximum Longitude [-180, 180]: ", lonMin, 180);

        try {
            GeographicSearchController.SearchResult result =
                    geoController.searchByGeographicArea(latMin, latMax, lonMin, lonMax);

            displaySearchResult(result);
            if (result.getStationCount() > 0) {
                offerToSaveResults(result.getStations(), "region_custom");
            }

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private void searchByPredefinedRegion() {
        System.out.println("\n╔══ PREDEFINED REGIONS ══╗");
        System.out.println();
        System.out.println("1. Portugal Continental");
        System.out.println("2. Spain");
        System.out.println("3. France");
        System.out.println("4. Germany");
        System.out.println("5. Italy");
        System.out.println("6. Switzerland");
        System.out.println("7. Benelux (Belgium, Netherlands, Luxembourg)");
        System.out.println("8. Iberian Peninsula");
        System.out.println("0. Cancel");
        System.out.println();

        int choice = validator.readInt("Select region: ", 0, 8);

        if (choice == 0) return;

        GeographicRegion region = getPredefinedRegion(choice);
        if (region == null) {
            System.out.println("Invalid region!");
            return;
        }

        String regionName = getRegionName(choice);

        System.out.println("\n Searching in " + regionName + "...");
        System.out.println("Region: " + region);

        GeographicSearchController.SearchResult result =
                geoController.searchByGeographicArea(region, null);

        displaySearchResult(result);
        if (result.getStationCount() > 0) {
            offerToSaveResults(result.getStations(), regionName.toLowerCase().replace(" ", "_"));
        }
    }

    private void searchWithFilters() {
        System.out.println("\n╔══ SEARCH WITH FILTERS ══╗");
        System.out.println();

        // Get region
        System.out.println("Enter geographic bounds:");
        double latMin = validator.readDouble("  Minimum Latitude: ", -90, 90);
        double latMax = validator.readDouble("  Maximum Latitude: ", latMin, 90);
        double lonMin = validator.readDouble("  Minimum Longitude: ", -180, 180);
        double lonMax = validator.readDouble("  Maximum Longitude: ", lonMin, 180);

        // Build filter
        StationFilter filter = buildFilterInteractive();

        if (filter == null) {
            System.out.println("Operation cancelled.");
            return;
        }

        System.out.println("\n Searching with filter: " + filter);

        try {
            GeographicSearchController.SearchResult result =
                    geoController.searchByGeographicArea(latMin, latMax, lonMin, lonMax, filter);

            displaySearchResult(result);
            if (result.getStationCount() > 0) {
                offerToSaveResults(result.getStations(), "region_filtered");
            }
        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private StationFilter buildFilterInteractive() {
        System.out.println("\n--- Configure Filters ---");
        System.out.println("(Press ENTER to skip any filter)");
        System.out.println();

        // City filter
        System.out.print("Filter by city status? (yes/no/ENTER to skip): ");
        String cityInput = scanner.nextLine().trim().toLowerCase();
        Boolean isCity = null;
        if (cityInput.equals("yes") || cityInput.equals("y")) {
            isCity = true;
        } else if (cityInput.equals("no") || cityInput.equals("n")) {
            isCity = false;
        }

        // Main station filter
        System.out.print("Filter by main station? (yes/no/ENTER to skip): ");
        String mainInput = scanner.nextLine().trim().toLowerCase();
        Boolean isMainStation = null;
        if (mainInput.equals("yes") || mainInput.equals("y")) {
            isMainStation = true;
        } else if (mainInput.equals("no") || mainInput.equals("n")) {
            isMainStation = false;
        }

        // Country filter
        System.out.print("Filter by country code (e.g., PT, ES) or ENTER to skip: ");
        String country = scanner.nextLine().trim();
        if (country.isEmpty()) {
            country = null;
        }

        return new StationFilter(isCity, isMainStation, country);
    }

    private void runSampleQueries() {
        System.out.println("\n╔══ SAMPLE QUERIES (DEMO) ══╗");
        System.out.println();
        System.out.println("Running 5 representative queries...");
        System.out.println("═".repeat(80));

        // Query 1: Portugal
        System.out.println("\n QUERY 1: All stations in Portugal");
        System.out.println("─".repeat(80));
        GeographicSearchController.SearchResult result1 =
                geoController.searchByGeographicArea(
                        36.96, 42.15, -9.50, -6.19, StationFilter.forCountry("PT")
                );
        displaySearchResultSummary(result1);

        // Query 2: Madrid main stations
        System.out.println("\n QUERY 2: Main stations in Madrid region");
        System.out.println("─".repeat(80));
        GeographicSearchController.SearchResult result2 =
                geoController.searchByGeographicArea(
                        40.2, 40.7, -4.0, -3.4, StationFilter.mainStationsInCountry("ES")
                );
        displaySearchResultSummary(result2);

        // Query 3: Spanish coastal cities
        System.out.println("\n QUERY 3: Cities on Spanish Mediterranean coast");
        System.out.println("─".repeat(80));
        GeographicSearchController.SearchResult result3 =
                geoController.searchByGeographicArea(
                        36.5, 43.0, -1.0, 4.5, StationFilter.citiesInCountry("ES")
                );
        displaySearchResultSummary(result3);

        // Query 4: Switzerland
        System.out.println("\n QUERY 4: All stations in Switzerland");
        System.out.println("─".repeat(80));
        GeographicSearchController.SearchResult result4 =
                geoController.searchByGeographicArea(
                        45.8, 47.8, 5.9, 10.5, StationFilter.forCountry("CH")
                );
        displaySearchResultSummary(result4);

        // Query 5: Benelux cities
        System.out.println("\n QUERY 5: Cities in Benelux region");
        System.out.println("─".repeat(80));
        GeographicSearchController.SearchResult result5 =
                geoController.searchByGeographicArea(
                        49.5, 53.5, 2.5, 7.2, StationFilter.citiesOnly()
                );
        displaySearchResultSummary(result5);

        System.out.println("\n═".repeat(80));
        System.out.println(" All sample queries completed!");
    }

    private void analyzeRegionDensity() {
        System.out.println("\n╔══ REGION DENSITY ANALYSIS ══╗");
        System.out.println();

        System.out.println("Enter region to analyze:");
        double latMin = validator.readDouble("  Minimum Latitude: ", -90, 90);
        double latMax = validator.readDouble("  Maximum Latitude: ", latMin, 90);
        double lonMin = validator.readDouble("  Minimum Longitude: ", -180, 180);
        double lonMax = validator.readDouble("  Maximum Longitude: ", lonMin, 180);

        int gridSize = validator.readInt("Grid size (e.g., 5 for 5x5 grid): ", 2, 20);

        System.out.println("\n Analyzing density with " + gridSize + "x" + gridSize + " grid...");

        double latStep = (latMax - latMin) / gridSize;
        double lonStep = (lonMax - lonMin) / gridSize;

        int[][] heatmap = new int[gridSize][gridSize];
        int totalStations = 0;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double cellLatMin = latMin + i * latStep;
                double cellLatMax = cellLatMin + latStep;
                double cellLonMin = lonMin + j * lonStep;
                double cellLonMax = cellLonMin + lonStep;

                GeographicSearchController.SearchResult result =
                        geoController.searchByGeographicArea(
                                cellLatMin, cellLatMax, cellLonMin, cellLonMax, null
                        );

                heatmap[i][j] = result.getStationCount();
                totalStations += result.getStationCount();
            }
        }

        System.out.println("\n Station Density Heatmap:");
        System.out.println("─".repeat(gridSize * 5 + 1));

        for (int i = gridSize - 1; i >= 0; i--) { // Print from top to bottom
            for (int j = 0; j < gridSize; j++) {
                System.out.printf("%4d ", heatmap[i][j]);
            }
            System.out.println();
        }

        System.out.println("─".repeat(gridSize * 5 + 1));
        System.out.printf("Total stations in region: %d%n", totalStations);
        System.out.printf("Average per cell: %.1f%n", (double) totalStations / (gridSize * gridSize));
    }

    private void showComplexityInfo() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       TEMPORAL COMPLEXITY ANALYSIS (USEI08)           ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("ALGORITHM: 2D-Tree Range Search with Pruning");
        System.out.println("─".repeat(60));
        System.out.println();
        System.out.println("BEST CASE: O(log n + k)");
        System.out.println("   When: Small region, sparse area, effective pruning");
        System.out.println("   Example: 0.1° × 0.1° region → ~5ms for 64k stations");
        System.out.println();
        System.out.println("AVERAGE CASE: O(√n + k)");
        System.out.println("   When: Medium region (1-5 degrees)");
        System.out.println("   Example: Portugal → ~15ms for 64k stations");
        System.out.println("   Why: Rectangular query intersects O(√n) splitting planes");
        System.out.println();
        System.out.println("WORST CASE: O(n)");
        System.out.println("   When: Region covers entire map (no pruning)");
        System.out.println("   Example: All Europe → ~100ms for 64k stations");
        System.out.println();
        System.out.println("─".repeat(60));
        System.out.println("COMPONENTS:");
        System.out.println("  • Region validation: O(1)");
        System.out.println("  • Tree traversal: O(log n) to O(n)");
        System.out.println("  • Filter application: O(k × f) where f = filter checks");
        System.out.println("  • Result collection: O(k)");
        System.out.println();
        System.out.println("WHERE:");
        System.out.println("  n = total stations in tree (~64,000)");
        System.out.println("  k = number of results found");
        System.out.println("─".repeat(60));
        System.out.println();
        System.out.println(" SPEEDUP vs Linear Scan:");
        System.out.println("   Linear scan: Always O(n) ≈ 50-100ms");
        System.out.println("   2D-Tree: Typically O(√n) ≈ 5-30ms");
        System.out.println("   Performance gain: 10-20x faster ");
        System.out.println();
        System.out.println("═".repeat(60));
    }

    // Helper methods

    private void displaySearchResult(GeographicSearchController.SearchResult result) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("SEARCH RESULTS");
        System.out.println("═".repeat(80));
        System.out.println();
        System.out.println(" Region: " + result.getRegion());
        System.out.println(" Filter: " + (result.hasFilter() ? result.getFilter() : "None"));
        System.out.println(" Results: " + result.getStationCount() + " stations found");
        System.out.println(" Execution time: " + String.format("%.3f", result.getExecutionTimeMs()) + " ms");
        System.out.println();

        if (result.getStationCount() == 0) {
            System.out.println(" No stations found in this region.");
            return;
        }

        // Group by country
        Map<String, Long> byCountry = result.getStations().stream()
                .collect(Collectors.groupingBy(Station::getCountry, Collectors.counting()));

        System.out.println("Distribution by country:");
        byCountry.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .forEach(entry ->
                        System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " stations")
                );

        System.out.println();

        // Show sample stations
        int sampleSize = Math.min(10, result.getStationCount());
        System.out.println("Sample stations (showing " + sampleSize + " of " + result.getStationCount() + "):");
        System.out.println("─".repeat(80));

        result.getStations().stream()
                .limit(sampleSize)
                .forEach(station -> {
                    System.out.printf("  %-40s  %s  [%.5f, %.5f]%n",
                            station.getName(),
                            station.getCountry(),
                            station.getLatitude(),
                            station.getLongitude()
                    );
                    if (station.isCity()) System.out.print("      City");
                    if (station.isMainStation()) System.out.print("    Main Station");
                    if (station.isAirport()) System.out.print("      Airport");
                    if (station.isCity() || station.isMainStation() || station.isAirport()) {
                        System.out.println();
                    }
                });

        System.out.println("─".repeat(80));

        if (result.getStationCount() > sampleSize) {
            System.out.println("... and " + (result.getStationCount() - sampleSize) + " more stations");
        }
    }

    private void displaySearchResultSummary(GeographicSearchController.SearchResult result) {
        System.out.printf("Region: %s%n", result.getRegion());
        System.out.printf("Filter: %s%n", result.hasFilter() ? result.getFilter() : "None");
        System.out.printf(" Found: %d stations in %.3f ms%n",
                result.getStationCount(), result.getExecutionTimeMs());

        if (result.getStationCount() > 0) {
            // Show first 3 stations
            System.out.println("Sample results:");
            result.getStations().stream()
                    .limit(3)
                    .forEach(s -> System.out.println("  • " + s.getName() + " (" + s.getCountry() + ")"));
        }
    }

    private GeographicRegion getPredefinedRegion(int choice) {
        return switch (choice) {
            case 1 -> new GeographicRegion(36.96, 42.15, -9.50, -6.19);  // Portugal
            case 2 -> new GeographicRegion(36.0, 43.8, -9.3, 3.3);        // Spain
            case 3 -> new GeographicRegion(42.3, 51.1, -5.1, 8.2);        // France
            case 4 -> new GeographicRegion(47.3, 55.0, 5.9, 15.0);        // Germany
            case 5 -> new GeographicRegion(36.6, 47.1, 6.6, 18.5);        // Italy
            case 6 -> new GeographicRegion(45.8, 47.8, 5.9, 10.5);        // Switzerland
            case 7 -> new GeographicRegion(49.5, 53.5, 2.5, 7.2);         // Benelux
            case 8 -> new GeographicRegion(36.0, 43.8, -9.5, 3.3);        // Iberian
            default -> null;
        };
    }

    private String getRegionName(int choice) {
        return switch (choice) {
            case 1 -> "Portugal Continental";
            case 2 -> "Spain";
            case 3 -> "France";
            case 4 -> "Germany";
            case 5 -> "Italy";
            case 6 -> "Switzerland";
            case 7 -> "Benelux";
            case 8 -> "Iberian Peninsula";
            default -> "Unknown";
        };
    }

    private KDTree2D getKDTreeFromController() {
        return stationController.getKDTree();
    }


    // ---  HELPER METHODS FOR LOGGING ---

    private void offerToSaveResults(List<Station> results, String suffix) {
        boolean save = validator.readConfirmation("\nSave ALL " + results.size() + " stations to a log file?");

        if (save) {
            // -----------------------------------------------------------
            // CONFIGURAÇÃO DO CAMINHO SUGERIDO
            // Preencha o espaço abaixo com o caminho base desejado (ex: "Logs/")
            String suggestedBasePath = "Logs/"; // <-- MODIFY THIS PATH AS NEEDED                                ";
            // -----------------------------------------------------------

            String defaultFilename = "geographic_" + suffix + "_" + System.currentTimeMillis() + ".csv";
            String fullSuggestedPath = suggestedBasePath + defaultFilename;

            System.out.println("\nDestination file path:");
            System.out.println("Default: " + fullSuggestedPath);
            System.out.print("Press ENTER to accept default or type new path: ");

            String input = scanner.nextLine().trim();
            String finalPath = input.isEmpty() ? fullSuggestedPath : input;

            saveResultsToFile(results, finalPath);
        }
    }

    /**
     * Saves all station results to the specified CSV file path.
     */
    private void saveResultsToFile(List<Station> results, String filePath) {
        try {
            try (FileWriter writer = new FileWriter(filePath)) {
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

            System.out.println("\nSuccess! Results saved to: " + filePath);

        } catch (IOException e) {
            System.err.println(" Error saving file: " + e.getMessage());
            System.err.println(" Please check if the directory exists and you have write permissions.");
        }
    }
}