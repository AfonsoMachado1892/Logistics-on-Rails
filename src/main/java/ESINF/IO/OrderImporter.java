package ESINF.IO;

import ESINF.model.Item;
import ESINF.model.Order;
import ESINF.model.OrderLine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Reads and validates CSV files required for USEI02:
 * - orders.csv
 * - order_lines.csv
 *
 * Rejects invalid records but continues loading valid ones.
 * Produces deterministic messages through the ImportLogger.
 */
public class OrderImporter {

    private final ImportLogger logger;
    private final List<Order> orders = new ArrayList<>();
    private final Map<String, Item> items; // Para validação de SKUs

    /**
     * Constructs a new order importer
     *
     * @param logger the import logger for tracking errors and information
     * @param items map of existing items for SKU validation
     */
    public OrderImporter(ImportLogger logger, Map<String, Item> items) {
        this.logger = logger;
        this.items = items;
    }

    /**
     * Loads orders and order lines from CSV files
     *
     * @param ordersFile path to the orders CSV file
     * @param orderLinesFile path to the order lines CSV file
     * @throws IOException if an I/O error occurs while reading the files
     */
    public void loadOrders(String ordersFile, String orderLinesFile) throws IOException {
        loadOrdersData(ordersFile);
        loadOrderLinesData(orderLinesFile);
        
        // Ordenar linhas de cada encomenda por lineNo
        for (Order order : orders) {
            order.sortLines();
        }
    }

    /**
     * Loads order data from orders.csv file
     *
     * @param filePath path to the orders CSV file
     * @throws IOException if an I/O error occurs while reading the file
     */
    private void loadOrdersData(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    logger.error("orders.csv", lineNo, "Invalid number of columns (expected 3): " + line);
                    continue;
                }

                try {
                    String orderId = parts[0].trim();
                    String dueDateStr = parts[1].trim();
                    int priority = Integer.parseInt(parts[2].trim());

                    // Validações
                    if (orderId.isEmpty()) {
                        logger.error("orders.csv", lineNo, "Empty orderId");
                        continue;
                    }

                    if (priority < 1 || priority > 3) {
                        logger.error("orders.csv", lineNo, "Invalid priority " + priority + " for order " + orderId + " (must be 1-3)");
                        continue;
                    }

                    LocalDateTime dueDate;
                    try {
                        dueDate = LocalDateTime.parse(dueDateStr);
                    } catch (DateTimeParseException e) {
                        logger.error("orders.csv", lineNo, "Invalid dueDate=" + dueDateStr + " for order " + orderId);
                        continue;
                    }

                    Order order = new Order(orderId, dueDate, priority);
                    orders.add(order);

                } catch (NumberFormatException e) {
                    logger.error("orders.csv", lineNo, "Numeric parse error: " + line);
                } catch (Exception e) {
                    logger.error("orders.csv", lineNo, "Unexpected error: " + e.getMessage());
                }
            }
        }

        logger.info("orders.csv", 0, "Loaded " + orders.size() + " valid orders.");
    }

    /**
     * Loads order line data from order_lines.csv file
     *
     * @param filePath path to the order lines CSV file
     * @throws IOException if an I/O error occurs while reading the file
     */
    private void loadOrderLinesData(String filePath) throws IOException {
        Map<String, Order> orderMap = new HashMap<>();
        for (Order order : orders) {
            orderMap.put(order.getOrderId(), order);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();
            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    logger.error("order_lines.csv", lineNo, "Invalid number of columns (expected 4): " + line);
                    continue;
                }

                try {
                    String orderId = parts[0].trim();
                    int lineNoInt = Integer.parseInt(parts[1].trim());
                    String sku = parts[2].trim();
                    int qty = Integer.parseInt(parts[3].trim());

                    // Validações
                    if (orderId.isEmpty()) {
                        logger.error("order_lines.csv", lineNo, "Empty orderId");
                        continue;
                    }

                    if (!orderMap.containsKey(orderId)) {
                        logger.error("order_lines.csv", lineNo, "Unknown orderId=" + orderId);
                        continue;
                    }

                    if (lineNoInt <= 0) {
                        logger.error("order_lines.csv", lineNo, "Invalid lineNo=" + lineNoInt + " for order " + orderId);
                        continue;
                    }

                    if (sku.isEmpty()) {
                        logger.error("order_lines.csv", lineNo, "Empty SKU for order " + orderId + " line " + lineNoInt);
                        continue;
                    }

                    if (!items.containsKey(sku)) {
                        logger.error("order_lines.csv", lineNo, "Unknown SKU=" + sku + " for order " + orderId);
                        continue;
                    }

                    if (qty <= 0) {
                        logger.error("order_lines.csv", lineNo, "Invalid quantity=" + qty + " for order " + orderId + " line " + lineNoInt);
                        continue;
                    }

                    OrderLine orderLine = new OrderLine(orderId, lineNoInt, sku, qty);
                    Order order = orderMap.get(orderId);
                    order.addLine(orderLine);

                } catch (NumberFormatException e) {
                    logger.error("order_lines.csv", lineNo, "Numeric parse error: " + line);
                } catch (Exception e) {
                    logger.error("order_lines.csv", lineNo, "Unexpected error: " + e.getMessage());
                }
            }
        }

        logger.info("order_lines.csv", 0, "Loaded order lines for " + orders.size() + " orders.");
    }

    /**
     * Returns the list of loaded orders
     *
     * @return list of orders
     */
    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Prints loading statistics
     */
    public void printStats() {
        System.out.println("\n=== ORDER IMPORT STATISTICS ===");
        System.out.println("Total orders loaded: " + orders.size());
        
        int totalLines = orders.stream().mapToInt(o -> o.getLines().size()).sum();
        System.out.println("Total order lines: " + totalLines);
        
        // Estatísticas por prioridade
        Map<Integer, Long> priorityStats = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Order::getPriority, 
                        java.util.stream.Collectors.counting()));
        
        System.out.println("Orders by priority:");
        for (int priority = 1; priority <= 3; priority++) {
            long count = priorityStats.getOrDefault(priority, 0L);
            System.out.println("  Priority " + priority + ": " + count + " orders");
        }
    }
}
