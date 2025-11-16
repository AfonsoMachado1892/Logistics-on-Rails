package LAPR3.UI.menu;

import ESINF.controller.StationQueryController;
import LAPR3.controller.DataController;
import LAPR3.UI.utils.InputValidator;

import java.sql.SQLOutput;
import java.util.Scanner;

/**
 * Main menu for the Cargo Handling System
 * Updated to include USLP03 - Travel Time Calculation
 */
public class MainMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final InputValidator validator;
    private final StationQueryController stationController;

    private final DataImportMenu dataImportMenu;
    private final WagonOperationsMenu wagonMenu;
    private final OrderManagementMenu orderMenu;
    private final ReturnsManagementMenu returnsMenu;
    private final PickingManagementMenu pickingMenu;
    private final PickPathSequencingMenu pickPathMenu;
    private final TravelTimeMenu travelTimeMenu;
    private final StationQueryMenu stationQueryMenu;
    private final KDTreeMenu kdTreeMenu;
    private final GeographicSearchMenu geoSearchMenu;

    public MainMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;

        this.stationController = new StationQueryController();

        this.dataImportMenu = new DataImportMenu(scanner, dataController, validator);
        this.wagonMenu = new WagonOperationsMenu(scanner, dataController, validator);
        this.orderMenu = new OrderManagementMenu(scanner, dataController, validator);
        this.returnsMenu = new ReturnsManagementMenu(scanner, dataController, validator);
        this.pickingMenu = new PickingManagementMenu(scanner, dataController, validator);
        this.pickPathMenu = new PickPathSequencingMenu(scanner, dataController, validator);
        this.travelTimeMenu = new TravelTimeMenu(scanner, validator);
        this.stationQueryMenu = new StationQueryMenu(scanner, validator, stationController);
        this.kdTreeMenu = new KDTreeMenu(scanner, validator, stationController);
        this.geoSearchMenu = new GeographicSearchMenu(scanner, validator, stationController);
    }

    public void display() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║          CARGO HANDLING SYSTEM - MAIN MENU       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // Show data status
        System.out.println(dataController.getDataSummary());

        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  WAREHOUSE OPERATIONS (Sprint 1)                │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  1. Data Import & Management                    │");
        System.out.println("│  2. Wagon Operations (USEI01)                   │");
        System.out.println("│  3. Order Management (USEI02)                   │");
        System.out.println("│  4. Picking Plans (USEI03)                      │");
        System.out.println("│  5. Pick Path Sequencing (USEI04)               │");
        System.out.println("│  6. Returns & Quarantine (USEI05)               │");
        System.out.println("│  7. View Current Inventory                      │");
        System.out.println("│  8. View Logs                                   │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  SPATIAL QUERIES (Sprint 2)                     │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  10. Station Queries - Time Zone Index (USEI06) │");
        System.out.println("│  11. 2D-Tree Spatial Index (USEI07)             │");
        System.out.println("│  12. Geographic Area Search (USEI08)            │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  RAILWAY OPERATIONS                             │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  9. Travel Time Calculation (USLP03)            │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  0. Exit                                        │");
        System.out.println("└─────────────────────────────────────────────────┘");
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
            case 9 -> travelTimeMenu.show();
            case 10 -> stationQueryMenu.show();
            case 11 -> kdTreeMenu.show();
            case 12 -> geoSearchMenu.show();
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