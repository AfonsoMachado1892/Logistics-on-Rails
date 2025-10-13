package ESINF.model;

public class OrderEligibility {
    private final String orderId;
    private final int lineNo;
    private final String sku;
    private final int requestedQty;
    private final int allocatedQty;
    private final String status;

    public OrderEligibility(String orderId, int lineNo, String sku, int requestedQty, int allocatedQty, String status) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = allocatedQty;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public int getLineNo() { return lineNo; }
    public String getSku() { return sku; }
    public int getRequestedQty() { return requestedQty; }
    public int getAllocatedQty() { return allocatedQty; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("Eligibility[%s line %d | SKU=%s | requested=%d | allocated=%d | %s]",
                orderId, lineNo, sku, requestedQty, allocatedQty, status);
    }
}
