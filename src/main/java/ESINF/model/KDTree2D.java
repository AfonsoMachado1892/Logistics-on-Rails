package ESINF.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.nio.file.Paths;

/**
 * USEI07 - Build a balanced 2D-Tree on Latitude/Longitude.
 * <p>
 * A KD-Tree (k=2) implementation designed for efficient spatial indexing of train stations.
 * It partitions the space using alternating axes:
 * <ul>
 * <li>Even depths: Split by Latitude</li>
 * <li>Odd depths: Split by Longitude</li>
 * </ul>
 * <p>
 * This structure supports efficient range queries (USEI08) and Nearest-Neighbor searches (USEI09).
 * <p>
 * <strong>Duplicate Handling:</strong> When multiple stations share the exact same coordinates,
 * they are stored in a single node's bucket, sorted by station name in ascending order.
 */
public class KDTree2D {

    /**
     * Represents a node in the 2D-Tree.
     * <p>
     * Each node represents a split in the 2D plane and contains a "bucket" (list)
     * of stations that share the exact coordinate of this split.
     * </p>
     */
    public static class Node {
        /** Latitude coordinate of this node (splitting value for even depths). */
        public double latitude;

        /** Longitude coordinate of this node (splitting value for odd depths). */
        public double longitude;

        /**
         * List of stations at this exact coordinate.
         * Maintained in ascending order by station name.
         */
        public List<Station> stations;

        /** Left subtree (contains points "smaller" than this node on the current split axis). */
        public Node left;

        /** Right subtree (contains points "greater" or equal to this node on the current split axis). */
        public Node right;

        /**
         * Creates a new node with the given coordinates and initializes the bucket with the first station.
         *
         * @param latitude  The latitude of the node.
         * @param longitude The longitude of the node.
         * @param station   The initial station to store in this node.
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
         * Adds a station to this node's bucket.
         * <p>
         * After insertion, the bucket is re-sorted by station name to maintain consistency.
         * </p>
         *
         * @param station The station to add.
         */
        public void addStation(Station station) {
            stations.add(station);
            stations.sort(Comparator.comparing(Station::getName));
        }

        /**
         * Returns a string representation of the node.
         *
         * @return A formatted string containing coordinates and bucket size.
         */
        @Override
        public String toString() {
            return String.format("Node(%.5f, %.5f) - %d station(s)",
                    latitude, longitude, stations.size());
        }
    }

    /** Root node of the 2D-Tree. */
    private Node root;

    /** Total number of nodes in the tree. */
    private int size;

    /** Height of the tree. */
    private int height;

    /** Map tracking bucket sizes (Key: "lat,lon", Value: number of stations). */
    private Map<String, Integer> bucketSizes;

    /**
     * Constructs an empty 2D-Tree.
     * Initializes statistics to zero.
     */
    public KDTree2D() {
        this.root = null;
        this.size = 0;
        this.height = 0;
        this.bucketSizes = new HashMap<>();
    }

    /**
     * Builds a balanced 2D-Tree from pre-sorted BST/AVL lists of latitude and longitude.
     * <p>
     * <strong>Build Strategy:</strong>
     * <ol>
     * <li>Merges and deduplicates stations from both input lists.</li>
     * <li>Uses a recursive median partitioning strategy (bulk loading) to ensure the tree is perfectly balanced.</li>
     * <li>At each level, selects the median element of the current axis to be the root/split-point.</li>
     * </ol>
     *
     * @param latitudeBST  List of stations sorted by latitude (from the BST/AVL index).
     * @param longitudeBST List of stations sorted by longitude (from the BST/AVL index).
     * @return A {@link TreeStatistics} object containing metrics about the built tree (size, height, build time).
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
     * @param stations List of stations to insert.
     * @param depth    Current depth (0 = root, even = split by lat, odd = split by lon).
     * @return Root node of the subtree.
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
     *
     * @param node The current node.
     * @return The height of the subtree.
     */
    private int calculateHeight(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(calculateHeight(node.left), calculateHeight(node.right));
    }

