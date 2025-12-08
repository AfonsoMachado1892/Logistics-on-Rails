package ESINF.service;

import Graph.Edge;
import Graph.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Result class for Minimum Spanning Tree computation
 * Encapsulates the MST graph and related statistics
 *
 * Based on USEI12 requirements (Sprint 3 documentation)
 */
public class MinimalBackboneResult<V, E> {

    private final Graph<V, E> mstGraph;
    private final double totalWeight;
    private final int edgeCount;
    private final int vertexCount;
    private final List<Edge<V, E>> mstEdges;
    private final int numComponents;
    private final boolean isFullyConnected;

    /**
     * Constructor that computes all statistics from the MST graph
     *
     * @param mstGraph the minimum spanning tree graph
     */
    public MinimalBackboneResult(Graph<V, E> mstGraph) {
        this.mstGraph = mstGraph;
        this.vertexCount = mstGraph.numVertices();
        this.edgeCount = mstGraph.numEdges();

        // Extract MST edges
        this.mstEdges = new ArrayList<>(mstGraph.edges());

        // Calculate total weight
        this.totalWeight = calculateTotalWeight();

        // Detect number of connected components
        this.numComponents = detectComponents();

        // Check if fully connected (single component)
        this.isFullyConnected = (numComponents == 1);
    }

    /**
     * Calculates the total weight of all edges in the MST
     *
     * @return sum of all edge weights
     */
    private double calculateTotalWeight() {
        double sum = 0.0;

        for (Edge<V, E> edge : mstEdges) {
            E weight = edge.getWeight();

            // Handle different numeric types
            if (weight instanceof Number) {
                sum += ((Number) weight).doubleValue();
            }
        }

        return sum;
    }

    /**
     * Detects the number of connected components in the MST
     * Uses DFS to count components
     *
     * @return number of connected components
     */
    private int detectComponents() {
        if (vertexCount == 0) {
            return 0;
        }

        Collection<V> vertices = mstGraph.vertices();
        List<V> unvisited = new ArrayList<>(vertices);
        int components = 0;

        while (!unvisited.isEmpty()) {
            // Start DFS from first unvisited vertex
            V start = unvisited.get(0);
            List<V> component = dfsComponent(start);

            // Remove all vertices in this component
            unvisited.removeAll(component);
            components++;
        }

        return components;
    }

    /**
     * DFS to find all vertices in a connected component
     */
    private List<V> dfsComponent(V start) {
        List<V> component = new ArrayList<>();
        dfsRecursive(start, component);
        return component;
    }

    /**
     * Recursive DFS helper
     */
    private void dfsRecursive(V vertex, List<V> visited) {
        visited.add(vertex);

        Collection<V> adjacents = mstGraph.adjVertices(vertex);
        if (adjacents != null) {
            for (V adjacent : adjacents) {
                if (!visited.contains(adjacent)) {
                    dfsRecursive(adjacent, visited);
                }
            }
        }
    }

    /**
     * Gets the MST graph
     * @return the minimum spanning tree graph
     */
    public Graph<V, E> getMstGraph() {
        return mstGraph;
    }

    /**
     * Gets the total weight of the MST
     * @return sum of all edge weights
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Gets the number of edges in the MST
     * For a fully connected graph: edgeCount = vertexCount - 1
     * For disconnected graph (MSF): edgeCount = vertexCount - numComponents
     *
     * @return number of edges
     */
    public int getEdgeCount() {
        return edgeCount;
    }

    /**
     * Gets the number of vertices in the MST
     * @return number of vertices
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Gets the list of edges included in the MST
     * @return list of MST edges
     */
    public List<Edge<V, E>> getMstEdges() {
        return new ArrayList<>(mstEdges);
    }

    /**
     * Gets the number of connected components
     * 1 = fully connected (true MST)
     * >1 = disconnected graph (MSF - Minimum Spanning Forest)
     *
     * @return number of components
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * Checks if the graph is fully connected
     * @return true if single component, false if multiple components
     */
    public boolean isFullyConnected() {
        return isFullyConnected;
    }

    /**
     * Validates if the result represents a valid MST/MSF
     * For fully connected: edges = vertices - 1
     * For k components: edges = vertices - k
     *
     * @return true if valid MST/MSF structure
     */
    public boolean isValid() {
        int expectedEdges = vertexCount - numComponents;
        return edgeCount == expectedEdges;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Minimum Spanning Tree Result ===\n");
        sb.append(String.format("Vertices: %d\n", vertexCount));
        sb.append(String.format("Edges: %d\n", edgeCount));
        sb.append(String.format("Total Weight: %.2f km\n", totalWeight));
        sb.append(String.format("Connected Components: %d\n", numComponents));

        if (isFullyConnected) {
            sb.append("Status: Fully Connected (MST)\n");
        } else {
            sb.append("Status: Disconnected (MSF - Minimum Spanning Forest)\n");
        }

        sb.append(String.format("Valid Structure: %s\n", isValid() ? "Yes" : "No"));

        sb.append("\nMST Edges:\n");
        for (Edge<V, E> edge : mstEdges) {
            sb.append(String.format("  %s\n", edge));
        }

        return sb.toString();
    }

    /**
     * Returns a summary string with key statistics
     * @return compact summary
     */
    public String getSummary() {
        return String.format(
                "MST: %d vertices, %d edges, weight=%.2f km, components=%d",
                vertexCount, edgeCount, totalWeight, numComponents
        );
    }
}