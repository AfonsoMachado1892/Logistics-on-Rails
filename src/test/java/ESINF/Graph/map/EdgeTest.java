package ESINF.Graph.map;

import Graph.Edge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeTest {

    @Test
    void testConstructorValid() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);

        assertEquals("A", edge.getVOrig(), "Origin should be A");
        assertEquals("B", edge.getVDest(), "Destination should be B");
        assertEquals(10, edge.getWeight(), "Weight should be 10");
    }

    @Test
    void testConstructorNullOrigin() {
        assertThrows(IllegalArgumentException.class,
                () -> new Edge<String, Integer>(null, "B", 10),
                "Should throw exception for null origin");
    }

    @Test
    void testConstructorNullDestination() {
        assertThrows(IllegalArgumentException.class,
                () -> new Edge<String, Integer>("A", null, 10),
                "Should throw exception for null destination");
    }

    @Test
    void testSetWeight() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);
        edge.setWeight(20);

        assertEquals(20, edge.getWeight(), "Weight should be updated to 20");
    }

    @Test
    void testEndpoints() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);

        Object[] endpoints = edge.endpoints();

        assertEquals(2, endpoints.length, "Should have 2 endpoints");
        assertEquals("A", endpoints[0], "First endpoint should be A");
        assertEquals("B", endpoints[1], "Second endpoint should be B");
    }

    @Test
    void testEqualsSameEdge() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);

        assertEquals(edge, edge, "Edge should equal itself");
    }

    @Test
    void testEqualsIdenticalEdges() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("A", "B", 10);

        assertEquals(edge1, edge2, "Edges with same vertices should be equal");
    }

    @Test
    void testEqualsDifferentWeight() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("A", "B", 20);

        assertEquals(edge1, edge2, "Edges with same vertices but different weight should be equal");
    }

    @Test
    void testEqualsDifferentOrigin() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("C", "B", 10);

        assertNotEquals(edge1, edge2, "Edges with different origin should not be equal");
    }

    @Test
    void testEqualsDifferentDestination() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("A", "C", 10);

        assertNotEquals(edge1, edge2, "Edges with different destination should not be equal");
    }

    @Test
    void testEqualsNull() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);

        assertNotEquals(null, edge, "Edge should not equal null");
    }

    @Test
    void testEqualsDifferentClass() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);

        assertNotEquals("A -> B", edge, "Edge should not equal a String");
    }

    @Test
    void testHashCodeSameEdges() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("A", "B", 20);

        assertEquals(edge1.hashCode(), edge2.hashCode(),
                "Edges with same vertices should have same hash code");
    }

    @Test
    void testHashCodeDifferentEdges() {
        Edge<String, Integer> edge1 = new Edge<>("A", "B", 10);
        Edge<String, Integer> edge2 = new Edge<>("A", "C", 10);

        assertNotEquals(edge1.hashCode(), edge2.hashCode(),
                "Edges with different vertices should have different hash codes");
    }

    @Test
    void testToString() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 10);
        String str = edge.toString();

        assertTrue(str.contains("A"), "toString should contain origin");
        assertTrue(str.contains("B"), "toString should contain destination");
        assertTrue(str.contains("10"), "toString should contain weight");
    }

    @Test
    void testEdgeWithDifferentTypes() {
        Edge<Integer, Double> edge = new Edge<>(1, 2, 10.5);

        assertEquals(1, edge.getVOrig(), "Origin should be 1");
        assertEquals(2, edge.getVDest(), "Destination should be 2");
        assertEquals(10.5, edge.getWeight(), 0.0001, "Weight should be 10.5");
    }

    @Test
    void testSelfLoop() {
        Edge<String, Integer> edge = new Edge<>("A", "A", 5);

        assertEquals("A", edge.getVOrig(), "Origin should be A");
        assertEquals("A", edge.getVDest(), "Destination should be A");
        assertEquals(edge, edge, "Self-loop should equal itself");
    }

    @Test
    void testNegativeWeight() {
        Edge<String, Integer> edge = new Edge<>("A", "B", -10);

        assertEquals(-10, edge.getWeight(), "Weight can be negative");
    }

    @Test
    void testZeroWeight() {
        Edge<String, Integer> edge = new Edge<>("A", "B", 0);

        assertEquals(0, edge.getWeight(), "Weight can be zero");
    }
}