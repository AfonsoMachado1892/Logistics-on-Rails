package ESINF.service;

import Graph.Graph;
import java.util.*;

/**
 * Service for computing Topological Sort using DFS-based algorithm
 * Based on theoretical material (slides 113-119)
 *
 * Algorithm (Depth-First Search based):
 * 1. Mark all vertices as unvisited
 * 2. For each unvisited vertex, perform DFS
 * 3. When a vertex has no more adjacent unvisited vertices, push it to stack
 * 4. The stack contains the topological order
 *
 * Time Complexity: O(V + E)
 * - V visits to vertices
 * - E examinations of edges
 */
public class TopologicalSortService {

    /**
     * Computes topological sort of a directed acyclic graph
     * @param graph the directed graph
     * @return LinkedList with vertices in topological order
     * @param <V> vertex type
     * @param <E> edge type
     */
    public <V, E> LinkedList<V> topologicalSort(Graph<V, E> graph) {
        LinkedList<V> topOrder = new LinkedList<>();
        Map<V, Boolean> visited = new HashMap<>();

        // Initialize all vertices as unvisited
        for (V vertex : graph.vertices()) {
            visited.put(vertex, false);
        }

        // Perform DFS from each unvisited vertex
        for (V vertex : graph.vertices()) {
            if (!visited.get(vertex)) {
                topologicalSortDFS(graph, vertex, visited, topOrder);
            }
        }

        return topOrder;
    }

    /**
     * Recursive DFS helper for topological sort
     *
     * Algorithm logic:
     * 1. Mark current vertex as visited
     * 2. Recursively visit all unvisited adjacent vertices
     * 3. After all adjacent vertices are processed, add current to front of list
     *
     * This ensures that a vertex appears before all vertices it has edges to
     */
    private <V, E> void topologicalSortDFS(Graph<V, E> graph,
                                           V vertex,
                                           Map<V, Boolean> visited,
                                           LinkedList<V> topOrder) {
        // Mark as visited
        visited.put(vertex, true);

        // Recursively visit all adjacent unvisited vertices
        for (V adjacent : graph.adjVertices(vertex)) {
            if (!visited.get(adjacent)) {
                topologicalSortDFS(graph, adjacent, visited, topOrder);
            }
        }

        // Push current vertex to front of list (stack behavior)
        // This ensures topological ordering:
        // vertices with no dependencies come first
        topOrder.addFirst(vertex);
    }

    /**
     * Alternative implementation using Kahn's algorithm (for reference)
     * Not used in main implementation but provided for completeness
     *
     * Time Complexity: O(V + E)
     */
    public <V, E> List<V> topologicalSortKahn(Graph<V, E> graph) {
        List<V> topOrder = new ArrayList<>();
        Map<V, Integer> inDegree = new HashMap<>();
        Queue<V> queue = new LinkedList<>();

        // Initialize in-degrees
        for (V vertex : graph.vertices()) {
            inDegree.put(vertex, graph.inDegree(vertex));
            if (inDegree.get(vertex) == 0) {
                queue.add(vertex);
            }
        }

        int processedVertices = 0;

        while (!queue.isEmpty()) {
            V vertex = queue.poll();
            topOrder.add(vertex);
            processedVertices++;

            // Reduce in-degree of adjacent vertices
            for (V adjacent : graph.adjVertices(vertex)) {
                inDegree.put(adjacent, inDegree.get(adjacent) - 1);
                if (inDegree.get(adjacent) == 0) {
                    queue.add(adjacent);
                }
            }
        }

        // If not all vertices processed, graph has cycle
        if (processedVertices < graph.numVertices()) {
            return new ArrayList<>(); // Empty list indicates cycle
        }

        return topOrder;
    }
}