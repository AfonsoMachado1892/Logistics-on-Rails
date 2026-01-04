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
import java.util.stream.Collectors;

/**
 * Traffic Manager Menu for USLP10
 * Integrates:
 * - USLP08: Route Planning (consumes planned routes)
 * - USLP09: Train Assembly (consumes assembled trains)
 * - USLP10: Scheduling (creates schedules with automatic/manual path)
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
    private final RoutePlanningService routePlanningService;

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
                              SegmentRepository segmentRepository,
                              RoutePlanningService routePlanningService) {
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
        this.routePlanningService = routePlanningService;

        // Initialize automatic path planning service
        this.automaticPathService = new AutomaticPathPlanningService(
                facilityRepository, lineRepository, segmentRepository
        );
    }

    public void show() {
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 8);

            switch (choice) {
                case 1 -> {
                    if (dataImportMenu != null) dataImportMenu.show();
                    else System.out.println("Data Import Menu not configured.");
                }
                case 2 -> listAllTrains();
                case 3 -> listPlannedRoutes();
                case 4 -> createScheduleForAssignedRoute();
                case 5 -> viewSchedule();
                case 6 -> generateDispatchPlan();
                case 7 -> viewDailyDispatchPlan();
                case 8 -> exportDispatchPlan();
                case 0 -> running = false;
            }

            if (running && choice != 0 && choice != 1) {
                pauseForUser();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│  TRAFFIC MANAGER - TRAIN SCHEDULER (USLP10)   │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  1. Data Import & Management");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  2. List All Trains");
        System.out.println("  3. List Planned Routes (from USLP08)");
        System.out.println("  4. Create Schedule");
        System.out.println("  5. View Schedules");
        System.out.println("  6. Generate Dispatch Plan");
        System.out.println("  7. View Dispatch Visualizations");
        System.out.println("  8. Export Dispatch Plan");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  0. Back to Main Menu");
        System.out.println();
    }

    // ============================================================================
    // USLP10 - LIST PLANNED ROUTES FROM USLP08
    // ============================================================================

    /**
     * Lists all planned routes created in USLP08
     */
    private void listPlannedRoutes() {
        System.out.println("\n═══ PLANNED ROUTES (from USLP08) ═══");

        List<PlannedRoute> routes = routePlanningService.getAllRoutes();

        if (routes.isEmpty()) {
            System.out.println("\nNo routes planned yet.");
            System.out.println("Please use the Freight Assignment Menu (USLP08) to create routes first.");
            return;
        }

        System.out.println();
        for (PlannedRoute route : routes) {
            System.out.println(routePlanningService.formatRoute(route));
            System.out.println("─".repeat(80));
        }

        System.out.printf("\nTotal: %d routes (%d simple, %d composite)\n",
                routes.size(),
                routes.stream().filter(PlannedRoute::isSimple).count(),
                routes.stream().filter(PlannedRoute::isComposite).count());
    }

    // ============================================================================
    // USLP10 - CREATE SCHEDULE FOR TRAIN
    // ============================================================================

    /**
     * USLP10 - Create schedule/Define Path (Lógica Corrigida)
     * Seleciona uma Rota já atribuída (USLP09) em vez de um Comboio solto.
     */
    private void createScheduleForAssignedRoute() {
        System.out.println("\n═══ DISPATCH TRAIN (USLP10) ═══");
        System.out.println("Select a route that already has a train assigned (from USLP09).");

        // 1. Obter rotas atribuídas
        List<PlannedRoute> assignedRoutes = routePlanningService.getAssignedRoutes();

        if (assignedRoutes.isEmpty()) {
            System.out.println("\n✗ No assigned routes found.");
            System.out.println("Please go to USLP09 (Train Assembly) and assign a train to a route first.");
            return;
        }

        // 2. Listar e Selecionar Rota
        System.out.println("\nReady for Dispatch:");
        for (int i = 0; i < assignedRoutes.size(); i++) {
            PlannedRoute r = assignedRoutes.get(i);
            System.out.printf("[%d] Route %s -> Assigned Train: %s (%s)\n",
                    i + 1, r.getId(), r.getAssignedTrainId(), r.getType());
        }

        int idx = validator.readInt("\nSelect Route Number (0 to cancel): ", 0, assignedRoutes.size());
        if (idx == 0) return;

        PlannedRoute selectedRoute = assignedRoutes.get(idx - 1);
        String trainId = selectedRoute.getAssignedTrainId();

        // 3. Validar consistência
        Train train = trainRepository.findById(trainId).orElse(null);
        if (train == null) {
            System.out.println("✗ Error: Assigned train " + trainId + " not found in repository.");
            return;
        }

        System.out.println("\n✓ Selected: Route " + selectedRoute.getId() + " with Train " + trainId);

        // Tentar encontrar o Schedule criado na USLP09
        Optional<Schedule> existingSchedule = scheduleRepository.findAll().stream()
                .filter(s -> s.getTrainId().equals(trainId))
                .findFirst();

        LocalDateTime departureTime;
        if (existingSchedule.isPresent()) {
            departureTime = existingSchedule.get().getDepartureDateTime();
            System.out.println("  Departure Time: " + departureTime.format(DATE_TIME_FORMATTER));
        } else {
            System.out.println("⚠ No schedule found. Creating new.");
            String dateStr = validator.readString("Enter Departure (yyyy-MM-dd HH:mm): ");
            try {
                departureTime = LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
            } catch (Exception e) {
                System.out.println("Invalid date. Using NOW.");
                departureTime = LocalDateTime.now();
            }
        }

        // 4. Calcular Caminho (Path Calculation)
        // (Reutiliza os métodos que JÁ TENS no ficheiro original)
        System.out.println("\n═══ PATH CALCULATION ═══");
        System.out.println("1. Manual Path Definition");
        System.out.println("2. Automatic Path Calculation (ESINF US15)");
        int pathChoice = validator.readInt("Choice: ", 1, 2);

        List<Integer> path;
        if (pathChoice == 1) {
            path = buildPathManually(selectedRoute);
        } else {
            path = calculatePathAutomatically(selectedRoute);
        }

        if (path == null || path.isEmpty()) {
            System.out.println("✗ Path calculation failed/cancelled.");
            return;
        }

        // 5. Guardar Caminho
        selectedRoute.setPath(path);
        train.setPath(path); // O comboio precisa do caminho para a física

        // 6. Atualizar/Criar Schedule
        if (existingSchedule.isPresent()) {
            System.out.println("✓ Schedule " + existingSchedule.get().getScheduleId() + " updated with Path.");
        } else {
            String schId = "SCH-" + trainId;
            Schedule newSched = new Schedule(schId, trainId, selectedRoute.getId(), departureTime, Schedule.ScheduleStatus.SCHEDULED);
            scheduleRepository.save(newSched);
            System.out.println("✓ New Schedule " + schId + " created.");
        }

        System.out.println("\nReady for Dispatch Plan Generation (Option 6).");
    }

    // ============================================================================
    // PATH CALCULATION METHODS
    // ============================================================================

    /**
     * Build path manually (user selects each station)
     */
    private List<Integer> buildPathManually(PlannedRoute route) {
        System.out.println("\n═══ MANUAL PATH DEFINITION ═══");

        List<Integer> mandatoryStops = route.getMandatoryFacilities();
        List<Integer> path = new ArrayList<>();

        System.out.println("\nMandatory stops (must visit in order):");
        for (Integer facilityId : mandatoryStops) {
            String name = facilityRepository.findById(facilityId)
                    .map(Facility::getName)
                    .orElse("Unknown");
            System.out.printf("  - %s (ID: %d)\n", name, facilityId);
        }

        int currentId = mandatoryStops.get(0);
        path.add(currentId);

        Set<Integer> remainingStops = new HashSet<>(mandatoryStops);
        remainingStops.remove(currentId);

        System.out.println("\nStarting at: " + facilityRepository.findById(currentId)
                .map(Facility::getName).orElse("Unknown"));

        while (!remainingStops.isEmpty()) {
            String currentName = facilityRepository.findById(currentId)
                    .map(Facility::getName)
                    .orElse("Unknown");

            System.out.println("\n" + "─".repeat(60));
            System.out.println("Current position: " + currentName);

            if (!remainingStops.isEmpty()) {
                System.out.println("Remaining mandatory stops: " + remainingStops.stream()
                        .map(id -> facilityRepository.findById(id)
                                .map(Facility::getName)
                                .orElse(String.valueOf(id)))
                        .collect(Collectors.joining(", ")));
            }

            Map<Integer, Line> connections = lineRepository.findDirectlyConnectedFacilities(currentId);

            if (connections.isEmpty()) {
                System.out.println("✗ No connections available. Path building failed.");
                return null;
            }

            System.out.println("\nDirectly connected facilities:");
            for (Integer neighborId : connections.keySet()) {
                String name = facilityRepository.findById(neighborId)
                        .map(Facility::getName)
                        .orElse("Unknown");
                boolean isMandatory = remainingStops.contains(neighborId);
                System.out.printf("  [%d] %s %s\n",
                        neighborId, name, isMandatory ? "⚠ MANDATORY" : "");
            }

            int nextId = validator.readInt("\nEnter next facility ID (0 to cancel): ", 0, Integer.MAX_VALUE);

            if (nextId == 0) {
                System.out.println("✗ Path building cancelled.");
                return null;
            }

            if (!connections.containsKey(nextId)) {
                System.out.println("✗ Not directly connected. Choose again.");
                continue;
            }

            path.add(nextId);
            currentId = nextId;

            if (remainingStops.contains(nextId)) {
                System.out.println("✓ Mandatory stop reached: " +
                        facilityRepository.findById(nextId)
                                .map(Facility::getName)
                                .orElse("Unknown"));
                remainingStops.remove(nextId);
            }
        }

        System.out.println("\n" + "✓".repeat(60));
        System.out.println("  Path completed successfully!");
        System.out.println("✓".repeat(60));
        System.out.println("\nFinal path: " + path.stream()
                .map(id -> facilityRepository.findById(id)
                        .map(Facility::getName)
                        .orElse(String.valueOf(id)))
                .collect(Collectors.joining(" → ")));

        return path;
    }

    /**
     * Calculate path automatically using ESINF US15 (Bellman-Ford)
     */
    private List<Integer> calculatePathAutomatically(PlannedRoute route) {
        System.out.println("\n═══ AUTOMATIC PATH CALCULATION (ESINF US15) ═══");
        System.out.println("Using Bellman-Ford algorithm with cost optimization\n");

        // Show network summary
        AutomaticPathPlanningService.NetworkSummary summary =
                automaticPathService.getNetworkSummary();

        System.out.println("Railway Network Summary:");
        System.out.println("  Facilities: " + summary.facilities);
        System.out.println("  Lines: " + summary.lines);
        System.out.println("  Segments: " + summary.segments);
        System.out.println("  Total Distance: " + String.format("%.2f", summary.totalDistanceKm) + " km");
        System.out.println();

        List<Integer> completePath = new ArrayList<>();
        List<Integer> mandatoryStops = route.getMandatoryFacilities();

        // For each consecutive pair of mandatory stops
        for (int i = 0; i < mandatoryStops.size() - 1; i++) {
            int from = mandatoryStops.get(i);
            int to = mandatoryStops.get(i + 1);

            String fromName = facilityRepository.findById(from)
                    .map(Facility::getName)
                    .orElse(String.valueOf(from));
            String toName = facilityRepository.findById(to)
                    .map(Facility::getName)
                    .orElse(String.valueOf(to));

            System.out.printf("⏳ Calculating: %s → %s\n", fromName, toName);

            // Use ESINF US15
            AutomaticPathPlanningService.PathCalculationResult result =
                    automaticPathService.calculateOptimalPath(from, to);

            if (!result.isSuccess()) {
                if (result.hasNegativeCycle()) {
                    System.out.println("\n✗ NEGATIVE CYCLE DETECTED!");
                    System.out.println("  Stations involved: " + result.getNegativeCycleStations());
                    System.out.println("  This indicates inconsistent costs in the network.");
                    return null;
                }

                System.out.println("✗ No path found between " + fromName + " and " + toName);
                return null;
            }

            List<Integer> segment = result.getPath();

            if (i > 0) {
                segment.remove(0);  // Remove duplicate facility
            }

            completePath.addAll(segment);

            System.out.printf("  ✓ Path: %s\n",
                    segment.stream()
                            .map(id -> facilityRepository.findById(id)
                                    .map(Facility::getName)
                                    .orElse(String.valueOf(id)))
                            .collect(Collectors.joining(" → ")));
            System.out.printf("    Distance: %.2f km | Cost: %.2f\n",
                    result.getTotalDistanceKm(), result.getTotalCost());
        }

        System.out.println("\n" + "✓".repeat(60));
        System.out.println("  Complete path calculated successfully!");
        System.out.println("✓".repeat(60));
        System.out.println("\nFinal path: " + completePath.stream()
                .map(id -> facilityRepository.findById(id)
                        .map(Facility::getName)
                        .orElse(String.valueOf(id)))
                .collect(Collectors.joining(" → ")));

        return completePath;
    }

    // ============================================================================
    // EXISTING METHODS (PRESERVED)
    // ============================================================================

    private void listAllTrains() {
        System.out.println("\n═══ TRAIN LIST ═══");
        List<Train> trains = trainRepository.findAll();

        if (trains.isEmpty()) {
            System.out.println("No trains defined in repository.");
            return;
        }

        System.out.printf("%-10s | %-20s | %-15s | %-10s | %-10s%n",
                "ID", "Operator", "Payload (t)", "Power (kW)", "Electric");
        System.out.println("─".repeat(75));

        for (Train t : trains) {
            System.out.printf("%-10s | %-20s | %-15.1f | %-10.0f | %-10s%n",
                    t.getTrainId(),
                    t.getOperator(),
                    t.getTotalPayloadTons(),
                    t.getTotalPowerKw(),
                    t.requiresElectrification() ? "Yes" : "No"
            );
        }
    }

    private void viewSchedule() {
        System.out.println("\n═══ SCHEDULE LIST ═══");
        List<Schedule> schedules = scheduleRepository.findAll();

        if (schedules.isEmpty()) {
            System.out.println("No schedules found.");
            return;
        }

        System.out.printf("%-10s | %-10s | %-10s | %-20s | %-10s%n",
                "ID", "Train", "Route", "Departure", "Status");
        System.out.println("─".repeat(70));

        for (Schedule s : schedules) {
            System.out.printf("%-10s | %-10s | %-10s | %-20s | %-10s%n",
                    s.getScheduleId(),
                    s.getTrainId(),
                    s.getRouteId(),
                    s.getDepartureDateTime().format(DATE_TIME_FORMATTER),
                    s.getStatus());
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

                System.out.println("⏳ Calculating physics and resolving conflicts...");
                plan = dispatcherService.createDailyDispatchPlan(date.atStartOfDay());

            } else {
                System.out.print("Enter Schedule IDs (comma separated, e.g., SCH-1001,SCH-1002): ");
                String input = scanner.nextLine();
                List<String> ids = Arrays.asList(input.split(","));
                ids.replaceAll(String::trim);

                System.out.println("⏳ Calculating physics and resolving conflicts...");
                plan = dispatcherService.createDispatchPlan(ids);
            }

            if (plan != null) {
                this.currentDispatchPlan = plan;
                System.out.println("\n✓ PLAN GENERATED SUCCESSFULLY");
                System.out.println(dispatcherService.getDispatchSummary(plan));

                // Validar resultados
                var validation = dispatcherService.validateDispatchPlan(plan);
                if (!validation.isValid()) {
                    System.out.println("\n⚠ Validation Warnings:");
                    validation.getWarnings().forEach(w -> System.out.println("  - " + w));
                }
            }

        } catch (DateTimeParseException e) {
            System.out.println("✗ Invalid date format.");
        } catch (Exception e) {
            System.out.println("✗ Error generating plan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void viewDailyDispatchPlan() {
        System.out.println("\n═══ DISPATCH VISUALIZATIONS ═══");

        if (currentDispatchPlan == null) {
            System.out.println("✗ No plan available in memory.");
            System.out.println("Please run Option 6 (Generate Dispatch Plan) first.");
            return;
        }

        boolean viewing = true;
        while (viewing) {
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
            System.out.println("✗ No plan available. Run Option 6 first.");
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