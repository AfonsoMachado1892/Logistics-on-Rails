package LAPR3.DTO;

/**
 * DTO containing speed calculation results for a train on a segment
 * Part of USLP07 - Subtask #63
 */
public class SpeedCalculationResult {
    private final int segmentId;
    private final double maxSpeedKmh;
    private final SpeedLimitingFactor limitingFactor;
    private final String details;

    /**
     * Factors that can limit train speed
     */
    public enum SpeedLimitingFactor {
        LOCOMOTIVE_MAX_SPEED,      // Limited by locomotive operational speed
        POWER_TO_WEIGHT_RATIO,     // Limited by insufficient power
        SEGMENT_WEIGHT_LIMIT,      // Limited by track weight capacity
        SEGMENT_MAX_SPEED          // Limited by track speed limit (future)
    }

    /**
     * Creates a speed calculation result
     *
     * @param segmentId segment identifier
     * @param maxSpeedKmh calculated maximum speed in km/h
     * @param limitingFactor the factor that limits the speed
     * @param details explanation of the calculation
     */
    public SpeedCalculationResult(int segmentId, double maxSpeedKmh,
                                  SpeedLimitingFactor limitingFactor,
                                  String details) {
        this.segmentId = segmentId;
        this.maxSpeedKmh = maxSpeedKmh;
        this.limitingFactor = limitingFactor;
        this.details = details;
    }

    /**
     * Gets the segment identifier
     * @return segment ID
     */
    public int getSegmentId() {
        return segmentId;
    }

    /**
     * Gets the calculated maximum speed
     * @return max speed in km/h
     */
    public double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    /**
     * Gets the limiting factor
     * @return the factor that limited the speed
     */
    public SpeedLimitingFactor getLimitingFactor() {
        return limitingFactor;
    }

    /**
     * Gets detailed explanation
     * @return details string
     */
    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return String.format("Segment %d: %.1f km/h (limited by %s) - %s",
                segmentId, maxSpeedKmh, limitingFactor, details);
    }
}