package LAPR3.model;

import java.util.Objects;

public class Segment implements Comparable<Segment> {
    private final int id;
    private final int lineId;
    private final int order;
    private final boolean electrified;
    private final double maxWeightKgPerMeter;
    private final double lengthMeters;
    private final int numberTracks;

    /**
     * Creates a new Segment
     * @param id segment identifier
     * @param lineId parent line identifier
     * @param order segment order in the line
     * @param electrified whether the segment is electrified
     * @param maxWeightKgPerMeter maximum weight per meter in kg/m
     * @param lengthMeters segment length in meters
     * @param numberTracks number of tracks (1, 2, 4, etc.)
     */
    public Segment(int id, int lineId, int order, boolean electrified,
                   double maxWeightKgPerMeter, double lengthMeters, int numberTracks) {
        if (id < 0) {
            throw new IllegalArgumentException("Segment ID cannot be negative");
        }
        if (lineId < 0) {
            throw new IllegalArgumentException("Line ID cannot be negative");
        }
        if (order <= 0) {
            throw new IllegalArgumentException("Order must be positive");
        }
        if (maxWeightKgPerMeter <= 0) {
            throw new IllegalArgumentException("Max weight must be positive");
        }
        if (lengthMeters <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (numberTracks <= 0) {
            throw new IllegalArgumentException("Number of tracks must be positive");
        }

        this.id = id;
        this.lineId = lineId;
        this.order = order;
        this.electrified = electrified;
        this.maxWeightKgPerMeter = maxWeightKgPerMeter;
        this.lengthMeters = lengthMeters;
        this.numberTracks = numberTracks;
    }

    /**
     * Gets the segment identifier
     *
     * @return the segment ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the parent line identifier
     *
     * @return the line ID
     */
    public int getLineId() {
        return lineId;
    }

    /**
     * Gets the segment order in the line
     *
     * @return the segment order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Checks if the segment is electrified
     *
     * @return true if electrified
     */
    public boolean isElectrified() {
        return electrified;
    }

    /**
     * Gets the maximum weight per meter in kg/m
     *
     * @return max weight in kg/m
     */
    public double getMaxWeightKgPerMeter() {
        return maxWeightKgPerMeter;
    }

    /**
     * Gets the segment length in meters
     *
     * @return length in meters
     */
    public double getLengthMeters() {
        return lengthMeters;
    }

    /**
     * Gets the number of tracks
     *
     * @return the number of tracks
     */
    public int getNumberTracks() {
        return numberTracks;
    }

    /**
     * Gets the segment length in kilometers
     *
     * @return length in kilometers
     */
    public double getLengthKilometers() {
        return lengthMeters / 1000.0;
    }

    /**
     * Compares this segment with another by order
     *
     * @param other the segment to compare to
     * @return a negative integer, zero, or a positive integer if this segment is less than, equal to, or greater than the other segment
     */
    @Override
    public int compareTo(Segment other) {
        // Compare by order for sorting segments of the same line
        return Integer.compare(this.order, other.order);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        return id == segment.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Segment{id=%d, line=%d, order=%d, length=%.0fm, tracks=%d, electrified=%s}",
                id, lineId, order, lengthMeters, numberTracks, electrified);
    }
}
