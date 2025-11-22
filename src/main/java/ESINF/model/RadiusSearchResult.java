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

}