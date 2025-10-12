package ESINF.model;

public class OrderLine {
    private String orderId;
    private int lineNo;
    private String sku;
    private int qty;

    public OrderLine(String orderId, int lineNo, String sku, int qty) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String getSku() {
        return sku;
    }

    public int getQty() {
        return qty;
    }
}
