package ESINF.service;

import Graph.Graph;
import java.util.*;

/**
 * Service for detecting cycles in directed graphs using Colored DFS
 * Based on theoretical material (slides 90-94)
 *
 * Algorithm (Colored DFS):
 * - WHITE: vertex not yet visited
 * - GRAY: vertex currently being processed (in DFS stack)
 * - BLACK: vertex completely processed (all descendants visited)
 *
 * If we encounter a GRAY vertex during DFS, we found a cycle
 *
 * Time Complexity: O(V + E)
 */
public class CycleDetectionService {

    private enum Color {
        WHITE,  // Not visited
        GRAY,   // Currently visiting (in recursion stack)
        BLACK   // Completely visited
    }

    /**
     * Detects if the graph contains any cycle
     * @param graph the directed graph
     * @return true if cycle exists, false otherwise
     */
    public <V, E> boolean hasCycle(Graph<V, E> graph) {
        Map<V, Color> colors = new HashMap<>();

        // Initialize all vertices as WHITE
        for (V vertex : graph.vertices()) {
            colors.put(vertex, Color.WHITE);
        }

        // Try DFS from each unvisited vertex
        for (V vertex : graph.vertices()) {
            if (colors.get(vertex) == Color.WHITE) {
                if (hasCycleDFS(graph, vertex, colors)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * DFS helper for cycle detection
     *
     * Logic:
     * 1. Mark current vertex GRAY (visiting)
     * 2. Visit all adjacent vertices
     *    - If adjacent is GRAY → cycle found!
     *    - If adjacent is WHITE → recursively check
     * 3. Mark current vertex BLACK (finished)
     */
    private <V, E> boolean hasCycleDFS(Graph<V, E> graph,
                                       V vertex,
                                       Map<V, Color> colors) {
        // Mark as GRAY (currently visiting)
        colors.put(vertex, Color.GRAY);

        // Check all adjacent vertices
        for (V adjacent : graph.adjVertices(vertex)) {
            Color adjColor = colors.get(adjacent);

            // If adjacent is GRAY, we found a back edge → cycle!
            if (adjColor == Color.GRAY) {
                return true;
            }

            // If adjacent is WHITE, recursively check
            if (adjColor == Color.WHITE) {
                if (hasCycleDFS(graph, adjacent, colors)) {
                    return true;
                }
            }
            // If adjacent is BLACK, it's already processed, skip
        }

        // Mark as BLACK (completely visited)
        colors.put(vertex, Color.BLACK);
        return false;
    }

    /**
     * Finds all cycles in the graph
     * @param graph the directed graph
     * @return list of cycles, where each cycle is a list of vertices
     */
    public <V, E> List<List<V>> findAllCycles(Graph<V, E> graph) {
        List<List<V>> allCycles = new ArrayList<>();
        Map<V, Color> colors = new HashMap<>();
        Map<V, V> parent = new HashMap<>();

        // Initialize
        for (V vertex : graph.vertices()) {
            colors.put(vertex, Color.WHITE);
            parent.put(vertex, null);
        }

        // Find cycles from each unvisited vertex
        for (V vertex : graph.vertices()) {
            if (colors.get(vertex) == Color.WHITE) {
                findCyclesDFS(graph, vertex, colors, parent, allCycles);
            }
        }

        return allCycles;
    }

    /**
     * DFS to find and extract cycle paths
     */
    private <V, E> void findCyclesDFS(Graph<V, E> graph,
                                      V vertex,
                                      Map<V, Color> colors,
                                      Map<V, V> parent,
                                      List<List<V>> allCycles) {
        colors.put(vertex, Color.GRAY);

        for (V adjacent : graph.adjVertices(vertex)) {
            Color adjColor = colors.get(adjacent);

            if (adjColor == Color.GRAY) {
                // Found a cycle! Extract the cycle path
                List<V> cycle = extractCycle(vertex, adjacent, parent);
                if (!cycle.isEmpty()) {
                    allCycles.add(cycle);
                }
            } else if (adjColor == Color.WHITE) {
                parent.put(adjacent, vertex);
                findCyclesDFS(graph, adjacent, colors, parent, allCycles);
            }
        }

        colors.put(vertex, Color.BLACK);
    }

    /**
     * Extracts the cycle path from start to end using parent pointers
     */
    private <V, E> List<V> extractCycle(V start, V end, Map<V, V> parent) {
        List<V> cycle = new ArrayList<>();

        // Trace back from start to end
        V current = start;
        cycle.add(current);

        while (current != null && !current.equals(end)) {
            current = parent.get(current);
            if (current != null) {
                cycle.add(0, current); // Add to front
            }

            // Prevent infinite loop
            if (cycle.size() > parent.size()) {
                break;
            }
        }

        // Add start again to close the cycle
        if (!cycle.isEmpty() && cycle.get(0).equals(end)) {
            cycle.add(end);
            return cycle;
        }

        return new ArrayList<>();
    }
}