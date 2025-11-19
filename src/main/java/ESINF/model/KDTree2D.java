package ESINF.model;

import java.util.*;

/**
 * USEI07 - Build a balanced 2D-Tree on Latitude/Longitude
 *
 * A KD-Tree (k=2) for spatial indexing of train stations using (latitude, longitude).
 * Supports efficient range queries and nearest-neighbor searches.
 *
 * When multiple stations share the same coordinates, they are stored in a bucket
 * sorted by station name in ascending order.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class KDTree2D {

    /**
     * Represents a node in the 2D-Tree.
     * Each node splits space either by latitude (even depth) or longitude (odd depth).
     */
    public static class Node {
        /** Latitude coordinate of this node */
        public double latitude;

        /** Longitude coordinate of this node */
        public double longitude;

        /** List of stations at this coordinate (sorted by name) */
        public List<Station> stations;

        /** Left subtree (lat < this.lat OR lon < this.lon depending on depth) */
        public Node left;

        /** Right subtree (lat >= this.lat OR lon >= this.lon depending on depth) */
        public Node right;

        /**
         * Creates a node with given coordinates and station.
         */
        public Node(double latitude, double longitude, Station station) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.stations = new ArrayList<>();
            this.stations.add(station);
            this.left = null;
            this.right = null;
        }

        /**
         * Adds a station to this node's bucket (maintains sorted order by name).
         */
        public void addStation(Station station) {
            stations.add(station);
            stations.sort(Comparator.comparing(Station::getName));
        }

        @Override
        public String toString() {
            return String.format("Node(%.5f, %.5f) - %d station(s)",
                    latitude, longitude, stations.size());
        }
    }

    /** Root node of the 2D-Tree */
    private Node root;

    /** Total number of nodes in the tree */
    private int size;

    /** Height of the tree */
    private int height;

    /** Map tracking bucket sizes (coordinate -> number of stations) */
    private Map<String, Integer> bucketSizes;

    /**
     * Constructs an empty 2D-Tree.
     */
    public KDTree2D() {
        this.root = null;
        this.size = 0;
        this.height = 0;
        this.bucketSizes = new HashMap<>();
    }

    /**
     * Builds a balanced 2D-Tree from BST/AVL trees of latitude and longitude.
     *
     * Strategy:
     * 1. Extract all stations from the latitude and longitude BSTs
     * 2. Remove duplicates (same station in both trees)
     * 3. Perform bulk build using recursive median partitioning
     *
     * @param latitudeBST BST/AVL ordered by latitude
     * @param longitudeBST BST/AVL ordered by longitude
     * @return Statistics about the built tree
     */
    public TreeStatistics buildBalanced(List<Station> latitudeBST, List<Station> longitudeBST) {
        long startTime = System.nanoTime();

        // Merge and deduplicate stations
        Set<Station> uniqueStations = new HashSet<>();
        uniqueStations.addAll(latitudeBST);
        uniqueStations.addAll(longitudeBST);

        List<Station> stations = new ArrayList<>(uniqueStations);

        if (stations.isEmpty()) {
            return new TreeStatistics(0, 0, new HashMap<>(), 0);
        }

        // Build balanced tree recursively
        root = buildBalancedRecursive(stations, 0);

        // Calculate statistics
        height = calculateHeight(root);
        size = countNodes(root);
        bucketSizes = calculateBucketSizes(root);

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        return new TreeStatistics(size, height, bucketSizes, elapsedTime);
    }

    /**
     * Recursively builds a balanced 2D-Tree.
     *
     * @param stations List of stations to insert
     * @param depth Current depth (0 = root, even = split by lat, odd = split by lon)
     * @return Root node of the subtree
     */
    private Node buildBalancedRecursive(List<Station> stations, int depth) {
        if (stations.isEmpty()) {
            return null;
        }

        // Determine axis: even depth = latitude, odd depth = longitude
        boolean splitByLatitude = (depth % 2 == 0);

        // Sort by the current axis
        if (splitByLatitude) {
            stations.sort(Comparator.comparingDouble(Station::getLatitude)
                    .thenComparing(Station::getName));
        } else {
            stations.sort(Comparator.comparingDouble(Station::getLongitude)
                    .thenComparing(Station::getName));
        }

        // Find median
        int medianIndex = stations.size() / 2;
        Station medianStation = stations.get(medianIndex);

        // Create node at median
        Node node = new Node(medianStation.getLatitude(),
                medianStation.getLongitude(),
                medianStation);

        // Group stations with same coordinates
        List<Station> leftStations = new ArrayList<>();
        List<Station> rightStations = new ArrayList<>();

        for (int i = 0; i < stations.size(); i++) {
            if (i == medianIndex) continue;

            Station s = stations.get(i);

            // Check if station shares the same coordinates
            if (Math.abs(s.getLatitude() - node.latitude) < 0.000001 &&
                    Math.abs(s.getLongitude() - node.longitude) < 0.000001) {
                node.addStation(s);
            } else {
                // Split by current axis
                boolean goLeft;
                if (splitByLatitude) {
                    goLeft = s.getLatitude() < node.latitude;
                } else {
                    goLeft = s.getLongitude() < node.longitude;
                }

                if (goLeft) {
                    leftStations.add(s);
                } else {
                    rightStations.add(s);
                }
            }
        }

        // Recursively build subtrees
        node.left = buildBalancedRecursive(leftStations, depth + 1);
        node.right = buildBalancedRecursive(rightStations, depth + 1);

        return node;
    }

    /**
     * Calculates the height of the tree.
     */
    private int calculateHeight(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(calculateHeight(node.left), calculateHeight(node.right));
    }

    /**
     * Counts total nodes in the tree.
     */
    private int countNodes(Node node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    /**
     * Calculates bucket sizes (number of stations per coordinate).
     */
    private Map<String, Integer> calculateBucketSizes(Node node) {
        Map<String, Integer> sizes = new HashMap<>();
        calculateBucketSizesRecursive(node, sizes);
        return sizes;
    }

    private void calculateBucketSizesRecursive(Node node, Map<String, Integer> sizes) {
        if (node == null) return;

        String key = String.format("%.5f,%.5f", node.latitude, node.longitude);
        sizes.put(key, node.stations.size());

        calculateBucketSizesRecursive(node.left, sizes);
        calculateBucketSizesRecursive(node.right, sizes);
    }

    /**
     * Performs an in-order traversal of the tree.
     */
    public List<Node> inOrderTraversal() {
        List<Node> result = new ArrayList<>();
        inOrderTraversalRecursive(root, result);
        return result;
    }

    private void inOrderTraversalRecursive(Node node, List<Node> result) {
        if (node == null) return;

        inOrderTraversalRecursive(node.left, result);
        result.add(node);
        inOrderTraversalRecursive(node.right, result);
    }

    /**
     * Calculates Haversine distance between two coordinates (in km).
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth radius in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Returns statistics about the tree.
     */
    public TreeStatistics getStatistics() {
        return new TreeStatistics(size, height, bucketSizes, 0);
    }

    /**
     * Statistics about the built tree.
     */
    public static class TreeStatistics {
        public int size;
        public int height;
        public Map<String, Integer> bucketSizes;
        public long buildTimeNanos;

        public TreeStatistics(int size, int height, Map<String, Integer> bucketSizes, long buildTimeNanos) {
            this.size = size;
            this.height = height;
            this.bucketSizes = bucketSizes;
            this.buildTimeNanos = buildTimeNanos;
        }

        /**
         * Gets all distinct bucket sizes.
         */
        public Set<Integer> getDistinctBucketSizes() {
            return new HashSet<>(bucketSizes.values());
        }

        /**
         * Gets frequency distribution of bucket sizes.
         */
        public Map<Integer, Integer> getBucketSizeFrequency() {
            Map<Integer, Integer> frequency = new HashMap<>();
            for (int size : bucketSizes.values()) {
                frequency.put(size, frequency.getOrDefault(size, 0) + 1);
            }
            return frequency;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 2D-Tree Statistics ===\n");
            sb.append(String.format("Size: %d nodes\n", size));
            sb.append(String.format("Height: %d\n", height));
            sb.append(String.format("Distinct bucket sizes: %s\n", getDistinctBucketSizes()));
            sb.append(String.format("Build time: %.3f ms\n", buildTimeNanos / 1_000_000.0));
            return sb.toString();
        }
    }

    /**
     * Returns the root node (for testing/visualization).
     */
    public Node getRoot() {
        return root;
    }
    /**
     * USEI08 - Range query with optional filtering.
     * Finds all stations within a rectangular region that match the filter criteria.
     *
     * @param region The geographic region to search
     * @param filter Optional filter (use null for no filtering)
     * @return List of stations matching region and filter
     */
    public List<Station> rangeSearchWithFilter(GeographicRegion region, StationFilter filter) {
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }

        List<Station> result = new ArrayList<>();
        long startTime = System.nanoTime();

        rangeSearchWithFilterRecursive(
                root,
                region.getLatMin(),
                region.getLatMax(),
                region.getLonMin(),
                region.getLonMax(),
                filter,
                0,
                result
        );

        long endTime = System.nanoTime();
        long elapsedNanos = endTime - startTime;

        // Store last query time for statistics
        this.lastQueryTimeNanos = elapsedNanos;

        return result;
    }

    /**
     * Recursive helper for range search with filtering and pruning.
     *
     * Pruning logic:
     * - If splitting by latitude and region.latMax < node.latitude -> only explore left
     * - If splitting by latitude and region.latMin > node.latitude -> only explore right
     * - Similar logic for longitude splits
     *
     * @param node Current node
     * @param minLat Minimum latitude of search region
     * @param maxLat Maximum latitude of search region
     * @param minLon Minimum longitude of search region
     * @param maxLon Maximum longitude of search region
     * @param filter Optional filter (null = no filtering)
     * @param depth Current depth (0 = root)
     * @param result List to accumulate results
     */
    private void rangeSearchWithFilterRecursive(Node node,
                                                double minLat, double maxLat,
                                                double minLon, double maxLon,
                                                StationFilter filter,
                                                int depth,
                                                List<Station> result) {
        if (node == null) {
            return;
        }

        // Check if node's coordinates are in range
        if (node.latitude >= minLat && node.latitude <= maxLat &&
                node.longitude >= minLon && node.longitude <= maxLon) {

            // Add all stations at this node that pass the filter
            for (Station station : node.stations) {
                if (filter == null || filter.matches(station)) {
                    result.add(station);
                }
            }
        }

        // Determine split axis (even depth = latitude, odd = longitude)
        boolean splitByLatitude = (depth % 2 == 0);

        // PRUNING: Decide which subtrees to explore
        if (splitByLatitude) {
            // Split by latitude
            // Explore left subtree if region extends below node's latitude
            if (minLat <= node.latitude) {
                rangeSearchWithFilterRecursive(node.left, minLat, maxLat, minLon, maxLon,
                        filter, depth + 1, result);
            }
            // Explore right subtree if region extends above node's latitude
            if (maxLat >= node.latitude) {
                rangeSearchWithFilterRecursive(node.right, minLat, maxLat, minLon, maxLon,
                        filter, depth + 1, result);
            }
        } else {
            // Split by longitude
            // Explore left subtree if region extends west of node's longitude
            if (minLon <= node.longitude) {
                rangeSearchWithFilterRecursive(node.left, minLat, maxLat, minLon, maxLon,
                        filter, depth + 1, result);
            }
            // Explore right subtree if region extends east of node's longitude
            if (maxLon >= node.longitude) {
                rangeSearchWithFilterRecursive(node.right, minLat, maxLat, minLon, maxLon,
                        filter, depth + 1, result);
            }
        }
    }

    // Add field to track last query time
    private long lastQueryTimeNanos = 0;

    /**
     * Gets the execution time of the last range search query.
     *
     * @return Time in nanoseconds
     */
    public long getLastQueryTimeNanos() {
        return lastQueryTimeNanos;
    }

    /**
     * Gets the execution time of the last range search query in milliseconds.
     *
     * @return Time in milliseconds
     */
    public double getLastQueryTimeMs() {
        return lastQueryTimeNanos / 1_000_000.0;
    }



    // ---  METHODS FOR USEI09 (Proximity Search) ---

    /**
     * Finds the N nearest *locations* (nodes) to a target coordinate.
     * Complies with USEI09: Handles N < X cases and optional time zone filtering.
     *
     * @param targetLat Target latitude
     * @param targetLon Target longitude
     * @param N         The number of nearest locations to find (must be > 0)
     * @param filter    Optional TimeZoneFilter (null for no filter)
     * @return A list of all stations from the N nearest locations, sorted by distance.
     */
    public List<Station> findNearestNStations(double targetLat, double targetLon, int N, TimeZoneFilter filter) {
        if (N <= 0) {
            throw new IllegalArgumentException("N must be greater than 0");
        }
        if (root == null) {
            return Collections.emptyList();
        }

        // Use a PriorityQueue as a Max-Heap to store the N-best results
        // We give it Collections.reverseOrder() so the *farthest* distance is at the top (peek)
        PriorityQueue<ProximityResult> maxHeap = new PriorityQueue<>(N, Collections.reverseOrder());

        // Start recursive search
        findNearestNRecursive(root, targetLat, targetLon, N, filter, 0, maxHeap);

        // --- Process Results ---
        // The heap now contains the N-best results, but in Max-Heap order.
        // We drain it into a LinkedList to reverse it (now Min-Heap order / sorted by distance)
        LinkedList<ProximityResult> sortedResults = new LinkedList<>();
        while (!maxHeap.isEmpty()) {
            sortedResults.addFirst(maxHeap.poll());
        }

        // Flatten the list of ProximityResult into a list of Station
        List<Station> finalStationList = new ArrayList<>();
        for (ProximityResult pr : sortedResults) {
            // Add all stations from this location
            // They are already pre-sorted by name (from USEI07 build)
            finalStationList.addAll(pr.stations());
        }

        return finalStationList;
    }

    /**
     * Recursive helper for the k-NN search.
     *
     * @param node      Current node to check
     * @param targetLat Target latitude
     * @param targetLon Target longitude
     * @param N         Number of neighbors to find
     * @param filter    Optional time zone filter
     * @param depth     Current depth in the tree
     * @param maxHeap   The Max-Heap (size N) holding the N-best results found so far
     */
    private void findNearestNRecursive(Node node, double targetLat, double targetLon,
                                       int N, TimeZoneFilter filter, int depth,
                                       PriorityQueue<ProximityResult> maxHeap) {
        if (node == null) {
            return;
        }

        // --- 1. Check current node ---
        boolean passesFilter = (filter == null || filter.matches(node.stations.get(0)));

        if (passesFilter) {
            double dist = haversineDistance(targetLat, targetLon, node.latitude, node.longitude);

            if (maxHeap.size() < N) {
                // Heap is not full, just add the new result
                maxHeap.add(new ProximityResult(dist, node.stations));
            } else if (dist < maxHeap.peek().distance()) {
                // Heap is full, but this node is CLOSER than the FARTHEST one in the heap
                // Remove the farthest, add this new one
                maxHeap.poll(); // Remove farthest
                maxHeap.add(new ProximityResult(dist, node.stations)); // Add new
            }
        }

        // --- 2. Determine "good" (closer) and "bad" (farther) subtrees ---
        boolean splitByLatitude = (depth % 2 == 0);
        Node goodSubtree, badSubtree;
        double diff; // Distance from target to the node's coordinate on the splitting axis

        if (splitByLatitude) {
            diff = targetLat - node.latitude;
        } else {
            diff = targetLon - node.longitude;
        }

        if (diff < 0) {
            goodSubtree = node.left;
            badSubtree = node.right;
        } else {
            goodSubtree = node.right;
            badSubtree = node.left;
        }

        // --- 3. Visit "good" subtree ---
        findNearestNRecursive(goodSubtree, targetLat, targetLon, N, filter, depth + 1, maxHeap);

        // --- 4. Pruning: Check if "bad" subtree is worth visiting ---
        double distToSplittingPlane;
        if (splitByLatitude) {
            // Distance from target to the latitude splitting line
            distToSplittingPlane = haversineDistance(targetLat, targetLon, node.latitude, targetLon);
        } else {
            // Distance from target to the longitude splitting line
            distToSplittingPlane = haversineDistance(targetLat, targetLon, targetLat, node.longitude);
        }

        if (maxHeap.size() < N || distToSplittingPlane < maxHeap.peek().distance()) {
            // The bad subtree *could* contain a point closer than our current-farthest.
            // We must explore it.
            findNearestNRecursive(badSubtree, targetLat, targetLon, N, filter, depth + 1, maxHeap);
        }
    }
}