package LAPR3.controller;

import ESINF.IO.AuditLogger;
import ESINF.IO.ImportLogger;
import ESINF.IO.ReturnImporter;
import ESINF.model.ReturnItem;
import ESINF.service.QuarantineManager;

import java.io.IOException;
import java.util.List;

/**
 * Controls the loading and processing of product returns
 * and manages the quarantine inspection workflow.
 * Implements USEI05 - Returns & Quarantine.
 */
public class ReturnsController {

    /** Reference to the data controller for warehouse and item data. */
    private final DataController dataController;

    /** List of loaded return items. */
    private List<ReturnItem> returns;

    /** Flag indicating whether returns have been successfully loaded. */
    private boolean returnsLoaded = false;

    /**
     * Creates a new ReturnsController.
     *
     * @param dataController shared data controller containing warehouses and items
     */
    public ReturnsController(DataController dataController) {
        this.dataController = dataController;
    }

    /**
     * Loads returns data from the provided CSV file.
     * Validates entries and stores them for quarantine processing.
     *
     * @param filePath path to the returns.csv file
     * @throws IOException if the file cannot be read
     * @throws IllegalStateException if items have not been loaded first
     */
    public void loadReturns(String filePath) throws IOException {
        if (!dataController.isItemsLoaded()) {
            throw new IllegalStateException("Items must be loaded before returns!");
        }

        ImportLogger logger = dataController.getLogger();
        ReturnImporter importer = new ReturnImporter(logger, dataController.getItems());

        this.returns = importer.loadReturns(filePath);
        this.returnsLoaded = true;

        // Save import log
        logger.saveToFile("Logs/import_log.txt");
    }

    /**
     * Processes all loaded returns through the quarantine inspection process.
     * Returns are inspected in reverse chronological order (latest first).
     * After processing, the internal list of returns is cleared to prevent reprocessing.
     *
     * @throws IllegalStateException if returns are not loaded or no warehouse is selected
     */
    public void processReturns() {
        if (!returnsLoaded || returns == null || returns.isEmpty()) {
            throw new IllegalStateException("No returns to process!");
        }

        if (!dataController.isWarehouseSelected()) {
            throw new IllegalStateException("Warehouse must be selected!");
        }

        // Create audit logger
        AuditLogger auditLogger = new AuditLogger("Logs/returns_audit.log");

        // Create quarantine manager
        QuarantineManager quarantineManager = new QuarantineManager(
                dataController.getCurrentWarehouse(),
                auditLogger,
                dataController.getItems()
        );

        // Process returns
        System.out.println("Processing " + returns.size() + " returns...");
        quarantineManager.processReturns(returns);
        System.out.println("Processing complete!");

        // Clear the list after successful processing to prevent re-processing.
        this.returns.clear();
    }

    /**
     * Returns the list of loaded returns.
     *
     * @return list of return items, or null if none loaded
     */
    public List<ReturnItem> getReturns() {
        return returns;
    }

    /**
     * Gets the total number of loaded returns.
     *
     * @return number of loaded returns
     */
    public int getReturnsCount() {
        return returns != null ? returns.size() : 0;
    }

    /**
     * Checks whether returns have been successfully loaded and are pending processing.
     *
     * @return true if returns are loaded and the list is not empty, false otherwise
     */
    public boolean hasReturns() {
        return returnsLoaded && returns != null && !returns.isEmpty();
    }

    /**
     * Prints a simple summary of the returns list,
     * showing how many are to be restocked or discarded.
     */
    public void printReturnsSummary() {
        if (!hasReturns()) return;

        long toRestock = returns.stream()
                .filter(ReturnItem::shouldRestock)
                .count();

        long toDiscard = returns.stream()
                .filter(ReturnItem::shouldDiscard)
                .count();

        System.out.println("  • To Restock: " + toRestock);
        System.out.println("  • To Discard: " + toDiscard);
    }
}
