package ESINF.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a box containing items in the warehouse.
 * Boxes are sorted using FEFO/FIFO ordering (First Expired First Out / First In First Out).
 */
public class Box implements Comparable<Box> {

    private String boxId;              // Identificador único da caixa
    private String sku;                // SKU do produto (referência a Item)
    private int qty;                   // Quantidade total na caixa
    private LocalDate expiryDate;      // Data de validade (pode ser null)
    private LocalDateTime receivedAt;  // Data/hora de receção no terminal
    private Bay bay;

    /**
     * Constructs a new box
     *
     * @param boxId unique box identifier
     * @param sku the product SKU
     * @param qty the quantity in the box
     * @param expiryDate the expiry date (null if non-perishable)
     * @param receivedAt the date and time the box was received
     */
    public Box(String boxId, String sku, int qty, LocalDate expiryDate, LocalDateTime receivedAt) {
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
    }

    /**
     * Gets the box identifier
     *
     * @return the box ID
     */
    public String getBoxId() { return boxId; }

    /**
     * Gets the product SKU
     *
     * @return the SKU
     */
    public String getSku() { return sku; }

    /**
     * Gets the quantity in the box
     *
     * @return the quantity
     */
    public int getQty() { return qty; }

    /**
     * Gets the expiry date
     *
     * @return the expiry date, or null if non-perishable
     */
    public LocalDate getExpiryDate() { return expiryDate; }

    /**
     * Gets the date and time the box was received
     *
     * @return the received date/time
     */
    public LocalDateTime getReceivedAt() { return receivedAt; }

    /**
     * Gets the bay where this box is stored
     *
     * @return the bay, or null if not allocated
     */
    public Bay getBay() { return bay; }

    /**
     * Sets the bay where this box is stored
     *
     * @param bay the bay to assign to this box
     */
    public void setBay(Bay bay) { this.bay = bay; }

    /**
     * Sets the quantity in the box
     *
     * @param qty the new quantity
     */
    public void setQty(int qty) { this.qty = qty; }

    /**
     * Checks if this box contains perishable items
     *
     * @return true if the box has an expiry date
     */
    public boolean isPerishable() {
        return expiryDate != null;
    }

    /**
     * Gets the aisle number where this box is stored
     *
     * @return the aisle number, or null if not allocated to a bay
     */
    public Integer getAisleNumber() {
        return (bay != null) ? bay.getAisleNumber() : null;
    }



    /**
     * Compares this box with another using FEFO/FIFO ordering
     * First by expiry date (if available), then by received date, finally by box ID
     *
     * @param other the box to compare to
     * @return a negative integer, zero, or a positive integer if this box is less than, equal to, or greater than the other box
     */
    @Override
    public int compareTo(Box other) {
        // FEFO (por expiryDate, depois FIFO)
        if (this.expiryDate != null || other.expiryDate != null) {
            if (this.expiryDate == null) return 1;
            if (other.expiryDate == null) return -1;
            int cmp = this.expiryDate.compareTo(other.expiryDate);
            if (cmp != 0) return cmp;
        }
        int cmp = this.receivedAt.compareTo(other.receivedAt);
        if (cmp != 0) return cmp;
        return this.boxId.compareTo(other.boxId);
    }

    @Override
    public String toString() {
        return String.format("Box[%s, SKU=%s, Qty=%d, Exp=%s, Rec=%s]",
                boxId, sku, qty, expiryDate, receivedAt);
    }
}
