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

        // Different aisle distance: D = b1 + |a1 - a2| + b2 (coordinates already * 3)
        // From (3,5) to (6,2): 5 + |3-6| + 2 = 5 + 3 + 2 = 10
        assertEquals(10.0, coord1.distanceTo(coord3), 0.001);

        // Symmetric distance
        assertEquals(coord1.distanceTo(coord3), coord3.distanceTo(coord1), 0.001);
    }

    @Test
    public void testConsolidatedBay() {
        PickPathSequencing.ConsolidatedBay bay = new PickPathSequencing.ConsolidatedBay(1, 5);
        
        assertEquals(1, bay.aisle);
        assertEquals(5, bay.bay);
        assertEquals(0, bay.totalQty);
        assertEquals(0.0, bay.totalWeight, 0.001);
        assertNotNull(bay.allocations);
        assertEquals("1,5", bay.getKey());
        
        PickPathSequencing.Coordinate coord = bay.getCoordinate();
        assertEquals(3, coord.x); // aisle * 3
        assertEquals(5, coord.y);
    }

    @Test
    public void testStrategyA_DeterministicSweep() {
        PickPathSequencing.PickPath path = sequencer.strategyA(testAllocations);

        assertNotNull(path);
        assertEquals("Strategy A - Deterministic Sweep", path.strategyName);
        assertNotNull(path.route);
        assertTrue(path.totalDistance >= 0);

        // Should consolidate bays and sort by aisle, then bay
        // Expected bays: (1,1), (1,2), (1,3), (2,1), (2,2)
        assertEquals(5, path.route.size());

        // Check ordering: aisle 1 bays first, then aisle 2 bays
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

        assertNotNull(path);
        assertEquals("Strategy B - Nearest-Neighbour", path.strategyName);
        assertNotNull(path.route);
        assertTrue(path.totalDistance >= 0);

        // Should visit all bays
        assertEquals(5, path.route.size());

        // First bay should be closest to origin (0,0)
        // From (0,0) to (1,1): 0 + |0-3| + 1 = 4
        // From (0,0) to (1,2): 0 + |0-3| + 2 = 5
        // From (0,0) to (1,3): 0 + |0-3| + 3 = 6
        // From (0,0) to (2,1): 0 + |0-6| + 1 = 7
        // From (0,0) to (2,2): 0 + |0-6| + 2 = 8
        // So first should be (1,1)
        assertEquals(1, path.route.get(0).aisle);
        assertEquals(1, path.route.get(0).bay);
    }

    @Test
    public void testGenerateBothPaths() {
        PickPathSequencing.PickPath[] paths = sequencer.generateBothPaths(testAllocations);

        assertNotNull(paths);
        assertEquals(2, paths.length);

        PickPathSequencing.PickPath pathA = paths[0];
        PickPathSequencing.PickPath pathB = paths[1];

        assertEquals("Strategy A - Deterministic Sweep", pathA.strategyName);
        assertEquals("Strategy B - Nearest-Neighbour", pathB.strategyName);

        // Both should visit all bays
        assertEquals(5, pathA.route.size());
        assertEquals(5, pathB.route.size());

        // Both should have valid distances
        assertTrue(pathA.totalDistance >= 0);
        assertTrue(pathB.totalDistance >= 0);
    }

    @Test
    public void testEmptyAllocations() {
        List<Allocation> emptyAllocations = new ArrayList<>();

        PickPathSequencing.PickPath pathA = sequencer.strategyA(emptyAllocations);
        PickPathSequencing.PickPath pathB = sequencer.strategyB(emptyAllocations);

        assertNotNull(pathA);
        assertNotNull(pathB);
        assertEquals(0, pathA.route.size());
        assertEquals(0, pathB.route.size());
        assertEquals(0.0, pathA.totalDistance, 0.001);
        assertEquals(0.0, pathB.totalDistance, 0.001);
    }

    @Test
    public void testNullAllocations() {
        PickPathSequencing.PickPath pathA = sequencer.strategyA(null);
        PickPathSequencing.PickPath pathB = sequencer.strategyB(null);

        assertNotNull(pathA);
        assertNotNull(pathB);
        assertEquals(0, pathA.route.size());
        assertEquals(0, pathB.route.size());
    }

    @Test
    public void testDuplicateBaysConsolidation() {
        // Create allocations with same aisle and bay
        List<Allocation> duplicateAllocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU001", 5, "BOX001", "1", "1"),
                new Allocation("ORD002", 1, "SKU002", 3, "BOX002", "1", "1"), // Same bay
                new Allocation("ORD003", 1, "SKU003", 2, "BOX003", "2", "2")
        );

        PickPathSequencing.PickPath path = sequencer.strategyA(duplicateAllocations);

        // Should consolidate into 2 unique bays: (1,1) and (2,2)
        assertEquals(2, path.route.size());

        // Find the consolidated bay (1,1)
        PickPathSequencing.ConsolidatedBay bay11 = path.route.stream()
                .filter(b -> b.aisle == 1 && b.bay == 1)
                .findFirst()
                .orElse(null);

        assertNotNull(bay11);
        assertEquals(2, bay11.allocations.size()); // Should have 2 allocations
        assertEquals(8, bay11.totalQty); // 5 + 3
    }

    @Test
    public void testDistanceCalculation() {
        // Test with specific allocations to verify distance calculation
        List<Allocation> simpleAllocations = Arrays.asList(
                new Allocation("ORD001", 1, "SKU001", 1, "BOX001", "1", "1"),
                new Allocation("ORD002", 1, "SKU002", 1, "BOX002", "1", "2")
        );

        PickPathSequencing.PickPath path = sequencer.strategyA(simpleAllocations);

        // Distance from (0,0) to (3,1) = 0 + |0-3| + 1 = 4
        // Distance from (3,1) to (3,2) = |1-2| = 1
        // Total = 4 + 1 = 5
        assertEquals(5.0, path.totalDistance, 0.001);
    }

    @Test
    public void testPickPathToString() {
        PickPathSequencing.PickPath path = sequencer.strategyA(testAllocations);
        String pathString = path.toString();

        assertNotNull(pathString);
        assertTrue(pathString.contains("Strategy A - Deterministic Sweep"));
        assertTrue(pathString.contains("Total Distance"));
        assertTrue(pathString.contains("Route sequence"));
    }

    @Test
    public void testConsolidatedBayToString() {
        PickPathSequencing.ConsolidatedBay bay = new PickPathSequencing.ConsolidatedBay(2, 5);
        bay.totalQty = 10;
        
        String bayString = bay.toString();
        assertNotNull(bayString);
        assertTrue(bayString.contains("aisle=2"));
        assertTrue(bayString.contains("bay=5"));
        assertTrue(bayString.contains("qty=10"));
    }
}