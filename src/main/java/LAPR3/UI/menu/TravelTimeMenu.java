package LAPR3.UI.menu;

import LAPR3.DTO.TravelTimeResult;
import LAPR3.UI.utils.InputValidator;
import LAPR3.UI.utils.TableFormatter;
import LAPR3.model.Facility;
import LAPR3.model.Line;
import LAPR3.model.Locomotive;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.LineRepository;
import LAPR3.repository.LocomotiveRepository;
import LAPR3.repository.SegmentRepository;
import LAPR3.service.TravelTimeService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * Menu for Travel Time Calculation (USLP03)
 */
public class TravelTimeMenu {

    private final Scanner scanner;
    private final InputValidator validator;
    private final TableFormatter formatter;

    // Repositories
    private final FacilityRepository facilityRepository;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;
    private final LocomotiveRepository locomotiveRepository;

    // Service
    private final TravelTimeService travelTimeService;

    // Data loaded flag
    private boolean dataLoaded = false;

    // Default CSV paths
    private static final String DEFAULT_FACILITIES = "Dataset/facilities.csv";
    private static final String DEFAULT_LINES = "Dataset/lines.csv";
    private static final String DEFAULT_SEGMENTS = "Dataset/segments.csv";
    private static final String DEFAULT_LOCOMOTIVES = "Dataset/locomotives.csv";

    public TravelTimeMenu(Scanner scanner, InputValidator validator) {
        this.scanner = scanner;
        this.validator = validator;
        this.formatter = new TableFormatter();

        // Initialize repositories
        this.facilityRepository = new FacilityRepository();
        this.lineRepository = new LineRepository();
        this.segmentRepository = new SegmentRepository();
        this.locomotiveRepository = new LocomotiveRepository();

        // Initialize service
        this.travelTimeService = new TravelTimeService(
                facilityRepository,
                lineRepository,
                segmentRepository,
                locomotiveRepository
        );
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 6);

