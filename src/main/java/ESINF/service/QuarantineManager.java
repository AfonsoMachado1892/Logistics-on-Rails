package ESINF.service;

import ESINF.IO.AuditLogger;
import ESINF.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controls the quarantine inspection process for returned goods.
 * Implements USEI05 - Returns & Quarantine.
 */
public class QuarantineManager {

    /** Reference to the warehouse for restocking items. */
    private final Warehouse warehouse;

    /** Logger used to write audit records for all quarantine actions. */
    private final AuditLogger auditLogger;

    /** Catalog of all valid items (SKUs) in the warehouse. */
    private final Map<String, Item> itemCatalog;

    /**
     * Creates a new QuarantineManager.
     *
     * @param warehouse warehouse where restocked items are stored
     * @param auditLogger logger for audit records
     * @param itemCatalog catalog of valid items (SKUs)
     */
    public QuarantineManager(Warehouse warehouse, AuditLogger auditLogger,
                             Map<String, Item> itemCatalog) {
        this.warehouse = warehouse;
        this.auditLogger = auditLogger;
        this.itemCatalog = itemCatalog;
    }

    /**
     * Processes all returns in reverse chronological order (LIFO - latest first).
     * Returns are already sorted by {@link ESINF.IO.ReturnImporter}.
     *
     * @param returnsList list of returned items to inspect
     */
    public void processReturns(List<ReturnItem> returnsList) {
        for (ReturnItem ret : returnsList) {
            processReturn(ret);
        }
    }

    /**
     * Processes a single return item.
     * Determines if it should be restocked or discarded and logs the result.
     *
     * @param ret the return item to process
     */
    private void processReturn(ReturnItem ret) {
        // Validate SKU exists in catalog
        if (!itemCatalog.containsKey(ret.getSku())) {
            auditLogger.writeEntry(ret.getReturnId(), ret.getSku(), ret.getQty(),
                    "Discarded-InvalidSKU");
            return;
        }

        // Discard damaged or expired goods
        if (ret.shouldDiscard()) {
            auditLogger.writeEntry(ret.getReturnId(), ret.getSku(), ret.getQty(), "Discarded");
            return;
        }

        // Restock eligible items
        if (ret.shouldRestock()) {
            restockItem(ret);
        }
    }

    /**
     * Handles the restocking of a valid returned item.
     * If the item has expired since the return, it is discarded instead.
     *
     * @param ret the return item to restock
     */
    private void restockItem(ReturnItem ret) {
        // Check if product expired since return
        if (ret.getExpiryDate() != null && ret.getExpiryDate().isBefore(LocalDate.now())) {
            auditLogger.writeEntry(ret.getReturnId(), ret.getSku(), ret.getQty(),
                    "Discarded-ExpiredDuringQuarantine");
            return;
        }

        // Create new box for restocking
        Box newBox = new Box(
                "RET-" + ret.getReturnId(),
                ret.getSku(),
                ret.getQty(),
                ret.getExpiryDate(),
                LocalDateTime.now()  // receivedAt = now
        );

        // Try to restock using FEFO/FIFO insertion rules
        boolean success = tryRestock(newBox);
        String action = success ? "Restocked" : "FailedRestock-NoSpace";
        auditLogger.writeEntry(ret.getReturnId(), ret.getSku(), ret.getQty(), action);
    }

    /**
     * Attempts to restock a box following FEFO/FIFO insertion rules.
     *
     * @param box the box to restock
     * @return true if restocking succeeded, false otherwise
     */
    private boolean tryRestock(Box box) {
        try {
            // Strategy 1: Find bay with same SKU (preferred)
            for (Aisle aisle : warehouse.getAisles().values()) {
                for (Bay bay : aisle.getBays().values()) {
                    if (!bay.isFull() && hasSameSku(bay, box.getSku())) {
                        bay.addBox(box);
                        box.setBay(bay);
                        return true;
                    }
                }
            }

            // Strategy 2: Use first available bay
            for (Aisle aisle : warehouse.getAisles().values()) {
                for (Bay bay : aisle.getBays().values()) {
                    if (!bay.isFull()) {
                        bay.addBox(box);
                        box.setBay(bay);
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error while restocking " + box.getBoxId() + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Checks whether a bay contains boxes with the same SKU.
     *
     * @param bay the bay to check
     * @param sku the SKU to look for
     * @return true if the bay contains the given SKU
     */
    private boolean hasSameSku(Bay bay, String sku) {
        if (bay.isEmpty()) return false;
        return bay.getBoxes().stream()
                .anyMatch(b -> b.getSku().equals(sku));
    }
}
