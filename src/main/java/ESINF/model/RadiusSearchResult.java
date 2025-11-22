package ESINF.model;

import java.util.*;

/**
 * USEI10 - Radius search and density summary result.
 * <p>
 * Represents the result of a radius-based spatial query on the KD-Tree.
 * Contains both a sorted tree of stations by distance and a density summary
 * grouped by country and city status.
 * </p>
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>Stations are stored in a BST/AVL tree sorted by distance (ASC) and station name (DESC).</li>
 * <li>Provides density metrics: counts by country and by isCity status.</li>
 * <li>Tracks query execution time for performance analysis.</li>
 * </ul>
 *
 * @author ESINF Team
 * @version 1.0
 */
public class RadiusSearchResult {

    /**
     * Composite key for sorting stations in the result tree.
     * Sorts by distance (ascending), then by station name (descending).
     */
    public static class StationKey implements Comparable<StationKey> {
        private final double distance;
        private final String stationName;
        private final Station station;

        public StationKey(double distance, Station station) {
            this.distance = distance;
            this.stationName = station.getName();
            this.station = station;
        }

        public double getDistance() {
            return distance;
        }

        public String getStationName() {
            return stationName;
        }

        public Station getStation() {
            return station;
        }

        @Override
        public int compareTo(StationKey other) {
            // First by distance (ascending)
            int distCmp = Double.compare(this.distance, other.distance);
            if (distCmp != 0) return distCmp;

            // Then by station name (descending)
            return other.stationName.compareTo(this.stationName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StationKey)) return false;
            StationKey that = (StationKey) o;
            return Double.compare(that.distance, distance) == 0 &&
                    Objects.equals(stationName, that.stationName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(distance, stationName);
        }

        @Override
        public String toString() {
            return String.format("%.3f km - %s", distance, stationName);
        }
    }

    /**
     * Represents density summary statistics for the search result.
     */
    public static class DensitySummary {
        private final Map<String, Integer> countryDistribution;
        private final Map<Boolean, Integer> cityDistribution;
        private final int totalStations;

        public DensitySummary(Map<String, Integer> countryDistribution,
                              Map<Boolean, Integer> cityDistribution,
                              int totalStations) {
            this.countryDistribution = new TreeMap<>(countryDistribution);
            this.cityDistribution = cityDistribution;
            this.totalStations = totalStations;
        }

        public Map<String, Integer> getCountryDistribution() {
            return countryDistribution;
        }

        public Map<Boolean, Integer> getCityDistribution() {
            return cityDistribution;
        }

        public int getTotalStations() {
            return totalStations;
        }

        public int getCityCount() {
            return cityDistribution.getOrDefault(true, 0);
        }

        public int getNonCityCount() {
            return cityDistribution.getOrDefault(false, 0);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Density Summary ===\n");
            sb.append(String.format("Total Stations: %d\n", totalStations));
            sb.append("\nBy Country:\n");
            for (Map.Entry<String, Integer> entry : countryDistribution.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / totalStations;
                sb.append(String.format("  %s: %d (%.1f%%)\n",
                        entry.getKey(), entry.getValue(), percentage));
            }
            sb.append("\nBy City Status:\n");
            sb.append(String.format("  Cities: %d (%.1f%%)\n",
                    getCityCount(), (getCityCount() * 100.0) / totalStations));
            sb.append(String.format("  Non-Cities: %d (%.1f%%)\n",
                    getNonCityCount(), (getNonCityCount() * 100.0) / totalStations));
            return sb.toString();
        }
    }

    // Fields
    private final TreeMap<StationKey, Station> stationTree;
    private final DensitySummary densitySummary;
    private final double targetLat;
    private final double targetLon;
    private final double radius;
    private final long queryTimeNanos;

    /**
     * Constructs a new RadiusSearchResult.
     *
     * @param stationTree     BST/AVL tree of stations sorted by distance and name
     * @param densitySummary  Density summary statistics
     * @param targetLat       Target latitude
     * @param targetLon       Target longitude
     * @param radius          Search radius in kilometers
     * @param queryTimeNanos  Query execution time in nanoseconds
     */
    public RadiusSearchResult(TreeMap<StationKey, Station> stationTree,
                              DensitySummary densitySummary,
                              double targetLat,
                              double targetLon,
                              double radius,
                              long queryTimeNanos) {
        this.stationTree = stationTree;
        this.densitySummary = densitySummary;
        this.targetLat = targetLat;
        this.targetLon = targetLon;
        this.radius = radius;
        this.queryTimeNanos = queryTimeNanos;
    }

