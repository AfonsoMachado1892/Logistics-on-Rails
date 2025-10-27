package ESINF.model;

/**
 * Represents the eligibility status of an order line.
 * Contains information about requested quantity, allocated quantity, and fulfillment status.
 */
public class OrderEligibility {
    private final String orderId;
    private final int lineNo;
    private final String sku;
    private final int requestedQty;
    private final int allocatedQty;
    private final String status;

    /**
     * Constructs a new order eligibility record
     *
     * @param orderId the order identifier
     * @param lineNo the line number
     * @param sku the product SKU
     * @param requestedQty the requested quantity
     * @param allocatedQty the allocated quantity
     * @param status the eligibility status (ELIGIBLE, PARTIAL, or UNDISPATCHABLE)
     */
    public OrderEligibility(String orderId, int lineNo, String sku, int requestedQty, int allocatedQty, String status) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = allocatedQty;
        this.status = status;
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
     * Gets the product SKU
     *
     * @return the SKU
     */
    public String getSku() { return sku; }

    /**
     * Gets the requested quantity
     *
     * @return the requested quantity
     */
    public int getRequestedQty() { return requestedQty; }

    /**
     * Gets the allocated quantity
     *
     * @return the allocated quantity
     */
    public int getAllocatedQty() { return allocatedQty; }

    /**
     * Gets the eligibility status
     *
     * @return the status
     */
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("Eligibility[%s line %d | SKU=%s | requested=%d | allocated=%d | %s]",
                orderId, lineNo, sku, requestedQty, allocatedQty, status);
    }
}
