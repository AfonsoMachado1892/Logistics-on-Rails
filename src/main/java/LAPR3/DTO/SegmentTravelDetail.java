package LAPR3.DTO;

/**
 * Represents detailed information about a segment during travel time calculation.
 * Contains segment properties, distance, speed, and time calculations.
 */
public class SegmentTravelDetail {
    private final int segmentId;
    private final int segmentOrder;
    private final double lengthMeters;
    private final double lengthKilometers;
    private final double speedKmh;
    private final double timeSeconds;
    private final double timeMinutes;
    private final boolean electrified;
    private final int numberTracks;

    /**
     * Constructs a new segment travel detail
     *
     * @param segmentId the segment identifier
     * @param segmentOrder the order of the segment in the line
     * @param lengthMeters the length of the segment in meters
     * @param speedKmh the speed in kilometers per hour
     * @param timeSeconds the travel time in seconds
     * @param electrified whether the segment is electrified
     * @param numberTracks the number of tracks
     */
    public SegmentTravelDetail(int segmentId, int segmentOrder, double lengthMeters,
                               double speedKmh, double timeSeconds,
                               boolean electrified, int numberTracks) {
        this.segmentId = segmentId;
        this.segmentOrder = segmentOrder;
        this.lengthMeters = lengthMeters;
        this.lengthKilometers = lengthMeters / 1000.0;
        this.speedKmh = speedKmh;
        this.timeSeconds = timeSeconds;
        this.timeMinutes = timeSeconds / 60.0;
        this.electrified = electrified;
        this.numberTracks = numberTracks;
    }

    /**
     * Gets the segment identifier
     *
     * @return the segment ID
     */
    public int getSegmentId() {
        return segmentId;
    }

    /**
     * Gets the segment order in the line
     *
     * @return the segment order
     */
    public int getSegmentOrder() {
        return segmentOrder;
    }

    /**
     * Gets the segment length in meters
     *
     * @return the length in meters
     */
    public double getLengthMeters() {
        return lengthMeters;
    }

    /**
     * Gets the segment length in kilometers
     *
     * @return the length in kilometers
     */
    public double getLengthKilometers() {
        return lengthKilometers;
    }

    /**
     * Gets the speed in kilometers per hour
     *
     * @return the speed in km/h
     */
    public double getSpeedKmh() {
        return speedKmh;
    }

    /**
     * Gets the travel time in seconds
     *
     * @return the time in seconds
     */
    public double getTimeSeconds() {
        return timeSeconds;
    }

    /**
     * Gets the travel time in minutes
     *
     * @return the time in minutes
     */
    public double getTimeMinutes() {
        return timeMinutes;
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
     * Gets the number of tracks
     *
     * @return the number of tracks
     */
    public int getNumberTracks() {
        return numberTracks;
    }

    /**
     * Format time as HH:MM:SS
     */
    public String getFormattedTime() {
        int hours = (int) (timeSeconds / 3600);
        int minutes = (int) ((timeSeconds % 3600) / 60);
        int seconds = (int) (timeSeconds % 60);

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public String toString() {
        return String.format("Segment %d: %.2f km @ %.1f km/h -> %s",
                segmentOrder, lengthKilometers, speedKmh, getFormattedTime());
    }
}
