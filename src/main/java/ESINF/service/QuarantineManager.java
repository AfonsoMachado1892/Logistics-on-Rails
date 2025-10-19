package ESINF.service;

import ESINF.IO.AuditLogger;
import ESINF.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controls the quarantine inspection process for returned goods.
 * Implements USEI05 - Returns & Quarantine
 */
public class QuarantineManager {

    private final Warehouse warehouse;
    private final AuditLogger auditLogger;
    private final Map<String, Item> itemCatalog;

    public QuarantineManager(Warehouse warehouse, AuditLogger auditLogger,
                             Map<String, Item> itemCatalog) {
        this.warehouse = warehouse;
        this.auditLogger = auditLogger;
        this.itemCatalog = itemCatalog;
    }

    /**
     * Processes all returns in reverse chronological order (LIFO - latest first).
     * Returns are already sorted by ReturnImporter.
     */
    public void processReturns(List<ReturnItem> returnsList) {
        for (ReturnItem ret : returnsList) {
            processReturn(ret);
        }
    }

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
     * Tries to restock a box following FEFO/FIFO insertion rules.
     * Strategy:
     * 1. Try to find bay with same SKU (keeps SKUs together)
     * 2. If not found, use first available bay
     * The PriorityQueue in Bay automatically maintains FEFO/FIFO order.
     */
    private boolean tryRestock(Box box) {
        try {
            // Strategy 1: Find bay with same SKU (preferred)
            for (Aisle aisle : warehouse.getAisles().values()) {
                for (Bay bay : aisle.getBays().values()) {
                    if (!bay.isFull() && hasSameSku(bay, box.getSku())) {
                        bay.addBox(box);  // PriorityQueue maintains FEFO/FIFO order
                        box.setBay(bay);
                        return true;
                    }
                }
            }

            // Strategy 2: Use first available bay
            for (Aisle aisle : warehouse.getAisles().values()) {
                for (Bay bay : aisle.getBays().values()) {
                    if (!bay.isFull()) {
                        bay.addBox(box);  // PriorityQueue maintains FEFO/FIFO order
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
     * Checks if bay contains boxes with the given SKU
     */
    private boolean hasSameSku(Bay bay, String sku) {
        if (bay.isEmpty()) return false;
        return bay.getBoxes().stream()
                .anyMatch(b -> b.getSku().equals(sku));
    }
}