            if (choice == 0) {
                return; // Back to main menu
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║       TRAVEL TIME CALCULATION (USLP03)           ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // Show data status
        if (dataLoaded) {
            System.out.println("   Data Status: ✓ Loaded");
            System.out.println("   • Facilities: " + facilityRepository.count());
            System.out.println("   • Lines: " + lineRepository.count());
            System.out.println("   • Segments: " + segmentRepository.count());
            System.out.println("   • Locomotives: " + locomotiveRepository.count());
        } else {
            System.out.println("⚠️  Data Status: Not loaded - Please load data first (Option 1)");
        }
        System.out.println();

        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Load Railway Network Data                  │");
        System.out.println("│  2. Calculate Travel Time                      │");
        System.out.println("│  3. List Facilities                             │");
        System.out.println("│  4. List Railway Lines                          │");
        System.out.println("│  5. List Locomotives                            │");
        System.out.println("│  6. View Data Summary                           │");
        System.out.println("│  0. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) throws IOException {
        switch (choice) {
            case 1 -> loadRailwayData();
            case 2 -> calculateTravelTime();
            case 3 -> listFacilities();
            case 4 -> listLines();
            case 5 -> listLocomotives();
            case 6 -> viewDataSummary();
        }
    }

    private void loadRailwayData() throws IOException {
        System.out.println("\n═══ LOAD RAILWAY NETWORK DATA ═══");

        String facilitiesPath = validator.readFilePath("Facilities CSV path", DEFAULT_FACILITIES);
        String linesPath = validator.readFilePath("Lines CSV path", DEFAULT_LINES);
        String segmentsPath = validator.readFilePath("Segments CSV path", DEFAULT_SEGMENTS);
        String locomotivesPath = validator.readFilePath("Locomotives CSV path", DEFAULT_LOCOMOTIVES);

        System.out.println("\n⏳ Loading railway network data...\n");

        facilityRepository.loadFromCSV(facilitiesPath);
        lineRepository.loadFromCSV(linesPath);
        segmentRepository.loadFromCSV(segmentsPath);
        locomotiveRepository.loadFromCSV(locomotivesPath);

        dataLoaded = true;

        System.out.println("\n✓ Railway network data loaded successfully!");
        System.out.println("  • Facilities: " + facilityRepository.count());
        System.out.println("  • Lines: " + lineRepository.count());
        System.out.println("  • Segments: " + segmentRepository.count());
        System.out.println("  • Locomotives: " + locomotiveRepository.count());
    }

    private void calculateTravelTime() {
        System.out.println("\n═══ CALCULATE TRAVEL TIME ═══");

        if (!dataLoaded) {
            System.out.println("Railway data not loaded!");
            System.out.println("Please load data first (Option 1).");
            return;
        }

        try {
            // Select origin
            System.out.println("\n--- SELECT ORIGIN FACILITY ---");
            int originId = selectFacility();
            if (originId < 0) return;

            // Select destination
            System.out.println("\n--- SELECT DESTINATION FACILITY ---");
            int destinationId = selectFacility();
            if (destinationId < 0) return;

            // Select locomotive
            System.out.println("\n--- SELECT LOCOMOTIVE ---");
            int locomotiveNumber = selectLocomotive();
            if (locomotiveNumber < 0) return;

            // Calculate
            System.out.println("\n⏳ Calculating travel time...\n");
            TravelTimeResult result = travelTimeService.calculateTravelTime(
                    originId, destinationId, locomotiveNumber);

            // Display result
            System.out.println(result.generateReport());

            // Ask to save
            if (validator.readConfirmation("\nSave report to file?")) {
                saveReport(result);
            }

        } catch (IllegalArgumentException e) {
            System.err.println("\nError: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\nUnexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int selectFacility() {
        List<Facility> facilities = facilityRepository.findAll();

        if (facilities.isEmpty()) {
            System.out.println("No facilities available!");
            return -1;
        }

        // Prepare data for table
        String[] headers = {"ID", "Name"};
        List<String[]> rows = facilities.stream()
                .map(f -> new String[]{String.valueOf(f.getId()), f.getName()})
                .toList();

        System.out.println("\nAvailable Facilities:");
        formatter.printTable(headers, rows);

        return validator.readInt("\nEnter Facility ID (0 to cancel): ", 0, Integer.MAX_VALUE);
    }

    private int selectLocomotive() {
        List<Locomotive> locomotives = locomotiveRepository.findAll();

        if (locomotives.isEmpty()) {
            System.out.println("No locomotives available!");
            return -1;
        }

        // Prepare data for table
        String[] headers = {"Number", "Name", "Type", "Speed (km/h)", "Gauge (mm)"};
        List<String[]> rows = locomotives.stream()
                .map(l -> new String[]{
                        String.valueOf(l.getNumber()),
                        l.getName(),
                        l.getType().toString(),
                        String.format("%.1f", l.getOperationalSpeedKmh()),
                        String.valueOf(l.getGauge())
                })
                .toList();

        System.out.println("\nAvailable Locomotives:");
        formatter.printTable(headers, rows);

        return validator.readInt("\nEnter Locomotive Number (0 to cancel): ", 0, Integer.MAX_VALUE);
    }

    private void listFacilities() {
        System.out.println("\n═══ ALL FACILITIES ═══\n");

        if (!dataLoaded) {
            System.out.println("Data not loaded!");
            return;
        }

        List<Facility> facilities = facilityRepository.findAll();

        if (facilities.isEmpty()) {
            System.out.println("No facilities found.");
            return;
        }

        String[] headers = {"ID", "Name"};
        List<String[]> rows = facilities.stream()
                .map(f -> new String[]{String.valueOf(f.getId()), f.getName()})
                .toList();

        System.out.println("Total: " + facilities.size() + " facilities\n");
        formatter.printTable(headers, rows);
    }

    private void listLines() {
        System.out.println("\n═══ ALL RAILWAY LINES ═══\n");

        if (!dataLoaded) {
            System.out.println("Data not loaded!");
            return;
        }

        List<Line> lines = lineRepository.findAll();

        if (lines.isEmpty()) {
            System.out.println("No lines found.");
            return;
        }

        System.out.println("Total: " + lines.size() + " lines\n");

        for (Line line : lines) {
            System.out.println("┌────────────────────────────────────────────────────────┐");
            System.out.printf("│ Line %d: %-45s │%n", line.getId(), line.getName());
            System.out.println("├────────────────────────────────────────────────────────┤");
            System.out.printf("│ From: %-48s │%n", line.getStartFacilityName());
            System.out.printf("│ To:   %-48s │%n", line.getEndFacilityName());
            System.out.printf("│ Owner: %-12s  │  Gauge: %4d mm              │%n",
                    line.getOwner(), line.getGauge());
            System.out.println("└────────────────────────────────────────────────────────┘");
            System.out.println();
        }
    }

    private void listLocomotives() {
        System.out.println("\n═══ ALL LOCOMOTIVES ═══\n");

        if (!dataLoaded) {
            System.out.println("Data not loaded!");
            return;
        }

        List<Locomotive> locomotives = locomotiveRepository.findAll();

        if (locomotives.isEmpty()) {
            System.out.println("No locomotives found.");
            return;
        }

        System.out.println("Total: " + locomotives.size() + " locomotives\n");

        for (Locomotive loco : locomotives) {
            System.out.println("┌────────────────────────────────────────────────────────┐");
            System.out.printf("│ #%-4d %-48s │%n", loco.getNumber(), loco.getName());
            System.out.println("├────────────────────────────────────────────────────────┤");
            System.out.printf("│ Make/Model: %-25s %-15s │%n",
                    loco.getMake(), loco.getModel());
            System.out.printf("│ Type: %-11s  │  Power: %6.0f kW  │  Year: %4d │%n",
                    loco.getType(), loco.getPowerKw(), loco.getServiceYear());
            System.out.printf("│ Max Speed: %6.1f km/h  │  Operational: %6.1f km/h  │%n",
                    loco.getMaxSpeedKmh(), loco.getOperationalSpeedKmh());
            System.out.printf("│ Gauge: %4d mm  │  Weight: %5.1f t  │  Operator: %-6s │%n",
                    loco.getGauge(), loco.getWeightTons(), loco.getOperator());
            System.out.println("└────────────────────────────────────────────────────────┘");
            System.out.println();
        }
    }

    private void viewDataSummary() {
        System.out.println("\n═══ RAILWAY NETWORK DATA SUMMARY ═══\n");

        if (!dataLoaded) {
            System.out.println("No data loaded yet!");
            System.out.println("Please load data first (Option 1).");
            return;
        }

        System.out.println("📊 Data Statistics:");
        System.out.println("─".repeat(50));
        System.out.printf("  Facilities:  %3d stations/terminals%n", facilityRepository.count());
        System.out.printf("  Lines:       %3d railway lines%n", lineRepository.count());
        System.out.printf("  Segments:    %3d track segments%n", segmentRepository.count());
        System.out.printf("  Locomotives: %3d units%n", locomotiveRepository.count());
        System.out.println("─".repeat(50));

        // Network stats
        double totalDistance = segmentRepository.findAll().stream()
                .mapToDouble(s -> s.getLengthMeters() / 1000.0)
                .sum();

        boolean allElectrified = segmentRepository.findAll().stream()
                .allMatch(s -> s.isElectrified());

        int electricLocos = locomotiveRepository.findByType(Locomotive.LocomotiveType.ELECTRIC).size();
        int dieselLocos = locomotiveRepository.findByType(Locomotive.LocomotiveType.DIESEL).size();

        System.out.println("\n🌐 Network Statistics:");
        System.out.println("─".repeat(50));
        System.out.printf("  Total Track Length:     %.2f km%n", totalDistance);
        System.out.printf("  Electrification:        %s%n", allElectrified ? "100%" : "Partial");
        System.out.printf("  Electric Locomotives:   %d%n", electricLocos);
        System.out.printf("  Diesel Locomotives:     %d%n", dieselLocos);
        System.out.println("─".repeat(50));
    }

    private void saveReport(TravelTimeResult result) {
        try {
            String filename = String.format("travel_report_%d_to_%d_%d.txt",
                    result.getOriginFacilityName().hashCode(),
                    result.getDestinationFacilityName().hashCode(),
                    System.currentTimeMillis());

            PrintWriter writer = new PrintWriter(filename);
            writer.println(result.generateReport());
            writer.close();

            System.out.println("Report saved to: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
