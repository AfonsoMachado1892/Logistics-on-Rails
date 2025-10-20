package ESINF.model;

import java.util.*;

/**
 * USEI04 - Pick Path Sequencing
 * Optimizes the picking route in a warehouse by sequencing bay visits
 * to minimize walking distance.
 */
public class PickPathSequencing {

    /**
     * Represents a 2D coordinate in the warehouse
     */
    public static class Coordinate {
        public int x; // aisle * 3
        public int y; // bay position

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Calculates distance between two coordinates using the warehouse distance model
         */
        public double distanceTo(Coordinate other) {
            if (this.x == other.x) {
                // Same aisle: simple bay distance
                return Math.abs(this.y - other.y);
            } else {
                // Different aisles: go down, cross, go up
                // D = b1 + |a1 - a2| * 3 + b2
                return Math.abs(this.y) + Math.abs(this.x - other.x) + Math.abs(other.y);
            }
        }

        @Override
        public String toString() {
            return String.format("(%d,%d)", x, y);
        }
    }

    /**
     * Represents a unique bay location with consolidated picking info
     */
    public static class ConsolidatedBay {
        public int aisle;
        public int bay;
        public List<Allocation> allocations; // All allocations for this bay
        public int totalQty; // Sum of quantities
        public double totalWeight; // Sum of weights

        public ConsolidatedBay(int aisle, int bay) {
            this.aisle = aisle;
            this.bay = bay;
            this.allocations = new ArrayList<>();
            this.totalQty = 0;
            this.totalWeight = 0.0;
        }

        public Coordinate getCoordinate() {
            return new Coordinate(aisle * 3, bay);
        }

        public String getKey() {
            return aisle + "," + bay;
        }

        @Override
        public String toString() {
            return String.format("Bay[aisle=%d, bay=%d, qty=%d]", aisle, bay, totalQty);
        }
    }

    /**
     * Represents a complete pick path route
     */
    public static class PickPath {
        public String strategyName;
        public List<ConsolidatedBay> route;
        public double totalDistance;

        public PickPath(String strategyName) {
            this.strategyName = strategyName;
            this.route = new ArrayList<>();
            this.totalDistance = 0.0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(strategyName).append(" ===\n");
            sb.append(String.format("Total Distance: %.2f units\n", totalDistance));
            sb.append("Route sequence:\n");
            for (int i = 0; i < route.size(); i++) {
                ConsolidatedBay bay = route.get(i);
                sb.append(String.format("  %d. %s\n", i + 1, bay));
            }
            return sb.toString();
        }
    }

    private Map<String, Item> itemCatalog;

    public PickPathSequencing(Map<String, Item> itemCatalog) {
        if (itemCatalog == null) {
            throw new IllegalArgumentException("Item catalog cannot be null");
        }
        this.itemCatalog = itemCatalog;
    }

    /**
     * Consolidates allocations by bay location, merging duplicates
     */
    private Map<String, ConsolidatedBay> consolidateBays(List<Allocation> allocations) {
        Map<String, ConsolidatedBay> consolidated = new HashMap<>();

        for (Allocation alloc : allocations) {
            if (alloc == null) {
                System.err.println("Warning: null allocation encountered");
                continue;
            }

            // Parse aisle and bay (they're stored as strings)
            int aisle, bay;
            try {
                aisle = Integer.parseInt(alloc.getAisle());
                bay = Integer.parseInt(alloc.getBay());
            } catch (NumberFormatException e) {
                System.err.println("Error parsing bay location - aisle: " + alloc.getAisle()
                        + ", bay: " + alloc.getBay());
                continue;
            }

            String key = aisle + "," + bay;
            ConsolidatedBay consolidated_bay = consolidated.computeIfAbsent(key,
                    k -> new ConsolidatedBay(aisle, bay));

            consolidated_bay.allocations.add(alloc);
            consolidated_bay.totalQty += alloc.getQty();

            // Calculate weight
            Item item = itemCatalog.get(alloc.getSku());
            if (item != null) {
                consolidated_bay.totalWeight += item.getUnitWeight() * alloc.getQty();
            }
        }

        return consolidated;
    }

