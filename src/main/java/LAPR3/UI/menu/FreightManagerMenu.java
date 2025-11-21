package LAPR3.UI.menu;

import LAPR3.DTO.*;
import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.*;
import LAPR3.UI.utils.InputValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Menu for Freight Manager - Train Dispatcher System
 * Allows manual path definition and schedule management
 * Part of USLP07 - Subtask #70
 * Integrated with existing UI system
 */
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

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates Freight Manager Menu
     */
    public FreightManagerMenu(Scanner scanner, InputValidator validator,
                              TrainDispatcherService dispatcherService,
                              RouteRepository routeRepository,
                              FacilityRepository facilityRepository,
                              TrainRepository trainRepository,
                              ScheduleRepository scheduleRepository,
                              RoutePathService routePathService,
                              ScheduleVisualizationService visualService) {
        this.scanner = scanner;
        this.validator = validator;
        this.dispatcherService = dispatcherService;
        this.routeRepository = routeRepository;
        this.facilityRepository = facilityRepository;
        this.trainRepository = trainRepository;
        this.scheduleRepository = scheduleRepository;
        this.routePathService = routePathService;
        this.visualService = visualService;
    }

    /**
     * Shows the Freight Manager menu
     */
    public void show() {
        boolean running = true;

        while (running) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 8);

            switch (choice) {
                case 1 -> listAllRoutes();
                case 2 -> createManualRoute();
                case 3 -> listAllTrains();
                case 4 -> createSchedule();
                case 5 -> viewSchedule();
                case 6 -> generateDispatchPlan();
                case 7 -> viewDailyDispatchPlan();
                case 8 -> exportDispatchPlan();
                case 0 -> running = false;
            }

            if (running && choice != 0) {
                pauseForUser();
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n┌────────────────────────────────────────────────┐");
        System.out.println("│  FREIGHT MANAGER - TRAIN DISPATCHER (USLP07)  │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  1. List All Routes");
        System.out.println("  2. Create Manual Route");
        System.out.println("  3. List All Trains");
        System.out.println("  4. Create Schedule");
        System.out.println("  5. View Schedule Details");
        System.out.println("  6. Generate Dispatch Plan");
        System.out.println("  7. View Daily Dispatch Plan");
        System.out.println("  8. Export Dispatch Plan");
        System.out.println("  0. Back to Main Menu");
        System.out.println();
    }

    /**
     * Lists all available routes
     */
    private void listAllRoutes() {
        System.out.println("\n═══ AVAILABLE ROUTES ═══");

        List<Route> routes = routeRepository.findAll();

        if (routes.isEmpty()) {
            System.out.println("✗ No routes available.");
            return;
        }

        System.out.println("\nTotal routes: " + routes.size());
        System.out.println("─".repeat(100));

        for (Route route : routes) {
            System.out.println("\n" + route);

            // Show detailed path
            RoutePathResult pathResult = routePathService.resolveRoutePath(route.getRouteId());
            if (pathResult.isValid()) {
                System.out.println("  Path:");
                List<Integer> facilityIds = route.getFacilityIds();
                for (int i = 0; i < facilityIds.size(); i++) {
                    int facilityId = facilityIds.get(i);
                    Facility facility = facilityRepository.findById(facilityId).orElse(null);
                    String name = facility != null ? facility.getName() : "Unknown";
                    String marker = i == 0 ? "🚉 START" :
                            (i == facilityIds.size() - 1 ? "🏁 END" : "⚫");
                    System.out.println(String.format("    %s %d. %s (ID: %d)",
                            marker, i + 1, name, facilityId));
                }
                System.out.println(String.format("  📏 Total Distance: %.2f km",
                        pathResult.getTotalDistance() / 1000.0));
                System.out.println(String.format("  🚦 Single-Track Segments: %s",
                        pathResult.hasSingleTrackSegments() ? "⚠️ YES" : "✓ NO"));
            } else {
                System.out.println("  ✗ Invalid route: " + pathResult.getErrorMessage());
            }
        }
    }

    /**
     * Creates a manual route by selecting facilities
     */
    private void createManualRoute() {
        System.out.println("\n═══ CREATE MANUAL ROUTE ═══");

        // List available facilities
        List<Facility> facilities = facilityRepository.findAll();

        if (facilities.isEmpty()) {
            System.out.println("✗ No facilities available. Cannot create route.");
            return;
        }

        System.out.println("\nAvailable Facilities:");
        for (int i = 0; i < facilities.size(); i++) {
            Facility f = facilities.get(i);
            System.out.println(String.format("  %2d. %s (ID: %d)",
                    i + 1, f.getName(), f.getId()));
        }

        // Get route details
        System.out.println("\n── Route Information ──");
        String routeId = validator.readString("Route ID (e.g., R999): ");
        String routeName = validator.readString("Route Name: ");

        // Select facilities for route
        List<Integer> facilityIds = new ArrayList<>();
        System.out.println("\n── Select Facilities in Order ──");
        System.out.println("(Enter facility number, or 0 to finish)");

        while (true) {
            int choice = validator.readInt("Facility number (0 to finish): ", 0, facilities.size());
            if (choice == 0) break;

            Facility selected = facilities.get(choice - 1);
            facilityIds.add(selected.getId());
            System.out.println("  ✓ Added: " + selected.getName());
        }

        if (facilityIds.size() < 2) {
            System.out.println("\n✗ Error: Route must have at least 2 facilities");
            return;
        }

        // Create route
        Route.RouteType type = facilityIds.size() > 2 ?
                Route.RouteType.COMPLEX : Route.RouteType.SIMPLE;

        try {
            Route route = new Route(routeId, routeName, facilityIds, type);
            routeRepository.save(route);

            System.out.println("\n✓ Route created successfully!");
            System.out.println(route);

            // Validate route
            RoutePathResult validation = routePathService.resolveRoutePath(routeId);
            if (!validation.isValid()) {
                System.out.println("\n⚠️ Warning: Route validation failed");
                System.out.println("  " + validation.getErrorMessage());
            } else {
                System.out.println("✓ Route validated successfully");
                System.out.println(String.format("  Distance: %.2f km",
                        validation.getTotalDistance() / 1000.0));
            }

        } catch (Exception e) {
            System.out.println("\n✗ Error creating route: " + e.getMessage());
        }
    }

    /**
     * Lists all available trains
     */
    private void listAllTrains() {
        System.out.println("\n═══ AVAILABLE TRAINS ═══");

        List<Train> trains = trainRepository.findAll();

        if (trains.isEmpty()) {
            System.out.println("✗ No trains available.");
            return;
        }

        System.out.println("\nTotal trains: " + trains.size());
        System.out.println("─".repeat(100));

        for (Train train : trains) {
            System.out.println("\n🚂 " + train);
            System.out.println(String.format("   ⚡ Power: %.0f kW | ⚖️  Weight: %.1f t | 📊 P/W Ratio: %.2f kW/t",
                    train.getTotalPowerKw(), train.getTotalWeightTons(), train.getPowerToWeightRatio()));
            System.out.println(String.format("   🚉 Locomotives: %d | 📦 Wagons: %d | 🔧 Gauge: %dmm",
                    train.getLocomotiveCount(), train.getWagonCount(),
                    train.getLocomotives().get(0).getGauge()));

            String validation = train.validate();
            if (!validation.isEmpty()) {
                System.out.println("   ⚠️  " + validation);
            }
        }
    }

    /**
     * Creates a new schedule
     */
    private void createSchedule() {
        System.out.println("\n═══ CREATE SCHEDULE ═══");

        // Select train
        List<Train> trains = trainRepository.findAll();
        if (trains.isEmpty()) {
            System.out.println("✗ No trains available.");
            return;
        }

        System.out.println("\n── Select Train ──");
        for (int i = 0; i < trains.size(); i++) {
            System.out.println(String.format("  %d. %s", i + 1, trains.get(i)));
        }
        int trainChoice = validator.readInt("Select train: ", 1, trains.size());
        Train selectedTrain = trains.get(trainChoice - 1);

        // Select route
        List<Route> routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            System.out.println("✗ No routes available.");
            return;
        }

        System.out.println("\n── Select Route ──");
        for (int i = 0; i < routes.size(); i++) {
            System.out.println(String.format("  %d. %s", i + 1, routes.get(i)));
        }
        int routeChoice = validator.readInt("Select route: ", 1, routes.size());
        Route selectedRoute = routes.get(routeChoice - 1);

        // Get departure time
        LocalDateTime departure = readDateTime("Departure date/time (yyyy-MM-dd HH:mm): ");

        // Generate schedule ID
        String scheduleId = String.format("S%03d", scheduleRepository.count() + 1);

        try {
            Schedule schedule = new Schedule(
                    scheduleId,
                    selectedTrain.getTrainId(),
                    selectedRoute.getRouteId(),
                    departure,
                    Schedule.ScheduleStatus.SCHEDULED
            );

            scheduleRepository.save(schedule);

            System.out.println("\n✓ Schedule created successfully!");
            System.out.println(schedule);

            // Calculate and show preview
            System.out.println("\n── Schedule Preview ──");
            TrainScheduleResult preview =
                    dispatcherService.createDispatchPlan(List.of(scheduleId))
                            .getSchedule(scheduleId)
                            .getOriginalSchedule();

            System.out.println(preview.toScheduleTable());

        } catch (Exception e) {
            System.out.println("\n✗ Error creating schedule: " + e.getMessage());
        }
    }

    /**
     * Views an existing schedule
     */
    private void viewSchedule() {
        System.out.println("\n═══ VIEW SCHEDULE ═══");

        List<Schedule> schedules = scheduleRepository.findAll();

        if (schedules.isEmpty()) {
            System.out.println("✗ No schedules available.");
            return;
        }

        System.out.println("\n── Available Schedules ──");
        for (int i = 0; i < schedules.size(); i++) {
            Schedule s = schedules.get(i);
            System.out.println(String.format("  %d. %s - Train %s on Route %s - 📅 %s",
                    i + 1, s.getScheduleId(), s.getTrainId(), s.getRouteId(),
                    s.getDepartureDateTime().format(DATE_FORMATTER)));
        }

        int choice = validator.readInt("Select schedule: ", 1, schedules.size());
        Schedule selected = schedules.get(choice - 1);

        try {
            DispatchPlan plan = dispatcherService.createDispatchPlan(
                    List.of(selected.getScheduleId()));
            ResolvedSchedule resolved = plan.getSchedule(selected.getScheduleId());

            System.out.println("\n" + resolved.toDetailedString());

            // Offer visualizations
            System.out.println("\n── Visualizations Available ──");
            System.out.println("1. Timeline View");
            System.out.println("2. Train Diagram");
            System.out.println("3. Crossing Operations");
            System.out.println("0. Skip");

            int vizChoice = validator.readInt("Select visualization: ", 0, 3);

            switch (vizChoice) {
                case 1 -> System.out.println("\n" + visualService.visualizeTimeline(plan));
                case 2 -> System.out.println("\n" + visualService.visualizeTrainDiagram(plan));
                case 3 -> System.out.println("\n" + visualService.visualizeCrossingOperations(plan));
            }

        } catch (Exception e) {
            System.out.println("\n✗ Error loading schedule: " + e.getMessage());
        }
    }

    /**
     * Generates dispatch plan for selected schedules
     */
    private void generateDispatchPlan() {
        System.out.println("\n═══ GENERATE DISPATCH PLAN ═══");

        List<Schedule> activeSchedules = scheduleRepository.findByStatus(
                Schedule.ScheduleStatus.SCHEDULED);

        if (activeSchedules.isEmpty()) {
            System.out.println("✗ No active schedules available.");
            return;
        }

        System.out.println("\n── Active Schedules ──");
        for (int i = 0; i < activeSchedules.size(); i++) {
            Schedule s = activeSchedules.get(i);
            System.out.println(String.format("  %d. %s - Train %s on Route %s - %s",
                    i + 1, s.getScheduleId(), s.getTrainId(), s.getRouteId(),
                    s.getDepartureDateTime().format(DATE_FORMATTER)));
        }

        System.out.println("\nSelect schedules to include:");
        System.out.println("  • Enter numbers separated by spaces (e.g., '1 2 3')");
        System.out.println("  • Enter 'all' for all schedules");
        System.out.print("Selection: ");

        String input = scanner.nextLine().trim();
        List<String> selectedIds = new ArrayList<>();

        if (input.equalsIgnoreCase("all")) {
            selectedIds = activeSchedules.stream()
                    .map(Schedule::getScheduleId)
                    .toList();
        } else {
            String[] numbers = input.split("\\s+");
            for (String num : numbers) {
                try {
                    int index = Integer.parseInt(num) - 1;
                    if (index >= 0 && index < activeSchedules.size()) {
                        selectedIds.add(activeSchedules.get(index).getScheduleId());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (selectedIds.isEmpty()) {
            System.out.println("\n✗ No schedules selected.");
            return;
        }

        try {
            System.out.println("\n⏳ Generating dispatch plan...");
            DispatchPlan plan = dispatcherService.createDispatchPlan(selectedIds);

            System.out.println("\n" + plan.toDetailedReport());

            // Validate
            var validation = dispatcherService.validateDispatchPlan(plan);
            System.out.println("\n── Validation Result ──");
            System.out.println(validation);

            // Show visualizations menu
            showVisualizationMenu(plan);

        } catch (Exception e) {
            System.out.println("\n✗ Error generating plan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows visualization options
     */
    private void showVisualizationMenu(DispatchPlan plan) {
        System.out.println("\n── Visualizations ──");
        System.out.println("1. Timeline View");
        System.out.println("2. Train Diagram");
        System.out.println("3. Crossing Operations");
        System.out.println("4. Conflict Heatmap");
        System.out.println("5. Delay Statistics");
        System.out.println("6. All Visualizations");
        System.out.println("0. Skip");

        int choice = validator.readInt("Select: ", 0, 6);

        switch (choice) {
            case 1 -> System.out.println("\n" + visualService.visualizeTimeline(plan));
            case 2 -> System.out.println("\n" + visualService.visualizeTrainDiagram(plan));
            case 3 -> System.out.println("\n" + visualService.visualizeCrossingOperations(plan));
            case 4 -> System.out.println("\n" + visualService.visualizeConflictHeatmap(plan));
            case 5 -> System.out.println("\n" + visualService.visualizeDelayStatistics(plan));
            case 6 -> {
                System.out.println("\n" + visualService.visualizeTimeline(plan));
                System.out.println("\n" + visualService.visualizeTrainDiagram(plan));
                System.out.println("\n" + visualService.visualizeCrossingOperations(plan));
                System.out.println("\n" + visualService.visualizeConflictHeatmap(plan));
                System.out.println("\n" + visualService.visualizeDelayStatistics(plan));
            }
        }
    }

    /**
     * Views daily dispatch plan
     */
    private void viewDailyDispatchPlan() {
        System.out.println("\n═══ DAILY DISPATCH PLAN ═══");

        LocalDateTime date = readDateTime("Date (yyyy-MM-dd HH:mm): ");

        try {
            System.out.println("\n⏳ Loading daily schedules...");
            DispatchPlan plan = dispatcherService.createDailyDispatchPlan(date);

            if (plan.getTotalTrains() == 0) {
                System.out.println("\n✗ No schedules found for this date.");
                return;
            }

            System.out.println("\n" + plan.toDetailedReport());

            showVisualizationMenu(plan);

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }

    /**
     * Exports dispatch plan
     */
    private void exportDispatchPlan() {
        System.out.println("\n═══ EXPORT DISPATCH PLAN ═══");
        System.out.println("\n⚠️  Export feature not yet implemented");
        System.out.println("Future capabilities:");
        System.out.println("  • CSV export for external tools");
        System.out.println("  • PDF report generation");
        System.out.println("  • JSON format for API integration");
    }

    /**
     * Reads datetime from user
     */
    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            try {
                String input = validator.readString(prompt);
                return LocalDateTime.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("✗ Invalid format. Use: yyyy-MM-dd HH:mm (e.g., 2025-11-18 08:30)");
            }
        }
    }

    /**
     * Pauses for user
     */
    private void pauseForUser() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}