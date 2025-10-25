package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AllocationTest {

    private Allocation allocation1;
    private Allocation allocation2;
    private Allocation allocation3;

    @Before
    public void setUp() {
        allocation1 = new Allocation("ORD001", 1, "SKU100", 50, "BOX001", "1", "5");
        allocation2 = new Allocation("ORD001", 2, "SKU200", 30, "BOX002", "2", "10");
        allocation3 = new Allocation("ORD002", 1, "SKU300", 100, "BOX003", "3", "15");
    }

    // --- Testes de Lógica Principal ---

    @Test
    public void testConstructorAndGetters() {
        assertEquals("Order ID deve ser ORD001", "ORD001", allocation1.getOrderId());
        assertEquals("Line number deve ser 1", 1, allocation1.getLineNo());
        assertEquals("SKU deve ser SKU100", "SKU100", allocation1.getSku());
        assertEquals("Quantity deve ser 50", 50, allocation1.getQty());
        assertEquals("Box ID deve ser BOX001", "BOX001", allocation1.getBoxId());
        assertEquals("Aisle deve ser 1", "1", allocation1.getAisle());
        assertEquals("Bay deve ser 5", "5", allocation1.getBay());
    }

    @Test
    public void testGetOrderId() {
        assertEquals("ORD001", allocation1.getOrderId());
        assertEquals("ORD001", allocation2.getOrderId());
        assertEquals("ORD002", allocation3.getOrderId());
    }

    @Test
    public void testGetLineNo() {
        assertEquals("Line number da alocação 1 deve ser 1", 1, allocation1.getLineNo());
        assertEquals("Line number da alocação 2 deve ser 2", 2, allocation2.getLineNo());
        assertEquals("Line number da alocação 3 deve ser 1", 1, allocation3.getLineNo());
    }

    @Test
    public void testGetSku() {
        assertEquals("SKU100", allocation1.getSku());
        assertEquals("SKU200", allocation2.getSku());
        assertEquals("SKU300", allocation3.getSku());
    }

    @Test
    public void testGetQty() {
        assertEquals("Quantidade da alocação 1 deve ser 50", 50, allocation1.getQty());
        assertEquals("Quantidade da alocação 2 deve ser 30", 30, allocation2.getQty());
        assertEquals("Quantidade da alocação 3 deve ser 100", 100, allocation3.getQty());
    }

    @Test
    public void testGetBoxId() {
        assertEquals("BOX001", allocation1.getBoxId());
        assertEquals("BOX002", allocation2.getBoxId());
        assertEquals("BOX003", allocation3.getBoxId());
    }

    @Test
    public void testGetAisle() {
        assertEquals("Aisle da alocação 1 deve ser 1", "1", allocation1.getAisle());
        assertEquals("Aisle da alocação 2 deve ser 2", "2", allocation2.getAisle());
        assertEquals("Aisle da alocação 3 deve ser 3", "3", allocation3.getAisle());
    }

    @Test
    public void testGetBay() {
        assertEquals("Bay da alocação 1 deve ser 5", "5", allocation1.getBay());
        assertEquals("Bay da alocação 2 deve ser 10", "10", allocation2.getBay());
        assertEquals("Bay da alocação 3 deve ser 15", "15", allocation3.getBay());
    }

    @Test
    public void testToString() {
        String str = allocation1.toString();

        assertTrue("String deve conter 'Allocation'", str.contains("Allocation"));
        assertTrue("String deve conter order ID", str.contains("ORD001"));
        assertTrue("String deve conter 'line 1'", str.contains("line 1"));
        assertTrue("String deve conter SKU", str.contains("SKU=SKU100"));
        assertTrue("String deve conter quantity", str.contains("qty=50"));
        assertTrue("String deve conter box ID", str.contains("box=BOX001"));
        assertTrue("String deve conter aisle", str.contains("aisle=1"));
        assertTrue("String deve conter bay", str.contains("bay=5"));
    }

    @Test
    public void testMultipleAllocationsFromSameOrder() {
        // Duas alocações da mesma ordem devem ter o mesmo orderId
        assertEquals("Ambas as alocações devem pertencer à mesma ordem",
                allocation1.getOrderId(), allocation2.getOrderId());
        assertNotEquals("Line numbers devem ser diferentes",
                allocation1.getLineNo(), allocation2.getLineNo());
    }

    @Test
    public void testDifferentOrders() {
        // Alocações de ordens diferentes devem ter orderIds diferentes
        assertNotEquals("Order IDs devem ser diferentes",
                allocation1.getOrderId(), allocation3.getOrderId());
    }

    // --- Testes de Cobertura (Edge Cases) ---

    @Test
    public void testConstructor_AllParametersUsed() {
        String testOrderId = "TEST-ORDER";
        int testLineNo = 99;
        String testSku = "TEST-SKU";
        int testQty = 777;
        String testBoxId = "TEST-BOX";
        String testAisle = "TEST-AISLE";
        String testBay = "TEST-BAY";

        Allocation testAllocation = new Allocation(
                testOrderId, testLineNo, testSku, testQty,
                testBoxId, testAisle, testBay);

        assertEquals(testOrderId, testAllocation.getOrderId());
        assertEquals(testLineNo, testAllocation.getLineNo());
        assertEquals(testSku, testAllocation.getSku());
        assertEquals(testQty, testAllocation.getQty());
        assertEquals(testBoxId, testAllocation.getBoxId());
        assertEquals(testAisle, testAllocation.getAisle());
        assertEquals(testBay, testAllocation.getBay());
    }

    @Test
    public void testConstructor_MinimumQuantity() {
        Allocation minAllocation = new Allocation(
                "ORD-MIN", 1, "SKU-MIN", 1, "BOX-MIN", "1", "1");
        assertEquals("Quantidade mínima deve ser 1", 1, minAllocation.getQty());
    }

    @Test
    public void testConstructor_LargeQuantity() {
        Allocation largeAllocation = new Allocation(
                "ORD-LARGE", 1, "SKU-LARGE", 999999, "BOX-LARGE", "1", "1");
        assertEquals("Quantidade grande deve ser armazenada corretamente",
                999999, largeAllocation.getQty());
    }

    @Test
    public void testConstructor_ZeroQuantity() {
        Allocation zeroAllocation = new Allocation(
                "ORD-ZERO", 1, "SKU-ZERO", 0, "BOX-ZERO", "0", "0");
        assertEquals("Quantidade zero deve ser permitida", 0, zeroAllocation.getQty());
    }

    @Test
    public void testAisleAndBay_StringFormat() {
        // Aisle e Bay são Strings, podem ter formatos diversos
        Allocation alloc1 = new Allocation("ORD", 1, "SKU", 10, "BOX", "A1", "B2");
        assertEquals("A1", alloc1.getAisle());
        assertEquals("B2", alloc1.getBay());

        Allocation alloc2 = new Allocation("ORD", 1, "SKU", 10, "BOX", "123", "456");
        assertEquals("123", alloc2.getAisle());
        assertEquals("456", alloc2.getBay());
    }

    @Test
    public void testGetters_Consistency() {
        // Multiple calls to getters should return the same values
        String orderId1 = allocation1.getOrderId();
        String orderId2 = allocation1.getOrderId();
        assertSame("Múltiplas chamadas ao getter devem retornar o mesmo objeto",
                orderId1, orderId2);

        String boxId1 = allocation1.getBoxId();
        String boxId2 = allocation1.getBoxId();
        assertSame("Múltiplas chamadas ao getter devem retornar o mesmo objeto",
                boxId1, boxId2);
    }

    @Test
    public void testMultipleInstances_Independence() {
        Allocation alloc1 = new Allocation("ORD-A", 1, "SKU-A", 10, "BOX-A", "1", "1");
        Allocation alloc2 = new Allocation("ORD-B", 2, "SKU-B", 20, "BOX-B", "2", "2");

        assertNotEquals("Instâncias diferentes devem ter order IDs diferentes",
                alloc1.getOrderId(), alloc2.getOrderId());
        assertNotEquals("Instâncias diferentes devem ter box IDs diferentes",
                alloc1.getBoxId(), alloc2.getBoxId());
    }

    @Test
    public void testToString_ContainsAllFields() {
        Allocation fullAllocation = new Allocation(
                "ORD-FULL", 5, "SKU-FULL", 250, "BOX-FULL", "AISLE-10", "BAY-20");

        String str = fullAllocation.toString();

        assertTrue("String deve conter order ID", str.contains("ORD-FULL"));
        assertTrue("String deve conter line number", str.contains("5"));
        assertTrue("String deve conter SKU", str.contains("SKU-FULL"));
        assertTrue("String deve conter quantity", str.contains("250"));
        assertTrue("String deve conter box ID", str.contains("BOX-FULL"));
        assertTrue("String deve conter aisle", str.contains("AISLE-10"));
        assertTrue("String deve conter bay", str.contains("BAY-20"));
    }

    @Test
    public void testAllocation_PartialBoxAllocation() {
        // Simula alocação parcial de uma box (não toda a quantidade)
        Allocation partialAllocation = new Allocation(
                "ORD-PARTIAL", 1, "SKU-PARTIAL", 15, "BOX-PARTIAL", "3", "7");

        assertEquals("Quantidade alocada deve ser 15", 15, partialAllocation.getQty());
        assertEquals("Box ID deve ser registado", "BOX-PARTIAL", partialAllocation.getBoxId());
    }

    @Test
    public void testAllocation_MultipleAllocationsFromSameBox() {
        // Duas alocações diferentes podem referenciar a mesma box (em teoria)
        Allocation alloc1 = new Allocation("ORD-X", 1, "SKU-X", 10, "BOX-SHARED", "1", "1");
        Allocation alloc2 = new Allocation("ORD-Y", 1, "SKU-Y", 20, "BOX-SHARED", "1", "1");

        assertEquals("Ambas devem referenciar a mesma box",
                alloc1.getBoxId(), alloc2.getBoxId());
        assertEquals("Ambas devem estar no mesmo aisle",
                alloc1.getAisle(), alloc2.getAisle());
        assertEquals("Ambas devem estar na mesma bay",
                alloc1.getBay(), alloc2.getBay());
    }

    @Test
    public void testAllocation_NegativeQuantity() {
        // Teste edge case: quantidade negativa (não recomendado mas sem validação)
        Allocation negAllocation = new Allocation(
                "ORD-NEG", 1, "SKU-NEG", -10, "BOX-NEG", "1", "1");
        assertEquals("Quantidade negativa deve ser armazenada (sem validação)",
                -10, negAllocation.getQty());
    }

    @Test
    public void testAllocation_EmptyStrings() {
        // Teste com strings vazias
        Allocation emptyAllocation = new Allocation(
                "", 1, "", 10, "", "", "");

        assertEquals("", emptyAllocation.getOrderId());
        assertEquals("", emptyAllocation.getSku());
        assertEquals("", emptyAllocation.getBoxId());
        assertEquals("", emptyAllocation.getAisle());
        assertEquals("", emptyAllocation.getBay());
    }
}
