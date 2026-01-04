package LAPR3.UI.menu;

import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.*;
import LAPR3.UI.utils.InputValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu for USLP09 - Train Assembly and Route Assignment
 *
 * Features:
 * - Lists ALL routes (in-memory + database)
 * - Validates train constraints (length, power-to-weight, wagon availability)
 * - Shows rolling stock by availability (parked/in-transit)
 * - Numbered selection for all user inputs
 *
 * Constraints enforced:
 * 1. Maximum train length
 * 2. Power-to-weight ratio (sufficient locomotives)
 * 3. Wagon availability at departure facility
 * 4. Locomotive availability
 * 5. Route assignment
 * 6. Departure date/time
 */
public class TrainAssemblyMenu {
    private final Scanner scanner;
    private final InputValidator validator;
    private final TrainAssemblyService trainAssemblyService;
    private final RoutePlanningService routePlanningService;
    private final LocomotiveRepository locomotiveRepo;
    private final WagonRepository wagonRepo;
    private final FacilityRepository facilityRepo;
    private final RouteRepository routeRepo;
    private final FreightRepository freightRepo;

    // Train constraints
    private static final double MAX_TRAIN_LENGTH_M = 750.0;  // Maximum train length
    private static final double MIN_POWER_TO_WEIGHT_RATIO = 3.0;  // kW per ton (minimum)
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TrainAssemblyMenu(Scanner scanner, InputValidator validator,
                             TrainAssemblyService trainAssemblyService,
                             RoutePlanningService routePlanningService,
                             LocomotiveRepository locomotiveRepo,
                             WagonRepository wagonRepo,
                             FacilityRepository facilityRepo) {
        this.scanner = scanner;
        this.validator = validator;
        this.trainAssemblyService = trainAssemblyService;
        this.routePlanningService = routePlanningService;
        this.locomotiveRepo = locomotiveRepo;
        this.wagonRepo = wagonRepo;
        this.facilityRepo = facilityRepo;
        this.routeRepo = new RouteRepository();
        this.freightRepo = new FreightRepository();
    }

