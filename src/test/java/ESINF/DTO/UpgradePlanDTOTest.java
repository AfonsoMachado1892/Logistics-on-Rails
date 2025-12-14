package ESINF.DTO;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpgradePlanDTOTest {

    @Test
    void testWithTopologicalOrder() {
        List<String> order = Arrays.asList("A", "B", "C", "D");
        UpgradePlanDTO dto = UpgradePlanDTO.withTopologicalOrder(order);

        assertFalse(dto.hasCycles(), "Should not have cycles");
        assertEquals(order, dto.getTopologicalOrder(), "Should return topological order");
        assertTrue(dto.getCycles().isEmpty(), "Cycles list should be empty");
        assertEquals(0, dto.getUniqueStationsInCycles(), "Should have 0 unique stations in cycles");
    }

    @Test
    void testWithTopologicalOrderNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UpgradePlanDTO.withTopologicalOrder(null),
                "Should throw exception for null order");
    }

    @Test
    void testWithCycles() {
        List<String> vertices = Arrays.asList("A", "B", "C");
        List<String> edges = Arrays.asList("A → B", "B → C", "C → A");
        UpgradePlanDTO.CycleInfo cycleInfo = new UpgradePlanDTO.CycleInfo(vertices, edges);

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycleInfo));

        assertTrue(dto.hasCycles(), "Should have cycles");
        assertEquals(1, dto.getCycles().size(), "Should have one cycle");
        assertTrue(dto.getTopologicalOrder().isEmpty(), "Topological order should be empty");
        assertEquals(3, dto.getUniqueStationsInCycles(), "Should have 3 unique stations in cycle");
    }

    @Test
    void testWithCyclesNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UpgradePlanDTO.withCycles(null),
                "Should throw exception for null cycles");
    }

    @Test
    void testWithCyclesEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> UpgradePlanDTO.withCycles(Arrays.asList()),
                "Should throw exception for empty cycles list");
    }

    @Test
    void testCycleInfo() {
        List<String> vertices = Arrays.asList("A", "B", "C");
        List<String> edges = Arrays.asList("A → B", "B → C", "C → A");
        UpgradePlanDTO.CycleInfo cycleInfo = new UpgradePlanDTO.CycleInfo(vertices, edges);

        assertEquals(vertices, cycleInfo.getVertices(), "Should return vertices");
        assertEquals(edges, cycleInfo.getEdges(), "Should return edges");
    }

    @Test
    void testCycleInfoImmutability() {
        List<String> vertices = Arrays.asList("A", "B", "C");
        List<String> edges = Arrays.asList("A → B", "B → C", "C → A");
        UpgradePlanDTO.CycleInfo cycleInfo = new UpgradePlanDTO.CycleInfo(vertices, edges);

        List<String> returnedVertices = cycleInfo.getVertices();
        returnedVertices.add("D");

        assertEquals(3, cycleInfo.getVertices().size(), "Original should not be modified");
    }

    @Test
    void testToStringWithTopologicalOrder() {
        List<String> order = Arrays.asList("A", "B", "C");
        UpgradePlanDTO dto = UpgradePlanDTO.withTopologicalOrder(order);

        String str = dto.toString();
        assertTrue(str.contains("A"), "Should contain station A");
        assertTrue(str.contains("→"), "Should contain arrow");
    }

    @Test
    void testToStringWithCycles() {
        List<String> vertices = Arrays.asList("A", "B");
        List<String> edges = Arrays.asList("A → B", "B → A");
        UpgradePlanDTO.CycleInfo cycleInfo = new UpgradePlanDTO.CycleInfo(vertices, edges);

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycleInfo));

        String str = dto.toString();
        assertTrue(str.contains("cycle"), "Should mention cycles");
        assertTrue(str.contains("Unique stations involved"), "Should mention unique stations");
    }

    @Test
    void testMultipleCycles() {
        UpgradePlanDTO.CycleInfo cycle1 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("A", "B"), Arrays.asList("A → B", "B → A"));
        UpgradePlanDTO.CycleInfo cycle2 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("C", "D"), Arrays.asList("C → D", "D → C"));

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycle1, cycle2));

        assertEquals(2, dto.getCycles().size(), "Should have two cycles");
        assertEquals(4, dto.getUniqueStationsInCycles(), "Should have 4 unique stations (A, B, C, D)");
    }

    @Test
    void testMultipleCyclesWithOverlap() {
        // Cycle 1: A → B → C → A
        UpgradePlanDTO.CycleInfo cycle1 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("A → B", "B → C", "C → A"));

        // Cycle 2: B → D → E → B (B is shared with cycle1)
        UpgradePlanDTO.CycleInfo cycle2 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("B", "D", "E"),
                Arrays.asList("B → D", "D → E", "E → B"));

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycle1, cycle2));

        assertEquals(2, dto.getCycles().size(), "Should have two cycles");
        assertEquals(5, dto.getUniqueStationsInCycles(),
                "Should have 5 unique stations (A, B, C, D, E) - B counted only once");
    }

    @Test
    void testSingleStationInMultipleCycles() {
        // Cycle 1: A → B → A
        UpgradePlanDTO.CycleInfo cycle1 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("A", "B"),
                Arrays.asList("A → B", "B → A"));

        // Cycle 2: A → C → A (A appears in both)
        UpgradePlanDTO.CycleInfo cycle2 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("A", "C"),
                Arrays.asList("A → C", "C → A"));

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycle1, cycle2));

        assertEquals(2, dto.getCycles().size(), "Should have two cycles");
        assertEquals(3, dto.getUniqueStationsInCycles(),
                "Should have 3 unique stations (A, B, C) - A counted only once");
    }

    @Test
    void testLargeCycleWithManyStations() {
        List<String> vertices = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H");
        List<String> edges = Arrays.asList(
                "A → B", "B → C", "C → D", "D → E",
                "E → F", "F → G", "G → H", "H → A"
        );
        UpgradePlanDTO.CycleInfo cycleInfo = new UpgradePlanDTO.CycleInfo(vertices, edges);

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycleInfo));

        assertEquals(1, dto.getCycles().size(), "Should have one cycle");
        assertEquals(8, dto.getUniqueStationsInCycles(), "Should have 8 unique stations");
    }

    @Test
    void testComplexMultipleCyclesScenario() {
        // Cycle 1: A → B → C → A (3 stations)
        UpgradePlanDTO.CycleInfo cycle1 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("A → B", "B → C", "C → A"));

        // Cycle 2: C → D → E → C (3 stations, C overlaps)
        UpgradePlanDTO.CycleInfo cycle2 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("C", "D", "E"),
                Arrays.asList("C → D", "D → E", "E → C"));

        // Cycle 3: E → F → E (2 stations, E overlaps)
        UpgradePlanDTO.CycleInfo cycle3 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("E", "F"),
                Arrays.asList("E → F", "F → E"));

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycle1, cycle2, cycle3));

        assertEquals(3, dto.getCycles().size(), "Should have three cycles");
        assertEquals(6, dto.getUniqueStationsInCycles(),
                "Should have 6 unique stations (A, B, C, D, E, F)");
    }

    @Test
    void testImmutability() {
        List<String> order = Arrays.asList("A", "B", "C");
        UpgradePlanDTO dto = UpgradePlanDTO.withTopologicalOrder(order);

        List<String> returned = dto.getTopologicalOrder();
        returned.add("D");

        assertEquals(3, dto.getTopologicalOrder().size(), "Original should not be modified");
    }

    @Test
    void testUniqueStationsCounterWithDuplicateVertices() {
        // Test edge case: same station appears multiple times in one cycle
        // (though this shouldn't happen in a proper cycle representation)
        List<String> vertices = Arrays.asList("A", "B", "A", "C");
        List<String> edges = Arrays.asList("A → B", "B → A", "A → C", "C → A");
        UpgradePlanDTO.CycleInfo cycleInfo = new UpgradePlanDTO.CycleInfo(vertices, edges);

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycleInfo));

        // Even with duplicates in the list, HashSet should count unique stations
        assertEquals(3, dto.getUniqueStationsInCycles(),
                "Should count only unique stations (A, B, C)");
    }

    @Test
    void testGettersReturnDefensiveCopies() {
        List<String> vertices = Arrays.asList("A", "B", "C");
        List<String> edges = Arrays.asList("A → B", "B → C", "C → A");
        UpgradePlanDTO.CycleInfo cycleInfo = new UpgradePlanDTO.CycleInfo(vertices, edges);
        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycleInfo));

        // Try to modify returned lists
        List<UpgradePlanDTO.CycleInfo> returnedCycles = dto.getCycles();
        returnedCycles.clear();

        // Original should remain unchanged
        assertEquals(1, dto.getCycles().size(), "Original cycles should not be modified");
        assertEquals(3, dto.getUniqueStationsInCycles(), "Unique stations count should remain 3");
    }
}