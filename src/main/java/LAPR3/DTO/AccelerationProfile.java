package LAPR3.DTO;

/**
 * DTO representing the acceleration profile of a train on a segment
 * Contains the three phases: acceleration, constant speed, deceleration
 * Part of USLP07 - Subtask #64
 */
public class AccelerationProfile {
    private final double accelerationPhaseDistance;    // meters
    private final double accelerationPhaseTime;        // seconds
    private final double constantSpeedPhaseDistance;   // meters
    private final double constantSpeedPhaseTime;       // seconds
    private final double decelerationPhaseDistance;    // meters
    private final double decelerationPhaseTime;        // seconds
    private final double maxSpeedReached;              // km/h
    private final double accelerationRate;             // m/s²
    private final double decelerationRate;             // m/s²

    /**
     * Creates an acceleration profile
     *
     * @param accelerationPhaseDistance distance covered during acceleration (m)
     * @param accelerationPhaseTime time spent accelerating (s)
     * @param constantSpeedPhaseDistance distance at constant speed (m)
     * @param constantSpeedPhaseTime time at constant speed (s)
     * @param decelerationPhaseDistance distance covered during deceleration (m)
     * @param decelerationPhaseTime time spent decelerating (s)
     * @param maxSpeedReached maximum speed achieved (km/h)
     * @param accelerationRate acceleration rate (m/s²)
     * @param decelerationRate deceleration/braking rate (m/s²)
     */
    public AccelerationProfile(double accelerationPhaseDistance, double accelerationPhaseTime,
                               double constantSpeedPhaseDistance, double constantSpeedPhaseTime,
                               double decelerationPhaseDistance, double decelerationPhaseTime,
                               double maxSpeedReached, double accelerationRate,
                               double decelerationRate) {
        this.accelerationPhaseDistance = accelerationPhaseDistance;
        this.accelerationPhaseTime = accelerationPhaseTime;
        this.constantSpeedPhaseDistance = constantSpeedPhaseDistance;
        this.constantSpeedPhaseTime = constantSpeedPhaseTime;
        this.decelerationPhaseDistance = decelerationPhaseDistance;
        this.decelerationPhaseTime = decelerationPhaseTime;
        this.maxSpeedReached = maxSpeedReached;
        this.accelerationRate = accelerationRate;
        this.decelerationRate = decelerationRate;
    }

    public double getAccelerationPhaseDistance() {
        return accelerationPhaseDistance;
    }

    public double getAccelerationPhaseTime() {
        return accelerationPhaseTime;
    }

    public double getConstantSpeedPhaseDistance() {
        return constantSpeedPhaseDistance;
    }

    public double getConstantSpeedPhaseTime() {
        return constantSpeedPhaseTime;
    }

    public double getDecelerationPhaseDistance() {
        return decelerationPhaseDistance;
    }

    public double getDecelerationPhaseTime() {
        return decelerationPhaseTime;
    }

    public double getMaxSpeedReached() {
        return maxSpeedReached;
    }

    public double getAccelerationRate() {
        return accelerationRate;
    }

    public double getDecelerationRate() {
        return decelerationRate;
    }

    /**
     * Gets total time for the segment
     * @return total time in seconds
     */
    public double getTotalTime() {
        return accelerationPhaseTime + constantSpeedPhaseTime + decelerationPhaseTime;
    }

    /**
     * Gets total distance covered
     * @return total distance in meters
     */
    public double getTotalDistance() {
        return accelerationPhaseDistance + constantSpeedPhaseDistance + decelerationPhaseDistance;
    }

    /**
     * Checks if segment is too short to reach max speed
     * @return true if constant speed phase has zero distance
     */
    public boolean isShortSegment() {
        return constantSpeedPhaseDistance < 1.0; // Less than 1 meter
    }

    @Override
    public String toString() {
        return String.format(
                "Profile: Accel[%.0fm, %.1fs] → Constant[%.0fm, %.1fs @ %.1fkm/h] → Decel[%.0fm, %.1fs] | Total: %.1fs",
                accelerationPhaseDistance, accelerationPhaseTime,
                constantSpeedPhaseDistance, constantSpeedPhaseTime, maxSpeedReached,
                decelerationPhaseDistance, decelerationPhaseTime,
                getTotalTime()
        );
    }
}