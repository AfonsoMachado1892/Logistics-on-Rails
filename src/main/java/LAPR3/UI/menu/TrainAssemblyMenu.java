package LAPR3.UI.menu;

import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.UI.utils.InputValidator;
import LAPR3.service.RoutePlanningService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu for USLP09 - Train Assembly and Route Assignment (FINAL VERSION)
 *
 * Features:
 * - Creates full TRAIN objects (compatible with USLP10)
 * - Validates GAUGE compatibility (Rolling Stock vs Infrastructure)
 * - Validates PHYSICS constraints (Power/Weight, Length)
 * - Persists to In-Memory Repositories (TrainRepo, ScheduleRepo)
 */
public class TrainAssemblyMenu {
    private final Scanner scanner;
    private final InputValidator validator;

    // Repositories
    private final LocomotiveRepository locomotiveRepo;
    private final WagonRepository wagonRepo;
    private final FacilityRepository facilityRepo;
    private final TrainRepository trainRepo;       // Adicionado
    private final ScheduleRepository scheduleRepo; // Adicionado
    private final LineRepository lineRepo;         // Adicionado para validar Gauge
    private final RouteRepository routeRepo;
    private final FreightRepository freightRepo;

    // Services
    private final RoutePlanningService routePlanningService;

    // Train constraints
    private static final double MAX_TRAIN_LENGTH_M = 750.0;
    private static final double MIN_POWER_TO_WEIGHT_RATIO = 3.0; // kW/ton
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TrainAssemblyMenu(Scanner scanner, InputValidator validator,
                             RoutePlanningService routePlanningService,
                             LocomotiveRepository locomotiveRepo,
                             WagonRepository wagonRepo,
                             FacilityRepository facilityRepo,
                             TrainRepository trainRepo,
                             ScheduleRepository scheduleRepo,
                             LineRepository lineRepo) {
        this.scanner = scanner;
        this.validator = validator;
        this.routePlanningService = routePlanningService;
        this.locomotiveRepo = locomotiveRepo;
        this.wagonRepo = wagonRepo;
        this.facilityRepo = facilityRepo;
        this.trainRepo = trainRepo;
        this.scheduleRepo = scheduleRepo;
        this.lineRepo = lineRepo;

        // Initialize internal repos if not injected (or inject them if available in MainMenu)
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
            System.out.println("│  4. View Assembled Trains (Ready for Dispatch)       │");
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
        System.out.println("║   USLP09 - ASSEMBLE AND ASSIGN TRAIN (FINAL)          ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    // ... [listAllAvailableRoutes mantido igual ao anterior] ...
    private void listAllAvailableRoutes() {
        System.out.println("\n═══ AVAILABLE ROUTES ═══");
        List<PlannedRoute> memoryRoutes = routePlanningService.getUnassignedRoutes();
        List<Route> dbRoutes = routeRepo.findAll();

        if (memoryRoutes.isEmpty() && dbRoutes.isEmpty()) {
            System.out.println("✗ No routes available.");
            return;
        }

        System.out.println("Found " + (memoryRoutes.size() + dbRoutes.size()) + " routes.");
        memoryRoutes.forEach(r -> System.out.println(routePlanningService.formatRoute(r)));
        dbRoutes.forEach(r -> System.out.println("DB Route: " + r.getRouteId() + " (" + r.getType() + ")"));
    }

    private void listAvailableRollingStock() {
        System.out.println("\n═══ AVAILABLE ROLLING STOCK ═══");

        List<Locomotive> locos = locomotiveRepo.findAll();
        List<Wagon> wagons = wagonRepo.findAll();

        System.out.println("\n--- Locomotives (" + locos.size() + ") ---");
        locos.forEach(l -> System.out.printf("  %s: %.0f kW, Gauge: %.0f mm\n",
                l.getLocoNumber(), l.getPowerKw(), l.getGauge()));

        System.out.println("\n--- Wagons (" + wagons.size() + ") ---");
        // Agrupar por facilidade para facilitar leitura
        Map<Integer, List<Wagon>> wagonsByFacility = wagons.stream()
                .filter(w -> w.getCurrentFacilityId() != null) // Apenas estacionados
                .collect(Collectors.groupingBy(Wagon::getCurrentFacilityId));

        wagonsByFacility.forEach((facilityId, ws) -> {
            String facilityName = facilityRepo.findById(facilityId).map(Facility::getName).orElse("ID " + facilityId);
            System.out.println("  At " + facilityName + ":");
            ws.forEach(w -> System.out.printf("    [%s] %s (%.0f mm)\n", w.getWagonNumber(), w.getTypeDesc(), w.getGauge()));
        });
    }

    /**
     * Main workflow: Assemble and assign train
     */
    private void assembleAndAssignTrain() {
        System.out.println("\n═══ ASSEMBLE TRAIN ═══");

        try {
            // STEP 1: Select Route
            PlannedRoute selectedRoute = selectRouteNumbered();
            if (selectedRoute == null) return;

            // Get route details for validation
            int departureFacilityId = selectedRoute.getMandatoryFacilities().get(0);
            Facility departureFacility = facilityRepo.findById(departureFacilityId)
                    .orElseThrow(() -> new IllegalStateException("Departure facility not found"));

            System.out.println("\n✓ Route: " + selectedRoute.getId() + " from " + departureFacility.getName());

            // STEP 2: Select Operator
            String operatorVat = selectOperatorNumbered();
            if (operatorVat == null) return;

            // STEP 3: Select Locomotives
            List<String> selectedLocoIds = selectLocomotivesNumbered();
            if (selectedLocoIds.isEmpty()) return;

            // STEP 4: Select Wagons
            List<String> selectedWagonIds = selectWagonsForRoute(selectedRoute, departureFacilityId);
            if (selectedWagonIds.isEmpty()) return;

            // --- CRITICAL STEP: Retrieve Full Objects ---
            List<Locomotive> selectedLocos = new ArrayList<>();
            for (String id : selectedLocoIds) {
                selectedLocos.add(locomotiveRepo.findByNumber(id).orElseThrow());
            }

            List<Wagon> selectedWagons = new ArrayList<>();
            for (String id : selectedWagonIds) {
                selectedWagons.add(wagonRepo.findById(id).orElseThrow());
            }

            // STEP 5: VALIDATE GAUGE (New Correction)
            System.out.println("\nChecking Gauge Compatibility...");
            try {
                validateGaugeCompatibility(selectedLocos, selectedWagons, selectedRoute);
            } catch (IllegalArgumentException e) {
                System.out.println("✗ GAUGE ERROR: " + e.getMessage());
                System.out.println("Assembly aborted due to infrastructure incompatibility.");
                return;
            }

            // STEP 6: Validate Physics
            TrainMetrics metrics = calculateTrainMetrics(selectedLocos, selectedWagons);
            double totalPowerKw = selectedLocos.stream().mapToDouble(Locomotive::getPowerKw).sum();

            if (!validateConstraints(metrics, totalPowerKw)) return;

            // STEP 7: Departure Time
            LocalDateTime departureDateTime = getDepartureDateTime();
            if (departureDateTime == null) return;

            // STEP 8: Create Full TRAIN Object (Eliminating AssembledTrain)
            Train train = new Train();
            int trainIdVal = 1000 + trainRepo.count() + 1; // Simple ID generation
            train.setId(trainIdVal);
            train.setOperatorVat(operatorVat);
            train.setStartFacilityId(departureFacilityId);
            train.setEndFacilityId(selectedRoute.getMandatoryFacilities().get(selectedRoute.getMandatoryFacilities().size()-1));
            train.setDepartureDate(departureDateTime.toLocalDate());
            train.setDepartureTime(departureDateTime.toLocalTime());
            train.setLocomotives(selectedLocos);
            train.setWagons(selectedWagons);

            // Save Train to Memory
            trainRepo.save(train);

            // Create Schedule
            String scheduleId = "SCH-" + trainIdVal;
            Schedule schedule = new Schedule(
                    scheduleId,
                    String.valueOf(trainIdVal),
                    selectedRoute.getId(),
                    departureDateTime,
                    Schedule.ScheduleStatus.SCHEDULED
            );
            scheduleRepo.save(schedule);

            // Mark route as assigned (In-Memory)
            selectedRoute.setAssignedTrainId(String.valueOf(trainIdVal));

            System.out.println("\n" + "✓".repeat(60));
            System.out.println("  SUCCESS! Train " + trainIdVal + " created.");
            System.out.println("  Schedule " + scheduleId + " is ready for Dispatch (USLP10).");
            System.out.println("✓".repeat(60));

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates compatibility between rolling stock gauge and route gauge
     */
    private void validateGaugeCompatibility(List<Locomotive> locos, List<Wagon> wagons, PlannedRoute route) {
        List<Integer> facilities = route.getMandatoryFacilities();
        if (facilities.size() < 2) {
            System.out.println("⚠ Warning: Route too short to validate gauge. Assuming compatible.");
            return;
        }

        // Check gauge of the first leg of the route
        int startId = facilities.get(0);
        int nextId = facilities.get(1);

        Line line = lineRepo.findLineConnecting(startId, nextId)
                .orElseThrow(() -> new IllegalArgumentException("No infrastructure line found between " + startId + " and " + nextId));

        double routeGauge = line.getGauge();
        System.out.printf("  Infrastructure Gauge: %.0f mm\n", routeGauge);

        // Validate Locos
        for (Locomotive l : locos) {
            if (Math.abs(l.getGauge() - routeGauge) > 0.01) {
                throw new IllegalArgumentException(String.format(
                        "Locomotive %s gauge (%.0f) does not match route (%.0f)",
                        l.getLocoNumber(), l.getGauge(), routeGauge));
            }
        }

        // Validate Wagons
        for (Wagon w : wagons) {
            if (Math.abs(w.getGauge() - routeGauge) > 0.01) {
                throw new IllegalArgumentException(String.format(
                        "Wagon %s gauge (%.0f) does not match route (%.0f)",
                        w.getWagonNumber(), w.getGauge(), routeGauge));
            }
        }
        System.out.println("  ✓ Rolling stock is compatible.");
    }

    // --- Helper Methods (Selections & Calculations) ---

    private PlannedRoute selectRouteNumbered() {
        List<PlannedRoute> routes = routePlanningService.getUnassignedRoutes();
        if (routes.isEmpty()) {
            System.out.println("✗ No unassigned routes.");
            return null;
        }
        for (int i = 0; i < routes.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, routePlanningService.formatRoute(routes.get(i)));
        }
        int idx = validator.readInt("Select route: ", 1, routes.size());
        return routes.get(idx - 1);
    }

    private String selectOperatorNumbered() {
        System.out.println("1. CP\n2. MEDWAY\n3. TAKARGO");
        int op = validator.readInt("Select operator: ", 1, 3);
        return switch(op) { case 1->"CP"; case 2->"MEDWAY"; default->"TAKARGO"; };
    }

    private List<String> selectLocomotivesNumbered() {
        List<Locomotive> all = locomotiveRepo.findAll();
        for(int i=0; i<all.size(); i++) {
            System.out.printf("[%d] %s (%.0f kW)\n", i+1, all.get(i).getLocoNumber(), all.get(i).getPowerKw());
        }
        System.out.println("Enter numbers comma-separated (e.g. 1,2):");
        scanner.nextLine(); // flush
        String in = scanner.nextLine();
        List<String> ids = new ArrayList<>();
        for(String s : in.split(",")) {
            try {
                int idx = Integer.parseInt(s.trim()) - 1;
                if(idx >=0 && idx < all.size()) ids.add(all.get(idx).getLocoNumber());
            } catch(Exception e) {}
        }
        return ids;
    }

    private List<String> selectWagonsForRoute(PlannedRoute route, int departureFacilityId) {
        // Tenta pegar automático dos freights
        List<Integer> freightIds = route.getFreightIds();
        List<String> wagonIds = new ArrayList<>();

        if (!freightIds.isEmpty()) {
            for(int fid : freightIds) {
                Freight f = freightRepo.findById(fid).orElse(null);
                if(f != null) f.getAssignedWagons().forEach(w -> wagonIds.add(w.getWagonNumber()));
            }
        }

        if (!wagonIds.isEmpty()) return wagonIds; // Retorna automático se houver

        // Senão, seleção manual
        System.out.println("No wagons linked to freights. Select manually from departure facility:");
        List<Wagon> available = wagonRepo.findAll().stream()
                .filter(w -> w.getCurrentFacilityId() != null && w.getCurrentFacilityId() == departureFacilityId)
                .toList();

        if (available.isEmpty()) {
            System.out.println("✗ No wagons at departure facility.");
            return Collections.emptyList();
        }

        for(int i=0; i<available.size(); i++) {
            System.out.printf("[%d] %s\n", i+1, available.get(i).getWagonNumber());
        }
        System.out.println("Enter numbers comma-separated:");
        String in = scanner.nextLine();
        List<String> ids = new ArrayList<>();
        for(String s : in.split(",")) {
            try {
                int idx = Integer.parseInt(s.trim()) - 1;
                if(idx >=0 && idx < available.size()) ids.add(available.get(idx).getWagonNumber());
            } catch(Exception e) {}
        }
        return ids;
    }

    private TrainMetrics calculateTrainMetrics(List<Locomotive> locos, List<Wagon> wagons) {
        double weight = locos.stream().mapToDouble(Locomotive::getWeightTons).sum() +
                wagons.stream().mapToDouble(Wagon::getTotalWeightTons).sum();
        double length = (locos.stream().mapToDouble(Locomotive::getLengthM).sum()) +
                (wagons.stream().mapToDouble(Wagon::getLengthMm).sum() / 1000.0);
        return new TrainMetrics(weight, length);
    }

    private boolean validateConstraints(TrainMetrics metrics, double power) {
        if (metrics.totalLengthM > MAX_TRAIN_LENGTH_M) {
            System.out.printf("✗ Length %.1fm exceeds max %.0fm\n", metrics.totalLengthM, MAX_TRAIN_LENGTH_M);
            return false;
        }
        double ratio = power / metrics.totalWeightTons;
        if (ratio < MIN_POWER_TO_WEIGHT_RATIO) {
            System.out.printf("✗ Power/Weight %.2f kW/t too low (min %.1f)\n", ratio, MIN_POWER_TO_WEIGHT_RATIO);
            return false;
        }
        return true;
    }

    private LocalDateTime getDepartureDateTime() {
        System.out.print("Enter departure (yyyy-MM-dd HH:mm): ");
        try {
            return LocalDateTime.parse(scanner.nextLine().trim(), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            System.out.println("Invalid format.");
            return null;
        }
    }

    private void viewAssembledTrains() {
        List<Train> trains = trainRepo.findAll();
        if (trains.isEmpty()) System.out.println("No trains assembled.");
        else trains.forEach(t -> System.out.println(t));
    }

    private static class TrainMetrics {
        final double totalWeightTons;
        final double totalLengthM;
        TrainMetrics(double w, double l) { this.totalWeightTons = w; this.totalLengthM = l; }
    }
}