package ESINF.model;

/**
 * Represents an allocation of items from a specific box to an order line.
 * Contains information about the order, SKU, quantity, and storage location.
 */
public class Allocation {
    private final String orderId;
    private final int lineNo;
    private final String sku;
    private final int qty;
    private final String boxId;
    private final String aisle;
    private final String bay;

    /**
     * Constructs a new allocation
     *
     * @param orderId the order identifier
     * @param lineNo the order line number
     * @param sku the product SKU
     * @param qty the quantity allocated
     * @param boxId the box identifier
     * @param aisle the aisle number as string
     * @param bay the bay number as string
     */
    public Allocation(String orderId, int lineNo, String sku, int qty, String boxId, String aisle, String bay) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
        this.boxId = boxId;
        this.aisle = aisle;
        this.bay = bay;
    }

    /**
     * Gets the order identifier
     *
     * @return the order ID
     */
    public String getOrderId() { return orderId; }

    /**
     * Gets the line number
     *
     * @return the line number
     */
    public int getLineNo() { return lineNo; }

    /**
     * Gets the SKU
     *
     * @return the SKU
     */
    public String getSku() { return sku; }

    /**
     * Gets the allocated quantity
     *
     * @return the allocated quantity
     */
    public int getQty() { return qty; }

    /**
     * Gets the box identifier
     *
     * @return the box ID
     */
    public String getBoxId() { return boxId; }

    /**
     * Gets the aisle number
     *
     * @return the aisle number as string
     */
    public String getAisle() { return aisle; }

    /**
     * Gets the bay number
     *
     * @return the bay number as string
     */
    public String getBay() { return bay; }

    @Override
    public String toString() {
        return String.format("Allocation[%s line %d | SKU=%s | qty=%d | box=%s | aisle=%s | bay=%s]",
                orderId, lineNo, sku, qty, boxId, aisle, bay);
    }
}