    /**
     * Counts total nodes in the tree.
     *
     * @param node The current node.
     * @return The count of nodes.
     */
    private int countNodes(Node node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    /**
     * Calculates bucket sizes (number of stations per coordinate).
     *
     * @param node The current node.
     * @return A map of coordinate strings to station counts.
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
     *
     * @return A list of nodes in in-order sequence.
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
     * Calculates Haversine distance between two coordinates.
     *
     * @param lat1 Latitude of point 1.
     * @param lon1 Longitude of point 1.
     * @param lat2 Latitude of point 2.
     * @param lon2 Longitude of point 2.
     * @return Distance in kilometers.
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
     * Retrieves statistics about the tree structure and build process.
     *
     * @return A {@link TreeStatistics} object.
     */
    public TreeStatistics getStatistics() {
        return new TreeStatistics(size, height, bucketSizes, 0);
    }

    /**
     * Inner class to hold tree statistics.
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
         * Gets all distinct bucket sizes present in the tree.
         * @return Set of sizes.
         */
        public Set<Integer> getDistinctBucketSizes() {
            return new HashSet<>(bucketSizes.values());
        }

        /**
         * Gets frequency distribution of bucket sizes (how many nodes have X stations).
         * @return Map where Key=BucketSize, Value=Frequency.
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
     * Returns the root node (primarily for testing and visualization purposes).
     *
     * @return The root {@link Node}.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Exports the structure of the 2D-Tree to a CSV file in the 'Logs/' directory.
     * <p>
     * Uses a Pre-Order traversal to write nodes. The CSV contains depth, split axis,
     * coordinates, and the list of stations in the bucket.
     * </p>
     *
     * @param filename The name of the CSV file to create (e.g., "tree_structure.csv").
     * @return The total number of nodes exported.
     * @throws IOException If an error occurs during file creation or writing.
     */
    public int exportTreeToCsv(String filename) throws IOException {
        // CONSTRÓI O CAMINHO DO ARQUIVO DENTRO DA PASTA 'Logs/'
        String path = Paths.get("Logs", filename).toString();

        try (FileWriter writer = new FileWriter(path)) {
            // Write header
            writer.write("Depth,SplitAxis,Latitude,Longitude,BucketSize,StationNames\n");

            // Start recursive export from root
            return exportTreeToCsvRecursive(root, 0, writer);
        }
    }

    /**
     * Recursive helper for exporting the tree in Pre-Order.
     */
    private int exportTreeToCsvRecursive(Node node, int depth, FileWriter writer) throws IOException {
        if (node == null) return 0;

        String splitAxis = (depth % 2 == 0) ? "Latitude" : "Longitude";

        // Collect station names in the bucket
        StringBuilder stationNames = new StringBuilder();
        for (int i = 0; i < node.stations.size(); i++) {
            // Escape quotes for CSV compliance
            stationNames.append(node.stations.get(i).getName().replace("\"", "\"\""));
            if (i < node.stations.size() - 1) {
                stationNames.append("|"); // Separador para múltiplas estações
            }
        }

        // Write node data (coordenadas com 5 casas decimais para consistência)
        String line = String.format("%d,%s,%.5f,%.5f,%d,\"%s\"\n",
                depth,
                splitAxis,
                node.latitude,
                node.longitude,
                node.stations.size(),
                stationNames.toString());

        writer.write(line);

        int count = 1;
        count += exportTreeToCsvRecursive(node.left, depth + 1, writer);
        count += exportTreeToCsvRecursive(node.right, depth + 1, writer);

        return count;
    }


    /**
     * USEI08 - Range query with optional filtering.
     * <p>
     * Finds all stations located within a specific rectangular geographic region.
     * Supports filtering logic (e.g., by City, Country) to refine results.
     * </p>
     * <p>
     * <strong>Pruning Logic:</strong>
     * The algorithm effectively prunes the search space by checking if the search region
     * overlaps with the splitting planes of the current nodes, avoiding traversal of
     * irrelevant subtrees.
     * </p>
     *
     * @param region The {@link GeographicRegion} defining the bounds (lat/lon range).
     * @param filter An optional {@link StationFilter} (pass {@code null} for no filtering).
     * @return A list of {@link Station} objects matching the region and filter criteria.
     * @throws IllegalArgumentException if the region is null.
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
     * Recursive helper for range search with pruning.
     *
     * @param node    Current node being visited.
     * @param minLat  Minimum latitude of search region.
     * @param maxLat  Maximum latitude of search region.
     * @param minLon  Minimum longitude of search region.
     * @param maxLon  Maximum longitude of search region.
     * @param filter  Optional filter criteria.
     * @param depth   Current depth in the tree.
     * @param result  Accumulator list for matching stations.
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
     * @return Time in nanoseconds.
     */
    public long getLastQueryTimeNanos() {
        return lastQueryTimeNanos;
    }

    /**
     * Gets the execution time of the last range search query in milliseconds.
     *
     * @return Time in milliseconds.
     */
    public double getLastQueryTimeMs() {
        return lastQueryTimeNanos / 1_000_000.0;
    }


    // ---  METHODS FOR USEI09 (Proximity Search) ---

    /**
     * USEI09 - Finds the N nearest stations to a target coordinate.
     * <p>
     * Uses a Nearest-Neighbor search algorithm on the KD-Tree.
     * Maintains a Max-Heap of size N to track the closest stations found so far.
     * </p>
     *
     * @param targetLat Target latitude.
     * @param targetLon Target longitude.
     * @param N         The number of nearest locations to find (must be > 0).
     * @param filter    Optional {@link TimeZoneFilter} (pass {@code null} for no filter).
     * @return A list of all stations from the N nearest locations, sorted by distance (closest first).
     * @throws IllegalArgumentException if N is less than or equal to 0.
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
     * Recursive helper for the k-Nearest Neighbors search.
     * <p>
     * <strong>Logic:</strong>
     * <ol>
     * <li>Checks distance from target to current node. Updates heap if closer.</li>
     * <li>Recursively searches the "good" side (closer to target) first.</li>
     * <li>Checks if the "bad" side intersects the hypersphere defined by the current kth-farthest distance.
     * If it intersects, searches the "bad" side as well.</li>
     * </ol>
     *
     * @param node      Current node to check.
     * @param targetLat Target latitude.
     * @param targetLon Target longitude.
     * @param N         Number of neighbors to find.
     * @param filter    Optional time zone filter.
     * @param depth     Current depth in the tree.
     * @param maxHeap   The Max-Heap (size N) holding the N-best results found so far.
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
    // --- METHODS FOR USEI10 (Radius Search + Density) ---

    /**
     * USEI10 - Radius Search with Density Summary
     */
    public RadiusSearchResult radiusSearchWithDensity(double targetLat,
                                                      double targetLon,
                                                      double radiusKm) {

        long startTime = System.nanoTime();

        TreeMap<RadiusSearchResult.StationKey, Station> tree = new TreeMap<>();
        Map<String, Integer> countryCount = new HashMap<>();
        Map<Boolean, Integer> cityCount = new HashMap<>();

        radiusSearchRecursive(root, targetLat, targetLon, radiusKm,
                0, tree, countryCount, cityCount);

        long endTime = System.nanoTime();

        RadiusSearchResult.DensitySummary density =
                new RadiusSearchResult.DensitySummary(
                        countryCount,
                        cityCount,
                        tree.size()
                );

        return new RadiusSearchResult(
                tree,
                density,
                targetLat,
                targetLon,
                radiusKm,
                endTime - startTime
        );
    }

    private void radiusSearchRecursive(Node node,
                                       double targetLat,
                                       double targetLon,
                                       double radiusKm,
                                       int depth,
                                       TreeMap<RadiusSearchResult.StationKey, Station> tree,
                                       Map<String, Integer> countryCount,
                                       Map<Boolean, Integer> cityCount) {

        if (node == null) return;

        double dist = haversineDistance(
                targetLat, targetLon,
                node.latitude, node.longitude
        );

        boolean insideRadius = dist <= radiusKm;

        if (insideRadius) {
            for (Station s : node.stations) {
                RadiusSearchResult.StationKey key =
                        new RadiusSearchResult.StationKey(dist, s);

                tree.put(key, s);

                countryCount.put(
                        s.getCountry(),
                        countryCount.getOrDefault(s.getCountry(), 0) + 1
                );

                cityCount.put(
                        s.isCity(),
                        cityCount.getOrDefault(s.isCity(), 0) + 1
                );
            }
        }

        boolean splitByLat = (depth % 2 == 0);
        double diff = splitByLat
                ? targetLat - node.latitude
                : targetLon - node.longitude;

        Node near = diff < 0 ? node.left : node.right;
        Node far  = diff < 0 ? node.right : node.left;

        radiusSearchRecursive(near, targetLat, targetLon,
                radiusKm, depth + 1, tree, countryCount, cityCount);

        double distToPlane = splitByLat
                ? haversineDistance(targetLat, targetLon, node.latitude, targetLon)
                : haversineDistance(targetLat, targetLon, targetLat, node.longitude);

        if (distToPlane <= radiusKm) {
            radiusSearchRecursive(far, targetLat, targetLon,
                    radiusKm, depth + 1, tree, countryCount, cityCount);
        }
    }

}
