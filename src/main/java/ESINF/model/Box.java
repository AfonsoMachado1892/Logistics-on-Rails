package ESINF.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Box implements Comparable<Box> {

    private String boxId;              // Identificador único da caixa
    private String sku;                // SKU do produto (referência a Item)
    private int qty;                   // Quantidade total na caixa
    private LocalDate expiryDate;      // Data de validade (pode ser null)
    private LocalDateTime receivedAt;  // Data/hora de receção no terminal
    private Bay bay;
    // Construtor
    public Box(String boxId, String sku, int qty, LocalDate expiryDate, LocalDateTime receivedAt) {
        this.boxId = boxId;
        this.sku = sku;
        this.qty = qty;
        this.expiryDate = expiryDate;
        this.receivedAt = receivedAt;
    }

    // Getters e Setters
    public String getBoxId() { return boxId; }
    public String getSku() { return sku; }
    public int getQty() { return qty; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public Bay getBay() { return bay; }
    public void setBay(Bay bay) { this.bay = bay; }
    public void setQty(int qty) { this.qty = qty; }

    public boolean isPerishable() {
        return expiryDate != null;
    }

    // Ordem FEFO/FIFO
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
