package ESINF.service;

import Graph.Edge;
import Graph.Graph;
import Graph.map.MapGraph;

import java.util.*;

/**
 * Service for computing Minimum Spanning Tree using Kruskal's Algorithm
 * Based on theoretical material (slides 102-107)
 *
 * <h2>Algorithm (Kruskal):</h2>
 * <ol>
 *   <li>Start with all vertices isolated</li>
 *   <li>Sort all edges by weight in ascending order</li>
 *   <li>For each edge in sorted order:
 *     <ul>
 *       <li>Check if adding the edge creates a cycle (using DFS)</li>
 *       <li>If not, add edge to MST</li>
 *     </ul>
 *   </li>
 *   <li>Stop when MST has V-1 edges (or no more edges available)</li>
 * </ol>
 *
 * <h2>Time Complexity: O(E log E)</h2>
 * <ul>
 *   <li>Sorting edges: O(E log E)</li>
 *   <li>For each edge: DFS check O(V + E_mst) where E_mst ≤ V-1</li>
 *   <li>Total: O(E log E + E×V) ≈ O(E log E) for sparse graphs</li>
 * </ul>
 *
 * <h2>References:</h2>
 * <ul>
 *   <li>Slides 102-107: Kruskal's Algorithm</li>
 *   <li>Slide 107: Pseudocode implementation</li>
 * </ul>
 *
 * @see MinimalBackboneResult
 * @see Graph
 * @author ESINF Team
 * @version 1.0
 */
public class MinimalBackboneService {

