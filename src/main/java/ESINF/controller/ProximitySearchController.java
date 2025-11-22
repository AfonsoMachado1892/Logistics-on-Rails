package ESINF.controller;

import ESINF.model.KDTree2D;
import ESINF.model.Station;
import ESINF.model.TimeZoneFilter;
import java.util.List;

/**
 * Controller for USEI09 - Proximity Search (Nearest-N Stations).
 * <p>
 * This class acts as the intermediary between the user interface and the {@link KDTree2D} model.
 * It orchestrates the search for the nearest N stations to a specific coordinate,
 * handles performance timing, and encapsulates the results.
 * </p>
 *
 */
public class ProximitySearchController {

    /** The spatial index used for searching. */
    private final KDTree2D kdTree;

    /**
     * Creates a new ProximitySearchController.
     * <p>
     * Validates that the provided KD-Tree is not null and has been populated (built)
     * before allowing the controller to be instantiated.
     * </p>
     *
     * @param kdTree The {@link KDTree2D} to search against.
     * @throws IllegalArgumentException if the provided {@code kdTree} is {@code null}.
     * @throws IllegalStateException if the provided {@code kdTree} is empty (root is null),
     * indicating it has not been built yet.
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
     * Executes a search for the N nearest stations to a target coordinate.
     * <p>
     * This method measures the execution time of the search operation for performance analysis.
     * </p>
     *
     * @param lat    The target latitude.
     * @param lon    The target longitude.
     * @param N      The number of nearest <em>locations</em> to find.
     * @param filter An optional {@link TimeZoneFilter} to restrict results (pass {@code null} for no filtering).
     * @return A {@link SearchResult} object containing the found stations and execution statistics.
     */
    public SearchResult searchNearest(double lat, double lon, int N, TimeZoneFilter filter) {
        long startTime = System.nanoTime();

        List<Station> stations = kdTree.findNearestNStations(lat, lon, N, filter);

        long endTime = System.nanoTime();
        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        return new SearchResult(lat, lon, N, filter, stations, elapsedMs);
    }

    /**
     * A Data Transfer Object (DTO) representing the result of a proximity search operation.
     * <p>
     * Encapsulates the input parameters (query context), the output (list of stations),
     * and performance metrics (execution time).
     * </p>
     */
    public static class SearchResult {
        private final double targetLat;
        private final double targetLon;
        private final int N;
        private final TimeZoneFilter filter;
        private final List<Station> stations;
        private final double executionTimeMs;

        /**
         * Constructs a new SearchResult.
         *
         * @param targetLat       The latitude used in the search query.
         * @param targetLon       The longitude used in the search query.
         * @param n               The N limit used in the search query.
         * @param filter          The filter applied (if any).
         * @param stations        The list of stations found.
         * @param executionTimeMs The time taken to execute the search in milliseconds.
         */
        public SearchResult(double targetLat, double targetLon, int n,
                            TimeZoneFilter filter, List<Station> stations, double executionTimeMs) {
            this.targetLat = targetLat;
            this.targetLon = targetLon;
            this.N = n;
            this.filter = filter;
            this.stations = stations;
            this.executionTimeMs = executionTimeMs;
        }

        // region Getters

        /**
         * Gets the target latitude used for the search.
         * @return the latitude.
         */
        public double getTargetLat() { return targetLat; }

        /**
         * Gets the target longitude used for the search.
         * @return the longitude.
         */
        public double getTargetLon() { return targetLon; }

        /**
         * Gets the requested number of nearest neighbors (N).
         * @return N.
         */
        public int getN() { return N; }

        /**
         * Gets the filter used in the search.
         * @return The filter object, or null if none was used.
         */
        public TimeZoneFilter getFilter() { return filter; }

        /**
         * Gets the list of stations found by the search.
         * @return A list of stations sorted by distance from the target.
         */
        public List<Station> getStations() { return stations; }

        /**
         * Gets the total count of stations found.
         * @return The number of stations in the result list.
         */
        public int getStationCount() { return stations.size(); }

        /**
         * Gets the execution time of the search algorithm.
         * @return Time in milliseconds.
         */
        public double getExecutionTimeMs() { return executionTimeMs; }

        // endregion

        /**
         * Checks if a filter was applied during this search.
         *
         * @return {@code true} if a valid filter was present; {@code false} otherwise.
         */
        public boolean hasFilter() {
            return filter != null && filter.hasFilter();
        }

        /**
         * Returns a formatted summary of the search results.
         * <p>
         * Includes query parameters, performance stats, and a preview of the top 10 nearest stations.
         * </p>
         *
         * @return A string representation of the result.
         */
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