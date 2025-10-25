package ESINF.IO;

import ESINF.model.Item;
import ESINF.model.Order;
import ESINF.model.OrderLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class OrderImporterTest {

    private OrderImporter orderImporter;
    private ImportLogger logger;
    private Map<String, Item> items;

    private File ordersFile;
    private File orderLinesFile;

    @Before
    public void setUp() {
        // Setup items catalog
        items = new HashMap<>();
        items.put("SKU100", new Item("SKU100", "Product 100", "Category A", "units", 0.5, 1.0));
        items.put("SKU200", new Item("SKU200", "Product 200", "Category B", "kg", 0.3, 0.8));
        items.put("SKU300", new Item("SKU300", "Product 300", "Category C", "L", 0.2, 0.6));

        // Setup logger
        logger = new ImportLogger();

        // Create temporary files
        try {
            ordersFile = File.createTempFile("test_orders", ".csv");
            orderLinesFile = File.createTempFile("test_order_lines", ".csv");
            ordersFile.deleteOnExit();
            orderLinesFile.deleteOnExit();
        } catch (IOException e) {
            fail("Failed to create temp files: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        // Clean up temp files
        if (ordersFile != null && ordersFile.exists()) {
            ordersFile.delete();
        }
        if (orderLinesFile != null && orderLinesFile.exists()) {
            orderLinesFile.delete();
        }
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testLoadOrders_ValidData() throws IOException {
        // Create valid orders file
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n" +
                        "ORD002,2025-11-30T12:00:00,2\n" +
                        "ORD003,2026-01-15T10:30:00,3\n"
        );

        // Create valid order lines file
        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,1,SKU100,50\n" +
                        "ORD001,2,SKU200,30\n" +
                        "ORD002,1,SKU300,100\n" +
                        "ORD003,1,SKU100,25\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Deve haver 3 ordens carregadas", 3, orders.size());

        // Verify first order
        Order order1 = findOrderById(orders, "ORD001");
        assertNotNull("ORD001 deve existir", order1);
        assertEquals("ORD001 deve ter prioridade 1", 1, order1.getPriority());
        assertEquals("ORD001 deve ter 2 linhas", 2, order1.getLines().size());

        // Verify second order
        Order order2 = findOrderById(orders, "ORD002");
        assertNotNull("ORD002 deve existir", order2);
        assertEquals("ORD002 deve ter prioridade 2", 2, order2.getPriority());
        assertEquals("ORD002 deve ter 1 linha", 1, order2.getLines().size());
    }

    @Test
    public void testLoadOrders_InvalidPriority() throws IOException {
        // Priority out of range (must be 1-3)
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,0\n" +
                        "ORD002,2025-11-30T12:00:00,4\n" +
                        "ORD003,2025-10-15T10:00:00,2\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD003,1,SKU100,10\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Apenas 1 ordem válida deve ser carregada", 1, orders.size());
        assertEquals("Ordem válida deve ser ORD003", "ORD003", orders.get(0).getOrderId());
        assertTrue("Deve haver erros registados", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Invalid priority")));
    }

    @Test
    public void testLoadOrders_InvalidDueDate() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,INVALID-DATE,1\n" +
                        "ORD002,2025-11-30T12:00:00,2\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD002,1,SKU100,10\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Apenas 1 ordem válida deve ser carregada", 1, orders.size());
        assertEquals("Ordem válida deve ser ORD002", "ORD002", orders.get(0).getOrderId());
        assertTrue("Deve haver erro de dueDate", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Invalid dueDate")));
    }

    @Test
    public void testLoadOrders_EmptyOrderId() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        ",2025-12-31T23:59:59,1\n" +
                        "ORD002,2025-11-30T12:00:00,2\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD002,1,SKU100,10\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Apenas 1 ordem válida deve ser carregada", 1, orders.size());
        assertTrue("Deve haver erro de orderId vazio", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Empty orderId")));
    }

    @Test
    public void testLoadOrders_UnknownSKU() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,1,SKU999,10\n" +
                        "ORD001,2,SKU100,20\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Ordem deve ser carregada", 1, orders.size());
        Order order = orders.get(0);
        assertEquals("Ordem deve ter apenas 1 linha válida", 1, order.getLines().size());
        assertEquals("Linha válida deve ter SKU100", "SKU100", order.getLines().get(0).getSku());
        assertTrue("Deve haver erro de SKU desconhecido", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Unknown SKU=SKU999")));
    }

    @Test
    public void testLoadOrders_UnknownOrderId() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD999,1,SKU100,10\n" +
                        "ORD001,1,SKU200,20\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Apenas ordem conhecida deve ter linhas", 1, orders.size());
        Order order = orders.get(0);
        assertEquals("ORD001", order.getOrderId());
        assertEquals("Deve ter 1 linha", 1, order.getLines().size());
        assertTrue("Deve haver erro de orderId desconhecido", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Unknown orderId=ORD999")));
    }

    @Test
    public void testLoadOrders_InvalidQuantity() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,1,SKU100,0\n" +
                        "ORD001,2,SKU200,-5\n" +
                        "ORD001,3,SKU300,10\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();
        Order order = orders.get(0);

        assertEquals("Apenas 1 linha válida deve ser carregada", 1, order.getLines().size());
        assertEquals("Linha válida deve ter SKU300", "SKU300", order.getLines().get(0).getSku());
        assertTrue("Deve haver erro de quantidade inválida", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Invalid quantity")));
    }

    @Test
    public void testLoadOrders_InvalidLineNo() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,0,SKU100,10\n" +
                        "ORD001,-1,SKU200,20\n" +
                        "ORD001,1,SKU300,30\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();
        Order order = orders.get(0);

        assertEquals("Apenas 1 linha válida deve ser carregada", 1, order.getLines().size());
        assertTrue("Deve haver erro de lineNo inválido", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Invalid lineNo")));
    }

    @Test
    public void testLoadOrders_OrderLinesSorted() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n"
        );

        // Lines in random order
        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,3,SKU300,30\n" +
                        "ORD001,1,SKU100,10\n" +
                        "ORD001,2,SKU200,20\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();
        Order order = orders.get(0);
        List<OrderLine> lines = order.getLines();

        assertEquals("Deve haver 3 linhas", 3, lines.size());
        assertEquals("Primeira linha deve ter lineNo=1", 1, lines.get(0).getLineNo());
        assertEquals("Segunda linha deve ter lineNo=2", 2, lines.get(1).getLineNo());
        assertEquals("Terceira linha deve ter lineNo=3", 3, lines.get(2).getLineNo());
    }

    // --- Testes de Cobertura (Edge Cases) ---

    @Test
    public void testLoadOrders_EmptyFiles() throws IOException {
        writeToFile(ordersFile, "orderId,dueDate,priority\n");
        writeToFile(orderLinesFile, "orderId,lineNo,sku,qty\n");

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertTrue("Lista de ordens deve estar vazia", orders.isEmpty());
    }

    @Test
    public void testLoadOrders_EmptyLines() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "\n" +
                        "ORD001,2025-12-31T23:59:59,1\n" +
                        "\n" +
                        "ORD002,2025-11-30T12:00:00,2\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "\n" +
                        "ORD001,1,SKU100,10\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Deve carregar 2 ordens válidas", 2, orders.size());
    }

    @Test
    public void testLoadOrders_InsufficientColumns_Orders() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59\n" +
                        "ORD002,2025-11-30T12:00:00,2\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD002,1,SKU100,10\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Apenas ordem válida deve ser carregada", 1, orders.size());
        assertTrue("Deve haver erro de colunas insuficientes", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Invalid number of columns")));
    }

    @Test
    public void testLoadOrders_InsufficientColumns_OrderLines() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,1,SKU100\n" +
                        "ORD001,2,SKU200,20\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();
        Order order = orders.get(0);

        assertEquals("Apenas 1 linha válida deve ser carregada", 1, order.getLines().size());
    }

    @Test
    public void testLoadOrders_NumericParseError() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,ABC\n" +
                        "ORD002,2025-11-30T12:00:00,2\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD002,1,SKU100,10\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Apenas ordem válida deve ser carregada", 1, orders.size());
        assertTrue("Deve haver erro de parse numérico", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Numeric parse error")));
    }

    @Test
    public void testLoadOrders_EmptySKU() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,1,,10\n" +
                        "ORD001,2,SKU100,20\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();
        Order order = orders.get(0);

        assertEquals("Apenas 1 linha válida deve ser carregada", 1, order.getLines().size());
        assertTrue("Deve haver erro de SKU vazio", logger.getLogs().stream()
                .anyMatch(log -> log.contains("Empty SKU")));
    }

    @Test
    public void testGetOrders() {
        orderImporter = new OrderImporter(logger, items);
        List<Order> orders = orderImporter.getOrders();

        assertNotNull("Lista de ordens não deve ser null", orders);
        assertTrue("Lista deve estar vazia inicialmente", orders.isEmpty());
    }

    @Test
    public void testConstructor() {
        orderImporter = new OrderImporter(logger, items);

        assertNotNull("OrderImporter não deve ser null", orderImporter);
        assertNotNull("Lista de ordens deve existir", orderImporter.getOrders());
    }

    @Test
    public void testPrintStats() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n" +
                        "ORD002,2025-11-30T12:00:00,1\n" +
                        "ORD003,2025-10-15T10:00:00,2\n" +
                        "ORD004,2025-09-01T08:00:00,3\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,1,SKU100,10\n" +
                        "ORD001,2,SKU200,20\n" +
                        "ORD002,1,SKU300,30\n" +
                        "ORD003,1,SKU100,40\n" +
                        "ORD004,1,SKU200,50\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        // Should not throw exception
        orderImporter.printStats();

        List<Order> orders = orderImporter.getOrders();
        assertEquals("Deve haver 4 ordens", 4, orders.size());
    }

    @Test
    public void testLoadOrders_MultipleOrdersMultipleLines() throws IOException {
        writeToFile(ordersFile,
                "orderId,dueDate,priority\n" +
                        "ORD001,2025-12-31T23:59:59,1\n" +
                        "ORD002,2025-11-30T12:00:00,2\n" +
                        "ORD003,2025-10-15T10:00:00,3\n"
        );

        writeToFile(orderLinesFile,
                "orderId,lineNo,sku,qty\n" +
                        "ORD001,1,SKU100,10\n" +
                        "ORD001,2,SKU200,20\n" +
                        "ORD001,3,SKU300,30\n" +
                        "ORD002,1,SKU100,40\n" +
                        "ORD002,2,SKU200,50\n" +
                        "ORD003,1,SKU300,60\n"
        );

        orderImporter = new OrderImporter(logger, items);
        orderImporter.loadOrders(ordersFile.getAbsolutePath(), orderLinesFile.getAbsolutePath());

        List<Order> orders = orderImporter.getOrders();

        assertEquals("Deve haver 3 ordens", 3, orders.size());

        Order order1 = findOrderById(orders, "ORD001");
        assertEquals("ORD001 deve ter 3 linhas", 3, order1.getLines().size());

        Order order2 = findOrderById(orders, "ORD002");
        assertEquals("ORD002 deve ter 2 linhas", 2, order2.getLines().size());

        Order order3 = findOrderById(orders, "ORD003");
        assertEquals("ORD003 deve ter 1 linha", 1, order3.getLines().size());
    }

    // --- Helper Methods ---

    private void writeToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    private Order findOrderById(List<Order> orders, String orderId) {
        return orders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
    }
}
