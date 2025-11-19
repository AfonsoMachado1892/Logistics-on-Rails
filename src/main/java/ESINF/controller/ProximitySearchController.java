package ESINF.controller;

import ESINF.model.KDTree2D;
import ESINF.model.Station;
import ESINF.model.TimeZoneFilter;
import java.util.List;

public class ProximitySearchController {

    private final KDTree2D kdTree;

    /**
     * Creates a new ProximitySearchController.
     *
     * @param kdTree The 2D-Tree to search (must be built)
     * @throws IllegalArgumentException if kdTree is null
     * @throws IllegalStateException if kdTree is not built (root is null)
     */
    public ProximitySearchController(KDTree2D kdTree) {
        if (kdTree == null) {
            throw new IllegalArgumentException("KDTree cannot be null");
        }
        if (kdTree.getRoot() == null) {
            throw new IllegalStateException("KDTree must be built before searching");
        }
        this.kdTree = kdTree;
    }

    /**
     * Searches for the N nearest stations to a target coordinate.
     *
     * @param lat    Target latitude
     * @param lon    Target longitude
     * @param N      The number of nearest *locations* to find
     * @param filter Optional filter (null for no filtering)
     * @return Search result with stations and statistics
     */
    public SearchResult searchNearest(double lat, double lon, int N, TimeZoneFilter filter) {
        long startTime = System.nanoTime();

        List<Station> stations = kdTree.findNearestNStations(lat, lon, N, filter);

        long endTime = System.nanoTime();
        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        return new SearchResult(lat, lon, N, filter, stations, elapsedMs);
    }

    /**
     * Result of a proximity search operation.
     */
    public static class SearchResult {
        private final double targetLat;
        private final double targetLon;
        private final int N;
        private final TimeZoneFilter filter;
        private final List<Station> stations;
        private final double executionTimeMs;

        public SearchResult(double targetLat, double targetLon, int n,
                            TimeZoneFilter filter, List<Station> stations, double executionTimeMs) {
            this.targetLat = targetLat;
            this.targetLon = targetLon;
            this.N = n;
            this.filter = filter;
            this.stations = stations;
            this.executionTimeMs = executionTimeMs;
        }

        // Getters
        public double getTargetLat() { return targetLat; }
        public double getTargetLon() { return targetLon; }
        public int getN() { return N; }
        public TimeZoneFilter getFilter() { return filter; }
        public List<Station> getStations() { return stations; }
        public int getStationCount() { return stations.size(); }
        public double getExecutionTimeMs() { return executionTimeMs; }

        public boolean hasFilter() {
            return filter != null && filter.hasFilter();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Proximity Search Result ===\n");
            sb.append(String.format("Target: (%.5f, %.5f)\n", targetLat, targetLon));
            sb.append(String.format("N: %d\n", N));
            sb.append(String.format("Filter: %s\n", filter != null ? filter : "None"));
            sb.append(String.format("Results: %d stations\n", stations.size()));
            sb.append(String.format("Execution time: %.3f ms\n", executionTimeMs));

            // Print top results
            sb.append("\n--- Nearest Stations ---\n");
            int count = 0;
            for (Station s : stations) {
                if (count >= 10) break; // Limit print to top 10
                sb.append(String.format("  %d. %s (%s, %s)\n",
                        count + 1, s.getName(), s.getCountry(), s.getTimeZoneGroup()));
                count++;
            }
            if (stations.size() > 10) {
                sb.append(String.format("  ... and %d more ...\n", stations.size() - 10));
            }

            return sb.toString();
        }
    }
}