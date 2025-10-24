package ESINF.model;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class PickPathSequencingTest {

    private PickPathSequencing sequencer;
    private Map<String, Item> itemCatalog;
    private List<Allocation> testAllocations;

    @Before
    public void setUp() {
        // Create test item catalog
        itemCatalog = new HashMap<>();
        itemCatalog.put("SKU001", new Item("SKU001", "Item 1", "Electronics", "units", 0.1, 1.0));
        itemCatalog.put("SKU002", new Item("SKU002", "Item 2", "Clothing", "units", 0.2, 2.0));
        itemCatalog.put("SKU003", new Item("SKU003", "Item 3", "Food", "kg", 0.05, 0.5));

        sequencer = new PickPathSequencing(itemCatalog);

        // Create test allocations with different aisles and bays
        testAllocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU001", 5, "BOX001", "1", "3"),
                new Allocation("ORD001", 2, "SKU002", 3, "BOX002", "1", "1"),
                new Allocation("ORD002", 1, "SKU003", 2, "BOX003", "2", "2"),
                new Allocation("ORD002", 2, "SKU001", 4, "BOX004", "2", "1"),
                new Allocation("ORD003", 1, "SKU002", 1, "BOX005", "1", "2")
        );
    }

    @Test
    public void testConstructor() {
        // Test valid constructor
        PickPathSequencing validSequencer = new PickPathSequencing(itemCatalog);
        assertNotNull(validSequencer);

        // Test null catalog
        try {
            new PickPathSequencing(null);
            fail("Should throw IllegalArgumentException for null catalog");
        } catch (IllegalArgumentException e) {
            assertEquals("Item catalog cannot be null", e.getMessage());
        }
    }


    @Test
    public void testCoordinateDistance() {
        PickPathSequencing.Coordinate coord1 = new PickPathSequencing.Coordinate(3, 5);
        PickPathSequencing.Coordinate coord2 = new PickPathSequencing.Coordinate(3, 8);
        PickPathSequencing.Coordinate coord3 = new PickPathSequencing.Coordinate(6, 2);

        // Same aisle distance
        assertEquals(3.0, coord1.distanceTo(coord2), 0.001);

        // Different aisle distance
        assertEquals(10.0, coord1.distanceTo(coord3), 0.001);

        // Symmetry
        assertEquals(coord1.distanceTo(coord3), coord3.distanceTo(coord1), 0.001);
    }

    @Test
    public void testConsolidatedBay() {
        PickPathSequencing.ConsolidatedBay bay = new PickPathSequencing.ConsolidatedBay(1, 5);

        assertEquals(1, bay.aisle);
        assertEquals(5, bay.bay);
        assertEquals("1,5", bay.getKey());
        assertNotNull(bay.allocations);

        PickPathSequencing.Coordinate coord = bay.getCoordinate();
        assertEquals(3, coord.x);
        assertEquals(5, coord.y);
    }

    @Test
    public void testStrategyA_DeterministicSweep() {
        PickPathSequencing.PickPath path = sequencer.strategyA(testAllocations);

        assertEquals("Strategy A - Deterministic Sweep", path.strategyName);
        assertEquals(5, path.route.size());

        // Check ordering: aisle 1 bays first (1,1),(1,2),(1,3) then aisle 2 (2,1),(2,2)
        assertEquals(1, path.route.get(0).aisle);
        assertEquals(1, path.route.get(0).bay);

        assertEquals(1, path.route.get(1).aisle);
        assertEquals(2, path.route.get(1).bay);

        assertEquals(1, path.route.get(2).aisle);
        assertEquals(3, path.route.get(2).bay);

        assertEquals(2, path.route.get(3).aisle);
        assertEquals(1, path.route.get(3).bay);

        assertEquals(2, path.route.get(4).aisle);
        assertEquals(2, path.route.get(4).bay);
    }


    @Test
    public void testStrategyB_NearestNeighbour() {
        PickPathSequencing.PickPath path = sequencer.strategyB(testAllocations);

        assertEquals("Strategy B - Nearest-Neighbour", path.strategyName);
        assertEquals(5, path.route.size());

        // First bay should be the nearest to (0,0)
        assertEquals(1, path.route.get(0).aisle);
        assertEquals(1, path.route.get(0).bay);
    }

    @Test
    public void testGenerateBothPaths() {
        PickPathSequencing.PickPath[] paths = sequencer.generateBothPaths(testAllocations);

        assertEquals(2, paths.length);
        assertEquals("Strategy A - Deterministic Sweep", paths[0].strategyName);
        assertEquals("Strategy B - Nearest-Neighbour", paths[1].strategyName);
        assertEquals(5, paths[0].route.size());
        assertEquals(5, paths[1].route.size());
    }

    @Test
    public void testEmptyAllocations() {
        List<Allocation> emptyAllocations = new ArrayList<>();

        PickPathSequencing.PickPath pathA = sequencer.strategyA(emptyAllocations);
        PickPathSequencing.PickPath pathB = sequencer.strategyB(emptyAllocations);

        assertTrue(pathA.route.isEmpty());
        assertTrue(pathB.route.isEmpty());
        assertEquals(0.0, pathA.totalDistance, 0.001);
        assertEquals(0.0, pathB.totalDistance, 0.001);
    }

    @Test
    public void testNullAllocations() {
        PickPathSequencing.PickPath pathA = sequencer.strategyA(null);
        PickPathSequencing.PickPath pathB = sequencer.strategyB(null);

        assertNotNull(pathA);
        assertNotNull(pathB);
        assertTrue(pathA.route.isEmpty());
        assertTrue(pathB.route.isEmpty());
    }

    @Test
    public void testDuplicateBaysConsolidation() {
        List<Allocation> duplicateAllocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU001", 5, "BOX001", "1", "1"),
                new Allocation("ORD002", 1, "SKU002", 3, "BOX002", "1", "1"),
                new Allocation("ORD003", 1, "SKU003", 2, "BOX003", "2", "2")
        );

        PickPathSequencing.PickPath path = sequencer.strategyA(duplicateAllocations);

        assertEquals(2, path.route.size());

        PickPathSequencing.ConsolidatedBay bay11 = path.route.stream()
                .filter(b -> b.aisle == 1 && b.bay == 1)
                .findFirst()
                .orElse(null);

        assertNotNull(bay11);
        assertEquals(2, bay11.allocations.size());
        assertEquals(8, bay11.totalQty);
    }

    @Test
    public void testDistanceCalculation() {
        List<Allocation> simpleAllocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU001", 1, "BOX001", "1", "1"),
                new Allocation("ORD002", 1, "SKU002", 1, "BOX002", "1", "2")
        );

        PickPathSequencing.PickPath path = sequencer.strategyA(simpleAllocations);
        assertEquals(5.0, path.totalDistance, 0.001);
    }

    @Test
    public void testPickPathToString() {
        PickPathSequencing.PickPath path = sequencer.strategyA(testAllocations);
        String s = path.toString();

        assertTrue(s.contains("Strategy A - Deterministic Sweep"));
        assertTrue(s.contains("Total Distance"));
        assertTrue(s.contains("Route sequence"));
    }

    @Test
    public void testConsolidatedBayToString() {
        PickPathSequencing.ConsolidatedBay bay = new PickPathSequencing.ConsolidatedBay(2, 5);
        bay.totalQty = 10;
        String s = bay.toString();

        assertTrue(s.contains("aisle=2"));
        assertTrue(s.contains("bay=5"));
        assertTrue(s.contains("qty=10"));
    }

    @Test
    public void testCoordinateToString() {
        PickPathSequencing.Coordinate coord = new PickPathSequencing.Coordinate(3, 7);
        assertEquals("(3,7)", coord.toString());
    }

    @Test
    public void testCalculateTotalDistanceIndirectly() {
        List<Allocation> allocs = Arrays.asList(
                new Allocation("O1", 1, "SKU001", 1, "B1", "1", "1"),
                new Allocation("O2", 2, "SKU002", 1, "B2", "2", "1")
        );

        PickPathSequencing.PickPath path = sequencer.strategyA(allocs);
        assertTrue(path.totalDistance > 0);
    }
}
