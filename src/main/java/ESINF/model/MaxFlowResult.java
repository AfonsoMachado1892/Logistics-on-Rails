package ESINF.model;

import java.util.*;

/**
 * USEI14 - Maximum Throughput Between Two Hubs
 *
 * Represents the result of a maximum flow calculation between two railway stations.
 * Uses Ford-Fulkerson algorithm with Edmonds-Karp BFS implementation to find
 * the maximum possible throughput (trains per day) between source and sink stations.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class MaxFlowResult {

    private final RailwayStation source;
    private final RailwayStation sink;
    private final double maxFlowValue;
    private final Map<String, Double> flowPerEdge;
    private final List<List<RailwayStation>> augmentingPaths;
    private final long computationTimeNanos;

    /**
     * Represents a flow edge in the network
     */
    public static class FlowEdge {
        private final RailwayStation from;
        private final RailwayStation to;
        private final double flow;
        private final double capacity;

        public FlowEdge(RailwayStation from, RailwayStation to, double flow, double capacity) {
            this.from = from;
            this.to = to;
            this.flow = flow;
            this.capacity = capacity;
        }

        public RailwayStation getFrom() { return from; }
        public RailwayStation getTo() { return to; }
        public double getFlow() { return flow; }
        public double getCapacity() { return capacity; }
        public double getRemainingCapacity() { return capacity - flow; }

        @Override
        public String toString() {
            return String.format("%s -> %s: %.2f/%.2f (%.1f%% utilized)",
                    from.getName(), to.getName(), flow, capacity,
                    (capacity > 0 ? (flow / capacity * 100) : 0));
        }
    }

    /**
     * Constructs a MaxFlowResult
     *
     * @param source The source station (origin)
     * @param sink The sink station (destination)
     * @param maxFlowValue The maximum flow value calculated
     * @param flowPerEdge Map of edge identifiers to flow values
     * @param augmentingPaths List of paths used in the calculation
     * @param computationTimeNanos Time taken to compute the result
     */
    public MaxFlowResult(RailwayStation source,
                         RailwayStation sink,
                         double maxFlowValue,
                         Map<String, Double> flowPerEdge,
                         List<List<RailwayStation>> augmentingPaths,
                         long computationTimeNanos) {
        this.source = source;
        this.sink = sink;
        this.maxFlowValue = maxFlowValue;
        this.flowPerEdge = flowPerEdge != null ? new HashMap<>(flowPerEdge) : new HashMap<>();
        this.augmentingPaths = augmentingPaths != null ? new ArrayList<>(augmentingPaths) : new ArrayList<>();
        this.computationTimeNanos = computationTimeNanos;
    }

    /**
     * Gets the source station
     * @return source station
     */
    public RailwayStation getSource() {
        return source;
    }

    /**
     * Gets the sink station
     * @return sink station
     */
    public RailwayStation getSink() {
        return sink;
    }

    /**
     * Gets the maximum flow value (trains per day)
     * @return maximum flow value
     */
    public double getMaxFlowValue() {
        return maxFlowValue;
    }

    /**
     * Gets the flow per edge
     * @return map of edge identifiers to flow values
     */
    public Map<String, Double> getFlowPerEdge() {
        return new HashMap<>(flowPerEdge);
    }

    /**
     * Gets the augmenting paths used in the calculation
     * @return list of paths
     */
    public List<List<RailwayStation>> getAugmentingPaths() {
        return new ArrayList<>(augmentingPaths);
    }

    /**
     * Gets the computation time in nanoseconds
     * @return computation time
     */
    public long getComputationTimeNanos() {
        return computationTimeNanos;
    }

    /**
     * Gets the computation time in milliseconds
     * @return computation time in ms
     */
    public double getComputationTimeMs() {
        return computationTimeNanos / 1_000_000.0;
    }

    /**
     * Gets the number of augmenting paths found
     * @return number of paths
     */
    public int getNumberOfPaths() {
        return augmentingPaths.size();
    }

    /**
     * Creates a summary string with source, sink, and max flow
     * @return summary string
     */
    public String getSummary() {
        return String.format("MaxFlow: %s (ID: %d) → %s (ID: %d) = %.2f trains/day",
                source.getName(), source.getStationId(),
                sink.getName(), sink.getStationId(),
                maxFlowValue);
    }

    /**
     * Gets statistics about the flow distribution
     * @return statistics object
     */
    public FlowStatistics getStatistics() {
        if (flowPerEdge.isEmpty()) {
            return new FlowStatistics(0, 0, 0, 0);
        }

        double totalFlow = flowPerEdge.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        double avgFlow = totalFlow / flowPerEdge.size();

        double minFlow = flowPerEdge.values().stream()
                .mapToDouble(Double::doubleValue)
                .filter(f -> f > 0)
                .min()
                .orElse(0);

        double maxFlow = flowPerEdge.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);

        return new FlowStatistics(totalFlow, avgFlow, minFlow, maxFlow);
    }

    /**
     * Statistics about the flow distribution
     */
    public static class FlowStatistics {
        private final double totalFlow;
        private final double averageFlow;
        private final double minFlow;
        private final double maxFlow;

        public FlowStatistics(double totalFlow, double averageFlow, double minFlow, double maxFlow) {
            this.totalFlow = totalFlow;
            this.averageFlow = averageFlow;
            this.minFlow = minFlow;
            this.maxFlow = maxFlow;
        }

        public double getTotalFlow() { return totalFlow; }
        public double getAverageFlow() { return averageFlow; }
        public double getMinFlow() { return minFlow; }
        public double getMaxFlow() { return maxFlow; }

        @Override
        public String toString() {
            return String.format("Flow Statistics: Total=%.2f, Avg=%.2f, Min=%.2f, Max=%.2f",
                    totalFlow, averageFlow, minFlow, maxFlow);
        }
    }

    /**
     * Returns a detailed string representation
     * @return formatted string with all details
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Maximum Flow Analysis ===\n");
        sb.append(String.format("Source: %s (ID: %d)\n", source.getName(), source.getStationId()));
        sb.append(String.format("Sink: %s (ID: %d)\n", sink.getName(), sink.getStationId()));
        sb.append(String.format("Maximum Flow Value: %.2f trains/day\n", maxFlowValue));
        sb.append(String.format("Number of Augmenting Paths: %d\n", augmentingPaths.size()));
        sb.append(String.format("Computation Time: %.3f ms\n\n", getComputationTimeMs()));

        if (!augmentingPaths.isEmpty()) {
            sb.append("=== Augmenting Paths ===\n");
            for (int i = 0; i < augmentingPaths.size(); i++) {
                List<RailwayStation> path = augmentingPaths.get(i);
                sb.append(String.format("Path %d: ", i + 1));
                for (int j = 0; j < path.size(); j++) {
                    sb.append(path.get(j).getName());
                    if (j < path.size() - 1) {
                        sb.append(" → ");
                    }
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        if (!flowPerEdge.isEmpty()) {
            sb.append("=== Flow Per Edge ===\n");
            FlowStatistics stats = getStatistics();
            sb.append(stats.toString()).append("\n\n");

            sb.append("Active Edges:\n");
            flowPerEdge.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .forEach(e -> sb.append(String.format("  %s: %.2f trains/day\n",
                            e.getKey(), e.getValue())));
        }

        return sb.toString();
    }

    /**
     * Returns a compact one-line summary
     * @return compact summary
     */
    public String toCompactString() {
        return String.format("MaxFlow[%s→%s]: %.2f trains/day (%d paths, %.3f ms)",
                source.getName(), sink.getName(), maxFlowValue,
                augmentingPaths.size(), getComputationTimeMs());
    }
}