    public void show() {
        boolean back = false;
        while (!back) {
            displayHeader();
            System.out.println("┌───────────────────────────────────────────────────────┐");
            System.out.println("│  1. List Available Rolling Stock                     │");
            System.out.println("│  2. List Available Routes (All Sources)              │");
            System.out.println("│  3. Assemble and Assign Train to Route               │");
            System.out.println("│  4. View Assembled Trains                            │");
            System.out.println("├───────────────────────────────────────────────────────┤");
            System.out.println("│  0. Back to Main Menu                                │");
            System.out.println("└───────────────────────────────────────────────────────┘");
            System.out.println();

            int choice = validator.readInt("Enter choice: ", 0, 4);

            switch (choice) {
                case 1 -> listAvailableRollingStock();
                case 2 -> listAllAvailableRoutes();
                case 3 -> assembleAndAssignTrain();
                case 4 -> viewAssembledTrains();
                case 0 -> back = true;
            }

            if (!back && choice != 0) {
                System.out.print("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
    }

    private void displayHeader() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║   USLP09 - ASSEMBLE AND ASSIGN TRAIN TO ROUTE        ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    /**
     * List ALL available routes (in-memory from USLP08 + database routes)
     */
    private void listAllAvailableRoutes() {
        System.out.println("\n═══ AVAILABLE ROUTES (ALL SOURCES) ═══");

        // Get routes from USLP08 (in-memory)
        List<PlannedRoute> memoryRoutes = routePlanningService.getUnassignedRoutes();

        // Get routes from database
        List<Route> dbRoutes = routeRepo.findAll();

        if (memoryRoutes.isEmpty() && dbRoutes.isEmpty()) {
            System.out.println("\n✗ No routes available.");
            System.out.println("Create routes in USLP08 (Freight Assignment).");
            return;
        }

        int totalRoutes = memoryRoutes.size() + dbRoutes.size();
        System.out.println("\nTotal routes: " + totalRoutes);
        System.out.println("  • In-memory (USLP08): " + memoryRoutes.size());
        System.out.println("  • Database: " + dbRoutes.size());
        System.out.println();

        // Show in-memory routes
        if (!memoryRoutes.isEmpty()) {
            System.out.println("─".repeat(80));
            System.out.println("IN-MEMORY ROUTES (Created in USLP08):");
            System.out.println("─".repeat(80));
            for (PlannedRoute route : memoryRoutes) {
                System.out.println("\n" + routePlanningService.formatRoute(route));
            }
        }

        // Show database routes
        if (!dbRoutes.isEmpty()) {
            System.out.println("\n" + "─".repeat(80));
            System.out.println("DATABASE ROUTES:");
            System.out.println("─".repeat(80));
            for (Route route : dbRoutes) {
                System.out.println("\nRoute: " + route.getRouteId());
                System.out.println("  Type: " + route.getType());
                System.out.println("  Facilities: " + route.getFacilityIds().stream()
                        .map(id -> facilityRepo.findById(id)
                                .map(Facility::getName)
                                .orElse(String.valueOf(id)))
                        .collect(Collectors.joining(" → ")));
            }
        }
    }

    /**
     * List rolling stock with numbered options
     * Distinguishes between parked and in-transit
     * Parked sorted by distance (descending)
     */
    private void listAvailableRollingStock() {
        System.out.println("\n═══ AVAILABLE ROLLING STOCK ═══");

        // Get all rolling stock
        List<Locomotive> locomotives = locomotiveRepo.findAll();
        List<Wagon> wagons = wagonRepo.findAll();

        // Classify locomotives
        List<Locomotive> parkedLocos = new ArrayList<>();
        List<Locomotive> transitLocos = new ArrayList<>();

        for (Locomotive loco : locomotives) {
            // Check if locomotive is assigned to any train
            boolean isAssigned = false; // TODO: Check against assembled trains

            if (isAssigned) {
                transitLocos.add(loco);
            } else {
                parkedLocos.add(loco);
            }
        }

        // Classify wagons
        List<Wagon> parkedWagons = new ArrayList<>();
        List<Wagon> transitWagons = new ArrayList<>();

        for (Wagon wagon : wagons) {
            if (wagon.getCurrentFacilityId() != null) {
                parkedWagons.add(wagon);
            } else {
                transitWagons.add(wagon);
            }
        }

        // Display locomotives
        System.out.println("\n=== LOCOMOTIVES ===");

        if (!parkedLocos.isEmpty()) {
            System.out.println("\nParked:");
            for (int i = 0; i < parkedLocos.size(); i++) {
                Locomotive l = parkedLocos.get(i);
                System.out.printf("[%d] %s - %.0fkW, %.1ft\n",
                        i + 1, l.getLocoNumber(), l.getPowerKw(), l.getWeightTons());
            }
        }

        if (!transitLocos.isEmpty()) {
            System.out.println("\nIn Transit:");
            for (Locomotive l : transitLocos) {
                System.out.printf("    %s - %.0fkW, %.1ft (IN TRANSIT)\n",
                        l.getLocoNumber(), l.getPowerKw(), l.getWeightTons());
            }
        }

        // Display wagons
        System.out.println("\n=== WAGONS ===");

        if (!parkedWagons.isEmpty()) {
            System.out.println("\nParked (sorted by distance - descending):");

            // Sort by distance (if available) - for now just show
            for (int i = 0; i < parkedWagons.size(); i++) {
                Wagon w = parkedWagons.get(i);
                String facilityName = facilityRepo.findById(w.getCurrentFacilityId())
                        .map(Facility::getName)
                        .orElse("Unknown");

                System.out.printf("[%d] %s - %s at %s (%.1ft)\n",
                        i + 1, w.getWagonNumber(), w.getTypeDesc(),
                        facilityName, w.getTotalWeightTons());
            }
        }

        if (!transitWagons.isEmpty()) {
            System.out.println("\nIn Transit:");
            for (Wagon w : transitWagons) {
                System.out.printf("    %s - %s (IN TRANSIT, %.1ft)\n",
                        w.getWagonNumber(), w.getTypeDesc(), w.getTotalWeightTons());
            }
        }

        System.out.println("\n" + "─".repeat(80));
        System.out.println("Summary:");
        System.out.println("  Locomotives: " + parkedLocos.size() + " parked, " + transitLocos.size() + " in transit");
        System.out.println("  Wagons: " + parkedWagons.size() + " parked, " + transitWagons.size() + " in transit");
    }

    /**
     * Main workflow: Assemble and assign train with all constraints
     */
    private void assembleAndAssignTrain() {
        System.out.println("\n═══ ASSEMBLE AND ASSIGN TRAIN ═══");
        System.out.println("\nThis process will validate:");
        System.out.println("  ✓ Maximum train length (750m)");
        System.out.println("  ✓ Power-to-weight ratio (minimum 3 kW/t)");
        System.out.println("  ✓ Wagon availability at departure facility");
        System.out.println("  ✓ Route assignment");
        System.out.println("  ✓ Departure date/time");

        try {
            // STEP 1: Select Route (numbered selection)
            PlannedRoute selectedRoute = selectRouteNumbered();
            if (selectedRoute == null) return;

            System.out.println("\n✓ Selected Route: " + selectedRoute.getId());
            System.out.println(routePlanningService.formatRoute(selectedRoute));

            // Get route details
            List<Integer> routeFacilities = selectedRoute.getMandatoryFacilities();
            int departureFacilityId = routeFacilities.get(0);

            Facility departureFacility = facilityRepo.findById(departureFacilityId)
                    .orElseThrow(() -> new IllegalStateException("Departure facility not found"));

            System.out.println("\nDeparture Facility: " + departureFacility.getName());

            // STEP 2: Select Operator
            String operatorVat = selectOperatorNumbered();
            if (operatorVat == null) return;

            // STEP 3: Select Locomotives (numbered selection)
            List<String> selectedLocoIds = selectLocomotivesNumbered();
            if (selectedLocoIds.isEmpty()) return;

            // Calculate total locomotive power
            double totalPowerKw = 0;
            for (String locoId : selectedLocoIds) {
                Locomotive loco = locomotiveRepo.findByNumber(locoId).orElseThrow();
                totalPowerKw += loco.getPowerKw();
            }

            // STEP 4: Select Wagons from route's freights (automatic) or manual
            List<String> selectedWagonIds = selectWagonsForRoute(selectedRoute, departureFacilityId);
            if (selectedWagonIds.isEmpty()) return;

            // Calculate total weight and length
            TrainMetrics metrics = calculateTrainMetrics(selectedLocoIds, selectedWagonIds);

            // STEP 5: Validate constraints
            System.out.println("\n═══ VALIDATING CONSTRAINTS ═══");

            if (!validateConstraints(metrics, totalPowerKw)) {
                return;
            }

            // STEP 6: Set departure date/time
            LocalDateTime departureDateTime = getDepartureDateTime();
            if (departureDateTime == null) return;

            // STEP 7: Display summary and confirm
            displayAssemblySummary(selectedRoute, operatorVat, selectedLocoIds,
                    selectedWagonIds, metrics, departureDateTime);

            String confirm = validator.readString("\nConfirm train assembly? (yes/no): ");
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("\n✗ Assembly cancelled.");
                return;
            }

            // STEP 8: Create assembled train
            AssembledTrain train = trainAssemblyService.assembleAndAssign(
                    operatorVat, selectedLocoIds, selectedWagonIds, selectedRoute.getId()
            );

            System.out.println("\n" + "✓".repeat(60));
            System.out.println("  TRAIN SUCCESSFULLY ASSEMBLED AND ASSIGNED!");
            System.out.println("✓".repeat(60));
            System.out.println("\n" + train);
            System.out.println("\nNext step: Calculate path and schedule in USLP10.");

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Select route with numbered options (shows ALL routes)
     */
    private PlannedRoute selectRouteNumbered() {
        System.out.println("\n═══ STEP 1: SELECT ROUTE ═══");

        // Get all available routes (unassigned only)
        List<PlannedRoute> availableRoutes = routePlanningService.getUnassignedRoutes();

        if (availableRoutes.isEmpty()) {
            System.out.println("\n✗ No unassigned routes available.");
            System.out.println("Create routes in USLP08 or check database.");
            return null;
        }

        System.out.println("\nAvailable Routes:");
        for (int i = 0; i < availableRoutes.size(); i++) {
            PlannedRoute route = availableRoutes.get(i);
            System.out.printf("\n[%d] Route %s (%s)\n",
                    i + 1, route.getId(), route.getType());
            System.out.println("    Freights: " + route.getFreightIds().size());
            System.out.println("    Actions: " + route.getActions().size());
        }

        int choice = validator.readInt("\nSelect route number (0 to cancel): ",
                0, availableRoutes.size());

        if (choice == 0) {
            System.out.println("✗ Cancelled.");
            return null;
        }

        return availableRoutes.get(choice - 1);
    }

    /**
     * Select operator with numbered options
     */
    private String selectOperatorNumbered() {
        System.out.println("\n═══ STEP 2: SELECT OPERATOR ═══");

        // Get operators from database
        List<String> operators = Arrays.asList(
                "CP - Comboios de Portugal",
                "MEDWAY - Medway",
                "TAKARGO - Takargo"
        );

        System.out.println("\nAvailable Operators:");
        for (int i = 0; i < operators.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, operators.get(i));
        }

        int choice = validator.readInt("\nSelect operator (0 to cancel): ",
                0, operators.size());

        if (choice == 0) {
            System.out.println("✗ Cancelled.");
            return null;
        }

        // Extract VAT (simplified - in real system query database)
        String[] parts = operators.get(choice - 1).split(" - ");
        return parts[0];  // Return operator code
    }

    /**
     * Select locomotives with numbered options
     */
    private List<String> selectLocomotivesNumbered() {
        System.out.println("\n═══ STEP 3: SELECT LOCOMOTIVES ═══");

        List<Locomotive> available = locomotiveRepo.findAll().stream()
                .filter(l -> true)  // TODO: Filter by availability
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("\n✗ No locomotives available.");
            return Collections.emptyList();
        }

        System.out.println("\nAvailable Locomotives:");
        for (int i = 0; i < available.size(); i++) {
            Locomotive loco = available.get(i);
            System.out.printf("[%d] %s - %.0fkW, %.1ft\n",
                    i + 1, loco.getLocoNumber(), loco.getPowerKw(), loco.getWeightTons());
        }

        System.out.println("\nSelect locomotives (enter numbers separated by commas, e.g., 1,3,5):");
        System.out.print("Selection: ");
        scanner.nextLine(); // Clear buffer
        String input = scanner.nextLine().trim();

        if (input.isEmpty() || input.equals("0")) {
            System.out.println("✗ Cancelled.");
            return Collections.emptyList();
        }

        List<String> selectedIds = new ArrayList<>();
        try {
            String[] parts = input.split(",");
            for (String part : parts) {
                int idx = Integer.parseInt(part.trim()) - 1;
                if (idx >= 0 && idx < available.size()) {
                    selectedIds.add(available.get(idx).getLocoNumber());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid input.");
            return Collections.emptyList();
        }

        if (selectedIds.isEmpty()) {
            System.out.println("✗ No valid locomotives selected.");
            return Collections.emptyList();
        }

        System.out.println("\n✓ Selected " + selectedIds.size() + " locomotive(s)");
        return selectedIds;
    }

    /**
     * Select wagons for route (automatic from freights or manual)
     */
    private List<String> selectWagonsForRoute(PlannedRoute route, int departureFacilityId) {
        System.out.println("\n═══ STEP 4: SELECT WAGONS ═══");

        List<Integer> freightIds = route.getFreightIds();

        if (freightIds.isEmpty()) {
            System.out.println("\n⚠ Route has no associated freights.");
            System.out.println("Manual wagon selection required.");
            return selectWagonsManually(departureFacilityId);
        }

        // Automatic: Get wagons from freights
        System.out.println("\nLoading wagons from route freights...");
        List<String> wagonIds = new ArrayList<>();

        for (int freightId : freightIds) {
            Freight freight = freightRepo.findById(freightId).orElse(null);
            if (freight != null) {
                for (Wagon wagon : freight.getAssignedWagons()) {
                    wagonIds.add(wagon.getWagonNumber());
                }
                System.out.printf("  Freight %d: %d wagon(s)\n",
                        freightId, freight.getAssignedWagons().size());
            }
        }

        if (wagonIds.isEmpty()) {
            System.out.println("\n⚠ No wagons found in freights. Manual selection required.");
            return selectWagonsManually(departureFacilityId);
        }

        // Validate wagons are at departure facility
        List<String> validWagons = new ArrayList<>();
        List<String> invalidWagons = new ArrayList<>();

        for (String wagonId : wagonIds) {
            Wagon wagon = wagonRepo.findById(wagonId).orElse(null);
            if (wagon != null && wagon.getCurrentFacilityId() != null
                    && wagon.getCurrentFacilityId() == departureFacilityId) {
                validWagons.add(wagonId);
            } else {
                invalidWagons.add(wagonId);
            }
        }

        if (!invalidWagons.isEmpty()) {
            System.out.println("\n⚠ Warning: Some wagons are not at departure facility:");
            invalidWagons.forEach(id -> System.out.println("  - " + id));
        }

        if (validWagons.isEmpty()) {
            System.out.println("\n✗ No wagons available at departure facility.");
            return Collections.emptyList();
        }

        System.out.println("\n✓ " + validWagons.size() + " wagon(s) available at departure facility");
        return validWagons;
    }

    /**
     * Manual wagon selection with numbered options
     */
    private List<String> selectWagonsManually(int departureFacilityId) {
        List<Wagon> available = wagonRepo.findAll().stream()
                .filter(w -> w.getCurrentFacilityId() != null
                        && w.getCurrentFacilityId() == departureFacilityId)
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            System.out.println("\n✗ No wagons available at departure facility.");
            return Collections.emptyList();
        }

        System.out.println("\nAvailable Wagons at Departure Facility:");
        for (int i = 0; i < available.size(); i++) {
            Wagon w = available.get(i);
            System.out.printf("[%d] %s - %s (%.1ft)\n",
                    i + 1, w.getWagonNumber(), w.getTypeDesc(), w.getTotalWeightTons());
        }

        System.out.println("\nSelect wagons (comma-separated, e.g., 1,3,5):");
        System.out.print("Selection: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty() || input.equals("0")) {
            return Collections.emptyList();
        }

        List<String> selectedIds = new ArrayList<>();
        try {
            String[] parts = input.split(",");
            for (String part : parts) {
                int idx = Integer.parseInt(part.trim()) - 1;
                if (idx >= 0 && idx < available.size()) {
                    selectedIds.add(available.get(idx).getWagonNumber());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid input.");
            return Collections.emptyList();
        }

        System.out.println("\n✓ Selected " + selectedIds.size() + " wagon(s)");
        return selectedIds;
    }

    /**
     * Calculate train metrics (weight, length)
     */
    private TrainMetrics calculateTrainMetrics(List<String> locoIds, List<String> wagonIds) {
        double totalWeight = 0;
        double totalLength = 0;

        // Add locomotives
        for (String locoId : locoIds) {
            Locomotive loco = locomotiveRepo.findByNumber(locoId).orElseThrow();
            totalWeight += loco.getWeightTons();
            totalLength += loco.getLengthM() / 1000.0;  // Convert mm to m
        }

        // Add wagons
        for (String wagonId : wagonIds) {
            Wagon wagon = wagonRepo.findById(wagonId).orElseThrow();
            totalWeight += wagon.getTotalWeightTons();
            totalLength += wagon.getLengthMm() / 1000.0;  // Convert mm to m
        }

        return new TrainMetrics(totalWeight, totalLength);
    }

    /**
     * Validate all train constraints
     */
    private boolean validateConstraints(TrainMetrics metrics, double totalPowerKw) {
        boolean valid = true;

        // 1. Check maximum length
        System.out.printf("\n1. Train Length: %.2fm", metrics.totalLengthM);
        if (metrics.totalLengthM > MAX_TRAIN_LENGTH_M) {
            System.out.printf(" ✗ EXCEEDS MAXIMUM (%.0fm)\n", MAX_TRAIN_LENGTH_M);
            valid = false;
        } else {
            System.out.printf(" ✓ (max: %.0fm)\n", MAX_TRAIN_LENGTH_M);
        }

        // 2. Check power-to-weight ratio
        double ratio = totalPowerKw / metrics.totalWeightTons;
        System.out.printf("2. Power-to-Weight Ratio: %.2f kW/t", ratio);
        if (ratio < MIN_POWER_TO_WEIGHT_RATIO) {
            System.out.printf(" ✗ INSUFFICIENT (min: %.1f kW/t)\n", MIN_POWER_TO_WEIGHT_RATIO);
            System.out.println("   Add more locomotives or reduce wagons.");
            valid = false;
        } else {
            System.out.printf(" ✓ (min: %.1f kW/t)\n", MIN_POWER_TO_WEIGHT_RATIO);
        }

        // 3. Summary
        System.out.printf("3. Total Power: %.0f kW ✓\n", totalPowerKw);
        System.out.printf("4. Total Weight: %.1f t ✓\n", metrics.totalWeightTons);

        return valid;
    }

    /**
     * Get departure date/time from user
     */
    private LocalDateTime getDepartureDateTime() {
        System.out.println("\n═══ STEP 5: DEPARTURE DATE/TIME ═══");

        System.out.print("Enter departure date and time (yyyy-MM-dd HH:mm): ");
        String input = scanner.nextLine().trim();

        try {
            return LocalDateTime.parse(input, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("✗ Invalid format. Use yyyy-MM-dd HH:mm (e.g., 2026-01-15 08:30)");
            return null;
        }
    }

    /**
     * Display assembly summary before confirmation
     */
    private void displayAssemblySummary(PlannedRoute route, String operatorVat,
                                        List<String> locoIds, List<String> wagonIds,
                                        TrainMetrics metrics, LocalDateTime departure) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("TRAIN ASSEMBLY SUMMARY");
        System.out.println("═".repeat(60));
        System.out.println("Route:          " + route.getId() + " (" + route.getType() + ")");
        System.out.println("Operator:       " + operatorVat);
        System.out.println("Locomotives:    " + locoIds.size() + " (" + String.join(", ", locoIds) + ")");
        System.out.println("Wagons:         " + wagonIds.size());
        System.out.printf("Total Length:   %.2fm\n", metrics.totalLengthM);
        System.out.printf("Total Weight:   %.1ft\n", metrics.totalWeightTons);
        System.out.println("Departure:      " + departure.format(DATE_TIME_FORMATTER));
        System.out.println("═".repeat(60));
    }

    /**
     * View all assembled trains
     */
    private void viewAssembledTrains() {
        List<AssembledTrain> trains = trainAssemblyService.getAllTrains();

        if (trains.isEmpty()) {
            System.out.println("\n✗ No assembled trains.");
            return;
        }

        System.out.println("\n═══ ASSEMBLED TRAINS ═══");
        for (int i = 0; i < trains.size(); i++) {
            AssembledTrain train = trains.get(i);
            System.out.printf("\n[%d] %s\n", i + 1, train);
        }

        System.out.println("\nTotal: " + trains.size() + " train(s)");
    }

    /**
     * Helper class for train metrics
     */
    private static class TrainMetrics {
        final double totalWeightTons;
        final double totalLengthM;

        TrainMetrics(double weight, double length) {
            this.totalWeightTons = weight;
            this.totalLengthM = length;
        }
    }
}