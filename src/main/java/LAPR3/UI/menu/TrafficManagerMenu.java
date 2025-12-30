package LAPR3.UI.menu;

import LAPR3.DTO.DispatchPlan;
import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.*;
import LAPR3.UI.utils.InputValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Traffic Manager Menu for USLP10
 * Extends USLP07 functionality with:
 * - Automatic Path Calculation (using ESINF algorithms)
 * - Manual Path Definition (from USLP07)
 * - Integration with Route Planner (USLP08) - future
 * - Integration with Train Assembly (USLP09) - future
 */
public class TrafficManagerMenu {
    private final Scanner scanner;
    private final InputValidator validator;
    private final TrainDispatcherService dispatcherService;
    private final RouteRepository routeRepository;
    private final FacilityRepository facilityRepository;
    private final TrainRepository trainRepository;
    private final ScheduleRepository scheduleRepository;
    private final RoutePathService routePathService;
    private final ScheduleVisualizationService visualService;
    private final TrainDataImportMenu dataImportMenu;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;

    // NEW for USLP10
    private final AutomaticPathPlanningService automaticPathService;

    // Campo para armazenar o último plano gerado em memória
    private DispatchPlan currentDispatchPlan;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TrafficManagerMenu(Scanner scanner, InputValidator validator,
                              TrainDispatcherService dispatcherService,
                              RouteRepository routeRepository,
                              FacilityRepository facilityRepository,
                              TrainRepository trainRepository,
                              ScheduleRepository scheduleRepository,
                              RoutePathService routePathService,
                              ScheduleVisualizationService visualService,
                              TrainDataImportMenu dataImportMenu,
                              LineRepository lineRepository,
                              SegmentRepository segmentRepository) {
        this.scanner = scanner;
        this.validator = validator;
        this.dispatcherService = dispatcherService;
        this.routeRepository = routeRepository;
        this.facilityRepository = facilityRepository;
        this.trainRepository = trainRepository;
        this.scheduleRepository = scheduleRepository;
        this.routePathService = routePathService;
        this.visualService = visualService;
        this.dataImportMenu = dataImportMenu;
        this.lineRepository = lineRepository;
        this.segmentRepository = segmentRepository;

        // Initialize automatic path planning service
        this.automaticPathService = new AutomaticPathPlanningService(
                facilityRepository, lineRepository, segmentRepository
        );
    }

    public void show() {
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 10);

            switch (choice) {
                case 1 -> {
                    if (dataImportMenu != null) dataImportMenu.show();
                    else System.out.println("Data Import Menu not configured.");
                }
                case 2 -> listAllRoutes();
                case 3 -> buildRouteManual();
                case 4 -> buildRouteAutomatic(); // NEW for USLP10
                case 5 -> listAllTrains();
                case 6 -> createSchedule();
                case 7 -> viewSchedule();
                case 8 -> generateDispatchPlan();
                case 9 -> viewDailyDispatchPlan();
                case 10 -> exportDispatchPlan();
                case 0 -> running = false;
            }

