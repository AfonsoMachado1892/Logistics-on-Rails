package LAPR3.DTO;

/**
 * Enhanced segment travel detail with acceleration profile
 * Extends basic segment info with realistic acceleration model
 * Part of USLP07 - Subtask #64
 */
public class EnhancedSegmentTravelDetail {
    private final int segmentId;
    private final int segmentOrder;
    private final double lengthMeters;
    private final double maxSpeedKmh;
    private final double timeSeconds;
    private final boolean electrified;
    private final int numberTracks;
    private final AccelerationProfile accelerationProfile;

    /**
     * Creates an enhanced segment travel detail
     *
     * @param segmentId segment identifier
     * @param segmentOrder segment order in the line
     * @param lengthMeters segment length in meters
     * @param maxSpeedKmh maximum speed on this segment in km/h
     * @param timeSeconds total travel time in seconds
     * @param electrified whether segment is electrified
     * @param numberTracks number of tracks (1=single, 2=double)
     * @param accelerationProfile detailed acceleration profile
     */
    public EnhancedSegmentTravelDetail(int segmentId, int segmentOrder, double lengthMeters,
                                       double maxSpeedKmh, double timeSeconds, boolean electrified,
                                       int numberTracks, AccelerationProfile accelerationProfile) {
        this.segmentId = segmentId;
        this.segmentOrder = segmentOrder;
        this.lengthMeters = lengthMeters;
        this.maxSpeedKmh = maxSpeedKmh;
        this.timeSeconds = timeSeconds;
        this.electrified = electrified;
        this.numberTracks = numberTracks;
        this.accelerationProfile = accelerationProfile;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public int getSegmentOrder() {
        return segmentOrder;
    }

    public double getLengthMeters() {
        return lengthMeters;
    }

    public double getLengthKilometers() {
        return lengthMeters / 1000.0;
    }

    public double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public double getTimeSeconds() {
        return timeSeconds;
    }

    public double getTimeMinutes() {
        return timeSeconds / 60.0;
    }

    public boolean isElectrified() {
        return electrified;
    }

    public int getNumberTracks() {
        return numberTracks;
    }

    public boolean isSingleTrack() {
        return numberTracks == 1;
    }

    public AccelerationProfile getAccelerationProfile() {
        return accelerationProfile;
    }

    /**
     * Gets average speed for this segment
     * @return average speed in km/h
     */
    public double getAverageSpeedKmh() {
        if (timeSeconds == 0) return 0;
        return (lengthMeters / 1000.0) / (timeSeconds / 3600.0);
    }

    @Override
    public String toString() {
        return String.format(
                "Segment %d (order=%d): %.2fkm in %.1fmin @ max %.1fkm/h (avg %.1fkm/h) [%s, %d-track]",
                segmentId, segmentOrder, getLengthKilometers(), getTimeMinutes(),
                maxSpeedKmh, getAverageSpeedKmh(),
                electrified ? "electrified" : "non-electrified", numberTracks
        );
    }

    /**
     * Gets detailed string with acceleration profile
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString()).append("\n");
        sb.append("  ").append(accelerationProfile.toString());
        return sb.toString();
    }
}