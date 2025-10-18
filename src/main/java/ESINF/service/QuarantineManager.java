package ESINF.service;

import ESINF.IO.AuditLogger;
import ESINF.model.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controls the quarantine inspection process for returned goods.
 */

public class QuarantineManager {

    private final Warehouse warehouse;
    private final AuditLogger auditLogger;

    public QuarantineManager(Warehouse warehouse, AuditLogger auditLogger) {
        this.warehouse = warehouse;
        this.auditLogger = auditLogger;
    }

    /**
     * Processes all returns in reverse chronological order.
     */
    public void processReturns(List<ReturnItem> returnsList) {
        for (ReturnItem ret : returnsList) {
            if (ret.shouldDiscard()) {
                auditLogger.writeEntry(ret.getReturnId(), ret.getSku(), ret.getQty(), "Discarded");
                continue;
            }

            if (ret.shouldRestock()) {
                Box newBox = new Box(
                        "RET-" + ret.getReturnId(),
                        ret.getSku(),
                        ret.getQty(),
                        ret.getExpiryDate(),
                        LocalDateTime.now()
                );

                boolean success = tryRestock(newBox);
                String action = success ? "Restocked" : "FailedRestock";
                auditLogger.writeEntry(ret.getReturnId(), ret.getSku(), ret.getQty(), action);
            }
        }
    }

    private boolean tryRestock(Box box) {
        try {
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
}
