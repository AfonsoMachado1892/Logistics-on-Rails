package ESINF.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a specific result found during a k-Nearest Neighbors (k-NN) search.
 * <p>
 * This immutable class encapsulates a calculated distance from a target coordinate
 * and the list of {@link Station} objects found at that exact distance (location).
 * </p>
 * <p>
 * It implements {@link Comparable} to define the natural ordering of results based
 * on distance (ascending), which is essential for maintaining the PriorityQueue
 * (Max-Heap) used in the {@link KDTree2D} search algorithm.
 * </p>
 *
 */
public final class ProximityResult implements Comparable<ProximityResult> {

    /** The calculated distance from the search target to this result's location. */
    private final double distance;

    /** The list of stations located at this specific distance/coordinate. */
    private final List<Station> stations;

    /**
     * Constructs a new ProximityResult.
     *
     * @param distance The distance from the target point (usually in kilometers).
     * @param stations The list of stations found at this distance.
     */
    public ProximityResult(double distance, List<Station> stations) {
        this.distance = distance;
        this.stations = stations;
    }

    // --- Accessor Methods ---

    /**
     * Gets the distance from the target coordinate.
     *
     * @return The distance value.
     */
    public double distance() {
        return distance;
    }

    /**
     * Gets the list of stations at this location.
     *
     * @return An unmodifiable list of stations.
     */
    public List<Station> stations() {
        return stations;
    }

    // --- Core Methods for k-NN Logic ---

    /**
     * Compares this result with another based on distance.
     * <p>
     * This defines the <strong>natural ordering</strong>: results with smaller distances
     * are considered "less than" results with larger distances.
     * </p>
     *
     * @param other The other {@code ProximityResult} to compare to.
     * @return A negative integer, zero, or a positive integer as this distance
     * is less than, equal to, or greater than the specified object's distance.
     */
    @Override
    public int compareTo(ProximityResult other) {
        return Double.compare(this.distance, other.distance);
    }

    // --- Standard Object Methods ---

    /**
     * Checks if this result is equal to another object.
     * Equality is determined by having the exact same distance and the same list of stations.
     *
     * @param o The object to compare.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProximityResult that)) return false;
        return Double.compare(distance, that.distance) == 0 &&
                Objects.equals(stations, that.stations);
    }

    /**
     * Generates a hash code for this result.
     *
     * @return The hash code value based on distance and stations.
     */
    @Override
    public int hashCode() {
        return Objects.hash(distance, stations);
    }

    /**
     * Returns a string representation of the result.
     *
     * @return A string containing the distance and the list of stations.
     */
    @Override
    public String toString() {
        return "ProximityResult[" +
                "distance=" + distance + ", " +
                "stations=" + stations + ']';
    }
}