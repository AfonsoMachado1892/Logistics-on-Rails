package ESINF.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order.
 * Orders are comparable by priority, due date, and order ID.
 */
public class Order implements Comparable<Order> {
    private String orderId;
    private LocalDateTime dueDate;
    private int priority;
    private List<OrderLine> lines = new ArrayList<>();

    /**
     * Constructs a new order
     *
     * @param orderId the order identifier
     * @param dueDate the due date for the order
     * @param priority the priority level (1=high, 2=medium, 3=low)
     */
    public Order(String orderId, LocalDateTime dueDate, int priority) {
        this.orderId = orderId;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    /**
     * Adds an order line to this order
     *
     * @param line the order line to add
     */
    public void addLine(OrderLine line) {
        lines.add(line);
    }

    /**
     * Gets all order lines for this order
     *
     * @return the list of order lines
     */
    public List<OrderLine> getLines() {
        return lines;
    }

    /**
     * Sorts order lines by lineNo (input order)
     */
    public void sortLines() {
        lines.sort((ol1, ol2) -> Integer.compare(ol1.getLineNo(), ol2.getLineNo()));
    }

    /**
     * Gets the priority level
     *
     * @return the priority (1=high, 2=medium, 3=low)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets the due date
     *
     * @return the due date
     */
    public LocalDateTime getDueDate() {
        return dueDate;
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
     * Compares this order with another by priority (ascending), due date (ascending), and order ID (ascending)
     *
     * @param other the order to compare to
     * @return a negative integer, zero, or a positive integer if this order is less than, equal to, or greater than the other order
     */
    @Override
    public int compareTo(Order other) {
        // First by priority (1=high, 2=medium, 3=low)
        int priorityCmp = Integer.compare(this.priority, other.priority);
        if (priorityCmp != 0) return priorityCmp;
        
        // Then by due date
        int dateCmp = this.dueDate.compareTo(other.dueDate);
        if (dateCmp != 0) return dateCmp;
        
        // Finally by order ID
        return this.orderId.compareTo(other.orderId);
    }

    @Override
    public String toString() {
        return String.format("Order[%s, due=%s, priority=%d, lines=%d]",
                orderId, dueDate, priority, lines.size());
    }
}
