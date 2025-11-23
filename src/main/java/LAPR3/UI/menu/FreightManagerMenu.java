package LAPR3.UI.menu;

import LAPR3.DTO.*;
import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.*;
import LAPR3.UI.utils.InputValidator;

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
                              TrainDataImportMenu dataImportMenu) {
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
                case 3 -> createManualRoute();
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
        System.out.println("  1. Data Import & Management ⚡");
        System.out.println("  ────────────────────────────────────────────");
        System.out.println("  2. List All Routes");
        System.out.println("  3. Find Best Route (Manual Calculation)");
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
            System.out.println("✗ No routes available.");
            return;
        }
        System.out.println("\nTotal routes: " + routes.size());
        System.out.println("─".repeat(80));
        System.out.printf("%-10s | %-10s | %-10s | %-15s%n", "Route ID", "Start ID", "End ID", "Segments");
        System.out.println("─".repeat(80));

        for (Route route : routes) {
            // Tentar obter contagem de segmentos via serviço ou lista direta
            int segmentCount = 0;
            try {
                segmentCount = routePathService.getAllSegments(route.getRouteId()).size();
            } catch (Exception e) { segmentCount = -1; }

            // CORREÇÃO: Usar getOriginFacilityId e getDestinationFacilityId
            System.out.printf("%-10s | %-10d | %-10d | %-15d%n",
                    route.getRouteId(),
                    route.getOriginFacilityId(),
                    route.getDestinationFacilityId(),
                    segmentCount
            );
        }
    }

    // --- NOVAS IMPLEMENTAÇÕES FUNCIONAIS ---

    private void createManualRoute() {
        System.out.println("\n═══ FIND BEST ROUTE ═══");
        System.out.println("Finds the shortest path between two facilities using the network topology.");

        // CORREÇÃO: Adicionar min e max (0 a Integer.MAX_VALUE)
        int originId = validator.readInt("Enter Origin Facility ID: ", 0, Integer.MAX_VALUE);
        int destId = validator.readInt("Enter Destination Facility ID: ", 0, Integer.MAX_VALUE);

        System.out.println("Calculating route...");
        Optional<Route> routeOpt = routePathService.findShortestRoute(originId, destId);

        if (routeOpt.isPresent()) {
            Route route = routeOpt.get();
            System.out.println("\n Route Found!");
            System.out.println("Route ID: " + route.getRouteId());
            System.out.println(routePathService.getRouteSummary(route.getRouteId()));

            double distance = routePathService.calculateRouteDistance(route.getRouteId());
            System.out.printf("Total Distance: %.2f meters%n", distance);
        } else {
            System.out.println(" No route found connecting Facility " + originId + " to " + destId);
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