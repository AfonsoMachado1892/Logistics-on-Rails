package LAPR3.UI.menu;

import ESINF.controller.StationQueryController;
import ESINF.model.Station;
import ESINF.repository.StationRepository;
import LAPR3.UI.utils.InputValidator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Menu for Station Queries (USEI06)
 * Time-Zone Index and Windowed Queries
 */
public class StationQueryMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final StationQueryController controller;

    public StationQueryMenu(Scanner scanner, InputValidator validator) {
        this.scanner = scanner;
        this.validator = validator;
        this.controller = new StationQueryController();
    }

    public StationQueryMenu(Scanner scanner, InputValidator validator, StationQueryController controller) {
        this.scanner = scanner;
        this.validator = validator;
        this.controller = controller;
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 8);

            if (choice == 0) {
                return;
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println(" Error: " + e.getMessage());
                if (choice != 1) { // Don't show stack trace for file loading errors
                    e.printStackTrace();
                }
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     STATION QUERIES - TIME-ZONE INDEX (USEI06)           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        if (controller.isDataLoaded()) {
            var stats = controller.getStatistics();
            System.out.println(stats.toString());
        } else {
            System.out.println("No station data loaded. Please import data first.");
        }

        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  1. Import Station Data from CSV                        │");
        System.out.println("│  2. Query by Time Zone Group                            │");
        System.out.println("│  3. Query by Time Zone Group + Country                  │");
        System.out.println("│  4. Query by Time Zone Window (multiple groups)         │");
        System.out.println("│  5. Query by Latitude Range                             │");
        System.out.println("│  6. Query by Longitude Range                            │");
        System.out.println("│  7. View Available Time Zone Groups                     │");
        System.out.println("│  8. View Complexity Analysis                            │");
        System.out.println("│  0. Back to Main Menu                                   │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> importStationData();
            case 2 -> queryByTimeZoneGroup();
            case 3 -> queryByTimeZoneGroupAndCountry();
            case 4 -> queryByTimeZoneWindow();
            case 5 -> queryByLatitudeRange();
            case 6 -> queryByLongitudeRange();
            case 7 -> viewTimeZoneGroups();
            case 8 -> viewComplexityAnalysis();
        }
    }

    private void importStationData() {
        System.out.println("\n═══ IMPORT STATION DATA ═══");

        String defaultPath = "Dataset/train_stations_europe.csv";
        String filePath = validator.readFilePath("Enter CSV file path", defaultPath);

        System.out.println("\n Loading station data from: " + filePath);
        System.out.println("Please wait...");

        try {
            long startTime = System.currentTimeMillis();
            StationRepository.ImportResult result = controller.loadStations(filePath);
            long endTime = System.currentTimeMillis();

            System.out.println("\n " + result.summary());
            System.out.printf("Import completed in %.2f seconds%n", (endTime - startTime) / 1000.0);

            if (result.hasErrors()) {
                System.out.println("\nSome records were rejected:");
                int shown = 0;
                for (String error : result.errors()) {
                    if (shown++ < 10) {
                        System.out.println("   • " + error);
                    } else {
                        System.out.println("   ... (" + (result.errors().size() - 10) + " more errors)");
                        break;
                    }
                }

                boolean saveErrors = validator.readConfirmation("\nSave all errors to file?");
                if (saveErrors) {
                    // Logic to handle default vs custom path
                    String defaultFileName = "import_errors_" + System.currentTimeMillis() + ".txt";
                    String defaultLogPath = "Logs/" + defaultFileName;

                    System.out.println("Enter file path to save errors:");
                    System.out.println("[Press ENTER to use default: " + defaultLogPath + "]");
                    System.out.print("> ");

                    String customPath = scanner.nextLine().trim();
                    String finalPath;

                    if (customPath.isEmpty()) {
                        finalPath = defaultLogPath;
                    } else {
                        // Check if the user entered a directory (e.g., C:\...\Logs)
                        java.io.File userPath = new java.io.File(customPath);
                        if (userPath.isDirectory()) {
                            // If it's a directory, append the filename to it
                            finalPath = new java.io.File(userPath, defaultFileName).getPath();
                            System.out.println(" (Target is a directory. Saving as: " + finalPath + ")");
                        } else {
                            finalPath = customPath;
                        }
                    }

                    saveErrorsToFile(result.errors(), finalPath);
                }
            }

            System.out.println("\n Index Statistics:");
            System.out.println("   " + controller.getStatistics().toString());

        } catch (IOException e) {
            System.err.println(" Failed to load file: " + e.getMessage());
        }
    }

    private void queryByTimeZoneGroup() {
        System.out.println("\n═══ QUERY BY TIME ZONE GROUP ═══");

        if (!ensureDataLoaded()) return;

        showAvailableTimeZoneGroups();

        String tzGroup = validator.readString("\nEnter time zone group (e.g., CET, WET): ");

        System.out.println("\n Searching for stations in time zone group: " + tzGroup);

        List<Station> results = controller.queryByTimeZoneGroup(tzGroup);

        displayResults(results, "Time Zone Group: " + tzGroup);

        if (!results.isEmpty()) {
            offerToSaveResults(results, "tz_group_" + tzGroup);
        }
    }

    private void queryByTimeZoneGroupAndCountry() {
        System.out.println("\n═══ QUERY BY TIME ZONE GROUP + COUNTRY ═══");

        if (!ensureDataLoaded()) return;

        showAvailableTimeZoneGroups();

        String tzGroup = validator.readString("\nEnter time zone group: ");

        Set<String> countries = controller.getCountriesInTimeZoneGroup(tzGroup);
        if (countries.isEmpty()) {
            System.out.println(" No countries found for time zone group: " + tzGroup);
            return;
        }

        System.out.println("\nAvailable countries in " + tzGroup + ":");
        countries.forEach(c -> System.out.println("   • " + c));

        String country = validator.readString("\nEnter country code (e.g., PT, ES, FR): ");

        System.out.println("\n Searching for stations in " + tzGroup + " / " + country);

        List<Station> results = controller.queryByTimeZoneGroupAndCountry(tzGroup, country);

        displayResults(results, "Time Zone: " + tzGroup + " | Country: " + country);

        if (!results.isEmpty()) {
            offerToSaveResults(results, "tz_" + tzGroup + "_" + country);
        }
    }

    private void queryByTimeZoneWindow() {
        System.out.println("\n═══ QUERY BY TIME ZONE WINDOW ═══");

        if (!ensureDataLoaded()) return;

        showAvailableTimeZoneGroups();

        System.out.println("\nEnter time zone groups separated by commas");
        System.out.println("Example: CET,WET,EET");

        String input = validator.readString("\nTime zone groups: ");
        String[] tzGroups = input.split(",");

        // Trim whitespace
        for (int i = 0; i < tzGroups.length; i++) {
            tzGroups[i] = tzGroups[i].trim();
        }

        System.out.println("\n Searching for stations in window: " + String.join(", ", tzGroups));

        List<Station> results = controller.queryByTimeZoneWindow(tzGroups);

        displayResults(results, "Time Zone Window: [" + String.join(", ", tzGroups) + "]");

        if (!results.isEmpty()) {
            offerToSaveResults(results, "tz_window");
        }
    }

    private void queryByLatitudeRange() {
        System.out.println("\n═══ QUERY BY LATITUDE RANGE ═══");

        if (!ensureDataLoaded()) return;

        System.out.println("\nLatitude must be in range [-90, 90]");
        System.out.println("Example: Portugal is approximately between 37°N and 42°N");

        System.out.print("\nEnter minimum latitude: ");
        double minLat = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter maximum latitude: ");
        double maxLat = Double.parseDouble(scanner.nextLine().trim());

        System.out.printf("\n Searching for stations in latitude range [%.5f, %.5f]%n", minLat, maxLat);

        List<Station> results = controller.queryByLatitudeRange(minLat, maxLat);

        displayResults(results, String.format("Latitude Range: [%.5f, %.5f]", minLat, maxLat));

        if (!results.isEmpty()) {
            offerToSaveResults(results, "lat_range");
        }
    }

    private void queryByLongitudeRange() {
        System.out.println("\n═══ QUERY BY LONGITUDE RANGE ═══");

        if (!ensureDataLoaded()) return;

        System.out.println("\nLongitude must be in range [-180, 180]");
        System.out.println("Example: Portugal is approximately between -10°W and -6°W");

        System.out.print("\nEnter minimum longitude: ");
        double minLon = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter maximum longitude: ");
        double maxLon = Double.parseDouble(scanner.nextLine().trim());

        System.out.printf("\n Searching for stations in longitude range [%.5f, %.5f]%n", minLon, maxLon);

        List<Station> results = controller.queryByLongitudeRange(minLon, maxLon);

        displayResults(results, String.format("Longitude Range: [%.5f, %.5f]", minLon, maxLon));

        if (!results.isEmpty()) {
            offerToSaveResults(results, "lon_range");
        }
    }

    private void viewTimeZoneGroups() {
        System.out.println("\n═══ AVAILABLE TIME ZONE GROUPS ═══");

        if (!ensureDataLoaded()) return;

        Set<String> tzGroups = controller.getAllTimeZoneGroups();

        System.out.println("\nTotal time zone groups: " + tzGroups.size());
        System.out.println();

        int count = 0;
        for (String tzGroup : tzGroups) {
            Set<String> countries = controller.getCountriesInTimeZoneGroup(tzGroup);
            System.out.printf("%-15s - %d countries%n", tzGroup, countries.size());
            count++;

            if (count >= 20 && tzGroups.size() > 20) {
                System.out.println("... (" + (tzGroups.size() - 20) + " more groups)");
                break;
            }
        }
    }

    private void viewComplexityAnalysis() {
        System.out.println(controller.getComplexityAnalysis());
    }

    private void displayResults(List<Station> results, String queryDescription) {
        System.out.println("\n" + "═".repeat(90));
        System.out.println("QUERY RESULTS: " + queryDescription);
        System.out.println("═".repeat(90));

        if (results.isEmpty()) {
            System.out.println("\n No stations found.");
            return;
        }

        System.out.println("\nTotal results: " + results.size());
        System.out.println();
        System.out.println("─".repeat(90));
        System.out.printf("%-40s | %-10s | %-10s | %-12s | %-8s%n",
                "Station", "Latitude", "Longitude", "Country", "TZ Group");
        System.out.println("─".repeat(90));

        int shown = 0;
        for (Station station : results) {
            if (shown++ < 50) {
                System.out.printf("%-40s | %10.5f | %10.5f | %-12s | %-8s%n",
                        truncate(station.getName(), 40),
                        station.getLatitude(),
                        station.getLongitude(),
                        station.getCountry(),
                        station.getTimeZoneGroup());
            } else {
                System.out.println("... (" + (results.size() - 50) + " more stations)");
                break;
            }
        }
        System.out.println("─".repeat(90));
    }

    private void showAvailableTimeZoneGroups() {
        Set<String> tzGroups = controller.getAllTimeZoneGroups();
        System.out.println("\nAvailable time zone groups (" + tzGroups.size() + " total):");

        int count = 0;
        for (String tzGroup : tzGroups) {
            System.out.print(String.format("%-12s", tzGroup));
            if (++count % 6 == 0) System.out.println();
        }
        if (count % 6 != 0) System.out.println();
    }

    private void offerToSaveResults(List<Station> results, String suffix) {
        boolean save = validator.readConfirmation("\nSave results to CSV?");
        if (save) {
            saveResultsToFile(results, suffix);
        }
    }

    private void saveResultsToFile(List<Station> results, String suffix) {
        try {
            String filename = "Logs/stations_" + suffix + "_" + System.currentTimeMillis() + ".csv";

            try (FileWriter writer = new FileWriter(filename)) {
                writer.write("station,latitude,longitude,country,timeZone,timeZoneGroup,isCity,isMainStation,isAirport\n");

                for (Station s : results) {
                    writer.write(String.format("%s,%.5f,%.5f,%s,%s,%s,%b,%b,%b\n",
                            escapeCsv(s.getName()),
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

            System.out.println(" Results saved to: " + filename);

        } catch (IOException e) {
            System.err.println(" Error saving file: " + e.getMessage());
        }
    }

    private void saveErrorsToFile(List<String> errors, String filename) {
        try {
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write("Station Import Errors\n");
                writer.write("=".repeat(80) + "\n\n");

                for (String error : errors) {
                    writer.write(error + "\n");
                }
            }

            System.out.println(" Errors saved to: " + filename);

        } catch (IOException e) {
            System.err.println(" Error saving errors file: " + e.getMessage());
        }
    }

    private boolean ensureDataLoaded() {
        if (!controller.isDataLoaded()) {
            System.out.println("\n No station data loaded!");
            System.out.println("Please import station data first (Option 1).");
            return false;
        }
        return true;
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
