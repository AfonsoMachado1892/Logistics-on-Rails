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

    public int getId() {
        return id;
    }

    public int getLineId() {
        return lineId;
    }

    public int getOrder() {
        return order;
    }

    public boolean isElectrified() {
        return electrified;
    }

    public double getMaxWeightKgPerMeter() {
        return maxWeightKgPerMeter;
    }

    public double getLengthMeters() {
        return lengthMeters;
    }

    public int getNumberTracks() {
        return numberTracks;
    }

    public double getLengthKilometers() {
        return lengthMeters / 1000.0;
    }

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
