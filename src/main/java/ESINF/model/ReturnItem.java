package ESINF.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a returned product item placed into quarantine for inspection.
 * Each return includes details such as SKU, quantity, reason, and timestamps.
 */
public class ReturnItem {

    /** Unique identifier of the return. */
    private final String returnId;

    /** SKU (Stock Keeping Unit) of the returned product. */
    private final String sku;

    /** Quantity of items returned. */
    private final int qty;

    /** Reason for the return (e.g., damaged, expired, customer-remorse). */
    private final String reason;

    /** Timestamp of when the return was registered. */
    private final LocalDateTime timestamp;

    /** Optional expiry date, used for perishable products. */
    private final LocalDate expiryDate;

    /**
     * Creates a new ReturnItem.
     *
     * @param returnId unique return identifier
     * @param sku SKU of the product
     * @param qty quantity of returned items
     * @param reason reason for return
     * @param timestamp date and time when the return occurred
     * @param expiryDate expiry date (nullable for non-perishables)
     * @throws IllegalArgumentException if any required field is invalid
     */
    public ReturnItem(String returnId, String sku, int qty, String reason,
                      LocalDateTime timestamp, LocalDate expiryDate) {

        if (returnId == null || returnId.isBlank())
            throw new IllegalArgumentException("ReturnId cannot be null or empty.");
        if (sku == null || sku.isBlank())
            throw new IllegalArgumentException("SKU cannot be null or empty.");
        if (qty <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("Reason cannot be null or empty.");
        if (timestamp == null)
            throw new IllegalArgumentException("Timestamp cannot be null.");

        this.returnId = returnId;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    /** @return the return ID */
    public String getReturnId() { return returnId; }

    /** @return the SKU of the product */
    public String getSku() { return sku; }

    /** @return the quantity returned */
    public int getQty() { return qty; }

    /** @return the reason for the return */
    public String getReason() { return reason; }

    /** @return the timestamp of the return */
    public LocalDateTime getTimestamp() { return timestamp; }

    /** @return the expiry date, or null if not applicable */
    public LocalDate getExpiryDate() { return expiryDate; }

    /**
     * Determines if the returned item can be restocked.
     * @return true if the item can be restocked
     */
    public boolean shouldRestock() {
        return reason.equalsIgnoreCase("customer-remorse") || reason.equalsIgnoreCase("cycle-count");
    }

    /**
     * Determines if the returned item must be discarded.
     * @return true if the item must be discarded
     */
    public boolean shouldDiscard() {
        return reason.equalsIgnoreCase("damaged") || reason.equalsIgnoreCase("expired");
    }

    @Override
    public String toString() {
        return String.format("Return[%s | SKU=%s | qty=%d | reason=%s | ts=%s | exp=%s]",
                returnId, sku, qty, reason, timestamp, expiryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnItem r)) return false;
        return Objects.equals(returnId, r.returnId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnId);
    }
}