    /**
     * Strategy A: Deterministic Sweep (ascending by aisle)
     */
    public PickPath strategyA(List<Allocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            System.out.println("No allocations for strategy A");
            return new PickPath("Strategy A - Deterministic Sweep");
        }

        // 1. Consolidate bays
        Map<String, ConsolidatedBay> consolidated = consolidateBays(allocations);
        List<ConsolidatedBay> uniqueBays = new ArrayList<>(consolidated.values());

        // 2. Sort by aisle ascending, then bay ascending
        uniqueBays.sort((b1, b2) -> {
            int aisleCmp = Integer.compare(b1.aisle, b2.aisle);
            if (aisleCmp != 0) return aisleCmp;
            return Integer.compare(b1.bay, b2.bay);
        });

        // 3. Calculate distance
        PickPath path = new PickPath("Strategy A - Deterministic Sweep");
        path.route = uniqueBays;
        path.totalDistance = calculateTotalDistance(uniqueBays);

        return path;
    }

    /**
     * Strategy B: Nearest-Neighbour (greedy approach)
     */
    public PickPath strategyB(List<Allocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            System.out.println("No allocations for strategy B");
            return new PickPath("Strategy B - Nearest-Neighbour");
        }

        // 1. Consolidate bays
        Map<String, ConsolidatedBay> consolidated = consolidateBays(allocations);
        List<ConsolidatedBay> uniqueBays = new ArrayList<>(consolidated.values());

        // 2. Greedy: start at (0,0) and always go to nearest unvisited bay
        List<ConsolidatedBay> route = new ArrayList<>();
        Set<ConsolidatedBay> visited = new HashSet<>();
        Coordinate current = new Coordinate(0, 0);

        while (visited.size() < uniqueBays.size()) {
            ConsolidatedBay nearest = null;
            double minDist = Double.MAX_VALUE;

            for (ConsolidatedBay bay : uniqueBays) {
                if (!visited.contains(bay)) {
                    double dist = current.distanceTo(bay.getCoordinate());
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = bay;
                    }
                }
            }

            if (nearest == null) break; // Should not happen

            route.add(nearest);
            visited.add(nearest);
            current = nearest.getCoordinate();
        }

        // 3. Calculate total distance
        PickPath path = new PickPath("Strategy B - Nearest-Neighbour");
        path.route = route;
        path.totalDistance = calculateTotalDistance(route);

        return path;
    }

    /**
     * Calculates total distance for a given route
     */
    private double calculateTotalDistance(List<ConsolidatedBay> route) {
        double total = 0;
        Coordinate current = new Coordinate(0, 0);

        for (ConsolidatedBay bay : route) {
            Coordinate next = bay.getCoordinate();
            total += current.distanceTo(next);
            current = next;
        }

        return total;
    }

    /**
     * Generates and displays detailed picking routes for both strategies
     */
    public void generateAndDisplayPickingRoutes(List<Allocation> allocations) {
        System.out.println("\n========== PICK PATH SEQUENCING (USEI04) ==========\n");

        PickPath pathA = strategyA(allocations);
        PickPath pathB = strategyB(allocations);

        displayPickPath(pathA, allocations);
        displayPickPath(pathB, allocations);

        System.out.println("===================================================\n");
    }

    /**
     * Displays detailed information about a pick path
     */
    private void displayPickPath(PickPath path, List<Allocation> allocations) {
        System.out.println(path);

        if (!path.route.isEmpty()) {
            System.out.println("Detailed picking list:");
            for (ConsolidatedBay bay : path.route) {
                System.out.printf("  Aisle %d, Bay %d (Coordinate: %s):\n",
                        bay.aisle, bay.bay, bay.getCoordinate());
                for (Allocation alloc : bay.allocations) {
                    System.out.printf("    - Order %s, Line %d | SKU %s | Qty %d | Box %s\n",
                            alloc.getOrderId(), alloc.getLineNo(), alloc.getSku(),
                            alloc.getQty(), alloc.getBoxId());
                }
            }
        }
        System.out.println();
    }

    /**
     * Returns both paths for programmatic use
     */
    public PickPath[] generateBothPaths(List<Allocation> allocations) {
        return new PickPath[]{strategyA(allocations), strategyB(allocations)};
    }
}