    /**
     * Computes the Minimum Spanning Tree (MST) of an undirected graph
     * using Kruskal's algorithm
     *
     * Based on slide 107 pseudocode
     *
     * Pre-conditions:
     * - Graph must be undirected
     * - Graph must have at least 1 vertex
     * - Edge weights must be non-negative and Comparable
     *
     * Post-conditions:
     * - Returns MST with V-1 edges (if connected) or V-k edges (k components)
     * - Total weight is minimized
     * - No cycles in result
     *
     * @param graph the undirected graph (must not be directed)
     * @param <V> vertex type
     * @param <E> edge weight type (must be Comparable)
     * @return MinimalBackboneResult containing the MST and statistics
     * @throws IllegalArgumentException if graph is null, directed, or empty
     * @complexity O(E log E) where E is the number of edges
     */
    public <V, E extends Comparable<E>> MinimalBackboneResult<V, E>
    computeMST(Graph<V, E> graph) {

        // Validation
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }

        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "MST requires undirected graph. Use convertToUndirected() first."
            );
        }

        if (graph.numVertices() == 0) {
            throw new IllegalArgumentException("Graph must have at least 1 vertex");
        }

        // Step 1: Create MST graph with all vertices, no edges initially
        Graph<V, E> mst = new MapGraph<>(false);

        // Add all vertices to MST
        for (V vertex : graph.vertices()) {
            mst.addVertex(vertex);
        }

        // Step 2: Get all edges and sort by weight in ascending order
        List<Edge<V, E>> sortedEdges = getSortedEdges(graph);

        // Statistics
        int edgesAdded = 0;
        int totalVertices = graph.numVertices();
        int targetEdges = totalVertices - 1; // MST has V-1 edges

        // Step 3: Process each edge in sorted order
        for (Edge<V, E> edge : sortedEdges) {
            V vOrig = edge.getVOrig();
            V vDest = edge.getVDest();
            E weight = edge.getWeight();

            // Check if vertices are already connected (would create cycle)
            Set<V> connectedVerts = depthFirstSearch(mst, vOrig);

            // If vDest is not in the connected component of vOrig, add edge
            if (!connectedVerts.contains(vDest)) {
                mst.addEdge(vOrig, vDest, weight);
                edgesAdded++;

                // Stop if we have V-1 edges (complete MST for connected graph)
                if (edgesAdded >= targetEdges) {
                    break;
                }
            }
        }

        // Create result
        return new MinimalBackboneResult<V,E>(mst);
    }

    /**
     * Sorts all edges of the graph by weight in ascending order
     *
     * @param graph the graph
     * @param <V> vertex type
     * @param <E> edge weight type (must be Comparable)
     * @return list of edges sorted by weight
     * @complexity O(E log E)
     */
    private <V, E extends Comparable<E>> List<Edge<V, E>>
    getSortedEdges(Graph<V, E> graph) {

        List<Edge<V, E>> edges = new ArrayList<>(graph.edges());

        // Sort by weight in ascending order
        edges.sort(Comparator.comparing(Edge::getWeight));

        return edges;
    }

    /**
     * Performs Depth-First Search to find all vertices connected to vOrig
     * Used in Kruskal's algorithm to detect cycles
     *
     * Based on slide 107: "connectedVerts = DepthFirstSearch(mst, vOrig)"
     *
     * @param graph the current MST being built
     * @param vOrig the origin vertex
     * @param <V> vertex type
     * @param <E> edge weight type
     * @return set of all vertices reachable from vOrig
     * @complexity O(V + E) where E ≤ V-1 in MST
     */
    private <V, E> Set<V> depthFirstSearch(Graph<V, E> graph, V vOrig) {
        Set<V> visited = new HashSet<>();
        dfsRecursive(graph, vOrig, visited);
        return visited;
    }

    /**
     * Recursive DFS helper method
     *
     * @param graph the graph
     * @param vertex current vertex
     * @param visited set of visited vertices (modified in-place)
     * @param <V> vertex type
     * @param <E> edge weight type
     */
    private <V, E> void dfsRecursive(Graph<V, E> graph, V vertex, Set<V> visited) {
        // Mark current vertex as visited
        visited.add(vertex);

        // Visit all adjacent unvisited vertices
        Collection<V> adjacents = graph.adjVertices(vertex);
        if (adjacents != null) {
            for (V adjacent : adjacents) {
                if (!visited.contains(adjacent)) {
                    dfsRecursive(graph, adjacent, visited);
                }
            }
        }
    }

    /**
     * Converts a directed graph to an undirected graph for MST computation
     *
     * For each directed edge A→B:
     * - Creates undirected edge A--B with the same weight
     * - If both A→B and B→A exist, uses the minimum weight
     *
     * @param directedGraph the directed graph
     * @param <V> vertex type
     * @param <E> edge weight type (must be Comparable)
     * @return new undirected graph
     * @complexity O(V + E)
     */
    public <V, E extends Comparable<E>> Graph<V, E>
    convertToUndirected(Graph<V, E> directedGraph) {

        if (!directedGraph.isDirected()) {
            return directedGraph; // Already undirected
        }

        // Create new undirected graph
        Graph<V, E> undirectedGraph = new MapGraph<>(false);

        // Copy all vertices
        for (V vertex : directedGraph.vertices()) {
            undirectedGraph.addVertex(vertex);
        }

        // Track processed edges to avoid duplicates
        Set<String> processedEdges = new HashSet<>();

        // Process all edges
        for (V vOrig : directedGraph.vertices()) {
            Collection<Edge<V, E>> outgoingEdges = directedGraph.outgoingEdges(vOrig);

            if (outgoingEdges != null) {
                for (Edge<V, E> edge : outgoingEdges) {
                    V vDest = edge.getVDest();
                    E weight = edge.getWeight();

                    // Create unique edge identifier (order-independent)
                    String edgeKey = createEdgeKey(vOrig, vDest);

                    // Only add if not already processed
                    if (!processedEdges.contains(edgeKey)) {
                        // Check if reverse edge exists
                        Edge<V, E> reverseEdge = directedGraph.edge(vDest, vOrig);

                        if (reverseEdge != null) {
                            // Both directions exist, use minimum weight
                            E reverseWeight = reverseEdge.getWeight();
                            E minWeight = weight.compareTo(reverseWeight) < 0 ?
                                    weight : reverseWeight;
                            undirectedGraph.addEdge(vOrig, vDest, minWeight);
                        } else {
                            // Only one direction exists
                            undirectedGraph.addEdge(vOrig, vDest, weight);
                        }

                        processedEdges.add(edgeKey);
                    }
                }
            }
        }

        return undirectedGraph;
    }

    /**
     * Creates a unique edge key for undirected edge comparison
     * Uses lexicographic order to ensure A-B and B-A produce same key
     */
    private <V> String createEdgeKey(V v1, V v2) {
        String s1 = v1.toString();
        String s2 = v2.toString();

        // Ensure consistent ordering
        if (s1.compareTo(s2) < 0) {
            return s1 + "<->" + s2;
        } else {
            return s2 + "<->" + s1;
        }
    }
}