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

    @Test
    public void testAllocationWithNullSKU() {
        suppressWarnings();

        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, null, 5, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 5, "BOX002", "A1", "B2")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        restoreWarnings();

        assertNotNull(trolleys);
        assertEquals(1, trolleys.size());
        assertEquals(1, trolleys.get(0).getAllocations().size());
    }

    @Test
    public void testAllocationWithEmptySKU() {
        suppressWarnings();

        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "", 5, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 5, "BOX002", "A1", "B2")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        restoreWarnings();

        assertNotNull(trolleys);
        assertEquals(1, trolleys.size());
        assertEquals(1, trolleys.get(0).getAllocations().size());
    }

    @Test
    public void testConstructorWithValidParameters() {
        assertNotNull(pickingPlan);
        PickingPlan pp = new PickingPlan(50.0, itemCatalog);
        assertNotNull(pp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroCapacity() {
        new PickingPlan(0, itemCatalog);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeCapacity() {
        new PickingPlan(-10, itemCatalog);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullCatalog() {
        new PickingPlan(10.0, null);
    }

    @Test
    public void testGeneratePickingPlanWithNullAllocations() {
        suppressWarnings();
        List<Trolley> result = pickingPlan.generatePickingPlan(null, PickingPlan.Heuristic.FF);
        restoreWarnings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGeneratePickingPlanWithEmptyAllocations() {
        suppressWarnings();
        List<Allocation> allocations = new ArrayList<>();
        List<Trolley> result = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);
        restoreWarnings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGeneratePickingPlanWithNullHeuristic() {
        List<Allocation> allocations = createSampleAllocations();
        pickingPlan.generatePickingPlan(allocations, null);
    }

    @Test
    public void testFirstFitHeuristic() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 10, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 6, "BOX002", "A1", "B2"),
                new Allocation("ORD002", 1, "SKU004", 8, "BOX003", "A2", "B1")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        assertNotNull(trolleys);
        assertEquals(2, trolleys.size());
        assertEquals(2, trolleys.get(0).getAllocations().size());
        assertTrue(trolleys.get(0).getUsedWeight() <= 10.0);
        assertEquals(1, trolleys.get(1).getAllocations().size());
    }

    @Test
    public void testFirstFitDecreasingHeuristic() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 4, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 12, "BOX002", "A1", "B2"),
                new Allocation("ORD002", 1, "SKU004", 8, "BOX003", "A2", "B1")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FFD);

        assertNotNull(trolleys);
        assertEquals(2, trolleys.size());
        assertEquals(2, trolleys.get(0).getAllocations().size());
        assertEquals(10.0, trolleys.get(0).getUsedWeight(), 0.01);
        assertEquals(1, trolleys.get(1).getAllocations().size());
        assertEquals(2.0, trolleys.get(1).getUsedWeight(), 0.01);
    }

    @Test
    public void testBestFitDecreasingHeuristic() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 14, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 6, "BOX002", "A1", "B2"),
                new Allocation("ORD002", 1, "SKU004", 4, "BOX003", "A2", "B1")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.BFD);

        assertNotNull(trolleys);
        assertEquals(2, trolleys.size());
        for (Trolley t : trolleys) {
            assertTrue(t.getUsedWeight() <= 10.0);
        }
    }

    @Test
    public void testPerfectFit() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 20, "BOX001", "A1", "B1")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        assertNotNull(trolleys);
        assertEquals(1, trolleys.size());
        assertEquals(10.0, trolleys.get(0).getUsedWeight(), 0.01);
        assertEquals(1.0, trolleys.get(0).getUtilization(), 0.01);
    }

    @Test
    public void testMultipleTrolleysNeeded() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 10, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 10, "BOX002", "A1", "B2"),
                new Allocation("ORD002", 1, "SKU004", 10, "BOX003", "A2", "B1"),
                new Allocation("ORD002", 2, "SKU004", 10, "BOX004", "A2", "B2")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        assertNotNull(trolleys);
        assertEquals(2, trolleys.size());
        assertEquals(10.0, trolleys.get(0).getUsedWeight(), 0.01);
        assertEquals(10.0, trolleys.get(1).getUsedWeight(), 0.01);
    }

    @Test
    public void testBFDProducesBetterPackingThanFF() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 12, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 8, "BOX002", "A1", "B2"),
                new Allocation("ORD002", 1, "SKU004", 8, "BOX003", "A2", "B1"),
                new Allocation("ORD002", 2, "SKU004", 4, "BOX004", "A2", "B2")
        );

        List<Trolley> trolleysFF = pickingPlan.generatePickingPlan(
                new ArrayList<>(allocations), PickingPlan.Heuristic.FF);
        List<Trolley> trolleysBFD = pickingPlan.generatePickingPlan(
                new ArrayList<>(allocations), PickingPlan.Heuristic.BFD);

        assertTrue(trolleysBFD.size() <= trolleysFF.size());
    }

    @Test
    public void testTrolleyNumbering() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 12, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 12, "BOX002", "A1", "B2")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        assertEquals(2, trolleys.size());
        assertEquals(1, trolleys.get(0).getId());
        assertEquals(2, trolleys.get(1).getId());
    }

    @Test
    public void testMixedWeights() {
        List<Allocation> allocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU001", 2, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 8, "BOX002", "A1", "B2"),
                new Allocation("ORD002", 1, "SKU002", 2, "BOX003", "A2", "B1")
        );

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(allocations, PickingPlan.Heuristic.FF);

        assertNotNull(trolleys);
        assertEquals(2, trolleys.size());
        for (Trolley t : trolleys) {
            assertTrue(t.getUsedWeight() <= 10.0);
        }
    }

    @Test
    public void testAllHeuristicsProduceSameTotalWeight() {
        List<Allocation> allocations = createSampleAllocations();

        List<Trolley> trolleysFF = pickingPlan.generatePickingPlan(
                new ArrayList<>(allocations), PickingPlan.Heuristic.FF);
        List<Trolley> trolleysFFD = pickingPlan.generatePickingPlan(
                new ArrayList<>(allocations), PickingPlan.Heuristic.FFD);
        List<Trolley> trolleysBFD = pickingPlan.generatePickingPlan(
                new ArrayList<>(allocations), PickingPlan.Heuristic.BFD);

        double totalFF = 0;
        for (Trolley t : trolleysFF) {
            totalFF += t.getUsedWeight();
        }

        double totalFFD = 0;
        for (Trolley t : trolleysFFD) {
            totalFFD += t.getUsedWeight();
        }

        double totalBFD = 0;
        for (Trolley t : trolleysBFD) {
            totalBFD += t.getUsedWeight();
        }

        assertEquals(totalFF, totalFFD, 0.01);
        assertEquals(totalFF, totalBFD, 0.01);
    }

    private List<Allocation> createSampleAllocations() {
        return Arrays.asList(
                new Allocation("ORD001", 1, "SKU004", 10, "BOX001", "A1", "B1"),
                new Allocation("ORD001", 2, "SKU004", 8, "BOX002", "A1", "B2"),
                new Allocation("ORD002", 1, "SKU004", 6, "BOX003", "A2", "B1")
        );
    }
}


