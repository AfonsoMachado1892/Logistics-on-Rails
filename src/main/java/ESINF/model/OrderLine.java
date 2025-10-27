package ESINF.model;

/**
 * Represents a single line item in an order.
 * Each order line specifies a product (SKU) and requested quantity.
 */
public class OrderLine {
    private String orderId;
    private int lineNo;
    private String sku;
    private int qty;

    /**
     * Constructs a new order line
     *
     * @param orderId the order identifier
     * @param lineNo the line number
     * @param sku the product SKU
     * @param qty the requested quantity
     */
    public OrderLine(String orderId, int lineNo, String sku, int qty) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
    }

    /**
     * Gets the order identifier
     *
     * @return the order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Gets the line number
     *
     * @return the line number
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * Gets the product SKU
     *
     * @return the SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gets the requested quantity
     *
     * @return the quantity
     */
    public int getQty() {
        return qty;
    }
}
