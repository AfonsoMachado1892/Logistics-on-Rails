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

}