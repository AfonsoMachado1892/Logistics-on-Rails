package ESINF.model;

import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.junit.Assert.*;

public class PickingPlanTest {

    private Map<String, Item> itemCatalog;
    private PickingPlan pickingPlan;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        itemCatalog = new HashMap<>();
        itemCatalog.put("SKU001", new Item("SKU001", "Item A", "Category1", "kg", 100.0, 2.0));
        itemCatalog.put("SKU002", new Item("SKU002", "Item B", "Category1", "kg", 50.0, 1.5));
        itemCatalog.put("SKU003", new Item("SKU003", "Item C", "Category2", "kg", 75.0, 3.0));
        itemCatalog.put("SKU004", new Item("SKU004", "Item D", "Category2", "kg", 25.0, 0.5));

        pickingPlan = new PickingPlan(10.0, itemCatalog);
    }


    private void suppressWarnings() {
        System.setErr(new PrintStream(errContent));
    }


    private void restoreWarnings() {
        System.setErr(originalErr);
    }

    @Test
    public void testInvalidAllocationWithNegativeQuantity() {
        suppressWarnings();

        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", -5, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 5, "BOX002", "A1", "B2")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        restoreWarnings();

        assertNotNull(trolleys);
        assertEquals(1, trolleys.size());
        assertEquals(1, trolleys.get(0).getAllocations().size());
    }
    @Test
    public void testAllocationExceedsTrolleyCapacity() {
        suppressWarnings();

        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU001", 10, "BOX001", "A1", "B1")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        restoreWarnings();

        assertNotNull(trolleys);
        assertEquals(1, trolleys.size());
        assertTrue(trolleys.get(0).getUsedWeight() > trolleys.get(0).getCapacity());
    }

    @Test
    public void testUnknownSKU() {
        suppressWarnings();

        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU999", 5, "BOX001", "A1", "B1")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        restoreWarnings();

        assertNotNull(trolleys);
        assertEquals(1, trolleys.size());
        assertEquals(0.0, trolleys.get(0).getUsedWeight(), 0.01);
    }

    @Test
    public void testInvalidAllocationWithZeroQuantity() {
        suppressWarnings();

        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 0, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 5, "BOX002", "A1", "B2")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        restoreWarnings();

        assertNotNull(trolleys);
        assertEquals(1, trolleys.size());
        assertEquals(1, trolleys.get(0).getAllocations().size());
    }

  
}

