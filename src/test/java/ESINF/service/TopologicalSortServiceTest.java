package ESINF.service;

import Graph.Graph;
import Graph.map.MapGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortServiceTest {

    private TopologicalSortService service;
    private Graph<String, Integer> graph;

    @BeforeEach
    void setUp() {
        service = new TopologicalSortService();
        graph = new MapGraph<>(true);
    }

    @Test
    void testTopologicalSortSimpleDAG() {
        // Create DAG: A -> B -> C
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);

        LinkedList<String> result = service.topologicalSort(graph);

        assertEquals(3, result.size(), "Should have 3 vertices");
        assertTrue(result.indexOf("A") < result.indexOf("B"), "A should come before B");
        assertTrue(result.indexOf("B") < result.indexOf("C"), "B should come before C");
    }

    @Test
    void testTopologicalSortComplexDAG() {
        // Create DAG with multiple dependencies
        // A -> C, B -> C, C -> D
        graph.addEdge("A", "C", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "D", 1);

        LinkedList<String> result = service.topologicalSort(graph);

        assertEquals(4, result.size(), "Should have 4 vertices");
        assertTrue(result.indexOf("A") < result.indexOf("C"), "A should come before C");
        assertTrue(result.indexOf("B") < result.indexOf("C"), "B should come before C");
        assertTrue(result.indexOf("C") < result.indexOf("D"), "C should come before D");
    }

    @Test
    void testTopologicalSortSingleVertex() {
        graph.addVertex("A");

        LinkedList<String> result = service.topologicalSort(graph);

        assertEquals(1, result.size(), "Should have 1 vertex");
        assertEquals("A", result.get(0), "Should contain vertex A");
    }

    @Test
    void testTopologicalSortDisconnectedComponents() {
        // Two separate components: A -> B and C -> D
        graph.addEdge("A", "B", 1);
        graph.addEdge("C", "D", 1);

        LinkedList<String> result = service.topologicalSort(graph);

        assertEquals(4, result.size(), "Should have 4 vertices");
        assertTrue(result.indexOf("A") < result.indexOf("B"), "A should come before B");
        assertTrue(result.indexOf("C") < result.indexOf("D"), "C should come before D");
    }

    @Test
    void testTopologicalSortEmptyGraph() {
        LinkedList<String> result = service.topologicalSort(graph);

        assertEquals(0, result.size(), "Should be empty for empty graph");
    }

    @Test
    void testTopologicalSortNoEdges() {
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");

        LinkedList<String> result = service.topologicalSort(graph);

        assertEquals(3, result.size(), "Should have all 3 vertices");
    }

    @Test
    void testTopologicalSortWithCycle() {
        // Create cycle: A -> B -> C -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 1);

        LinkedList<String> result = service.topologicalSort(graph);

        // DFS-based topological sort doesn't detect cycles, it just returns an order
        // The order won't be valid, but we can check that all vertices are present
        assertEquals(3, result.size(), "Should return all vertices even with cycle");
    }

    @Test
    void testTopologicalSortKahnSimpleDAG() {
        // Create DAG: A -> B -> C
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);

        List<String> result = service.topologicalSortKahn(graph);

        assertEquals(3, result.size(), "Should have 3 vertices");
        assertTrue(result.indexOf("A") < result.indexOf("B"), "A should come before B");
        assertTrue(result.indexOf("B") < result.indexOf("C"), "B should come before C");
    }

    @Test
    void testTopologicalSortKahnWithCycle() {
        // Create cycle: A -> B -> C -> A
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 1);
        graph.addEdge("C", "A", 1);

        List<String> result = service.topologicalSortKahn(graph);

        assertEquals(0, result.size(), "Should return empty list when cycle detected");
    }

    @Test
    void testTopologicalSortKahnComplexDAG() {
        // Create complex DAG
        graph.addEdge("A", "D", 1);
        graph.addEdge("B", "D", 1);
        graph.addEdge("C", "E", 1);
        graph.addEdge("D", "E", 1);
        graph.addEdge("E", "F", 1);

        List<String> result = service.topologicalSortKahn(graph);

        assertEquals(6, result.size(), "Should have 6 vertices");
        assertTrue(result.indexOf("A") < result.indexOf("D"), "A should come before D");
        assertTrue(result.indexOf("B") < result.indexOf("D"), "B should come before D");
        assertTrue(result.indexOf("D") < result.indexOf("E"), "D should come before E");
        assertTrue(result.indexOf("E") < result.indexOf("F"), "E should come before F");
    }

    @Test
    void testTopologicalSortKahnSingleVertex() {
        graph.addVertex("A");

        List<String> result = service.topologicalSortKahn(graph);

        assertEquals(1, result.size(), "Should have 1 vertex");
        assertEquals("A", result.get(0), "Should contain vertex A");
    }

    @Test
    void testTopologicalSortKahnEmptyGraph() {
        List<String> result = service.topologicalSortKahn(graph);

        assertEquals(0, result.size(), "Should be empty for empty graph");
    }

    @Test
    void testTopologicalSortDiamond() {
        // Diamond structure: A -> B, A -> C, B -> D, C -> D
        graph.addEdge("A", "B", 1);
        graph.addEdge("A", "C", 1);
        graph.addEdge("B", "D", 1);
        graph.addEdge("C", "D", 1);

        LinkedList<String> result = service.topologicalSort(graph);

        assertEquals(4, result.size(), "Should have 4 vertices");
        assertEquals("A", result.get(0), "A should be first");
        assertEquals("D", result.get(3), "D should be last");
    }
}