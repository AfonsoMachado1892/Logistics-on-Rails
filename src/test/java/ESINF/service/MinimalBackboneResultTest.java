package ESINF.service;

import Graph.Edge;
import Graph.Graph;
import Graph.map.MapGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit4 test suite for MinimalBackboneResult
 *
 * Tests cover:
 * - Result object creation with valid MST graphs
 * - Statistics computation (weight, edges, vertices)
 * - Component detection (connected/disconnected graphs)
 * - Validation logic for MST/MSF structures
 * - String representations
 *
 * Note: MinimalBackboneResult expects valid graph structures from MST computation.
 * Tests focus on realistic MST scenarios rather than edge cases that would never
 * occur in actual MST algorithm output.
 *
 * @author ESINF Test Team
 * @version 1.0
 */
public class MinimalBackboneResultTest {

    private MinimalBackboneService service;

    @Before
    public void setUp() {
        service = new MinimalBackboneService();
    }

    /**
     * Helper method to create a valid MST from a connected graph
     */
    private MinimalBackboneResult<String, Double> createConnectedMST() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        return service.computeMST(graph);
    }

    /**
     * Helper method to create a valid MSF (disconnected components)
     */
    private MinimalBackboneResult<String, Double> createDisconnectedMSF() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 2.0);
        return service.computeMST(graph);
    }

    /**
     * Helper method to create a single vertex MST
     */
    private MinimalBackboneResult<String, Double> createSingleVertexMST() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addVertex("A");
        return service.computeMST(graph);
    }

    // ========== Constructor and Basic Tests ==========

    /**
     * Test that MinimalBackboneResult can be constructed from MST
     */
    @Test
    public void testConstructor_ValidMST_NotNull() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertNotNull(result);
    }

    /**
     * Test that result from single vertex has correct vertex count
     */
    @Test
    public void testConstructor_SingleVertex_OneVertex() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertEquals(1, result.getVertexCount());
    }

    /**
     * Test that result from single vertex has zero edges
     */
    @Test
    public void testConstructor_SingleVertex_ZeroEdges() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertEquals(0, result.getEdgeCount());
    }

    /**
     * Test that result from single vertex has zero weight
     */
    @Test
    public void testConstructor_SingleVertex_ZeroWeight() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertEquals(0.0, result.getTotalWeight(), 0.001);
    }

    /**
     * Test that result from single vertex has one component
     */
    @Test
    public void testConstructor_SingleVertex_OneComponent() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertEquals(1, result.getNumComponents());
    }

    /**
     * Test that single vertex MST is considered fully connected
     */
    @Test
    public void testConstructor_SingleVertex_IsFullyConnected() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertTrue(result.isFullyConnected());
    }

    // ========== getMstGraph Tests ==========

    /**
     * Test that getMstGraph returns non-null graph
     */
    @Test
    public void testGetMstGraph_ReturnsNonNull() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertNotNull(result.getMstGraph());
    }

    /**
     * Test that getMstGraph returns the MST graph
     */
    @Test
    public void testGetMstGraph_ReturnsMSTGraph() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        Graph<String, Double> mst = result.getMstGraph();
        assertTrue(mst.numVertices() > 0);
    }


    /**
     * Test total weight with zero-weight edges
     */
    @Test
    public void testGetTotalWeight_ZeroWeightEdges_ReturnsZero() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 0.0);
        graph.addEdge("B", "C", 0.0);
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(0.0, result.getTotalWeight(), 0.001);
    }
    

    // ========== getEdgeCount Tests ==========

    /**
     * Test edge count for connected MST (V-1 edges)
     */
    @Test
    public void testGetEdgeCount_ConnectedMST_VerticesMinusOne() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertEquals(2, result.getEdgeCount());
    }

    /**
     * Test edge count for disconnected MSF
     */
    @Test
    public void testGetEdgeCount_DisconnectedMSF_CorrectCount() {
        MinimalBackboneResult<String, Double> result = createDisconnectedMSF();
        assertEquals(2, result.getEdgeCount());
    }

    /**
     * Test edge count for triangle MST
     */
    @Test
    public void testGetEdgeCount_TriangleMST_TwoEdges() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "A", 3.0);
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(2, result.getEdgeCount());
    }

    /**
     * Test edge count for larger MST
     */
    @Test
    public void testGetEdgeCount_FiveVertices_FourEdges() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "D", 3.0);
        graph.addEdge("D", "E", 4.0);
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(4, result.getEdgeCount());
    }

    // ========== getVertexCount Tests ==========

    /**
     * Test vertex count for three-vertex MST
     */
    @Test
    public void testGetVertexCount_ThreeVertices_ReturnsThree() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertEquals(3, result.getVertexCount());
    }

    /**
     * Test vertex count for disconnected MSF
     */
    @Test
    public void testGetVertexCount_DisconnectedMSF_ReturnsFour() {
        MinimalBackboneResult<String, Double> result = createDisconnectedMSF();
        assertEquals(4, result.getVertexCount());
    }

    /**
     * Test vertex count matches MST graph vertex count
     */
    @Test
    public void testGetVertexCount_MatchesGraphVertexCount() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertEquals(result.getMstGraph().numVertices(), result.getVertexCount());
    }

    // ========== getMstEdges Tests ==========

    /**
     * Test that getMstEdges returns non-null list
     */
    @Test
    public void testGetMstEdges_ReturnsNonNull() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertNotNull(result.getMstEdges());
    }



    /**
     * Test that getMstEdges returns new list (defensive copy)
     */
    @Test
    public void testGetMstEdges_ReturnsNewList() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        List<Edge<String, Double>> edges1 = result.getMstEdges();
        List<Edge<String, Double>> edges2 = result.getMstEdges();
        assertNotSame(edges1, edges2);
    }

    /**
     * Test that getMstEdges contains Edge objects
     */
    @Test
    public void testGetMstEdges_ContainsEdgeObjects() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        List<Edge<String, Double>> edges = result.getMstEdges();
        assertTrue(edges.get(0) instanceof Edge);
    }

    /**
     * Test that getMstEdges for single vertex returns empty list
     */
    @Test
    public void testGetMstEdges_SingleVertex_EmptyList() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertTrue(result.getMstEdges().isEmpty());
    }

    // ========== getNumComponents Tests ==========

    /**
     * Test component count for fully connected MST
     */
    @Test
    public void testGetNumComponents_ConnectedMST_OneComponent() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertEquals(1, result.getNumComponents());
    }

    /**
     * Test component count for two-component MSF
     */
    @Test
    public void testGetNumComponents_TwoComponents_ReturnsTwo() {
        MinimalBackboneResult<String, Double> result = createDisconnectedMSF();
        assertEquals(2, result.getNumComponents());
    }

    /**
     * Test component count for three-component MSF
     */
    @Test
    public void testGetNumComponents_ThreeComponents_ReturnsThree() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 2.0);
        graph.addEdge("E", "F", 3.0);
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(3, result.getNumComponents());
    }

    /**
     * Test component count for larger connected MST
     */
    @Test
    public void testGetNumComponents_LargerConnectedMST_OneComponent() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "D", 3.0);
        graph.addEdge("D", "E", 4.0);
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(1, result.getNumComponents());
    }

    /**
     * Test component count with isolated vertex
     */
    @Test
    public void testGetNumComponents_WithIsolatedVertex_CorrectCount() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addVertex("C");
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(2, result.getNumComponents());
    }

    // ========== isFullyConnected Tests ==========

    /**
     * Test that connected MST is identified as fully connected
     */
    @Test
    public void testIsFullyConnected_ConnectedMST_ReturnsTrue() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.isFullyConnected());
    }

    /**
     * Test that disconnected MSF is not fully connected
     */
    @Test
    public void testIsFullyConnected_DisconnectedMSF_ReturnsFalse() {
        MinimalBackboneResult<String, Double> result = createDisconnectedMSF();
        assertFalse(result.isFullyConnected());
    }

    /**
     * Test that single vertex is considered fully connected
     */
    @Test
    public void testIsFullyConnected_SingleVertex_ReturnsTrue() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertTrue(result.isFullyConnected());
    }

    /**
     * Test fully connected with larger graph
     */
    @Test
    public void testIsFullyConnected_LargerConnectedGraph_ReturnsTrue() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "D", 3.0);
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertTrue(result.isFullyConnected());
    }

    // ========== isValid Tests ==========

    /**
     * Test validation for connected MST (E = V - 1)
     */
    @Test
    public void testIsValid_ConnectedMST_ReturnsTrue() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.isValid());
    }

    /**
     * Test validation for disconnected MSF (E = V - k)
     */
    @Test
    public void testIsValid_DisconnectedMSF_ReturnsTrue() {
        MinimalBackboneResult<String, Double> result = createDisconnectedMSF();
        assertTrue(result.isValid());
    }

    /**
     * Test validation for single vertex
     */
    @Test
    public void testIsValid_SingleVertex_ReturnsTrue() {
        MinimalBackboneResult<String, Double> result = createSingleVertexMST();
        assertTrue(result.isValid());
    }

    /**
     * Test validation for three-component MSF
     */
    @Test
    public void testIsValid_ThreeComponents_ReturnsTrue() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 2.0);
        graph.addVertex("E");
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertTrue(result.isValid());
    }

    /**
     * Test validation for larger MST
     */
    @Test
    public void testIsValid_LargerMST_ReturnsTrue() {
        Graph<String, Double> graph = new MapGraph<>(false);
        for (int i = 0; i < 9; i++) {
            graph.addEdge("V" + i, "V" + (i + 1), (double) i);
        }
        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertTrue(result.isValid());
    }

    // ========== toString Tests ==========

    /**
     * Test that toString returns non-null string
     */
    @Test
    public void testToString_ReturnsNonNull() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertNotNull(result.toString());
    }

    /**
     * Test that toString returns non-empty string
     */
    @Test
    public void testToString_ReturnsNonEmpty() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().length() > 0);
    }

    /**
     * Test that toString contains header
     */
    @Test
    public void testToString_ContainsHeader() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("Minimum Spanning Tree"));
    }

    /**
     * Test that toString contains vertex count
     */
    @Test
    public void testToString_ContainsVertexCount() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("Vertices"));
    }

    /**
     * Test that toString contains edge count
     */
    @Test
    public void testToString_ContainsEdgeCount() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("Edges"));
    }

    /**
     * Test that toString contains total weight
     */
    @Test
    public void testToString_ContainsTotalWeight() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("Total Weight"));
    }

    /**
     * Test that toString contains component count
     */
    @Test
    public void testToString_ContainsComponentCount() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("Connected Components"));
    }

    /**
     * Test that toString indicates fully connected status
     */
    @Test
    public void testToString_ConnectedGraph_ShowsFullyConnected() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("Fully Connected"));
    }

    /**
     * Test that toString for disconnected MSF shows correct status
     */
    @Test
    public void testToString_DisconnectedMSF_ShowsMSFStatus() {
        MinimalBackboneResult<String, Double> result = createDisconnectedMSF();
        assertTrue(result.toString().contains("Disconnected"));
    }

    /**
     * Test that toString shows validation status
     */
    @Test
    public void testToString_ShowsValidationStatus() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("Valid Structure"));
    }

    /**
     * Test that toString lists MST edges
     */
    @Test
    public void testToString_ListsMSTEdges() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.toString().contains("MST Edges"));
    }

    // ========== getSummary Tests ==========

    /**
     * Test that getSummary returns non-null string
     */
    @Test
    public void testGetSummary_ReturnsNonNull() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertNotNull(result.getSummary());
    }

    /**
     * Test that getSummary returns non-empty string
     */
    @Test
    public void testGetSummary_ReturnsNonEmpty() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.getSummary().length() > 0);
    }

    /**
     * Test that getSummary is shorter than toString
     */
    @Test
    public void testGetSummary_ShorterThanToString() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.getSummary().length() < result.toString().length());
    }

    /**
     * Test that getSummary contains MST prefix
     */
    @Test
    public void testGetSummary_ContainsMSTPrefix() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.getSummary().startsWith("MST"));
    }

    /**
     * Test that getSummary contains vertex count
     */
    @Test
    public void testGetSummary_ContainsVertexCount() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.getSummary().contains("vertices"));
    }

    /**
     * Test that getSummary contains edge count
     */
    @Test
    public void testGetSummary_ContainsEdgeCount() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.getSummary().contains("edges"));
    }

    /**
     * Test that getSummary contains weight information
     */
    @Test
    public void testGetSummary_ContainsWeight() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.getSummary().contains("weight"));
    }

    /**
     * Test that getSummary contains component count
     */
    @Test
    public void testGetSummary_ContainsComponentCount() {
        MinimalBackboneResult<String, Double> result = createConnectedMST();
        assertTrue(result.getSummary().contains("components"));
    }



    /**
     * Test result with Integer weights has correct edge count
     */
    @Test
    public void testIntegerWeights_CorrectEdgeCount() {
        Graph<String, Integer> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 10);
        MinimalBackboneResult<String, Integer> result = service.computeMST(graph);

        assertEquals(2, result.getEdgeCount());
    }

    /**
     * Test result with Integer weights is valid
     */
    @Test
    public void testIntegerWeights_IsValid() {
        Graph<String, Integer> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 5);
        graph.addEdge("B", "C", 10);
        graph.addEdge("C", "A", 15);
        MinimalBackboneResult<String, Integer> result = service.computeMST(graph);

        assertTrue(result.isValid());
    }
}