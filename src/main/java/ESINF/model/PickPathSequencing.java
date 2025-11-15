package ESINF.model;

import java.util.*;

/**
 * USEI04 - Pick Path Sequencing.
 * This class optimizes the picking route in a warehouse by sequencing
 * bay visits to minimize walking distance. It supports multiple strategies
 * such as deterministic sweep and nearest-neighbour (greedy) approach.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class PickPathSequencing {

    /**
     * Represents a 2D coordinate in the warehouse.
     * Coordinates are used to compute distances between bays.
     */
    public static class Coordinate {
        /** The X position in the warehouse (aisle * 3). */
        public int x;

        /** The Y position in the warehouse (bay position). */
        public int y;

        /**
         * Creates a coordinate at a specific (x, y) position.
         *
         * @param x X coordinate (aisle * 3)
         * @param y Y coordinate (bay position)
         */
        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Calculates the distance between two coordinates using
         * the warehouse's internal distance model.
         *
         * @param other The other coordinate
         * @return The computed distance in distance units
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

        /**
         * Returns a string representation of the coordinate.
         *
         * @return A string in the format "(x,y)"
         */
        @Override
        public String toString() {
            return String.format("(%d,%d)", x, y);
        }
    }

    /**
     * Represents a unique bay location with consolidated picking information.
     * Multiple allocations within the same bay are merged into one.
     */
    public static class ConsolidatedBay {
        /** The aisle number. */
        public int aisle;

        /** The bay number within the aisle. */
        public int bay;

        /** List of all allocations corresponding to this bay. */
        public List<Allocation> allocations;

        /** Total quantity of items to pick from this bay. */
        public int totalQty;

        /** Total weight of all items in this bay. */
        public double totalWeight;

        /**
         * Constructs a consolidated bay object.
         *
         * @param aisle The aisle number
         * @param bay   The bay number
         */
        public ConsolidatedBay(int aisle, int bay) {
            this.aisle = aisle;
            this.bay = bay;
            this.allocations = new ArrayList<>();
            this.totalQty = 0;
            this.totalWeight = 0.0;
        }

        /**
         * Returns the coordinate representation of this bay.
         *
         * @return The coordinate of this bay
         */
        public Coordinate getCoordinate() {
            return new Coordinate(aisle * 3, bay);
        }

        /**
         * Returns a unique string key for this bay, based on aisle and bay.
         *
         * @return Unique bay key in the format "aisle,bay"
         */
        public String getKey() {
            return aisle + "," + bay;
        }

        /**
         * Returns a string representation of the consolidated bay.
         *
         * @return A string containing aisle, bay, and total quantity information
         */
        @Override
        public String toString() {
            return String.format("Bay[aisle=%d, bay=%d, qty=%d]", aisle, bay, totalQty);
        }
    }

    /**
     * Represents a complete picking path through the warehouse.
     */
    public static class PickPath {
        /** Name of the strategy used to generate this path. */
        public String strategyName;

        /** Ordered list of bays in the path. */
        public List<ConsolidatedBay> route;

        /** Total walking distance for the entire route. */
        public double totalDistance;

        /**
         * Constructs a pick path associated with a given strategy.
         *
         * @param strategyName The name of the pathfinding strategy
         */
        public PickPath(String strategyName) {
            this.strategyName = strategyName;
            this.route = new ArrayList<>();
            this.totalDistance = 0.0;
        }

        /**
         * Returns a formatted string representation of the pick path,
         * including strategy name, total distance, and route sequence.
         *
         * @return A formatted multi-line string describing the pick path
         */
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

    /** Map of SKU → Item details (unit weight, etc.). */
    private Map<String, Item> itemCatalog;

    /**
     * Constructs a PickPathSequencing instance.
     *
     * @param itemCatalog The item catalog containing SKU details
     * @throws IllegalArgumentException If the item catalog is null
     */
    public PickPathSequencing(Map<String, Item> itemCatalog) {
        if (itemCatalog == null) {
            throw new IllegalArgumentException("Item catalog cannot be null");
        }
        this.itemCatalog = itemCatalog;
    }

    /**
     * Consolidates allocations by bay location, merging duplicates
     * and computing total quantities and weights.
     *
     * @param allocations List of allocations to consolidate
     * @return A map of bay key → ConsolidatedBay object
     */
    private Map<String, ConsolidatedBay> consolidateBays(List<Allocation> allocations) {
        Map<String, ConsolidatedBay> consolidated = new HashMap<>();

        for (Allocation alloc : allocations) {
            if (alloc == null) {
                System.err.println("Warning: null allocation encountered");
                continue;
            }

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

            Item item = itemCatalog.get(alloc.getSku());
            if (item != null) {
                consolidated_bay.totalWeight += item.getUnitWeight() * alloc.getQty();
            }
        }

        return consolidated;
    }

    /**
     * Strategy A: Deterministic Sweep (ascending by aisle and bay).
     * Visits all bays in ascending order of aisle, then bay number.
     *
     * @param allocations List of allocations to process
     * @return The generated pick path using Strategy A
     */
    public PickPath strategyA(List<Allocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            //System.out.println("No allocations for strategy A");
            return new PickPath("Strategy A - Deterministic Sweep");
        }

        Map<String, ConsolidatedBay> consolidated = consolidateBays(allocations);
        List<ConsolidatedBay> uniqueBays = new ArrayList<>(consolidated.values());

        uniqueBays.sort((b1, b2) -> {
            int aisleCmp = Integer.compare(b1.aisle, b2.aisle);
            if (aisleCmp != 0) return aisleCmp;
            return Integer.compare(b1.bay, b2.bay);
        });

        PickPath path = new PickPath("Strategy A - Deterministic Sweep");
        path.route = uniqueBays;
        path.totalDistance = calculateTotalDistance(uniqueBays);

        return path;
    }

    /**
     * Strategy B: Nearest-Neighbour (Greedy approach).
     * Starts at (0,0) and repeatedly moves to the nearest unvisited bay.
     *
     * @param allocations List of allocations to process
     * @return The generated pick path using Strategy B
     */
    public PickPath strategyB(List<Allocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            //System.out.println("No allocations for strategy B");
            return new PickPath("Strategy B - Nearest-Neighbour");
        }

        Map<String, ConsolidatedBay> consolidated = consolidateBays(allocations);
        List<ConsolidatedBay> uniqueBays = new ArrayList<>(consolidated.values());

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

            if (nearest == null) break;

            route.add(nearest);
            visited.add(nearest);
            current = nearest.getCoordinate();
        }

        PickPath path = new PickPath("Strategy B - Nearest-Neighbour");
        path.route = route;
        path.totalDistance = calculateTotalDistance(route);

        return path;
    }

    /**
     * Calculates the total walking distance for a given route.
     * Distance is computed from the starting point (0,0) through all bays in sequence.
     *
     * @param route Ordered list of consolidated bays
     * @return Total distance in warehouse units
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
     * Returns both pick paths (Strategy A and Strategy B) for programmatic use.
     *
     * @param allocations List of allocations to process
     * @return An array with both paths: [0] = Strategy A, [1] = Strategy B
     */
    public PickPath[] generateBothPaths(List<Allocation> allocations) {
        return new PickPath[]{strategyA(allocations), strategyB(allocations)};
    }
}