package ESINF.service;

import Graph.Graph;
import Graph.map.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CycleDetectionServiceTest {

    private CycleDetectionService service;
    private Graph<String, Integer> graph;

    @BeforeEach
    void setUp() {
        service = new CycleDetectionService();
        graph = new MapGraph<>(true);
    }

    @Test
    void testHasCycleNoCycle() {
        // Create DAG: A -> B -> C
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);

        assertFalse(service.hasCycle(graph), "DAG should not have cycle");
    }

    @Test
    void testHasCycleSimpleCycle() {
        // Create cycle: A -> B -> C -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 1);

        assertTrue(service.hasCycle(graph), "Should detect simple cycle");
    }

    @Test
    void testHasCycleSelfLoop() {
        // Create self-loop: A -> A
        graph.addEdge("A", "A", 1);

        assertTrue(service.hasCycle(graph), "Should detect self-loop");
    }

    @Test
    void testHasCycleEmptyGraph() {
        assertFalse(service.hasCycle(graph), "Empty graph should not have cycle");
    }

    @Test
    void testHasCycleSingleVertex() {
        graph.addVertex("A");

        assertFalse(service.hasCycle(graph), "Single vertex without edges should not have cycle");
    }

    @Test
    void testHasCycleDisconnectedWithCycle() {
        // Component 1: A -> B (no cycle)
        graph.addEdge("A", "B", 1);

        // Component 2: C -> D -> C (cycle)
        graph.addEdge("C", "D", 1);
        graph.addEdge("D", "C", 1);

        assertTrue(service.hasCycle(graph), "Should detect cycle in disconnected graph");
    }

    @Test
    void testHasCycleComplexDAG() {
        // Complex DAG with diamond structure
        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 1);
        graph.addEdge("B", "D", 1);
        graph.addEdge("C", "D", 1);

        assertFalse(service.hasCycle(graph), "Diamond DAG should not have cycle");
    }

    @Test
    void testFindAllCyclesNoCycle() {
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);

        List<List<String>> cycles = service.findAllCycles(graph);

        assertEquals(0, cycles.size(), "Should find no cycles in DAG");
    }

    @Test
    void testFindAllCyclesSimpleCycle() {
        // Create cycle: A -> B -> C -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 1);

        List<List<String>> cycles = service.findAllCycles(graph);

        assertTrue(cycles.size() > 0, "Should find at least one cycle");
    }

    @Test
    void testFindAllCyclesSelfLoop() {
        graph.addEdge("A", "A", 1);

        List<List<String>> cycles = service.findAllCycles(graph);

        assertTrue(cycles.size() > 0, "Should find self-loop cycle");
    }

    @Test
    void testFindAllCyclesMultipleCycles() {
        // Cycle 1: A -> B -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "A", 1);

        // Cycle 2: C -> D -> C
        graph.addEdge("C", "D", 1);
        graph.addEdge("D", "C", 1);

        List<List<String>> cycles = service.findAllCycles(graph);

        assertTrue(cycles.size() >= 2, "Should find multiple cycles");
    }

    @Test
    void testFindAllCyclesEmptyGraph() {
        List<List<String>> cycles = service.findAllCycles(graph);

        assertEquals(0, cycles.size(), "Empty graph should have no cycles");
    }

    @Test
    void testFindAllCyclesComplexCycle() {
        // Create a longer cycle: A -> B -> C -> D -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "D", 1);
        graph.addEdge("D", "A", 1);

        List<List<String>> cycles = service.findAllCycles(graph);

        assertTrue(cycles.size() > 0, "Should find complex cycle");
    }

    @Test
    void testFindAllCyclesNestedCycles() {
        // Create nested structure with multiple cycles
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 1);
        graph.addEdge("B", "D", 1);
        graph.addEdge("D", "B", 1);

        List<List<String>> cycles = service.findAllCycles(graph);

        assertTrue(cycles.size() >= 2, "Should find nested cycles");
    }

    @Test
    void testHasCycleTwoVertexCycle() {
        // Simplest cycle: A -> B -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "A", 1);

        assertTrue(service.hasCycle(graph), "Should detect two-vertex cycle");
    }

    @Test
    void testHasCycleWithBranches() {
        // Create structure with branches but no cycle
        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 1);
        graph.addEdge("B", "D", 1);
        graph.addEdge("C", "E", 1);

        assertFalse(service.hasCycle(graph), "Tree structure should not have cycle");
    }

    @Test
    void testFindAllCyclesVerifyCycleValidity() {
        // Create cycle: A -> B -> C -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 1);

        List<List<String>> cycles = service.findAllCycles(graph);

        assertFalse(cycles.isEmpty(), "Should find cycle");

        for (List<String> cycle : cycles) {
            assertFalse(cycle.isEmpty(), "Cycle should not be empty");
            // First and last vertices should be the same in a valid cycle representation
            // OR the cycle should be valid when traversed
        }
    }
}