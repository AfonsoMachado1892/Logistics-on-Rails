package ESINF.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

public class ReturnItem {
    private final String returnId;
    private final String sku;
    private final int qty;
    private final String reason;
    private final LocalDateTime timestamp;
    private final LocalDate expiryDate;

    public ReturnItem(String returnId, String sku, int qty, String reason, LocalDateTime timestamp, LocalDate expiryDate) {

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

    public String getReturnId() { return returnId; }
    public String getSku() { return sku; }
    public int getQty() { return qty; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LocalDate getExpiryDate() { return expiryDate; }


    public boolean shouldRestock() {
        return reason.equalsIgnoreCase("customer-remorse") || reason.equalsIgnoreCase("cycle-count");
    }

    public boolean shouldDiscard() {
        return reason.equalsIgnoreCase("damaged") || reason.equalsIgnoreCase("expired");
    }

    @Override
    public String toString() {
        return String.format("Return[%s | SKU=%s | qty=%d | reason=%s | ts=%s | exp=%s]",
                returnId, sku, qty, reason, timestamp, expiryDate);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReturnItem r)) return false;
        return Objects.equals(returnId, r.returnId);
    }

    @Override public int hashCode() {
        return Objects.hash(returnId);
    }
}
