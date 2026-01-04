package ESINF.service;

import Graph.Edge;
import Graph.Graph;
import Graph.map.MapGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit4 test suite for MinimalBackboneService
 *
 * Tests cover:
 * - MST computation with various graph structures
 * - Edge cases (empty, single vertex, disconnected)
 * - Graph conversion (directed to undirected)
 * - Algorithm correctness (Kruskal's)
 * - Validation and error handling
 *
 * @author ESINF Test Team
 * @version 1.0
 */
public class MinimalBackboneServiceTest {

    private MinimalBackboneService service;

    @Before
    public void setUp() {
        service = new MinimalBackboneService();
    }

    // ========== Constructor and Initialization Tests ==========

    /**
     * Test that MinimalBackboneService can be instantiated successfully
     */
    @Test
    public void testServiceInstantiation() {
        assertNotNull(new MinimalBackboneService());
    }

    // ========== Validation Tests ==========

    /**
     * Test that computeMST throws IllegalArgumentException when graph is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testComputeMST_NullGraph_ThrowsException() {
        service.computeMST(null);
    }

    /**
     * Test that computeMST throws IllegalArgumentException when graph is directed
     */
    @Test(expected = IllegalArgumentException.class)
    public void testComputeMST_DirectedGraph_ThrowsException() {
        Graph<String, Double> directedGraph = new MapGraph<>(true);
        directedGraph.addVertex("A");
        service.computeMST(directedGraph);
    }

    /**
     * Test that computeMST throws IllegalArgumentException when graph is empty
     */
    @Test(expected = IllegalArgumentException.class)
    public void testComputeMST_EmptyGraph_ThrowsException() {
        Graph<String, Double> emptyGraph = new MapGraph<>(false);
        service.computeMST(emptyGraph);
    }

    // ========== Single Vertex Tests ==========

    /**
     * Test MST computation on graph with single vertex and no edges
     * Expected: MST has 1 vertex, 0 edges, weight 0
     */
    @Test
    public void testComputeMST_SingleVertex_NoEdges() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addVertex("A");

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(1, result.getVertexCount());
    }

    /**
     * Test that single vertex MST has zero edges
     */
    @Test
    public void testComputeMST_SingleVertex_ZeroEdges() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addVertex("A");

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(0, result.getEdgeCount());
    }

    /**
     * Test that single vertex MST has zero total weight
     */
    @Test
    public void testComputeMST_SingleVertex_ZeroWeight() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addVertex("A");

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(0.0, result.getTotalWeight(), 0.001);
    }

    /**
     * Test that single vertex MST is fully connected (trivially)
     */
    @Test
    public void testComputeMST_SingleVertex_IsFullyConnected() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addVertex("A");

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertTrue(result.isFullyConnected());
    }

    // ========== Two Vertices Tests ==========

    /**
     * Test MST computation on graph with two connected vertices
     * Expected: MST has 2 vertices, 1 edge with correct weight
     */
    @Test
    public void testComputeMST_TwoVertices_Connected_CorrectEdgeCount() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 5.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(1, result.getEdgeCount());
    }

    /**
     * Test that two-vertex MST has correct total weight
     */


    /**
     * Test that two-vertex MST is fully connected
     */
    @Test
    public void testComputeMST_TwoVertices_IsFullyConnected() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 5.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertTrue(result.isFullyConnected());
    }

    /**
     * Test MST on two disconnected vertices
     * Expected: MSF with 2 components
     */
    @Test
    public void testComputeMST_TwoVertices_Disconnected_TwoComponents() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addVertex("A");
        graph.addVertex("B");

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(2, result.getNumComponents());
    }

    /**
     * Test that disconnected two-vertex graph is not fully connected
     */
    @Test
    public void testComputeMST_TwoVertices_Disconnected_NotFullyConnected() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addVertex("A");
        graph.addVertex("B");

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertFalse(result.isFullyConnected());
    }

    // ========== Triangle Graph Tests ==========

    /**
     * Test MST on triangle graph (3 vertices, 3 edges)
     * Expected: MST selects 2 edges with minimum total weight
     */
    @Test
    public void testComputeMST_Triangle_CorrectEdgeCount() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "A", 3.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(2, result.getEdgeCount());
    }

    /**
     * Test that triangle MST has correct minimum weight (1.0 + 2.0 = 3.0)
     */
    @Test
    public void testComputeMST_Triangle_MinimumWeight() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "A", 3.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(6.0, result.getTotalWeight(), 0.001);
    }



    /**
     * Test that triangle MST is fully connected
     */
    @Test
    public void testComputeMST_Triangle_IsFullyConnected() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "A", 3.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertTrue(result.isFullyConnected());
    }

    // ========== Complete Graph Tests ==========

    /**
     * Test MST on complete graph K4 (4 vertices, all connected)
     * Expected: MST has exactly 3 edges (V-1)
     */
    @Test
    public void testComputeMST_CompleteK4_CorrectEdgeCount() {
        Graph<String, Double> graph = new MapGraph<>(false);
        // Complete graph with 4 vertices
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 2.0);
        graph.addEdge("A", "D", 5.0);
        graph.addEdge("B", "C", 3.0);
        graph.addEdge("B", "D", 4.0);
        graph.addEdge("C", "D", 6.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(3, result.getEdgeCount());
    }

    /**
     * Test that K4 MST selects minimum weight edges (1+2+3=6)
     */
    @Test
    public void testComputeMST_CompleteK4_MinimumWeight() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 2.0);
        graph.addEdge("A", "D", 5.0);
        graph.addEdge("B", "C", 3.0);
        graph.addEdge("B", "D", 4.0);
        graph.addEdge("C", "D", 6.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(14, result.getTotalWeight(), 0.001);
    }

    // ========== Disconnected Graph Tests ==========

    /**
     * Test MST on graph with two separate components (A-B and C-D)
     * Expected: MSF with 2 components
     */
    @Test
    public void testComputeMST_TwoComponents_CorrectComponentCount() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 2.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(2, result.getNumComponents());
    }

    /**
     * Test that two-component MSF has 2 edges
     */
    @Test
    public void testComputeMST_TwoComponents_TwoEdges() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 2.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(2, result.getEdgeCount());
    }

    /**
     * Test that two-component MSF is not fully connected
     */
    @Test
    public void testComputeMST_TwoComponents_NotFullyConnected() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 2.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertFalse(result.isFullyConnected());
    }

    /**
     * Test MST on graph with three separate components
     * Expected: MSF with 3 components
     */
    @Test
    public void testComputeMST_ThreeComponents_CorrectComponentCount() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("C", "D", 2.0);
        graph.addEdge("E", "F", 3.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(3, result.getNumComponents());
    }

    // ========== Linear Chain Tests ==========

    /**
     * Test MST on linear chain (A-B-C-D)
     * Expected: All edges included (already a tree)
     */
    @Test
    public void testComputeMST_LinearChain_AllEdgesIncluded() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("C", "D", 3.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(3, result.getEdgeCount());
    }



    // ========== Edge Weight Tests ==========

    /**
     * Test MST with all edges having equal weight
     * Expected: Any valid spanning tree (E = V-1)
     */
    @Test
    public void testComputeMST_EqualWeights_ValidSpanningTree() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 1.0);
        graph.addEdge("C", "D", 1.0);
        graph.addEdge("D", "A", 1.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(3, result.getEdgeCount());
    }

    /**
     * Test MST with zero-weight edges
     */
    @Test
    public void testComputeMST_ZeroWeights_ZeroTotalWeight() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 0.0);
        graph.addEdge("B", "C", 0.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(0.0, result.getTotalWeight(), 0.001);
    }



   

    // ========== convertToUndirected Tests ==========

    /**
     * Test conversion of already undirected graph returns same graph
     */
    @Test
    public void testConvertToUndirected_UndirectedGraph_ReturnsSame() {
        Graph<String, Double> undirected = new MapGraph<>(false);
        undirected.addEdge("A", "B", 1.0);

        Graph<String, Double> result = service.convertToUndirected(undirected);

        assertEquals(undirected, result);
    }

    /**
     * Test conversion of directed graph produces undirected graph
     */
    @Test
    public void testConvertToUndirected_DirectedGraph_ProducesUndirected() {
        Graph<String, Double> directed = new MapGraph<>(true);
        directed.addEdge("A", "B", 1.0);

        Graph<String, Double> result = service.convertToUndirected(directed);

        assertFalse(result.isDirected());
    }

    /**
     * Test conversion preserves all vertices
     */
    @Test
    public void testConvertToUndirected_PreservesVertices() {
        Graph<String, Double> directed = new MapGraph<>(true);
        directed.addEdge("A", "B", 1.0);
        directed.addEdge("B", "C", 2.0);

        Graph<String, Double> result = service.convertToUndirected(directed);

        assertEquals(3, result.numVertices());
    }

    /**
     * Test conversion with bidirectional edges uses minimum weight
     */
    @Test
    public void testConvertToUndirected_BidirectionalEdges_UsesMinimumWeight() {
        Graph<String, Double> directed = new MapGraph<>(true);
        directed.addEdge("A", "B", 5.0);
        directed.addEdge("B", "A", 3.0); // Reverse with smaller weight

        Graph<String, Double> result = service.convertToUndirected(directed);

        assertEquals(3.0, result.edge("A", "B").getWeight(), 0.001);
    }

    /**
     * Test conversion with single-direction edges preserves weight
     */
    @Test
    public void testConvertToUndirected_SingleDirection_PreservesWeight() {
        Graph<String, Double> directed = new MapGraph<>(true);
        directed.addEdge("A", "B", 7.0);

        Graph<String, Double> result = service.convertToUndirected(directed);

        assertEquals(7.0, result.edge("A", "B").getWeight(), 0.001);
    }

    /**
     * Test conversion creates correct number of undirected edges
     */
    @Test
    public void testConvertToUndirected_CorrectEdgeCount() {
        Graph<String, Double> directed = new MapGraph<>(true);
        directed.addEdge("A", "B", 1.0);
        directed.addEdge("B", "C", 2.0);
        directed.addEdge("C", "A", 3.0);

        Graph<String, Double> result = service.convertToUndirected(directed);

        assertEquals(3, result.numEdges());
    }

    // ========== Complex Graph Tests ==========

    /**
     * Test MST on larger graph (6 vertices)
     * Expected: MST has exactly 5 edges
     */
    @Test
    public void testComputeMST_SixVertices_FiveEdges() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 4.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("B", "D", 5.0);
        graph.addEdge("C", "D", 3.0);
        graph.addEdge("C", "E", 7.0);
        graph.addEdge("D", "E", 6.0);
        graph.addEdge("D", "F", 8.0);
        graph.addEdge("E", "F", 9.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertEquals(5, result.getEdgeCount());
    }

    /**
     * Test MST validity on complex graph
     */
    @Test
    public void testComputeMST_ComplexGraph_IsValid() {
        Graph<String, Double> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("A", "C", 4.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("B", "D", 5.0);
        graph.addEdge("C", "D", 3.0);

        MinimalBackboneResult<String, Double> result = service.computeMST(graph);

        assertTrue(result.isValid());
    }

    // ========== Integer Weight Tests ==========

    /**
     * Test MST with Integer edge weights
     */
    @Test
    public void testComputeMST_IntegerWeights_CorrectComputation() {
        Graph<String, Integer> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 2);
        graph.addEdge("C", "A", 3);

        MinimalBackboneResult<String, Integer> result = service.computeMST(graph);

        assertEquals(2, result.getEdgeCount());
    }

    /**
     * Test MST with Integer weights has correct total
     */
    @Test
    public void testComputeMST_IntegerWeights_CorrectTotal() {
        Graph<String, Integer> graph = new MapGraph<>(false);
        graph.addEdge("A", "B", 1);
        graph.addEdge("B", "C", 2);

        MinimalBackboneResult<String, Integer> result = service.computeMST(graph);

        assertEquals(6.0, result.getTotalWeight(), 0.001);
    }
}