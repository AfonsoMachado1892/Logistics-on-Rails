package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OrderLineTest {

    private OrderLine orderLine1;
    private OrderLine orderLine2;
    private OrderLine orderLine3;

    @Before
    public void setUp() {
        orderLine1 = new OrderLine("ORD001", 1, "SKU100", 50);
        orderLine2 = new OrderLine("ORD001", 2, "SKU200", 100);
        orderLine3 = new OrderLine("ORD002", 1, "SKU300", 25);
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testConstructorAndGetters() {
        assertEquals("Order ID deve ser ORD001", "ORD001", orderLine1.getOrderId());
        assertEquals("Line number deve ser 1", 1, orderLine1.getLineNo());
        assertEquals("SKU deve ser SKU100", "SKU100", orderLine1.getSku());
        assertEquals("Quantity deve ser 50", 50, orderLine1.getQty());
    }

    @Test
    public void testGetOrderId() {
        assertEquals("ORD001", orderLine1.getOrderId());
        assertEquals("ORD001", orderLine2.getOrderId());
        assertEquals("ORD002", orderLine3.getOrderId());
    }

    @Test
    public void testGetLineNo() {
        assertEquals("Line number da linha 1 deve ser 1", 1, orderLine1.getLineNo());
        assertEquals("Line number da linha 2 deve ser 2", 2, orderLine2.getLineNo());
        assertEquals("Line number da linha 3 deve ser 1", 1, orderLine3.getLineNo());
    }

    @Test
    public void testGetSku() {
        assertEquals("SKU100", orderLine1.getSku());
        assertEquals("SKU200", orderLine2.getSku());
        assertEquals("SKU300", orderLine3.getSku());
    }

    @Test
    public void testGetQty() {
        assertEquals("Quantidade da linha 1 deve ser 50", 50, orderLine1.getQty());
        assertEquals("Quantidade da linha 2 deve ser 100", 100, orderLine2.getQty());
        assertEquals("Quantidade da linha 3 deve ser 25", 25, orderLine3.getQty());
    }

    @Test
    public void testMultipleLinesInSameOrder() {
        // Duas linhas da mesma ordem devem ter o mesmo orderId mas lineNo diferente
        assertEquals("Ambas as linhas devem pertencer à mesma ordem",
                orderLine1.getOrderId(), orderLine2.getOrderId());
        assertNotEquals("Line numbers devem ser diferentes",
                orderLine1.getLineNo(), orderLine2.getLineNo());
    }

    @Test
    public void testDifferentOrders() {
        // Linhas de ordens diferentes devem ter orderIds diferentes
        assertNotEquals("Order IDs devem ser diferentes",
                orderLine1.getOrderId(), orderLine3.getOrderId());
    }

    // --- Testes de Cobertura (Edge Cases) ---

    @Test
    public void testConstructor_MinimumQuantity() {
        OrderLine minLine = new OrderLine("ORD-MIN", 1, "SKU-MIN", 1);
        assertEquals("Quantidade mínima deve ser 1", 1, minLine.getQty());
    }

    @Test
    public void testConstructor_LargeQuantity() {
        OrderLine largeLine = new OrderLine("ORD-LARGE", 1, "SKU-LARGE", 1000000);
        assertEquals("Quantidade grande deve ser armazenada corretamente",
                1000000, largeLine.getQty());
    }

    @Test
    public void testConstructor_ZeroQuantity() {
        OrderLine zeroLine = new OrderLine("ORD-ZERO", 1, "SKU-ZERO", 0);
        assertEquals("Quantidade zero deve ser permitida", 0, zeroLine.getQty());
    }

    @Test
    public void testConstructor_HighLineNumber() {
        OrderLine highLineLine = new OrderLine("ORD-HIGH", 999, "SKU-HIGH", 10);
        assertEquals("Line number alto deve ser armazenado corretamente",
                999, highLineLine.getLineNo());
    }

    @Test
    public void testConstructor_AllParameters() {
        String testOrderId = "TEST-ORDER-ID";
        int testLineNo = 42;
        String testSku = "TEST-SKU-123";
        int testQty = 789;

        OrderLine testLine = new OrderLine(testOrderId, testLineNo, testSku, testQty);

        assertEquals(testOrderId, testLine.getOrderId());
        assertEquals(testLineNo, testLine.getLineNo());
        assertEquals(testSku, testLine.getSku());
        assertEquals(testQty, testLine.getQty());
    }

    @Test
    public void testGetters_Consistency() {
        // Multiple calls to getters should return the same values
        String orderId1 = orderLine1.getOrderId();
        String orderId2 = orderLine1.getOrderId();
        assertSame("Múltiplas chamadas ao getter devem retornar o mesmo objeto",
                orderId1, orderId2);

        int lineNo1 = orderLine1.getLineNo();
        int lineNo2 = orderLine1.getLineNo();
        assertEquals("Múltiplas chamadas ao getter devem retornar o mesmo valor",
                lineNo1, lineNo2);
    }

    @Test
    public void testMultipleInstances_Independence() {
        OrderLine line1 = new OrderLine("ORD-A", 1, "SKU-A", 10);
        OrderLine line2 = new OrderLine("ORD-B", 2, "SKU-B", 20);

        assertNotEquals("Instâncias diferentes devem ter order IDs diferentes",
                line1.getOrderId(), line2.getOrderId());
        assertNotEquals("Instâncias diferentes devem ter line numbers diferentes",
                line1.getLineNo(), line2.getLineNo());
        assertNotEquals("Instâncias diferentes devem ter SKUs diferentes",
                line1.getSku(), line2.getSku());
        assertNotEquals("Instâncias diferentes devem ter quantidades diferentes",
                line1.getQty(), line2.getQty());
    }

    @Test
    public void testSKU_CaseSensitivity() {
        OrderLine lowerLine = new OrderLine("ORD-CASE", 1, "sku100", 10);
        OrderLine upperLine = new OrderLine("ORD-CASE", 2, "SKU100", 10);

        assertNotEquals("SKUs com case diferente devem ser distintos",
                lowerLine.getSku(), upperLine.getSku());
    }

    @Test
    public void testOrderLine_SameOrderDifferentLines() {
        OrderLine line1 = new OrderLine("ORD-SAME", 1, "SKU-A", 10);
        OrderLine line2 = new OrderLine("ORD-SAME", 2, "SKU-B", 20);
        OrderLine line3 = new OrderLine("ORD-SAME", 3, "SKU-C", 30);

        assertEquals("Todas as linhas devem ter o mesmo order ID",
                line1.getOrderId(), line2.getOrderId());
        assertEquals("Todas as linhas devem ter o mesmo order ID",
                line2.getOrderId(), line3.getOrderId());

        // But different line numbers
        assertNotEquals(line1.getLineNo(), line2.getLineNo());
        assertNotEquals(line2.getLineNo(), line3.getLineNo());
        assertNotEquals(line1.getLineNo(), line3.getLineNo());
    }

    @Test
    public void testOrderLine_NegativeQuantity() {
        // While not recommended, test if negative quantities are stored
        OrderLine negativeLine = new OrderLine("ORD-NEG", 1, "SKU-NEG", -5);
        assertEquals("Quantidade negativa deve ser armazenada (sem validação no construtor)",
                -5, negativeLine.getQty());
    }
}
