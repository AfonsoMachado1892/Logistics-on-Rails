package LAPR3.UI.menu;

import LAPR3.DTO.*;
import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.*;
import LAPR3.UI.utils.InputValidator;
import LAPR3.model.Line;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class FreightManagerMenu {
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

    // Campo para armazenar o último plano gerado em memória
    private DispatchPlan currentDispatchPlan;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FreightManagerMenu(Scanner scanner, InputValidator validator,
                              TrainDispatcherService dispatcherService,
                              RouteRepository routeRepository,
                              FacilityRepository facilityRepository,
                              TrainRepository trainRepository,
                              ScheduleRepository scheduleRepository,
                              RoutePathService routePathService,
                              ScheduleVisualizationService visualService,
                              TrainDataImportMenu dataImportMenu,
                              LineRepository lineRepository) {
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
    }

    public void show() {
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 9);

            switch (choice) {
                case 1 -> {
                    if (dataImportMenu != null) dataImportMenu.show();
                    else System.out.println("Data Import Menu not configured.");
                }
                case 2 -> listAllRoutes();
                case 3 -> buildRoute();
                case 4 -> listAllTrains();
                case 5 -> createSchedule();
                case 6 -> viewSchedule();
                case 7 -> generateDispatchPlan();
                case 8 -> viewDailyDispatchPlan();
                case 9 -> exportDispatchPlan();
                case 0 -> running = false;
            }

            if (running && choice != 0 && choice != 1) {
                pauseForUser();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│  FREIGHT MANAGER - TRAIN DISPATCHER (USLP07)   │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  1. Data Import & Management  ");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  2. List All Routes");
        System.out.println("  3. Build Route (Route Builder)");
        System.out.println("  4. List All Trains");
        System.out.println("  5. Create New Schedule");
        System.out.println("  6. View Schedule Details");
        System.out.println("  7. Generate Dispatch Plan (Process Schedules)");
        System.out.println("  8. View Dispatch Visualizations (Timeline/Heatmap)");
        System.out.println("  9. Export Dispatch Plan Report");
        System.out.println("  0. Back to Main Menu");
        System.out.println();
    }

    private void pauseForUser() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }

    // --- MÉTODOS EXISTENTES ---

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

    // --- NOVAS IMPLEMENTAÇÕES FUNCIONAIS ---

    private void buildRoute() {
        System.out.println("\n=== ROUTE BUILDER ===");

        // 1. Starting facility
        int startId = validator.readInt("Enter Starting Facility ID: ", 0, Integer.MAX_VALUE);
        Facility startFacility = facilityRepository.findById(startId)
                .orElse(null);

        if (startFacility == null) {
            System.out.println("✗ Error: Starting facility not found.");
            return;
        }

        System.out.println("Starting facility: " + startFacility.getName());

        // 2. Mandatory stops (optional)
        List<Integer> mandatoryStops = new ArrayList<>();
        System.out.print("Enter Mandatory Stops (comma-separated Facility IDs, or press ENTER to skip): ");
        String mandatoryInput = scanner.nextLine().trim();

        if (!mandatoryInput.isEmpty()) {
            String[] stopIds = mandatoryInput.split(",");
            for (String stopIdStr : stopIds) {
                try {
                    int stopId = Integer.parseInt(stopIdStr.trim());
                    if (facilityRepository.findById(stopId).isPresent()) {
                        mandatoryStops.add(stopId);
                    } else {
                        System.out.println("Warning: Facility ID " + stopId + " not found, skipping.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println(" Warning: Invalid facility ID: " + stopIdStr);
                }
            }
        }

        // 3. Build route step by step
        List<Integer> routeFacilities = new ArrayList<>();
        routeFacilities.add(startId);

        int currentFacilityId = startId;
        Set<Integer> visitedFacilities = new HashSet<>();
        visitedFacilities.add(startId);

        System.out.println("\nCurrent route: " + getFacilityName(startId));
        if (!mandatoryStops.isEmpty()) {
            System.out.println("Mandatory stops:");
            for (Integer stopId : mandatoryStops) {
                System.out.println("  - " + getFacilityName(stopId) + " (ID " + stopId + ")");
            }
        }

        while (true) {
            // Check if all mandatory stops are visited
            List<Integer> remainingMandatory = new ArrayList<>();
            for (Integer stopId : mandatoryStops) {
                if (!routeFacilities.contains(stopId)) {
                    remainingMandatory.add(stopId);
                }
            }

            if (!remainingMandatory.isEmpty()) {
                System.out.println("\nMandatory destinations still required:");
                for (Integer stopId : remainingMandatory) {
                    System.out.println("  - " + getFacilityName(stopId) + " (ID " + stopId + ")");
                }
            }

            // Get directly connected facilities
            Map<Integer, Line> connected = lineRepository.findDirectlyConnectedFacilities(currentFacilityId);

            // Filter out already visited facilities
            List<Integer> availableOptions = connected.keySet().stream()
                    .filter(id -> !visitedFacilities.contains(id))
                    .sorted()
                    .toList();

            if (availableOptions.isEmpty()) {
                System.out.println("\n⚠ No more unvisited facilities directly connected to " +
                        getFacilityName(currentFacilityId));
                System.out.println("Choose next stop (direct connections only):");
                System.out.println("  0) Finish route");

                int choice = validator.readInt("Choose option: ", 0, 0);
                if (choice == 0) break;
            } else {
                System.out.println("\nChoose next stop (direct connections only):");
                int optionNum = 1;
                for (Integer facilityId : availableOptions) {
                    Facility fac = facilityRepository.findById(facilityId).orElse(null);
                    String name = fac != null ? fac.getName() : "Unknown";
                    System.out.printf("  %d) %s (ID %d)%n", optionNum, name, facilityId);
                    optionNum++;
                }
                System.out.println("  0) Finish route");

                int maxOption = availableOptions.size();
                int choice = validator.readInt("Choose option: ", 0, maxOption);

                if (choice == 0) {
                    // Check if all mandatory stops are included
                    if (!remainingMandatory.isEmpty()) {
                        System.out.println("\n⚠ Warning: Route does not include all mandatory stops!");
                        System.out.print("Finish route anyway? (y/n): ");
                        String confirm = scanner.nextLine().trim().toLowerCase();
                        if (!confirm.equals("y") && !confirm.equals("yes")) {
                            continue;
                        }
                    }
                    break;
                }

                int selectedFacilityId = availableOptions.get(choice - 1);
                routeFacilities.add(selectedFacilityId);
                visitedFacilities.add(selectedFacilityId);
                currentFacilityId = selectedFacilityId;

                System.out.println("✓ Added: " + getFacilityName(selectedFacilityId));
                System.out.println("Current route: " + buildRouteString(routeFacilities));
            }
        }

        // 4. Validate route
        if (routeFacilities.size() < 2) {
            System.out.println("\n✗ Error: Route must have at least 2 facilities.");
            return;
        }

        // 5. Create route name
        String routeName = buildRouteName(routeFacilities);

        // 6. Generate route ID
        String routeId = "R" + String.format("%03d", routeRepository.count() + 1);
        while (routeRepository.findById(routeId).isPresent()) {
            routeId = "R" + String.format("%03d", Integer.parseInt(routeId.substring(1)) + 1);
        }

        // 7. Determine route type (SIMPLE if 2 facilities, COMPLEX otherwise)
        Route.RouteType routeType = routeFacilities.size() == 2
                ? Route.RouteType.SIMPLE
                : Route.RouteType.COMPLEX;

        // 8. Validate route path
        Optional<String> validationError = routePathService.validateRoute(routeId);
        if (validationError.isPresent() && !validationError.get().contains("not found")) {
            // Try to create anyway, validation will happen when saving
        }

        // 9. Create and save route
        try {
            Route newRoute = new Route(routeId, routeName, routeFacilities, routeType);

            // Validate the route path before saving
            boolean isValid = true;
            try {
                // Check each leg
                for (int i = 0; i < routeFacilities.size() - 1; i++) {
                    int from = routeFacilities.get(i);
                    int to = routeFacilities.get(i + 1);
                    if (lineRepository.findLineConnecting(from, to).isEmpty()) {
                        System.out.println("\n✗ Error: No direct line between " +
                                getFacilityName(from) + " and " + getFacilityName(to));
                        isValid = false;
                        break;
                    }
                }
            } catch (Exception e) {
                isValid = false;
            }

            if (!isValid) {
                System.out.println("✗ Route validation failed. Route not saved.");
                return;
            }

            routeRepository.save(newRoute);
            System.out.println("\n✓ Route created successfully!");
            System.out.println("Route ID: " + routeId);
            System.out.println("Route Name: " + routeName);
            System.out.println("Facilities: " + buildRouteString(routeFacilities));
            System.out.println("Type: " + routeType);

            // Show route summary
            System.out.println("\n" + routePathService.getRouteSummary(routeId));

        } catch (IllegalArgumentException e) {
            System.out.println("\n✗ Error creating route: " + e.getMessage());
        }
    }

    private String getFacilityName(int facilityId) {
        return facilityRepository.findById(facilityId)
                .map(Facility::getName)
                .orElse("Unknown (ID " + facilityId + ")");
    }

    private String buildRouteString(List<Integer> facilityIds) {
        return facilityIds.stream()
                .map(this::getFacilityName)
                .reduce((a, b) -> a + " → " + b)
                .orElse("");
    }

    private String buildRouteName(List<Integer> facilityIds) {
        if (facilityIds.size() == 2) {
            return getFacilityName(facilityIds.get(0)) + "-" + getFacilityName(facilityIds.get(1));
        } else {
            return getFacilityName(facilityIds.get(0)) + " → " +
                    getFacilityName(facilityIds.get(facilityIds.size() - 1)) +
                    " (via " + (facilityIds.size() - 2) + " stops)";
        }
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
            // CORREÇÃO: Usar getTotalPayloadTons()
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
}