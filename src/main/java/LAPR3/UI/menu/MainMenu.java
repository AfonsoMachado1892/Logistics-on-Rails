package LAPR3.UI.menu;

import ESINF.controller.MinimalBackboneController;
import ESINF.controller.StationQueryController;
import ESINF.controller.UpgradePlanController;
import ESINF.controller.RiskAwareShortestPathController;
import ESINF.repository.RailwayNetworkRepository;
import LAPR3.UI.menu.MinimalBackboneMenu;
import LAPR3.UI.menu.FreightAssignmentMenu;
import LAPR3.controller.DataController;
import LAPR3.UI.utils.InputValidator;
import LAPR3.repository.*;
import LAPR3.service.*;

import java.util.Scanner;

/**
 * Main menu for the Cargo Handling System
 * Updated for USLP08/USLP09/USLP10
 *
 * Changes:
 * - USLP08: RoutePlanningService (in-memory routes)
 * - USLP09: TrainAssemblyService (in-memory assembled trains)
 * - USLP10: TrafficManagerMenu with path planning
 *
 * @author LAPR3 Team
 * @version 4.0 (USLP08/09/10)
 */
public class MainMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final InputValidator validator;
    private final StationQueryController stationController;
    private final UpgradePlanController upgradePlanController;
    private final MinimalBackboneController minimalBackboneController;
    private final CentralityAnalysisMenu centralityAnalysisMenu;

    // ===== SERVICES (USLP08/09) =====
    private final RoutePlanningService routePlanningService;
    private final TrainAssemblyService trainAssemblyService;
    // ================================

    private final DataImportMenu dataImportMenu;
    private final WagonOperationsMenu wagonMenu;
    private final OrderManagementMenu orderMenu;
    private final ReturnsManagementMenu returnsMenu;
    private final PickingManagementMenu pickingMenu;
    private final PickPathSequencingMenu pickPathMenu;

    private final StationQueryMenu stationQueryMenu;
    private final KDTreeMenu kdTreeMenu;
    private final GeographicSearchMenu geoSearchMenu;
    private final ProximitySearchMenu proximitySearchMenu;
    private final RadiusSearchResultMenu radiusSearchResultMenu;

    private final FreightAssignmentMenu freightsAssigmentMenu;
    private final TrafficManagerMenu trafficManagerMenu;

    private final UpgradePlanMenu upgradePlanMenu;
    private final MinimalBackboneMenu minimalBackboneMenu;
    private final RiskAwareShortestPathMenu riskAwareShortestPathMenu;

    private final TrainAssemblyMenu trainAssemblyMenu;

    public MainMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;

        // -------------------------------------------------------------------------
        // BOOTSTRAP - CARREGAMENTO DA BASE DE DADOS (LAPR3)
        // -------------------------------------------------------------------------
        System.out.println("\n[System] Connecting to Oracle Database (LAPR3 Data)...");

        // 1. Instanciar Repositórios
        FacilityRepository facilityRepo = new FacilityRepository();
        LineRepository lineRepo = new LineRepository();
        SegmentRepository segmentRepo = new SegmentRepository();
        LocomotiveRepository locoRepo = new LocomotiveRepository();
        WagonRepository wagonRepo = new WagonRepository();
        TrainRepository trainRepo = new TrainRepository();
        ScheduleRepository scheduleRepo = new ScheduleRepository();
        RouteRepository routeRepo = new RouteRepository();
        FreightRepository freightRepo = new FreightRepository();

        // 2. Carregar Dados
        try {
            facilityRepo.loadFromDatabase();
            lineRepo.loadFromDatabase();
            segmentRepo.loadFromDatabase();
            locoRepo.loadFromDatabase();
            wagonRepo.loadFromDatabase();
            trainRepo.loadFromDatabase();
            routeRepo.loadFromDatabase();
            freightRepo.loadFromDatabase();
            scheduleRepo.loadFromDatabase();

            System.out.println("[System] LAPR3 Database Load Complete.\n");
        } catch (Exception e) {
            System.err.println("[AVISO] Falha ao ler da BD. Os menus LAPR3 podem estar vazios ou usar dados parciais.");
            System.err.println("Erro: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // INICIALIZAÇÃO ESINF (Mantido Igual)
        // -------------------------------------------------------------------------
        this.stationController = new StationQueryController();
        this.upgradePlanController = new UpgradePlanController();

        RailwayNetworkRepository railNetworkRepo = upgradePlanController.getRepository();
        this.minimalBackboneController = new MinimalBackboneController(railNetworkRepo);
        RiskAwareShortestPathController riskController = new RiskAwareShortestPathController(railNetworkRepo);

        // -------------------------------------------------------------------------
        // INICIALIZAÇÃO MENUS
        // -------------------------------------------------------------------------

        this.dataImportMenu = new DataImportMenu(scanner, dataController, validator);
        this.wagonMenu = new WagonOperationsMenu(scanner, dataController, validator);
        this.orderMenu = new OrderManagementMenu(scanner, dataController, validator);
        this.returnsMenu = new ReturnsManagementMenu(scanner, dataController, validator);
        this.pickingMenu = new PickingManagementMenu(scanner, dataController, validator);
        this.pickPathMenu = new PickPathSequencingMenu(scanner, dataController, validator);

        this.stationQueryMenu = new StationQueryMenu(scanner, validator, stationController);
        this.kdTreeMenu = new KDTreeMenu(scanner, validator, stationController);
        this.geoSearchMenu = new GeographicSearchMenu(scanner, validator, stationController);
        this.proximitySearchMenu = new ProximitySearchMenu(scanner, validator, stationController);
        this.radiusSearchResultMenu = new RadiusSearchResultMenu(scanner, validator, stationController);

        // ===== USLP08: Route Planning Service (IN-MEMORY) =====
        this.routePlanningService = new RoutePlanningService(
                freightRepo,
                facilityRepo
        );

        // ===== USLP09: Train Assembly Service (IN-MEMORY) =====
        this.trainAssemblyService = new TrainAssemblyService(
                locoRepo,
                wagonRepo,
                routePlanningService
        );
        // ======================================================

        // USLP08: Freight Assignment Menu
        this.freightsAssigmentMenu = new FreightAssignmentMenu(
                freightRepo,
                facilityRepo,
                routePlanningService
        );

        // USLP09: Train Assembly Menu
        this.trainAssemblyMenu = new TrainAssemblyMenu(
                scanner,
                validator,
                routePlanningService,
                locoRepo,
                wagonRepo,
                facilityRepo,
                trainRepo,
                scheduleRepo,
                lineRepo

                );

        // USLP10: Traffic Manager Menu
        this.trafficManagerMenu = initializeTrafficManagerMenu(
                facilityRepo, lineRepo, segmentRepo, routeRepo,
                locoRepo, wagonRepo, trainRepo, scheduleRepo
        );

        // Initialize ESINF menus
        this.upgradePlanMenu = new UpgradePlanMenu(scanner, validator, upgradePlanController);
        this.minimalBackboneMenu = new MinimalBackboneMenu(scanner, validator, minimalBackboneController);
        this.centralityAnalysisMenu = new CentralityAnalysisMenu(scanner, validator, railNetworkRepo);
        this.riskAwareShortestPathMenu = new RiskAwareShortestPathMenu(scanner, validator, riskController);
    }

    /**
     * USLP10: Initializes TrafficManagerMenu
     */
    private TrafficManagerMenu initializeTrafficManagerMenu(
            FacilityRepository facilityRepo,
            LineRepository lineRepo,
            SegmentRepository segmentRepo,
            RouteRepository routeRepo,
            LocomotiveRepository locoRepo,
            WagonRepository wagonRepo,
            TrainRepository trainRepo,
            ScheduleRepository scheduleRepo) {

        // Initialize services
        RoutePathService routePathService = new RoutePathService(
                routeRepo, facilityRepo, lineRepo, segmentRepo);

        ScheduleCalculationService scheduleService = new ScheduleCalculationService(
                routeRepo, facilityRepo, lineRepo, segmentRepo, trainRepo, scheduleRepo);

        CrossingConflictDetectionService conflictService =
                new CrossingConflictDetectionService(scheduleService, segmentRepo);

        CrossingResolutionService resolutionService =
                new CrossingResolutionService(conflictService, scheduleService, facilityRepo);

        TrainDispatcherService dispatcherService = new TrainDispatcherService(
                scheduleService, conflictService, resolutionService, scheduleRepo, segmentRepo);

        ScheduleVisualizationService visualService =
                new ScheduleVisualizationService(facilityRepo);

        // Initialize TrainDataImportMenu
        TrainDataImportMenu trainImportMenu = new TrainDataImportMenu(
                scanner, validator, facilityRepo, lineRepo, segmentRepo,
                locoRepo, wagonRepo, trainRepo, routeRepo, scheduleRepo
        );

        // Return TrafficManagerMenu with routePlanningService
        return new TrafficManagerMenu(
                scanner, validator, dispatcherService, routeRepo, facilityRepo,
                trainRepo, scheduleRepo, routePathService, visualService,
                trainImportMenu, lineRepo, segmentRepo, routePlanningService
        );
    }

    public void display() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   CARGO HANDLING SYSTEM - MAIN MENU                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Show data status
        System.out.println(dataController.getDataSummary());

        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  WAREHOUSE OPERATIONS (Sprint 1)                           │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  1. Data Import & Management                               │");
        System.out.println("│  2. Wagon Operations (USEI01 / BD)                         │");
        System.out.println("│  3. Order Management (USEI02)                              │");
        System.out.println("│  4. Picking Plans (USEI03)                                 │");
        System.out.println("│  5. Pick Path Sequencing (USEI04)                          │");
        System.out.println("│  6. Returns & Quarantine (USEI05)                          │");
        System.out.println("│  7. View Current Inventory                                 │");
        System.out.println("│  8. View Logs                                              │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  SPATIAL QUERIES (Sprint 2)                                │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  10. Station Queries - Time Zone (USEI06)                  │");
        System.out.println("│  11. 2D-Tree Spatial Index (USEI07)                        │");
        System.out.println("│  12. Geographic Area Search (USEI08)                       │");
        System.out.println("│  13. Proximity Search (USEI09)                             │");
        System.out.println("│  14. Radius Search (USEI10)                                │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  RAILWAY NETWORK OPTIMIZATION (Sprint 3)                   │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  17. Directed Line Upgrade Plan (USEI11)                   │");
        System.out.println("│  18. Minimal Backbone Network (USEI12)                     │");
        System.out.println("│  19. Rail Hub Centrality Analysis (USEI13)                 │");
        System.out.println("│  20. Risk-Aware Shortest Paths (USEI15)                    │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  RAILWAY OPERATIONS (LAPR3)                                │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  9. Travel Time Calculation (USLP03)                       │");
        System.out.println("│  15. Freight Assignment (USLP08)                           │");
        System.out.println("│  21. Train Assembly and Route Assignment (USLP09)          │");
        System.out.println("│  16. Traffic Manager (USLP10)                              │");
        System.out.println("├────────────────────────────────────────────────────────────┤");
        System.out.println("│  0. Exit                                                   │");
        System.out.println("└────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    public void handleChoice(int choice) {
        switch (choice) {
            case 1 -> dataImportMenu.show();
            case 2 -> wagonMenu.show();
            case 3 -> orderMenu.show();
            case 4 -> pickingMenu.show();
            case 5 -> pickPathMenu.show();
            case 6 -> returnsMenu.show();
            case 7 -> viewInventory();
            case 8 -> viewLogs();
            case 10 -> stationQueryMenu.show();
            case 11 -> kdTreeMenu.show();
            case 12 -> geoSearchMenu.show();
            case 13 -> proximitySearchMenu.show();
            case 14 -> radiusSearchResultMenu.show();
            case 15 -> freightsAssigmentMenu.show();
            case 16 -> trafficManagerMenu.show();
            case 17 -> upgradePlanMenu.show();
            case 18 -> minimalBackboneMenu.show();
            case 19 -> centralityAnalysisMenu.run();
            case 20 -> riskAwareShortestPathMenu.show();
            case 21 -> trainAssemblyMenu.show();
            default -> System.out.println("Invalid option!");
        }
    }

    private void viewInventory() {
        System.out.println("\n═══ VIEW INVENTORY ═══");

        if (!dataController.isWarehouseSelected()) {
            System.out.println("No warehouse selected!");
            System.out.println("Please select a warehouse from the Data Import menu first.");
            return;
        }

        if (dataController.getWarehouseManager() == null) {
            System.out.println("Warehouse manager not initialized!");
            return;
        }

        dataController.getWarehouseManager().printInventory();
    }

    private void viewLogs() {
        System.out.println("\n═══ SYSTEM LOGS ═══");

        if (dataController.getLogger() == null) {
            System.out.println("No logs available yet.");
            return;
        }

        var logs = dataController.getLogger().getLogs();

        if (logs.isEmpty()) {
            System.out.println("No logs recorded.");
            return;
        }

        System.out.println("Total log entries: " + logs.size());
        System.out.println("\nRecent logs (last 20):");
        System.out.println("─".repeat(80));

        int start = Math.max(0, logs.size() - 20);
        for (int i = start; i < logs.size(); i++) {
            System.out.println(logs.get(i));
        }

        System.out.println("─".repeat(80));
        System.out.println("\nFull log saved to: Logs/import_log.txt");
    }
}