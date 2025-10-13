package ESINF.model;

public class Allocation {
    private final String orderId;
    private final int lineNo;
    private final String sku;
    private final int qty;
    private final String boxId;
    private final String aisle;
    private final String bay;

    public Allocation(String orderId, int lineNo, String sku, int qty, String boxId, String aisle, String bay) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
        this.boxId = boxId;
        this.aisle = aisle;
        this.bay = bay;
    }

    public String getOrderId() { return orderId; }
    public int getLineNo() { return lineNo; }
    public String getSku() { return sku; }
    public int getQty() { return qty; }
    public String getBoxId() { return boxId; }
    public String getAisle() { return aisle; }
    public String getBay() { return bay; }

    @Override
    public String toString() {
        return String.format("Allocation[%s line %d | SKU=%s | qty=%d | box=%s | aisle=%s | bay=%s]",
                orderId, lineNo, sku, qty, boxId, aisle, bay);
    }
}
