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
    }

    @Test
    void testMultipleCycles() {
        UpgradePlanDTO.CycleInfo cycle1 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("A", "B"), Arrays.asList("A → B", "B → A"));
        UpgradePlanDTO.CycleInfo cycle2 = new UpgradePlanDTO.CycleInfo(
                Arrays.asList("C", "D"), Arrays.asList("C → D", "D → C"));

        UpgradePlanDTO dto = UpgradePlanDTO.withCycles(Arrays.asList(cycle1, cycle2));

        assertEquals(2, dto.getCycles().size(), "Should have two cycles");
    }

    @Test
    void testImmutability() {
        List<String> order = Arrays.asList("A", "B", "C");
        UpgradePlanDTO dto = UpgradePlanDTO.withTopologicalOrder(order);

        List<String> returned = dto.getTopologicalOrder();
        returned.add("D");

        assertEquals(3, dto.getTopologicalOrder().size(), "Original should not be modified");
    }
}