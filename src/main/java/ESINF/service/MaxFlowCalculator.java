package ESINF.service;

import ESINF.model.RailwayStation;
import ESINF.model.MaxFlowResult;
import java.util.*;

/**
 * USEI14 - Maximum Throughput Calculator
 *
 * Implements the Ford-Fulkerson algorithm with Edmonds-Karp (BFS) implementation
 * to calculate maximum flow between two stations in a railway network.
 *
 * The algorithm finds the maximum number of trains that can flow from a source
 * station to a sink station per day, considering the capacity constraints of
 * each railway connection.
 *
 * Time Complexity: O(V * E²) where V is the number of vertices and E is the number of edges
 * - Each BFS takes O(E) time
 * - We can have at most O(V * E) augmenting paths
 *
 * Space Complexity: O(V²) for storing the residual graph
 *
 * @author ESINF Team
 * @version 1.0
 */
public class MaxFlowCalculator {

    /**
     * Represents a directed edge in the flow network
     */
    private static class Edge {
        int to;
        double capacity;
        double flow;
        Edge reverse;

        Edge(int to, double capacity) {
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }
    }

    /**
     * Calculates the maximum flow between source and sink stations
     *
     * @param graph The railway network graph represented as adjacency matrix
     *              graph[i][j] represents capacity from station i to station j
     * @param stations Array mapping indices to RailwayStation objects
     * @param sourceIdx Index of the source station
     * @param sinkIdx Index of the sink station
     * @return MaxFlowResult containing the maximum flow and detailed information
     * @throws IllegalArgumentException if inputs are invalid
     */
    public static MaxFlowResult calculateMaxFlow(double[][] graph,
                                                 RailwayStation[] stations,
                                                 int sourceIdx,
                                                 int sinkIdx) {

        if (graph == null || stations == null) {
            throw new IllegalArgumentException("Graph and stations cannot be null");
        }

        if (sourceIdx < 0 || sourceIdx >= stations.length) {
            throw new IllegalArgumentException("Invalid source index");
        }

        if (sinkIdx < 0 || sinkIdx >= stations.length) {
            throw new IllegalArgumentException("Invalid sink index");
        }

        if (sourceIdx == sinkIdx) {
            throw new IllegalArgumentException("Source and sink must be different stations");
        }

        long startTime = System.nanoTime();

        int n = graph.length;

        // Build adjacency list representation with Edge objects
        List<List<Edge>> adjList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }

        // Create forward and backward edges
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (graph[i][j] > 0) {
                    Edge forward = new Edge(j, graph[i][j]);
                    Edge backward = new Edge(i, 0); // Residual edge with 0 initial capacity

                    forward.reverse = backward;
                    backward.reverse = forward;

                    adjList.get(i).add(forward);
                    adjList.get(j).add(backward);
                }
            }
        }

        double maxFlow = 0;
        List<List<RailwayStation>> augmentingPaths = new ArrayList<>();

        // Edmonds-Karp: repeatedly find augmenting paths using BFS
        while (true) {
            // BFS to find an augmenting path
            Edge[] parent = new Edge[n];
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(sourceIdx);

            boolean foundPath = false;

            while (!queue.isEmpty() && !foundPath) {
                int u = queue.poll();

                for (Edge edge : adjList.get(u)) {
                    int v = edge.to;

                    // If not visited and has remaining capacity
                    if (parent[v] == null && v != sourceIdx && edge.capacity - edge.flow > 1e-9) {
                        parent[v] = edge;

                        if (v == sinkIdx) {
                            foundPath = true;
                            break;
                        }

                        queue.offer(v);
                    }
                }
            }

            if (!foundPath) {
                break; // No more augmenting paths
            }

            // Find minimum residual capacity along the path
            double pathFlow = Double.MAX_VALUE;
            for (int v = sinkIdx; v != sourceIdx; v = parent[v].reverse.to) {
                Edge edge = parent[v];
                pathFlow = Math.min(pathFlow, edge.capacity - edge.flow);
            }

            // Update flows along the path
            List<RailwayStation> path = new ArrayList<>();
            path.add(stations[sourceIdx]);

            for (int v = sinkIdx; v != sourceIdx; v = parent[v].reverse.to) {
                Edge edge = parent[v];
                edge.flow += pathFlow;
                edge.reverse.flow -= pathFlow;
            }

            // Reconstruct path for recording
            int current = sinkIdx;
            List<RailwayStation> pathReverse = new ArrayList<>();
            while (current != sourceIdx) {
                pathReverse.add(stations[current]);
                current = parent[current].reverse.to;
            }

            Collections.reverse(pathReverse);
            path.addAll(pathReverse);
            augmentingPaths.add(path);

            maxFlow += pathFlow;
        }

        // Build flow per edge map
        Map<String, Double> flowPerEdge = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (Edge edge : adjList.get(i)) {
                if (edge.flow > 1e-9 && edge.capacity > 0) { // Only forward edges with positive flow
                    String edgeKey = String.format("%s → %s",
                            stations[i].getName(),
                            stations[edge.to].getName());
                    flowPerEdge.put(edgeKey, edge.flow);
                }
            }
        }

        long endTime = System.nanoTime();

        return new MaxFlowResult(
                stations[sourceIdx],
                stations[sinkIdx],
                maxFlow,
                flowPerEdge,
                augmentingPaths,
                endTime - startTime
        );
    }

    /**
     * Calculates maximum flow using station IDs
     *
     * @param graph The railway network graph
     * @param stations Array of stations
     * @param sourceStationId ID of source station
     * @param sinkStationId ID of sink station
     * @return MaxFlowResult
     * @throws IllegalArgumentException if station IDs not found
     */
    public static MaxFlowResult calculateMaxFlowByStationId(double[][] graph,
                                                            RailwayStation[] stations,
                                                            int sourceStationId,
                                                            int sinkStationId) {

        int sourceIdx = -1;
        int sinkIdx = -1;

        for (int i = 0; i < stations.length; i++) {
            if (stations[i].getStationId() == sourceStationId) {
                sourceIdx = i;
            }
            if (stations[i].getStationId() == sinkStationId) {
                sinkIdx = i;
            }
        }

        if (sourceIdx == -1) {
            throw new IllegalArgumentException("Source station ID not found: " + sourceStationId);
        }

        if (sinkIdx == -1) {
            throw new IllegalArgumentException("Sink station ID not found: " + sinkStationId);
        }

        return calculateMaxFlow(graph, stations, sourceIdx, sinkIdx);
    }

    /**
     * Finds bottleneck edges in the maximum flow network
     *
     * @param result The maximum flow result
     * @param graph Original capacity graph
     * @param stations Array of stations
     * @return List of bottleneck edges (fully saturated)
     */
    public static List<String> findBottlenecks(MaxFlowResult result,
                                               double[][] graph,
                                               RailwayStation[] stations) {

        List<String> bottlenecks = new ArrayList<>();
        Map<String, Double> flowPerEdge = result.getFlowPerEdge();

        // Find edges where flow equals capacity
        for (int i = 0; i < stations.length; i++) {
            for (int j = 0; j < stations.length; j++) {
                if (graph[i][j] > 0) {
                    String edgeKey = String.format("%s → %s",
                            stations[i].getName(),
                            stations[j].getName());

                    Double flow = flowPerEdge.get(edgeKey);
                    if (flow != null && Math.abs(flow - graph[i][j]) < 1e-9) {
                        bottlenecks.add(String.format("%s (capacity: %.2f)",
                                edgeKey, graph[i][j]));
                    }
                }
            }
        }

        return bottlenecks;
    }

    /**
     * Validates if the graph represents a valid flow network
     *
     * @param graph The capacity graph
     * @return true if valid, false otherwise
     */
    public static boolean isValidFlowNetwork(double[][] graph) {
        if (graph == null) {
            return false;
        }

        int n = graph.length;

        // Check if square matrix
        for (int i = 0; i < n; i++) {
            if (graph[i].length != n) {
                return false;
            }
        }

        // Check for non-negative capacities
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (graph[i][j] < 0) {
                    return false;
                }
            }
        }

        return true;
    }
}