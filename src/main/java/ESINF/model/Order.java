package ESINF.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order implements Comparable<Order> {
    private String orderId;
    private LocalDateTime dueDate;
    private int priority;
    private List<OrderLine> lines = new ArrayList<>();

    public Order(String orderId, LocalDateTime dueDate, int priority) {
        this.orderId = orderId;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    /**
     * Sorts order lines by lineNo (input order)
     */
    public void sortLines() {
        lines.sort((ol1, ol2) -> Integer.compare(ol1.getLineNo(), ol2.getLineNo()));
    }

    public int getPriority() {
        return priority;
    }


    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public String getOrderId() {
        return orderId;
    }

    // Implementação do Comparable para ordenação: priority ASC, dueDate ASC, orderId ASC
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
