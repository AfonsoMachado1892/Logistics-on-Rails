package ESINF.IO;

import ESINF.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Reads and validates CSV files required for USEI01:
 * - items.csv
 * - bays.csv
 * - wagons.csv
 *
 * Rejects invalid records but continues loading valid ones.
 * Produces deterministic messages through the ImportLogger.
 */
public class DataImporter {

    private final ImportLogger logger;

    private final Map<String, Item> items = new HashMap<>();
    private final Map<String, Warehouse> warehouses = new HashMap<>();
    private final List<Wagon> wagons = new ArrayList<>();
    private final Set<String> boxIds = new HashSet<>();

    /**
     * Constructs a new data importer
     *
     * @param logger the import logger for tracking errors and information
     */
    public DataImporter(ImportLogger logger) {
        this.logger = logger;
    }

    // -------------------------------------------------------------
    // ENTRADA PRINCIPAL
    // -------------------------------------------------------------
    public void loadAll(String itemsFile, String baysFile, String wagonsFile) throws IOException {
        loadItems(itemsFile);
        loadBays(baysFile);
        loadWagons(wagonsFile);
    }

    // -------------------------------------------------------------
    // ITEMS
    // -------------------------------------------------------------
    public void loadItems(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 6) {
                    logger.error("items.csv", lineNo, "Invalid number of columns (expected 6): " + line);
                    continue;
                }

                try {
                    String sku = parts[0].trim();
                    String name = parts[1].trim();
                    String category = parts[2].trim();
                    String unit = parts[3].trim();
                    double volume = Double.parseDouble(parts[4].trim());
                    double weight = Double.parseDouble(parts[5].trim());

                    if (sku.isEmpty()) {
                        logger.error("items.csv", lineNo, "Empty SKU");
                        continue;
                    }
                    if (volume < 0 || weight < 0) {
                        logger.error("items.csv", lineNo, "Negative volume/weight for SKU=" + sku);
                        continue;
                    }

                    items.put(sku, new Item(sku, name, category, unit, volume, weight));

                } catch (NumberFormatException e) {
                    logger.error("items.csv", lineNo, "Invalid numeric value: " + line);
                } catch (Exception e) {
                    logger.error("items.csv", lineNo, "Unexpected error: " + e.getMessage());
                }
            }
        }

        logger.info("items.csv", 0, "Loaded " + items.size() + " valid items.");
    }

    // -------------------------------------------------------------
    // BAYS
    // -------------------------------------------------------------
    public void loadBays(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("[,;]");
                if (parts.length < 4) {
                    logger.error("bays.csv", lineNo, "Invalid number of columns (expected 4): " + line);
                    continue;
                }

                try {
                    String warehouseId = parts[0].trim();
                    int aisle = Integer.parseInt(parts[1].trim());
                    int bayNum = Integer.parseInt(parts[2].trim());
                    int capacity = Integer.parseInt(parts[3].trim());

                    if (capacity <= 0) {
                        logger.error("bays.csv", lineNo, "Invalid capacity " + capacity + " for bay " + bayNum);
                        continue;
                    }

                    Warehouse wh = warehouses.computeIfAbsent(warehouseId, Warehouse::new);
                    Aisle a = wh.getAisle(aisle);
                    if (a == null) {
                        a = new Aisle(aisle);
                        wh.addAisle(a);
                    }

                    if (a.getBay(bayNum) != null) {
                        logger.error("bays.csv", lineNo, "Duplicate bay " + bayNum + " in aisle " + aisle);
                        continue;
                    }

                    Bay bay = new Bay(bayNum, warehouseId, aisle, capacity);
                    a.addBay(bay);

                } catch (NumberFormatException e) {
                    logger.error("bays.csv", lineNo, "Numeric parse error: " + line);
                } catch (Exception e) {
                    logger.error("bays.csv", lineNo, "Unexpected error: " + e.getMessage());
                }
            }
        }

        logger.info("bays.csv", 0, "Loaded " + warehouses.size() + " warehouses.");
    }

    // -------------------------------------------------------------
    // WAGONS
    // -------------------------------------------------------------
    public void loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagonMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 6) {
                    logger.error("wagons.csv", lineNo, "Invalid number of columns (expected 6): " + line);
                    continue;
                }

                try {
                    String wagonId = parts[0].trim();
                    String boxId = parts[1].trim();
                    String sku = parts[2].trim();
                    int qty = Integer.parseInt(parts[3].trim());
                    String expiryStr = parts[4].trim();
                    String receivedStr = parts[5].trim();

                    // Validações
                    if (boxIds.contains(boxId)) {
                        logger.error("wagons.csv", lineNo, "Duplicate boxId=" + boxId);
                        continue;
                    }

                    if (!items.containsKey(sku)) {
                        logger.error("wagons.csv", lineNo, "Unknown SKU=" + sku + " in Wagon=" + wagonId);
                        continue;
                    }

                    if (qty <= 0) {
                        logger.error("wagons.csv", lineNo, "Invalid quantity=" + qty + " for Box=" + boxId);
                        continue;
                    }

                    if (receivedStr.isEmpty()) {
                        logger.error("wagons.csv", lineNo, "Missing receivedAt for Box=" + boxId);
                        continue;
                    }

                    LocalDate expiry = null;
                    if (!expiryStr.isEmpty()) {
                        try {
                            expiry = LocalDate.parse(expiryStr);
                        } catch (DateTimeParseException e) {
                            logger.error("wagons.csv", lineNo, "Invalid expiryDate=" + expiryStr);
                        }
                    }

                    LocalDateTime receivedAt;
                    try {
                        receivedAt = LocalDateTime.parse(receivedStr);
                    } catch (DateTimeParseException e) {
                        logger.error("wagons.csv", lineNo, "Invalid receivedAt=" + receivedStr);
                        continue;
                    }

                    Box box = new Box(boxId, sku, qty, expiry, receivedAt);
                    boxIds.add(boxId);

                    Wagon wagon = wagonMap.computeIfAbsent(wagonId, Wagon::new);
                    wagon.addBox(box);

                } catch (NumberFormatException e) {
                    logger.error("wagons.csv", lineNo, "Numeric parse error: " + line);
                } catch (Exception e) {
                    logger.error("wagons.csv", lineNo, "Unexpected error: " + e.getMessage());
                }
            }
        }

        wagons.addAll(wagonMap.values());
        logger.info("wagons.csv", 0, "Loaded " + wagons.size() + " wagons.");
    }

    // -------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------
    public Map<String, Item> getItems() { return items; }

    public Map<String, Warehouse> getWarehouses() { return warehouses; }

    public List<Wagon> getWagons() { return wagons; }
}
