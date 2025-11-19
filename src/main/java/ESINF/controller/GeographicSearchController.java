package ESINF.controller;

import ESINF.model.*;
import java.util.List;

/**
 * USEI08 - Geographic Search Controller
 * Provides high-level API for geographic area searches with filtering.
 *
 * @author ESINF Team
 * @version 1.0
 */
public class GeographicSearchController {

    private final KDTree2D kdTree;

    /**
     * Creates a new GeographicSearchController.
     *
     * @param kdTree The 2D-Tree to search (must be built)
     * @throws IllegalArgumentException if kdTree is null
     */
    public GeographicSearchController(KDTree2D kdTree) {
        if (kdTree == null) {
            throw new IllegalArgumentException("KDTree cannot be null");
        }
        if (kdTree.getRoot() == null) {
            throw new IllegalArgumentException("KDTree must be built before searching");
        }
        this.kdTree = kdTree;
    }

    /**
     * Searches for all stations within a geographic area.
     *
     * @param latMin Minimum latitude
     * @param latMax Maximum latitude
     * @param lonMin Minimum longitude
     * @param lonMax Maximum longitude
     * @param filter Optional filter (null for no filtering)
     * @return Search result with stations and statistics
     */
    public SearchResult searchByGeographicArea(double latMin, double latMax,
                                               double lonMin, double lonMax,
                                               StationFilter filter) {
        // Validate and create region
        GeographicRegion region = new GeographicRegion(latMin, latMax, lonMin, lonMax);

        // Perform search
        long startTime = System.nanoTime();
        List<Station> stations = kdTree.rangeSearchWithFilter(region, filter);
        long endTime = System.nanoTime();

        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        return new SearchResult(region, filter, stations, elapsedMs);
    }

    /**
     * Searches for all stations within a geographic area (no filter).
     *
     * @param latMin Minimum latitude
     * @param latMax Maximum latitude
     * @param lonMin Minimum longitude
     * @param lonMax Maximum longitude
     * @return Search result with stations and statistics
     */
    public SearchResult searchByGeographicArea(double latMin, double latMax,
                                               double lonMin, double lonMax) {
        return searchByGeographicArea(latMin, latMax, lonMin, lonMax, null);
    }

    /**
     * Searches for all stations within a geographic area using a region object.
     *
     * @param region The geographic region
     * @param filter Optional filter
     * @return Search result with stations and statistics
     */
    public SearchResult searchByGeographicArea(GeographicRegion region, StationFilter filter) {
        if (region == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }

        return searchByGeographicArea(
                region.getLatMin(), region.getLatMax(),
                region.getLonMin(), region.getLonMax(),
                filter
        );
    }

    /**
     * Result of a geographic search operation.
     */
    public static class SearchResult {
        private final GeographicRegion region;
        private final StationFilter filter;
        private final List<Station> stations;
        private final double executionTimeMs;

        public SearchResult(GeographicRegion region, StationFilter filter,
                            List<Station> stations, double executionTimeMs) {
            this.region = region;
            this.filter = filter;
            this.stations = stations;
            this.executionTimeMs = executionTimeMs;
        }

        public GeographicRegion getRegion() { return region; }
        public StationFilter getFilter() { return filter; }
        public List<Station> getStations() { return stations; }
        public int getStationCount() { return stations.size(); }
        public double getExecutionTimeMs() { return executionTimeMs; }

        public boolean hasFilter() {
            return filter != null && filter.hasFilters();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Geographic Search Result ===\n");
            sb.append(String.format("Region: %s\n", region));
            sb.append(String.format("Filter: %s\n", filter != null ? filter : "None"));
            sb.append(String.format("Results: %d stations\n", stations.size()));
            sb.append(String.format("Execution time: %.3f ms\n", executionTimeMs));
            return sb.toString();
        }
    }
}