    /**
     * Gets the tree of stations sorted by distance (ASC) and name (DESC).
     *
     * @return TreeMap of station keys to stations
     */
    public TreeMap<StationKey, Station> getStationTree() {
        return stationTree;
    }

    /**
     * Gets all stations as a list in sorted order.
     *
     * @return List of stations sorted by distance and name
     */
    public List<Station> getStationList() {
        return new ArrayList<>(stationTree.values());
    }

    /**
     * Gets all station keys in sorted order.
     *
     * @return List of station keys
     */
    public List<StationKey> getStationKeys() {
        return new ArrayList<>(stationTree.keySet());
    }

    /**
     * Gets the density summary.
     *
     * @return Density summary object
     */
    public DensitySummary getDensitySummary() {
        return densitySummary;
    }

    /**
     * Gets the target latitude.
     *
     * @return Target latitude
     */
    public double getTargetLat() {
        return targetLat;
    }

    /**
     * Gets the target longitude.
     *
     * @return Target longitude
     */
    public double getTargetLon() {
        return targetLon;
    }

    /**
     * Gets the search radius.
     *
     * @return Radius in kilometers
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Gets the query execution time in nanoseconds.
     *
     * @return Query time in nanoseconds
     */
    public long getQueryTimeNanos() {
        return queryTimeNanos;
    }

    /**
     * Gets the query execution time in milliseconds.
     *
     * @return Query time in milliseconds
     */
    public double getQueryTimeMs() {
        return queryTimeNanos / 1_000_000.0;
    }

    /**
     * Gets the total number of stations found.
     *
     * @return Number of stations
     */
    public int getTotalStations() {
        return stationTree.size();
    }

    /**
     * Gets the closest station (minimum distance).
     *
     * @return The closest station, or null if no stations found
     */
    public Station getClosestStation() {
        if (stationTree.isEmpty()) return null;
        return stationTree.firstEntry().getValue();
    }

    /**
     * Gets the farthest station (maximum distance within radius).
     *
     * @return The farthest station, or null if no stations found
     */
    public Station getFarthestStation() {
        if (stationTree.isEmpty()) return null;
        return stationTree.lastEntry().getValue();
    }

    /**
     * Gets the minimum distance found.
     *
     * @return Minimum distance in km, or Double.MAX_VALUE if empty
     */
    public double getMinDistance() {
        if (stationTree.isEmpty()) return Double.MAX_VALUE;
        return stationTree.firstKey().getDistance();
    }

    /**
     * Gets the maximum distance found.
     *
     * @return Maximum distance in km, or 0 if empty
     */
    public double getMaxDistance() {
        if (stationTree.isEmpty()) return 0;
        return stationTree.lastKey().getDistance();
    }

    /**
     * Returns a comprehensive string representation of the search result.
     *
     * @return Formatted multi-line string with query details and results
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Radius Search Result ===\n");
        sb.append(String.format("Target: (%.5f, %.5f)\n", targetLat, targetLon));
        sb.append(String.format("Radius: %.2f km\n", radius));
        sb.append(String.format("Stations Found: %d\n", getTotalStations()));
        sb.append(String.format("Query Time: %.3f ms\n\n", getQueryTimeMs()));

        if (!stationTree.isEmpty()) {
            sb.append(String.format("Distance Range: %.3f - %.3f km\n\n",
                    getMinDistance(), getMaxDistance()));
        }

        sb.append(densitySummary.toString());

        sb.append("\n=== Station List (sorted by distance ASC, name DESC) ===\n");
        int count = 0;
        for (Map.Entry<StationKey, Station> entry : stationTree.entrySet()) {
            count++;
            StationKey key = entry.getKey();
            Station station = entry.getValue();
            sb.append(String.format("%3d. [%.3f km] %s (%s) %s\n",
                    count,
                    key.getDistance(),
                    station.getName(),
                    station.getCountry(),
                    station.isCity() ? "[City]" : ""));
        }

        return sb.toString();
    }

    /**
     * Returns a compact summary string.
     *
     * @return Brief summary of the search result
     */
    public String toSummaryString() {
        return String.format(
                "Radius Search: %d stations within %.2f km (%.3f ms) - %d cities, %d countries",
                getTotalStations(),
                radius,
                getQueryTimeMs(),
                densitySummary.getCityCount(),
                densitySummary.getCountryDistribution().size()
        );
    }
}