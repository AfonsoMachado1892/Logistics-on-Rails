package LAPR3.controller;

import ESINF.IO.AuditLogger;
import ESINF.IO.ImportLogger;
import ESINF.IO.ReturnImporter;
import ESINF.model.ReturnItem;
import ESINF.service.QuarantineManager;

import java.io.IOException;
import java.util.List;

/**
 * Controller for returns and quarantine operations
 */
public class ReturnsController {

    private final DataController dataController;
    private List<ReturnItem> returns;
    private boolean returnsLoaded = false;

    public ReturnsController(DataController dataController) {
        this.dataController = dataController;
    }

    /**
     * Load returns from CSV file
     */
    public void loadReturns(String filePath) throws IOException {
        if (!dataController.isItemsLoaded()) {
            throw new IllegalStateException("Items must be loaded before returns!");
        }

        ImportLogger logger = dataController.getLogger();
        ReturnImporter importer = new ReturnImporter(logger, dataController.getItems());

        this.returns = importer.loadReturns(filePath);
        this.returnsLoaded = true;

        // Save log
        logger.saveToFile("Logs/import_log.txt");
    }

    /**
     * Process all returns through quarantine inspection
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
    }

    /**
     * Get loaded returns
     */
    public List<ReturnItem> getReturns() {
        return returns;
    }

    /**
     * Get count of loaded returns
     */
    public int getReturnsCount() {
        return returns != null ? returns.size() : 0;
    }

    /**
     * Check if returns are loaded
     */
    public boolean hasReturns() {
        return returnsLoaded && returns != null && !returns.isEmpty();
    }

    /**
     * Print summary of returns
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