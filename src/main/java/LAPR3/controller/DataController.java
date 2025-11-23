package LAPR3.controller;

import ESINF.IO.DataImporter;
import ESINF.IO.ImportLogger;
import ESINF.IO.OrderImporter;
import ESINF.model.Item;
import ESINF.model.Order;
import ESINF.model.Wagon;
import ESINF.model.Warehouse;
import ESINF.service.WarehouseManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Central controller for data management
 * Handles all data import operations and provides access to loaded data
 */
public class DataController {

    private ImportLogger logger;
    private DataImporter dataImporter;
    private OrderImporter orderImporter;

    // Loaded data
    private Map<String, Warehouse> warehouses;
    private Map<String, Item> items;
    private List<Wagon> wagons;
    private List<Order> orders;

    // Current working warehouse
    private Warehouse currentWarehouse;
    private WarehouseManager warehouseManager;

    // Data status flags
    private boolean itemsLoaded = false;
    private boolean baysLoaded = false;
    private boolean wagonsLoaded = false;
    private boolean ordersLoaded = false;

    public DataController() {
        this.logger = new ImportLogger();
    }

    /**
     * Load all base data (items, bays, wagons)
     */
    public void loadAllBaseData(String itemsPath, String baysPath, String wagonsPath) throws IOException {
        dataImporter = new DataImporter(logger);
        dataImporter.loadAll(itemsPath, baysPath, wagonsPath);

        this.warehouses = dataImporter.getWarehouses();
        this.items = dataImporter.getItems();
        this.wagons = dataImporter.getWagons();

        itemsLoaded = true;
        baysLoaded = true;
        wagonsLoaded = true;

        // Save log
        logger.saveToFile("Logs/import_log.txt");
    }

    /**
     * Load items from CSV
     */
    public void loadItems(String path) throws IOException {
        if (dataImporter == null) {
            dataImporter = new DataImporter(logger);
        }
        dataImporter.loadItems(path);
        this.items = dataImporter.getItems();
        itemsLoaded = true;
        logger.saveToFile("Logs/import_log.txt");
    }

    /**
     * Load bays from CSV
     */
    public void loadBays(String path) throws IOException {
        if (dataImporter == null) {
            dataImporter = new DataImporter(logger);
        }
        dataImporter.loadBays(path);
        this.warehouses = dataImporter.getWarehouses();
        baysLoaded = true;
        logger.saveToFile("Logs/import_log.txt");
    }

    /**
     * Load wagons from CSV
     */
    public void loadWagons(String path) throws IOException {
        if (dataImporter == null) {
            dataImporter = new DataImporter(logger);
        }
        if (!itemsLoaded) {
            throw new IllegalStateException("Items must be loaded before wagons!");
        }
        dataImporter.loadWagons(path);
        this.wagons = dataImporter.getWagons();
        wagonsLoaded = true;
        logger.saveToFile("Logs/import_log.txt");
    }

    /**
     * Load orders and order lines
     */
    public void loadOrders(String ordersPath, String orderLinesPath) throws IOException {
        if (!itemsLoaded) {
            throw new IllegalStateException("Items must be loaded before orders!");
        }

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersPath, orderLinesPath);
        this.orders = orderImporter.getOrders();
        ordersLoaded = true;
        logger.saveToFile("Logs/import_log.txt");
    }

    /**
     * Set current working warehouse
     */
    public boolean setCurrentWarehouse(String warehouseId) {
        if (warehouses == null || !warehouses.containsKey(warehouseId)) {
            return false;
        }

        this.currentWarehouse = warehouses.get(warehouseId);
        this.warehouseManager = new WarehouseManager(currentWarehouse);
        return true;
    }

    /**
     * Get available warehouse IDs
     */
    public List<String> getAvailableWarehouses() {
        if (warehouses == null) return List.of();
        return warehouses.keySet().stream().sorted().toList();
    }

    // Getters
    public Map<String, Warehouse> getWarehouses() { return warehouses; }
    public Map<String, Item> getItems() { return items; }
    public List<Wagon> getWagons() { return wagons; }
    public List<Order> getOrders() { return orders; }
    public Warehouse getCurrentWarehouse() { return currentWarehouse; }
    public WarehouseManager getWarehouseManager() { return warehouseManager; }
    public ImportLogger getLogger() { return logger; }

    // Status checkers
    public boolean isItemsLoaded() { return itemsLoaded; }
    public boolean isBaysLoaded() { return baysLoaded; }
    public boolean isWagonsLoaded() { return wagonsLoaded; }
    public boolean isOrdersLoaded() { return ordersLoaded; }
    public boolean isWarehouseSelected() { return currentWarehouse != null; }

    /**
     * Get summary of loaded data
     */
    public String getDataSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══ DATA STATUS ═══\n");
        sb.append(String.format("Items:      %s (%d loaded)\n",
                itemsLoaded ? "✓" : "0", items != null ? items.size() : 0));
        sb.append(String.format("Warehouses: %s (%d loaded)\n",
                baysLoaded ? "✓" : "0", warehouses != null ? warehouses.size() : 0));
        sb.append(String.format("Wagons:     %s (%d loaded)\n",
                wagonsLoaded ? "✓" : "0", wagons != null ? wagons.size() : 0));
        sb.append(String.format("Orders:     %s (%d loaded)\n",
                ordersLoaded ? "✓" : "0", orders != null ? orders.size() : 0));
        sb.append(String.format("Current WH: %s\n",
                currentWarehouse != null ? currentWarehouse.getId() : "None"));
        sb.append("═══════════════════\n");
        return sb.toString();
    }
}