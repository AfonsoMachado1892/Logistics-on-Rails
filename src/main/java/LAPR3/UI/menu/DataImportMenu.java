package LAPR3.UI.menu;

import LAPR3.controller.DataController;
import LAPR3.UI.utils.InputValidator;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Submenu for data import operations
 */
public class DataImportMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final InputValidator validator;

    // Default paths
    private static final String DEFAULT_ITEMS = "warehouseMngt/train_station_dataset/items.csv";
    private static final String DEFAULT_BAYS = "warehouseMngt/train_station_dataset/bays.csv";
    private static final String DEFAULT_WAGONS = "warehouseMngt/train_station_dataset/wagons.csv";
    private static final String DEFAULT_ORDERS = "warehouseMngt/train_station_dataset/orders.csv";
    private static final String DEFAULT_ORDER_LINES = "warehouseMngt/train_station_dataset/order_lines.csv";

    public DataImportMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 7);

            if (choice == 0) {
                return; // Back to main menu
            }

            try {
                handleChoice(choice);
            } catch (IOException e) {
                System.err.println("Error loading file: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║          DATA IMPORT & MANAGEMENT                ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Load Items                                  │");
        System.out.println("│  2. Load Bays (Warehouses)                      │");
        System.out.println("│  3. Load Wagons                                 │");
        System.out.println("│  4. Load Orders                                 │");
        System.out.println("│  5. Load All Base Data (Items + Bays + Wagons)  │");
        System.out.println("│  6. Select Working Warehouse                    │");
        System.out.println("│  7. View Data Summary                           │");
        System.out.println("│  0. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) throws IOException {
        switch (choice) {
            case 1 -> loadItems();
            case 2 -> loadBays();
            case 3 -> loadWagons();
            case 4 -> loadOrders();
            case 5 -> loadAllBaseData();
            case 6 -> selectWarehouse();
            case 7 -> viewDataSummary();
        }
    }

    private void loadItems() throws IOException {
        System.out.println("\n═══ LOAD ITEMS ═══");
        String path = validator.readFilePath("Enter items CSV path", DEFAULT_ITEMS);

        System.out.println("Loading items...");
        dataController.loadItems(path);

        int count = dataController.getItems().size();
        System.out.println("Successfully loaded " + count + " items!");
    }

    private void loadBays() throws IOException {
        System.out.println("\n═══ LOAD BAYS ═══");
        String path = validator.readFilePath("Enter bays CSV path", DEFAULT_BAYS);

        System.out.println("Loading bays...");
        dataController.loadBays(path);

        int count = dataController.getWarehouses().size();
        System.out.println(" Successfully loaded " + count + " warehouse(s)!");
    }

    private void loadWagons() throws IOException {
        System.out.println("\n═══ LOAD WAGONS ═══");

        if (!dataController.isItemsLoaded()) {
            System.out.println("Items must be loaded first!");
            return;
        }

        String path = validator.readFilePath("Enter wagons CSV path", DEFAULT_WAGONS);

        System.out.println("Loading wagons...");
        dataController.loadWagons(path);

        int count = dataController.getWagons().size();
        System.out.println(" Successfully loaded " + count + " wagon(s)!");
    }

    private void loadOrders() throws IOException {
        System.out.println("\n═══ LOAD ORDERS ═══");

        if (!dataController.isItemsLoaded()) {
            System.out.println("Items must be loaded first!");
            return;
        }

        String ordersPath = validator.readFilePath("Enter orders CSV path", DEFAULT_ORDERS);
        String linesPath = validator.readFilePath("Enter order lines CSV path", DEFAULT_ORDER_LINES);

        System.out.println("Loading orders...");
        dataController.loadOrders(ordersPath, linesPath);

        int count = dataController.getOrders().size();
        System.out.println(" Successfully loaded " + count + " order(s)!");
    }

    private void loadAllBaseData() throws IOException {
        System.out.println("\n═══ LOAD ALL BASE DATA ═══");

        String itemsPath = validator.readFilePath("Items CSV path", DEFAULT_ITEMS);
        String baysPath = validator.readFilePath("Bays CSV path", DEFAULT_BAYS);
        String wagonsPath = validator.readFilePath("Wagons CSV path", DEFAULT_WAGONS);

        System.out.println("\nLoading all base data...");
        dataController.loadAllBaseData(itemsPath, baysPath, wagonsPath);

        System.out.println(" All base data loaded successfully!");
        System.out.println("  • Items: " + dataController.getItems().size());
        System.out.println("  • Warehouses: " + dataController.getWarehouses().size());
        System.out.println("  • Wagons: " + dataController.getWagons().size());
    }

    private void selectWarehouse() {
        System.out.println("\n═══ SELECT WAREHOUSE ═══");

        if (!dataController.isBaysLoaded()) {
            System.out.println("No warehouses loaded yet!");
            return;
        }

        List<String> warehouses = dataController.getAvailableWarehouses();

        if (warehouses.isEmpty()) {
            System.out.println("No warehouses available!");
            return;
        }

        System.out.println("\nAvailable warehouses:");
        for (int i = 0; i < warehouses.size(); i++) {
            String wh = warehouses.get(i);
            System.out.printf("  %d. %s%n", i + 1, wh);
        }

        int choice = validator.readSelection("\nSelect warehouse number: ", warehouses.size());
        String selectedId = warehouses.get(choice - 1);

        if (dataController.setCurrentWarehouse(selectedId)) {
            System.out.println(" Warehouse " + selectedId + " selected!");
        } else {
            System.out.println("Failed to select warehouse!");
        }
    }

    private void viewDataSummary() {
        System.out.println(dataController.getDataSummary());
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}