            if (running && choice != 0 && choice != 1) {
                pauseForUser();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│  TRAFFIC MANAGER - TRAIN DISPATCHER (USLP10)   │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  1. Data Import & Management  ");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  2. List All Routes");
        System.out.println("  3. Build Route (Manual Path)");
        System.out.println("  4. Build Route (Automatic Path) ⭐ NEW");
        System.out.println("  5. List All Trains");
        System.out.println("  6. Create New Schedule");
        System.out.println("  7. View Schedule Details");
        System.out.println("  8. Generate Dispatch Plan (Process Schedules)");
        System.out.println("  9. View Dispatch Visualizations (Timeline/Heatmap)");
        System.out.println("  10. Export Dispatch Plan");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  0. Back to Main Menu");
        System.out.println();
    }

    // ============================================================================
    // NEW FOR USLP10: AUTOMATIC PATH CALCULATION
    // ============================================================================

    /**
     * Builds a route using automatic path calculation (USLP10)
     * Uses Bellman-Ford algorithm from ESINF to find optimal path
     */
    private void buildRouteAutomatic() {
        System.out.println("\n═══ BUILD ROUTE - AUTOMATIC PATH CALCULATION ═══");
        System.out.println("Using cost-based optimization (Bellman-Ford Algorithm)");
        System.out.println();

        // Show network summary
        AutomaticPathPlanningService.NetworkSummary summary =
                automaticPathService.getNetworkSummary();

        System.out.println("Railway Network Summary:");
        System.out.println("  Facilities: " + summary.facilities);
        System.out.println("  Lines: " + summary.lines);
        System.out.println("  Segments: " + summary.segments);
        System.out.println("  Total Distance: " + String.format("%.2f", summary.totalDistanceKm) + " km");
        System.out.println("  Electrified: " + summary.electrifiedSegments + " segments");
        System.out.println("  Double Track: " + summary.doubleTrackSegments + " segments");
        System.out.println();

        // 1. Select starting facility
        int startId = validator.readInt("Enter Starting Facility ID: ", 0, Integer.MAX_VALUE);
        Facility startFacility = facilityRepository.findById(startId).orElse(null);

        if (startFacility == null) {
            System.out.println("✗ Error: Starting facility not found.");
            return;
        }

        System.out.println("✓ Starting facility: " + startFacility.getName());

        // 2. Select ending facility
        int endId = validator.readInt("Enter Ending Facility ID: ", 0, Integer.MAX_VALUE);
        Facility endFacility = facilityRepository.findById(endId).orElse(null);

        if (endFacility == null) {
            System.out.println("✗ Error: Ending facility not found.");
            return;
        }

        System.out.println("✓ Ending facility: " + endFacility.getName());

        if (startId == endId) {
            System.out.println("✗ Error: Start and end must be different.");
            return;
        }

        // 3. Calculate optimal path
        System.out.println("\n⏳ Calculating optimal path...");

        AutomaticPathPlanningService.PathCalculationResult result =
                automaticPathService.calculateOptimalPath(startId, endId);

        if (!result.isSuccess()) {
            if (result.hasNegativeCycle()) {
                System.out.println("\n✗ NEGATIVE COST CYCLE DETECTED!");
                System.out.println("The network configuration contains a negative cost cycle.");
                System.out.println("This means there's a loop where traversing it reduces cost indefinitely.");
                System.out.println("\nStations involved in cycle:");
                for (String station : result.getCycleInfo()) {
                    System.out.println("  • " + station);
                }
                System.out.println("\n⚠️  This indicates a configuration error in line costs.");
            } else {
                System.out.println("✗ Error: " + result.getErrorMessage());
            }
            return;
        }

        // 4. Display calculated path
        System.out.println("\n✓ OPTIMAL PATH FOUND!");
        System.out.println("─".repeat(80));
        System.out.println(String.format("Total Cost: %.2f", result.getTotalCost()));
        System.out.println(String.format("Facilities: %d", result.getFacilityIds().size()));
        System.out.println("─".repeat(80));

        System.out.println("\nPath Details:");
        List<Integer> path = result.getFacilityIds();
        List<Double> costs = result.getCumulativeCosts();

        for (int i = 0; i < path.size(); i++) {
            int facId = path.get(i);
            Facility fac = facilityRepository.findById(facId).orElse(null);
            String facName = fac != null ? fac.getName() : "Unknown";

            if (i < costs.size()) {
                System.out.printf("%2d. %-30s (Cost: %.2f)%n",
                        i + 1, facName, costs.get(i));
            } else {
                System.out.printf("%2d. %-30s%n", i + 1, facName);
            }

            // Show connection info
            if (i < path.size() - 1) {
                int nextId = path.get(i + 1);
                Optional<Line> lineOpt = lineRepository.findLineConnecting(facId, nextId);
                if (lineOpt.isPresent()) {
                    Line line = lineOpt.get();
                    List<Segment> segments = segmentRepository.findByLineId(line.getId());
                    double distance = segments.stream()
                            .mapToDouble(s -> s.getLengthMeters() / 1000.0)
                            .sum();
                    System.out.printf("    ↓ %s (%.2f km)%n", line.getName(), distance);
                }
            }
        }

        System.out.println("─".repeat(80));

        // 5. Ask if user wants to save as route
        System.out.print("\nSave this path as a Route? (y/n): ");
        String save = scanner.nextLine().trim().toLowerCase();

        if (save.equals("y") || save.equals("yes")) {
            saveCalculatedRoute(result.getFacilityIds(), startFacility, endFacility);
        }
    }

    /**
     * Saves automatically calculated path as a route
     */
    private void saveCalculatedRoute(List<Integer> facilityIds,
                                     Facility start, Facility end) {
        System.out.println("\n═══ SAVE ROUTE ═══");

        // Generate route ID
        String routeId = "AUTO_" + System.currentTimeMillis();

        // Get route name
        System.out.print("Enter Route Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = start.getName() + " → " + end.getName() + " (Auto)";
        }

        // Determine route type
        Route.RouteType type = facilityIds.size() <= 3 ?
                Route.RouteType.SIMPLE : Route.RouteType.COMPLEX;

        // Create route
        Route route = new Route(routeId, name, type, facilityIds);

        // Save route
        routeRepository.save(route);

        System.out.println("✓ Route saved successfully!");
        System.out.println("  Route ID: " + routeId);
        System.out.println("  Name: " + name);
        System.out.println("  Type: " + type);
        System.out.println("  Facilities: " + facilityIds.size());
    }

    // ============================================================================
    // EXISTING METHODS FROM USLP07 (Mantidos)
    // ============================================================================

    private void listAllRoutes() {
        System.out.println("\n═══ AVAILABLE ROUTES ═══");
        List<Route> routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            System.out.println("No routes available.");
            return;
        }
        System.out.println("\nTotal routes: " + routes.size());
        System.out.println("─".repeat(90));
        System.out.printf("%-10s | %-30s | %-10s | %-10s | %-10s%n",
                "Route ID", "Name", "Type", "Facilities", "Segments");
        System.out.println("─".repeat(90));

        for (Route route : routes) {
            int facilityCount = route.getFacilityIds().size();
            int legCount = facilityCount > 1 ? facilityCount - 1 : 0;

            int segmentCount = 0;
            try {
                List<Segment> segments = routePathService.getAllSegments(route.getRouteId());
                segmentCount = segments.size();
            } catch (Exception e) {
                segmentCount = legCount;
            }

            String routeName = route.getName();
            if (routeName.length() > 30) {
                routeName = routeName.substring(0, 27) + "...";
            }

            System.out.printf("%-10s | %-30s | %-10s | %-10d | %-10d%n",
                    route.getRouteId(),
                    routeName,
                    route.getType(),
                    facilityCount,
                    segmentCount
            );
        }
    }

    private void buildRouteManual() {
        System.out.println("\n═══ BUILD ROUTE - MANUAL PATH DEFINITION ═══");

        // 1. Starting facility
        int startId = validator.readInt("Enter Starting Facility ID: ", 0, Integer.MAX_VALUE);
        Facility startFacility = facilityRepository.findById(startId).orElse(null);

        if (startFacility == null) {
            System.out.println("✗ Error: Starting facility not found.");
            return;
        }

        System.out.println("✓ Starting facility: " + startFacility.getName());

        // 2. Ending facility
        int endId = validator.readInt("Enter Ending Facility ID: ", 0, Integer.MAX_VALUE);
        Facility endFacility = facilityRepository.findById(endId).orElse(null);

        if (endFacility == null) {
            System.out.println("✗ Error: Ending facility not found.");
            return;
        }

        System.out.println("✓ Ending facility: " + endFacility.getName());

        // 3. Build path manually
        List<Integer> path = new ArrayList<>();
        path.add(startId);

        int currentId = startId;
        while (currentId != endId) {
            Map<Integer, Line> connected = lineRepository.findDirectlyConnectedFacilities(currentId);

            if (connected.isEmpty()) {
                System.out.println("✗ Error: No connections from current facility.");
                return;
            }

            System.out.println("\nAvailable connections from " +
                    facilityRepository.findById(currentId).get().getName() + ":");

            for (Map.Entry<Integer, Line> entry : connected.entrySet()) {
                Facility fac = facilityRepository.findById(entry.getKey()).orElse(null);
                System.out.println("  " + entry.getKey() + ": " +
                        (fac != null ? fac.getName() : "Unknown") +
                        " (via " + entry.getValue().getName() + ")");
            }

            int nextId = validator.readInt("Select next facility ID (or 0 to cancel): ",
                    0, Integer.MAX_VALUE);

            if (nextId == 0) {
                System.out.println("Route building cancelled.");
                return;
            }

            if (!connected.containsKey(nextId)) {
                System.out.println("✗ Error: No direct connection to that facility.");
                continue;
            }

            path.add(nextId);
            currentId = nextId;
        }

        // 4. Create and save route
        System.out.println("\n✓ Path completed!");
        System.out.println("Path: " + path);

        saveCalculatedRoute(path, startFacility, endFacility);
    }

    private void listAllTrains() {
        System.out.println("\n═══ ALL TRAINS ═══");
        List<Train> trains = trainRepository.findAll();

        if (trains.isEmpty()) {
            System.out.println("No trains defined in repository.");
            return;
        }

        System.out.printf("%-10s | %-20s | %-15s | %-10s%n", "ID", "Operator", "Payload (t)", "Electric");
        System.out.println("─".repeat(65));

        for (Train t : trains) {
            System.out.printf("%-10s | %-20s | %-15.1f | %-10s%n",
                    t.getTrainId(),
                    t.getOperator(),
                    t.getTotalPayloadTons(),
                    t.requiresElectrification() ? "Yes" : "No"
            );
        }
    }

    private void createSchedule() {
        System.out.println("\n═══ CREATE NEW SCHEDULE ═══");
        System.out.println("\n═══ CREATE NEW SCHEDULE ═══");

        // 1. Selecionar Comboio
        String trainId = validator.readString("Enter Train ID: ");

        if (trainRepository.findById(trainId).isEmpty()) {
            System.out.println("✗ Error: Train ID not found.");
            return;
        }

        // 2. Selecionar Rota
        String routeId = validator.readString("Enter Route ID: ");

        // Validar se a rota existe
        Optional<String> validationResult = routePathService.validateRoute(routeId);

        if (validationResult.isPresent()) {
            System.out.println("✗ Error: " + validationResult.get());
            return;
        }

        // 3. Definir Data/Hora
        String dateStr = validator.readString("Enter Departure Time (yyyy-MM-dd HH:mm): ");

        try {
            LocalDateTime departure = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            // Gerar ID simples e único
            String scheduleId = "SCH-" + (scheduleRepository.findAll().size() + 1001);

            // CORREÇÃO: Passar o Status no construtor (5 argumentos)
            Schedule schedule = new Schedule(
                    scheduleId,
                    trainId,
                    routeId,
                    departure,
                    Schedule.ScheduleStatus.SCHEDULED
            );

            scheduleRepository.save(schedule);
            System.out.println(" Schedule created successfully! ID: " + scheduleId);

        } catch (DateTimeParseException e) {
            System.out.println(" Invalid date format. Please use yyyy-MM-dd HH:mm");
        } catch (Exception e) {
            System.out.println(" Error creating schedule: " + e.getMessage());
        }
    }
    private void viewSchedule() {
        System.out.println("\n═══ SCHEDULE LIST ═══");
        List<Schedule> schedules = scheduleRepository.findAll();

        if (schedules.isEmpty()) {
            System.out.println("No schedules found.");
            return;
        }

        for (Schedule s : schedules) {
            System.out.printf("ID: %-8s | Train: %-8s | Route: %-8s | Time: %s%n",
                    s.getScheduleId(), s.getTrainId(), s.getRouteId(),
                    s.getDepartureDateTime().format(DATE_TIME_FORMATTER));
        }
    }

    private void generateDispatchPlan() {
        System.out.println("\n═══ GENERATE DISPATCH PLAN ═══");
        System.out.println("1. Process daily plan (All schedules for a specific date)");
        System.out.println("2. Process specific schedules (Enter IDs manually)");

        int opt = validator.readInt("Option: ", 1, 2);

        try {
            DispatchPlan plan = null;

            if (opt == 1) {
                System.out.print("Enter Date (yyyy-MM-dd): ");
                String dayStr = scanner.nextLine().trim();
                LocalDate date = LocalDate.parse(dayStr, DATE_ONLY_FORMATTER);

                System.out.println("Calculating physics and resolving conflicts...");
                plan = dispatcherService.createDailyDispatchPlan(date.atStartOfDay());

            } else {
                System.out.print("Enter Schedule IDs (comma separated, e.g., S001,S002): ");
                String input = scanner.nextLine();
                List<String> ids = Arrays.asList(input.split(","));
                ids.replaceAll(String::trim);

                System.out.println("Calculating physics and resolving conflicts...");
                plan = dispatcherService.createDispatchPlan(ids);
            }

            if (plan != null) {
                this.currentDispatchPlan = plan; // Guardar em memória
                System.out.println("\n PLAN GENERATED SUCCESSFULLY");
                System.out.println(dispatcherService.getDispatchSummary(plan));

                // Validar resultados
                var validation = dispatcherService.validateDispatchPlan(plan);
                if (!validation.isValid()) {
                    System.out.println("\n Validation Warnings:");
                    validation.getWarnings().forEach(w -> System.out.println("  - " + w));
                }
            }

        } catch (DateTimeParseException e) {
            System.out.println(" Invalid date format.");
        } catch (Exception e) {
            System.out.println(" Error generating plan: " + e.getMessage());
            // e.printStackTrace(); // Debug se necessário
        }
    }

    private void viewDailyDispatchPlan() {
        System.out.println("\n═══ DISPATCH VISUALIZATIONS ═══");

        if (currentDispatchPlan == null) {
            System.out.println(" No plan available in memory.");
            System.out.println("Please run Option 7 (Generate Dispatch Plan) first.");
            return;
        }

        boolean viewing = true;
        while(viewing) {
            System.out.println("\nSelect Visualization:");
            System.out.println("1. Timeline View (ASCII)");
            System.out.println("2. Train Diagram (Bildfahrplan)");
            System.out.println("3. Crossing Operations List");
            System.out.println("4. Conflict Heatmap");
            System.out.println("5. Delay Statistics");
            System.out.println("0. Return to Menu");

            int choice = validator.readInt("View: ", 0, 5);

            switch (choice) {
                case 1 -> System.out.println(visualService.visualizeTimeline(currentDispatchPlan));
                case 2 -> System.out.println(visualService.visualizeTrainDiagram(currentDispatchPlan));
                case 3 -> System.out.println(visualService.visualizeCrossingOperations(currentDispatchPlan));
                case 4 -> System.out.println(visualService.visualizeConflictHeatmap(currentDispatchPlan));
                case 5 -> System.out.println(visualService.visualizeDelayStatistics(currentDispatchPlan));
                case 0 -> viewing = false;
            }

            if (viewing) pauseForUser();
        }
    }

    private void exportDispatchPlan() {
        System.out.println("\n═══ EXPORT PLAN ═══");

        if (currentDispatchPlan == null) {
            System.out.println(" No plan available. Run Option 7 first.");
            return;
        }

        String report = dispatcherService.exportDispatchPlan(currentDispatchPlan);
        System.out.println("Generating report preview:\n");
        System.out.println(report);

        System.out.println("\n[System] Plan exported to console output.");
    }

    private void pauseForUser() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}