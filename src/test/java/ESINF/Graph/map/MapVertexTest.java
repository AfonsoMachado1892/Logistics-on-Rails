package ESINF.Graph.map;

import Graph.Edge;
import Graph.map.MapVertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapVertexTest {

    private MapVertex<String, Integer> vertex;

    @BeforeEach
    void setUp() {
        vertex = new MapVertex<>("A");
    }

    @Test
    void testConstructorValid() {
        MapVertex<String, Integer> v = new MapVertex<>("TestVertex");

        assertEquals("TestVertex", v.getElement(), "Element should be TestVertex");
        assertEquals(0, v.numAdjVerts(), "Should have no adjacent vertices initially");
    }

    @Test
    void testConstructorNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new MapVertex<String, Integer>(null),
                "Should throw exception for null element");
    }

    @Test
    void testGetElement() {
        assertEquals("A", vertex.getElement(), "Element should be A");
    }

    @Test
    void testGetOutgoingInitiallyEmpty() {
        Map<String, Edge<String, Integer>> outgoing = vertex.getOutgoing();

        assertNotNull(outgoing, "Outgoing map should not be null");
        assertEquals(0, outgoing.size(), "Should have no outgoing edges initially");
    }

    @Test
    void testAddEdge() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);
        Edge<String, Integer> result = vertex.addEdge("B", edge);

        assertNull(result, "Should return null when adding new edge");
        assertEquals(1, vertex.numAdjVerts(), "Should have 1 adjacent vertex");
        assertEquals(edge, vertex.getEdge("B"), "Should retrieve the added edge");
    }

    @Test
    void testAddEdgeReplaceExisting() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("A", "B", 20);

        vertex.addEdge("B", edge1);
        Edge<String, Integer> result = vertex.addEdge("B", edge2);

        assertEquals(edge1, result, "Should return previous edge");
        assertEquals(edge2, vertex.getEdge("B"), "Should have new edge");
        assertEquals(1, vertex.numAdjVerts(), "Should still have 1 adjacent vertex");
    }

    @Test
    void testAddMultipleEdges() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("A", "C", 20);
        Edge<String, Integer> edge3 = new Edge<>("A", "D", 30);

        vertex.addEdge("B", edge1);
        vertex.addEdge("C", edge2);
        vertex.addEdge("D", edge3);

        assertEquals(3, vertex.numAdjVerts(), "Should have 3 adjacent vertices");
    }

    @Test
    void testRemoveEdge() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);
        vertex.addEdge("B", edge);

        Edge<String, Integer> result = vertex.removeEdge("B");

        assertEquals(edge, result, "Should return removed edge");
        assertEquals(0, vertex.numAdjVerts(), "Should have no adjacent vertices");
        assertNull(vertex.getEdge("B"), "Edge should be removed");
    }

    @Test
    void testRemoveNonExistentEdge() {
        Edge<String, Integer> result = vertex.removeEdge("NonExistent");

        assertNull(result, "Should return null for non-existent edge");
        assertEquals(0, vertex.numAdjVerts(), "Should still have 0 adjacent vertices");
    }

    @Test
    void testGetEdge() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);
        vertex.addEdge("B", edge);

        Edge<String, Integer> result = vertex.getEdge("B");

        assertEquals(edge, result, "Should retrieve the edge");
    }

    @Test
    void testGetNonExistentEdge() {
        Edge<String, Integer> result = vertex.getEdge("NonExistent");

        assertNull(result, "Should return null for non-existent edge");
    }

    @Test
    void testNumAdjVerts() {
        assertEquals(0, vertex.numAdjVerts(), "Should be 0 initially");

        vertex.addEdge("B", new Edge<>("A", "B", 10));
        assertEquals(1, vertex.numAdjVerts(), "Should be 1 after adding edge");

        vertex.addEdge("C", new Edge<>("A", "C", 20));
        assertEquals(2, vertex.numAdjVerts(), "Should be 2 after adding second edge");

        vertex.removeEdge("B");
        assertEquals(1, vertex.numAdjVerts(), "Should be 1 after removing edge");
    }

    @Test
    void testEqualsSameVertex() {
        assertEquals(vertex, vertex, "Vertex should equal itself");
    }

    @Test
    void testEqualsIdenticalVertices() {
        MapVertex<String, Integer> v1 = new MapVertex<>("A");
        MapVertex<String, Integer> v2 = new MapVertex<>("A");

        assertEquals(v1, v2, "Vertices with same element should be equal");
    }

    @Test
    void testEqualsDifferentElement() {
        MapVertex<String, Integer> v1 = new MapVertex<>("A");
        MapVertex<String, Integer> v2 = new MapVertex<>("B");

        assertNotEquals(v1, v2, "Vertices with different elements should not be equal");
    }

    @Test
    void testEqualsWithDifferentEdges() {
        MapVertex<String, Integer> v1 = new MapVertex<>("A");
        MapVertex<String, Integer> v2 = new MapVertex<>("A");

        v1.addEdge("B", new Edge<>("A", "B", 10));

        assertEquals(v1, v2, "Vertices with same element should be equal regardless of edges");
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(null, vertex, "Vertex should not equal null");
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals("A", vertex, "Vertex should not equal a String");
    }

    @Test
    void testHashCodeSameElement() {
        MapVertex<String, Integer> v1 = new MapVertex<>("A");
        MapVertex<String, Integer> v2 = new MapVertex<>("A");

        assertEquals(v1.hashCode(), v2.hashCode(),
                "Vertices with same element should have same hash code");
    }

    @Test
    void testHashCodeDifferentElement() {
        MapVertex<String, Integer> v1 = new MapVertex<>("A");
        MapVertex<String, Integer> v2 = new MapVertex<>("B");

        assertNotEquals(v1.hashCode(), v2.hashCode(),
                "Vertices with different elements should have different hash codes");
    }

    @Test
    void testToString() {
        String str = vertex.toString();

        assertEquals("A", str, "toString should return element");
    }

    @Test
    void testGetOutgoingMapIsModifiable() {
        Map<String, Edge<String, Integer>> outgoing = vertex.getOutgoing();
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);

        outgoing.put("B", edge);

        assertEquals(1, vertex.numAdjVerts(), "Modifying returned map should affect vertex");
    }

    @Test
    void testAddSelfLoop() {
        Edge<String, Integer> edge = new Edge<>("A", "A", 5);
        vertex.addEdge("A", edge);

        assertEquals(1, vertex.numAdjVerts(), "Should allow self-loop");
        assertEquals(edge, vertex.getEdge("A"), "Should retrieve self-loop edge");
    }
}