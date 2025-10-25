package ESINF.service;

import ESINF.DTO.AllocationOutput;
import ESINF.model.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AllocationManagerTest {
    
    private AllocationManager allocationManager;
    private WarehouseManager warehouseManager;
    private Warehouse warehouse;
    private List<Order> orders;
    private LocalDateTime now;

    @Before
    public void setUp() {
        // Setup warehouse with aisles and bays
        warehouse = new Warehouse("W01");
        Aisle aisle1 = new Aisle(1);
        Bay bay1 = new Bay(1, "W01", 1, 10);
        Bay bay2 = new Bay(2, "W01", 1, 10);
        aisle1.addBay(bay1);
        aisle1.addBay(bay2);
        warehouse.addAisle(aisle1);

        // Add boxes to bays
        now = LocalDateTime.now();
        Box box1 = new Box("BOX001", "SKU100", 50, null, now);
        Box box2 = new Box("BOX002", "SKU100", 30, null, now.plusMinutes(10));
        Box box3 = new Box("BOX003", "SKU200", 20, LocalDate.now().plusDays(5), now);

        box1.setBay(bay1);
        box2.setBay(bay1);
        box3.setBay(bay2);

        bay1.addBox(box1);
        bay1.addBox(box2);
        bay2.addBox(box3);

        // Initialize managers
        warehouseManager = new WarehouseManager(warehouse);
        allocationManager = new AllocationManager(warehouseManager);

        // Create orders
        orders = new ArrayList<>();
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testAllocateOrders_StrictMode_FullyEligible() {
        // Order requesting 40 units of SKU100 (available: 50+30=80)
        Order order1 = new Order("ORD001", LocalDateTime.now().plusDays(1), 1);
        OrderLine line1 = new OrderLine("ORD001", 1, "SKU100", 40);
        order1.addLine(line1);
        orders.add(order1);

        AllocationOutput output = allocationManager.allocateOrders(orders, true);

        assertEquals("Deve haver 1 resultado de elegibilidade", 1, output.results().size());
        assertEquals("Deve haver 1 alocação", 1, output.allocations().size());

        OrderEligibility eligibility = output.results().get(0);
        assertEquals("Status deve ser ELIGIBLE", "ELIGIBLE", eligibility.getStatus());
        assertEquals("Quantidade alocada deve ser 40", 40, eligibility.getAllocatedQty());
        assertEquals("Quantidade requisitada deve ser 40", 40, eligibility.getRequestedQty());
    }

    @Test
    public void testAllocateOrders_StrictMode_Undispatchable() {
        // Order requesting 100 units of SKU100 (available: 50+30=80)
        Order order1 = new Order("ORD002", LocalDateTime.now().plusDays(1), 1);
        OrderLine line1 = new OrderLine("ORD002", 1, "SKU100", 100);
        order1.addLine(line1);
        orders.add(order1);

        AllocationOutput output = allocationManager.allocateOrders(orders, true);

        assertEquals("Deve haver 1 resultado de elegibilidade", 1, output.results().size());
        assertEquals("Não deve haver alocações em modo strict", 0, output.allocations().size());

        OrderEligibility eligibility = output.results().get(0);
        assertEquals("Status deve ser UNDISPATCHABLE", "UNDISPATCHABLE", eligibility.getStatus());
        assertEquals("Quantidade alocada deve ser 0", 0, eligibility.getAllocatedQty());
        assertEquals("Quantidade requisitada deve ser 100", 100, eligibility.getRequestedQty());
    }

    @Test
    public void testAllocateOrders_PartialMode_PartialAllocation() {
        // Order requesting 100 units of SKU100 (available: 50+30=80)
        Order order1 = new Order("ORD003", LocalDateTime.now().plusDays(1), 1);
        OrderLine line1 = new OrderLine("ORD003", 1, "SKU100", 100);
        order1.addLine(line1);
        orders.add(order1);

        AllocationOutput output = allocationManager.allocateOrders(orders, false);

        assertEquals("Deve haver 1 resultado de elegibilidade", 1, output.results().size());
        assertEquals("Deve haver 2 alocações (2 boxes)", 2, output.allocations().size());

        OrderEligibility eligibility = output.results().get(0);
        assertEquals("Status deve ser PARTIAL", "PARTIAL", eligibility.getStatus());
        assertEquals("Quantidade alocada deve ser 80", 80, eligibility.getAllocatedQty());
        assertEquals("Quantidade requisitada deve ser 100", 100, eligibility.getRequestedQty());
    }

    @Test
    public void testAllocateOrders_PartialMode_UndispatchableZeroStock() {
        // Order requesting SKU that doesn't exist
        Order order1 = new Order("ORD004", LocalDateTime.now().plusDays(1), 1);
        OrderLine line1 = new OrderLine("ORD004", 1, "SKU999", 10);
        order1.addLine(line1);
        orders.add(order1);

        AllocationOutput output = allocationManager.allocateOrders(orders, false);

        assertEquals("Deve haver 1 resultado de elegibilidade", 1, output.results().size());
        assertEquals("Não deve haver alocações", 0, output.allocations().size());

        OrderEligibility eligibility = output.results().get(0);
        assertEquals("Status deve ser UNDISPATCHABLE", "UNDISPATCHABLE", eligibility.getStatus());
        assertEquals("Quantidade alocada deve ser 0", 0, eligibility.getAllocatedQty());
    }

    @Test
    public void testAllocateOrders_MultipleLines_StrictMode() {
        Order order1 = new Order("ORD005", LocalDateTime.now().plusDays(1), 1);
        OrderLine line1 = new OrderLine("ORD005", 1, "SKU100", 30);
        OrderLine line2 = new OrderLine("ORD005", 2, "SKU200", 15);
        order1.addLine(line1);
        order1.addLine(line2);
        orders.add(order1);

        AllocationOutput output = allocationManager.allocateOrders(orders, true);

        assertEquals("Deve haver 2 resultados de elegibilidade", 2, output.results().size());

        // Line 1: SKU100 (30 units available: 50+30)
        OrderEligibility elig1 = output.results().get(0);
        assertEquals("Linha 1 deve ser ELIGIBLE", "ELIGIBLE", elig1.getStatus());
        assertEquals("SKU100", elig1.getSku());

        // Line 2: SKU200 (15 units requested, 20 available)
        OrderEligibility elig2 = output.results().get(1);
        assertEquals("Linha 2 deve ser ELIGIBLE", "ELIGIBLE", elig2.getStatus());
        assertEquals("SKU200", elig2.getSku());
    }

    @Test
    public void testAllocateOrders_OrderSorting_ByPriority() {
        // Create orders with different priorities
        Order order1 = new Order("ORD-LOW", LocalDateTime.now().plusDays(1), 3);
        order1.addLine(new OrderLine("ORD-LOW", 1, "SKU100", 10));

        Order order2 = new Order("ORD-HIGH", LocalDateTime.now().plusDays(1), 1);
        order2.addLine(new OrderLine("ORD-HIGH", 1, "SKU100", 10));

        Order order3 = new Order("ORD-MED", LocalDateTime.now().plusDays(1), 2);
        order3.addLine(new OrderLine("ORD-MED", 1, "SKU100", 10));

        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        AllocationOutput output = allocationManager.allocateOrders(orders, true);

        // Check processing order: HIGH -> MED -> LOW
        List<OrderEligibility> results = output.results();
        assertEquals("Primeira ordem processada deve ser HIGH", "ORD-HIGH", results.get(0).getOrderId());
        assertEquals("Segunda ordem processada deve ser MED", "ORD-MED", results.get(1).getOrderId());
        assertEquals("Terceira ordem processada deve ser LOW", "ORD-LOW", results.get(2).getOrderId());
    }

    @Test
    public void testAllocateOrders_ConsumesStockSequentially() {
        // First order takes 50 units
        Order order1 = new Order("ORD-FIRST", LocalDateTime.now().plusDays(1), 1);
        order1.addLine(new OrderLine("ORD-FIRST", 1, "SKU100", 50));

        // Second order tries to take 40 units (only 30 left)
        Order order2 = new Order("ORD-SECOND", LocalDateTime.now().plusDays(1), 1);
        order2.addLine(new OrderLine("ORD-SECOND", 1, "SKU100", 40));

        orders.add(order1);
        orders.add(order2);

        AllocationOutput output = allocationManager.allocateOrders(orders, true);

        OrderEligibility elig1 = output.results().get(0);
        assertEquals("Primeira ordem deve ser ELIGIBLE", "ELIGIBLE", elig1.getStatus());
        assertEquals(50, elig1.getAllocatedQty());

        OrderEligibility elig2 = output.results().get(1);
        assertEquals("Segunda ordem deve ser UNDISPATCHABLE", "UNDISPATCHABLE", elig2.getStatus());
        assertEquals(0, elig2.getAllocatedQty());
    }

    // --- Testes de Cobertura (Edge Cases) ---

    @Test
    public void testAllocateOrders_EmptyOrdersList() {
        AllocationOutput output = allocationManager.allocateOrders(new ArrayList<>(), true);

        assertNotNull("Output não deve ser null", output);
        assertTrue("Lista de elegibilidade deve estar vazia", output.results().isEmpty());
        assertTrue("Lista de alocações deve estar vazia", output.allocations().isEmpty());
    }

    @Test
    public void testAllocateOrders_OrderWithNoLines() {
        Order order1 = new Order("ORD-EMPTY", LocalDateTime.now().plusDays(1), 1);
        orders.add(order1);

        AllocationOutput output = allocationManager.allocateOrders(orders, true);

        assertTrue("Não deve haver resultados de elegibilidade", output.results().isEmpty());
        assertTrue("Não deve haver alocações", output.allocations().isEmpty());
    }

    @Test
    public void testAllocateOrders_AllocationSpansMultipleBays() {
        // Request 70 units: 50 from BOX001 + 20 from BOX002
        Order order1 = new Order("ORD006", LocalDateTime.now().plusDays(1), 1);
        order1.addLine(new OrderLine("ORD006", 1, "SKU100", 70));
        orders.add(order1);

        AllocationOutput output = allocationManager.allocateOrders(orders, true);

        assertEquals("Deve haver 1 resultado de elegibilidade", 1, output.results().size());
        assertEquals("Deve haver 2 alocações (2 boxes)", 2, output.allocations().size());

        OrderEligibility eligibility = output.results().get(0);
        assertEquals("ELIGIBLE", eligibility.getStatus());
        assertEquals(70, eligibility.getAllocatedQty());

        // Verify allocations reference correct boxes
        List<Allocation> allocations = output.allocations();
        assertTrue("Primeira alocação deve ser BOX001", allocations.get(0).getBoxId().equals("BOX001"));
        assertTrue("Segunda alocação deve ser BOX002", allocations.get(1).getBoxId().equals("BOX002"));
    }

    @Test
    public void testConstructor() {
        assertNotNull("AllocationManager não deve ser null", allocationManager);
    }
}
