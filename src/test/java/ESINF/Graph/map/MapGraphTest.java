package Graph.map;

import Graph.Edge;
import Graph.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class MapGraphTest {

    private MapGraph<String, Integer> directedGraph;
    private MapGraph<String, Integer> undirectedGraph;

    @BeforeEach
    void setUp() {
        directedGraph = new MapGraph<>(true);
        undirectedGraph = new MapGraph<>(false);
    }

    @Test
    void testVertexByPredicate() {
        directedGraph.addVertex("Alpha");
        directedGraph.addVertex("Beta");
        directedGraph.addVertex("Gamma");

        String result = directedGraph.vertex(v -> v.startsWith("B"));

        assertEquals("Beta", result, "Should find vertex starting with B");
    }

    @Test
    void testVertexByPredicateNotFound() {
        directedGraph.addVertex("Alpha");
        directedGraph.addVertex("Beta");

        String result = directedGraph.vertex(v -> v.startsWith("Z"));

        assertNull(result, "Should return null when no vertex matches");
    }

    @Test
    void testVertexByInvalidKey() {
        directedGraph.addVertex("A");

        assertNull(directedGraph.vertex(10), "Should return null for invalid key");
        assertNull(directedGraph.vertex(-1), "Should return null for negative key");
    }

    @Test
    void testAdjVerticesEmpty() {
        directedGraph.addVertex("A");

        Collection<String> adj = directedGraph.adjVertices("A");

        assertNotNull(adj, "Should return empty collection, not null");
        assertEquals(0, adj.size(), "Should have no adjacent vertices");
    }

    @Test
    void testAdjVerticesInvalidVertex() {
        Collection<String> adj = directedGraph.adjVertices("NonExistent");

        assertNull(adj, "Should return null for invalid vertex");
    }

    @Test
    void testAddVertexNull() {
        assertThrows(IllegalArgumentException.class,
                () -> directedGraph.addVertex(null),
                "Should throw exception for null vertex");
    }

    @Test
    void testAddEdgeNullOrigin() {
        assertThrows(IllegalArgumentException.class,
                () -> directedGraph.addEdge(null, "B", 10),
                "Should throw exception for null origin");
    }

    @Test
    void testAddEdgeNullDestination() {
        assertThrows(IllegalArgumentException.class,
                () -> directedGraph.addEdge("A", null, 10),
                "Should throw exception for null destination");
    }

    @Test
    void testAddEdgeCreatesVertices() {
        boolean result = directedGraph.addEdge("A", "B", 10);

        assertTrue(result, "Should add edge");
        assertEquals(2, directedGraph.numVertices(), "Should create both vertices");
        assertTrue(directedGraph.validVertex("A"), "Should have vertex A");
        assertTrue(directedGraph.validVertex("B"), "Should have vertex B");
    }

    @Test
    void testAddDuplicateEdge() {
        directedGraph.addEdge("A", "B", 10);
        boolean result = directedGraph.addEdge("A", "B", 20);

        assertFalse(result, "Should not add duplicate edge");
        assertEquals(10, directedGraph.edge("A", "B").getWeight(),
                "Original edge weight should remain");
    }

    @Test
    void testRemoveVertexInvalidVertex() {
        boolean result = directedGraph.removeVertex("NonExistent");

        assertFalse(result, "Should return false for non-existent vertex");
    }

    @Test
    void testRemoveVertexWithIncomingEdges() {
        directedGraph.addEdge("A", "B", 10);
        directedGraph.addEdge("C", "B", 20);

        directedGraph.removeVertex("B");

        assertEquals(2, directedGraph.numVertices(), "Should have 2 vertices left");
        assertEquals(0, directedGraph.numEdges(), "Should have no edges left");
    }

    @Test
    void testRemoveEdgeInvalidVertices() {
        boolean result = directedGraph.removeEdge("A", "B");

        assertFalse(result, "Should return false for invalid vertices");
    }

    @Test
    void testRemoveNonExistentEdge() {
        directedGraph.addVertex("A");
        directedGraph.addVertex("B");

        boolean result = directedGraph.removeEdge("A", "B");

        assertFalse(result, "Should return false when edge doesn't exist");
    }

    @Test
    void testUndirectedGraphSymmetry() {
        undirectedGraph.addEdge("A", "B", 10);

        assertNotNull(undirectedGraph.edge("A", "B"), "Should have edge A -> B");
        assertNotNull(undirectedGraph.edge("B", "A"), "Should have edge B -> A");
        assertEquals(1, undirectedGraph.numEdges(), "Should count as single edge");
    }

    @Test
    void testUndirectedGraphRemoveEdge() {
        undirectedGraph.addEdge("A", "B", 10);
        undirectedGraph.removeEdge("A", "B");

        assertNull(undirectedGraph.edge("A", "B"), "Edge A -> B should be removed");
        assertNull(undirectedGraph.edge("B", "A"), "Edge B -> A should be removed");
        assertEquals(0, undirectedGraph.numEdges(), "Should have no edges");
    }

    @Test
    void testCloneDirectedGraph() {
        directedGraph.addEdge("A", "B", 10);
        directedGraph.addEdge("B", "C", 20);

        Graph<String, Integer> clone = directedGraph.clone();

        assertEquals(directedGraph.numVertices(), clone.numVertices(),
                "Clone should have same number of vertices");
        assertEquals(directedGraph.numEdges(), clone.numEdges(),
                "Clone should have same number of edges");
        assertTrue(clone.isDirected(), "Clone should be directed");
    }

    @Test
    void testCloneUndirectedGraph() {
        undirectedGraph.addEdge("A", "B", 10);
        undirectedGraph.addEdge("B", "C", 20);

        Graph<String, Integer> clone = undirectedGraph.clone();

        assertEquals(undirectedGraph.numVertices(), clone.numVertices(),
                "Clone should have same number of vertices");
        assertEquals(undirectedGraph.numEdges(), clone.numEdges(),
                "Clone should have same number of edges");
        assertFalse(clone.isDirected(), "Clone should be undirected");
    }

    @Test
    void testCloneIndependence() {
        directedGraph.addEdge("A", "B", 10);
        Graph<String, Integer> clone = directedGraph.clone();

        clone.removeVertex("A");

        assertEquals(2, directedGraph.numVertices(),
                "Original should not be affected by clone modification");
        assertEquals(1, clone.numVertices(),
                "Clone should be modified");
    }

    @Test
    void testEdgeByKey() {
        directedGraph.addEdge("A", "B", 10);
        directedGraph.addEdge("B", "C", 20);

        Edge<String, Integer> edge = directedGraph.edge(0, 1);

        assertNotNull(edge, "Should find edge by key");
        assertEquals(10, edge.getWeight(), "Edge weight should match");
    }

    @Test
    void testEdgeByInvalidKey() {
        directedGraph.addEdge("A", "B", 10);

        assertNull(directedGraph.edge(0, 10), "Should return null for invalid key");
        assertNull(directedGraph.edge(10, 0), "Should return null for invalid key");
    }

    @Test
    void testOutDegreeMultipleEdges() {
        directedGraph.addEdge("A", "B", 10);
        directedGraph.addEdge("A", "C", 20);
        directedGraph.addEdge("A", "D", 30);

        assertEquals(3, directedGraph.outDegree("A"), "A should have out-degree 3");
    }

    @Test
    void testInDegreeMultipleEdges() {
        directedGraph.addEdge("A", "D", 10);
        directedGraph.addEdge("B", "D", 20);
        directedGraph.addEdge("C", "D", 30);

        assertEquals(3, directedGraph.inDegree("D"), "D should have in-degree 3");
    }

    @Test
    void testIncomingEdgesNone() {
        directedGraph.addVertex("A");

        Collection<Edge<String, Integer>> incoming = directedGraph.incomingEdges("A");

        assertNotNull(incoming, "Should return empty collection");
        assertEquals(0, incoming.size(), "Should have no incoming edges");
    }

    @Test
    void testOutgoingEdgesNone() {
        directedGraph.addVertex("A");

        Collection<Edge<String, Integer>> outgoing = directedGraph.outgoingEdges("A");

        assertNotNull(outgoing, "Should return empty collection");
        assertEquals(0, outgoing.size(), "Should have no outgoing edges");
    }

    @Test
    void testSelfLoopDirected() {
        directedGraph.addEdge("A", "A", 5);

        assertEquals(1, directedGraph.outDegree("A"), "Should have out-degree 1");
        assertEquals(1, directedGraph.inDegree("A"), "Should have in-degree 1");
        assertEquals(1, directedGraph.numEdges(), "Should have 1 edge");
    }


    @Test
    void testToString() {
        directedGraph.addEdge("A", "B", 10);

        String str = directedGraph.toString();

        assertTrue(str.contains("directed"), "Should mention directed");
        assertTrue(str.contains("A"), "Should contain vertex A");
        assertTrue(str.contains("B"), "Should contain vertex B");
    }

    @Test
    void testVerticesOrderPreserved() {
        directedGraph.addVertex("C");
        directedGraph.addVertex("A");
        directedGraph.addVertex("B");

        ArrayList<String> vertices = directedGraph.vertices();

        assertEquals("C", vertices.get(0), "Should preserve insertion order");
        assertEquals("A", vertices.get(1), "Should preserve insertion order");
        assertEquals("B", vertices.get(2), "Should preserve insertion order");
    }
}