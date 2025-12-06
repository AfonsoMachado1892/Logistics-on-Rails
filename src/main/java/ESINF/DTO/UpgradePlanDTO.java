package ESINF.DTO;

import java.util.*;

/**
 * Data Transfer Object for Upgrade Plan Results (USEI11)
 * Contains either a topological order or cycle information
 *
 * Uses Factory Methods pattern to avoid constructor ambiguity
 */
public class UpgradePlanDTO {

    private final boolean hasCycles;
    private final List<String> topologicalOrder;
    private final List<CycleInfo> cycles;

    /**
     * Private constructor - use factory methods instead
     */
    private UpgradePlanDTO(boolean hasCycles, List<String> topologicalOrder, List<CycleInfo> cycles) {
        this.hasCycles = hasCycles;
        this.topologicalOrder = topologicalOrder != null ? new ArrayList<>(topologicalOrder) : new ArrayList<>();
        this.cycles = cycles != null ? new ArrayList<>(cycles) : new ArrayList<>();
    }

    /**
     * Factory method for successful topological sort (no cycles)
     * @param topologicalOrder the ordered list of station names
     * @return UpgradePlanDTO with topological order
     */
    public static UpgradePlanDTO withTopologicalOrder(List<String> topologicalOrder) {
        if (topologicalOrder == null) {
            throw new IllegalArgumentException("Topological order cannot be null");
        }
        return new UpgradePlanDTO(false, topologicalOrder, null);
    }

    /**
     * Factory method for graphs with cycles
     * @param cycles the list of detected cycles
     * @return UpgradePlanDTO with cycle information
     */
    public static UpgradePlanDTO withCycles(List<CycleInfo> cycles) {
        if (cycles == null || cycles.isEmpty()) {
            throw new IllegalArgumentException("Cycles list cannot be null or empty");
        }
        return new UpgradePlanDTO(true, null, cycles);
    }

    public boolean hasCycles() {
        return hasCycles;
    }

    public List<String> getTopologicalOrder() {
        return new ArrayList<>(topologicalOrder);
    }

    public List<CycleInfo> getCycles() {
        return new ArrayList<>(cycles);
    }

    /**
     * Inner class to represent cycle information
     */
    public static class CycleInfo {
        private final List<String> vertices;
        private final List<String> edges;

        public CycleInfo(List<String> vertices, List<String> edges) {
            this.vertices = new ArrayList<>(vertices);
            this.edges = new ArrayList<>(edges);
        }

        public List<String> getVertices() {
            return new ArrayList<>(vertices);
        }

        public List<String> getEdges() {
            return new ArrayList<>(edges);
        }

        @Override
        public String toString() {
            return String.format("Cycle: %s via edges %s",
                    String.join(" → ", vertices),
                    String.join(", ", edges));
        }
    }

    @Override
    public String toString() {
        if (hasCycles) {
            StringBuilder sb = new StringBuilder("Graph contains cycles:\n");
            for (int i = 0; i < cycles.size(); i++) {
                sb.append(String.format("  Cycle %d: %s\n", i + 1, cycles.get(i)));
            }
            return sb.toString();
        } else {
            return "Topological Order: " + String.join(" → ", topologicalOrder);
        }
    }
}