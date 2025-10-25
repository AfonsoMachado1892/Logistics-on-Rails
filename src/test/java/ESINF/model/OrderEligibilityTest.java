package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OrderEligibilityTest {

    private OrderEligibility eligibleOrder;
    private OrderEligibility partialOrder;
    private OrderEligibility undispatchableOrder;

    @Before
    public void setUp() {
        eligibleOrder = new OrderEligibility("ORD001", 1, "SKU100", 50, 50, "ELIGIBLE");
        partialOrder = new OrderEligibility("ORD002", 2, "SKU200", 100, 75, "PARTIAL");
        undispatchableOrder = new OrderEligibility("ORD003", 1, "SKU300", 20, 0, "UNDISPATCHABLE");
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testConstructorAndGetters_EligibleStatus() {
        assertEquals("Order ID deve ser ORD001", "ORD001", eligibleOrder.getOrderId());
        assertEquals("Line number deve ser 1", 1, eligibleOrder.getLineNo());
        assertEquals("SKU deve ser SKU100", "SKU100", eligibleOrder.getSku());
        assertEquals("Requested quantity deve ser 50", 50, eligibleOrder.getRequestedQty());
        assertEquals("Allocated quantity deve ser 50", 50, eligibleOrder.getAllocatedQty());
        assertEquals("Status deve ser ELIGIBLE", "ELIGIBLE", eligibleOrder.getStatus());
    }

    @Test
    public void testConstructorAndGetters_PartialStatus() {
        assertEquals("Order ID deve ser ORD002", "ORD002", partialOrder.getOrderId());
        assertEquals("Line number deve ser 2", 2, partialOrder.getLineNo());
        assertEquals("SKU deve ser SKU200", "SKU200", partialOrder.getSku());
        assertEquals("Requested quantity deve ser 100", 100, partialOrder.getRequestedQty());
        assertEquals("Allocated quantity deve ser 75", 75, partialOrder.getAllocatedQty());
        assertEquals("Status deve ser PARTIAL", "PARTIAL", partialOrder.getStatus());
    }

    @Test
    public void testConstructorAndGetters_UndispatchableStatus() {
        assertEquals("Order ID deve ser ORD003", "ORD003", undispatchableOrder.getOrderId());
        assertEquals("Line number deve ser 1", 1, undispatchableOrder.getLineNo());
        assertEquals("SKU deve ser SKU300", "SKU300", undispatchableOrder.getSku());
        assertEquals("Requested quantity deve ser 20", 20, undispatchableOrder.getRequestedQty());
        assertEquals("Allocated quantity deve ser 0", 0, undispatchableOrder.getAllocatedQty());
        assertEquals("Status deve ser UNDISPATCHABLE", "UNDISPATCHABLE", undispatchableOrder.getStatus());
    }

    @Test
    public void testToString_EligibleOrder() {
        String str = eligibleOrder.toString();

        assertTrue("String deve conter 'Eligibility'", str.contains("Eligibility"));
        assertTrue("String deve conter order ID", str.contains("ORD001"));
        assertTrue("String deve conter 'line 1'", str.contains("line 1"));
        assertTrue("String deve conter SKU", str.contains("SKU=SKU100"));
        assertTrue("String deve conter requested", str.contains("requested=50"));
        assertTrue("String deve conter allocated", str.contains("allocated=50"));
        assertTrue("String deve conter status ELIGIBLE", str.contains("ELIGIBLE"));
    }

    @Test
    public void testToString_PartialOrder() {
        String str = partialOrder.toString();

        assertTrue("String deve conter order ID", str.contains("ORD002"));
        assertTrue("String deve conter 'line 2'", str.contains("line 2"));
        assertTrue("String deve conter SKU", str.contains("SKU=SKU200"));
        assertTrue("String deve conter requested", str.contains("requested=100"));
        assertTrue("String deve conter allocated", str.contains("allocated=75"));
        assertTrue("String deve conter status PARTIAL", str.contains("PARTIAL"));
    }

    @Test
    public void testToString_UndispatchableOrder() {
        String str = undispatchableOrder.toString();

        assertTrue("String deve conter order ID", str.contains("ORD003"));
        assertTrue("String deve conter 'line 1'", str.contains("line 1"));
        assertTrue("String deve conter SKU", str.contains("SKU=SKU300"));
        assertTrue("String deve conter requested", str.contains("requested=20"));
        assertTrue("String deve conter allocated", str.contains("allocated=0"));
        assertTrue("String deve conter status UNDISPATCHABLE", str.contains("UNDISPATCHABLE"));
    }

    // --- Testes de Cobertura (Edge Cases) ---

    @Test
    public void testConstructor_ZeroQuantities() {
        OrderEligibility zeroOrder = new OrderEligibility("ORD000", 1, "SKU000", 0, 0, "ELIGIBLE");

        assertEquals(0, zeroOrder.getRequestedQty());
        assertEquals(0, zeroOrder.getAllocatedQty());
    }

    @Test
    public void testConstructor_LargeQuantities() {
        OrderEligibility largeOrder = new OrderEligibility(
                "ORD999", 99, "SKU999", 1000000, 999999, "PARTIAL");

        assertEquals(1000000, largeOrder.getRequestedQty());
        assertEquals(999999, largeOrder.getAllocatedQty());
    }

    @Test
    public void testConstructor_AllParametersUsed() {
        OrderEligibility testOrder = new OrderEligibility(
                "TEST-ID", 42, "TEST-SKU", 123, 456, "TEST-STATUS");

        assertEquals("TEST-ID", testOrder.getOrderId());
        assertEquals(42, testOrder.getLineNo());
        assertEquals("TEST-SKU", testOrder.getSku());
        assertEquals(123, testOrder.getRequestedQty());
        assertEquals(456, testOrder.getAllocatedQty());
        assertEquals("TEST-STATUS", testOrder.getStatus());
    }

    @Test
    public void testGetters_ImmutableBehavior() {
        // Getters should return consistent values
        String orderId1 = eligibleOrder.getOrderId();
        String orderId2 = eligibleOrder.getOrderId();

        assertSame("Múltiplas chamadas a getOrderId devem retornar o mesmo objeto",
                orderId1, orderId2);
    }

    @Test
    public void testToString_ContainsAllFields() {
        OrderEligibility fullOrder = new OrderEligibility(
                "ORD-FULL", 5, "SKU-FULL", 200, 150, "PARTIAL");

        String str = fullOrder.toString();

        assertTrue(str.contains("ORD-FULL"));
        assertTrue(str.contains("5"));
        assertTrue(str.contains("SKU-FULL"));
        assertTrue(str.contains("200"));
        assertTrue(str.contains("150"));
        assertTrue(str.contains("PARTIAL"));
    }

    @Test
    public void testMultipleInstances_Independence() {
        OrderEligibility order1 = new OrderEligibility("ORD-A", 1, "SKU-A", 10, 10, "ELIGIBLE");
        OrderEligibility order2 = new OrderEligibility("ORD-B", 2, "SKU-B", 20, 15, "PARTIAL");

        assertNotEquals("Instâncias diferentes devem ter order IDs diferentes",
                order1.getOrderId(), order2.getOrderId());
        assertNotEquals("Instâncias diferentes devem ter line numbers diferentes",
                order1.getLineNo(), order2.getLineNo());
    }
}
