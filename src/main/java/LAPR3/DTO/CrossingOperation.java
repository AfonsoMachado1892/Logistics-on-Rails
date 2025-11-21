package LAPR3.DTO;

import java.time.LocalDateTime;

/**
 * Represents a crossing operation where a train waits for another
 * Part of USLP07 - Subtask #68
 */
public class CrossingOperation {
    private final String waitingTrainId;
    private final String passingTrainId;
    private final int facilityId;
    private final String facilityName;
    private final int segmentId;
    private final LocalDateTime arrivalTime;
    private final LocalDateTime waitStartTime;
    private final LocalDateTime waitEndTime;
    private final LocalDateTime departureTime;
    private final CrossingReason reason;

    /**
     * Reason for crossing operation
     */
    public enum CrossingReason {
        HEAD_ON_CONFLICT,      // Trains approaching from opposite directions
        PRIORITY,              // Lower priority train waits
        SCHEDULE_OPTIMIZATION  // Strategic wait for better overall flow
    }

    /**
     * Creates a crossing operation
     *
     * @param waitingTrainId train that waits
     * @param passingTrainId train that passes
     * @param facilityId facility where wait occurs
     * @param facilityName facility name
     * @param segmentId conflicted segment ID
     * @param arrivalTime when waiting train arrives at facility
     * @param waitStartTime when wait begins
     * @param waitEndTime when passing train clears
     * @param departureTime when waiting train departs
     * @param reason reason for crossing
     */
    public CrossingOperation(String waitingTrainId, String passingTrainId,
                             int facilityId, String facilityName, int segmentId,
                             LocalDateTime arrivalTime, LocalDateTime waitStartTime,
                             LocalDateTime waitEndTime, LocalDateTime departureTime,
                             CrossingReason reason) {
        this.waitingTrainId = waitingTrainId;
        this.passingTrainId = passingTrainId;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.segmentId = segmentId;
        this.arrivalTime = arrivalTime;
        this.waitStartTime = waitStartTime;
        this.waitEndTime = waitEndTime;
        this.departureTime = departureTime;
        this.reason = reason;
    }

    public String getWaitingTrainId() {
        return waitingTrainId;
    }

    public String getPassingTrainId() {
        return passingTrainId;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalDateTime getWaitStartTime() {
        return waitStartTime;
    }

    public LocalDateTime getWaitEndTime() {
        return waitEndTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public CrossingReason getReason() {
        return reason;
    }

    /**
     * Gets total wait duration in minutes
     * @return wait duration
     */
    public double getWaitDurationMinutes() {
        return java.time.Duration.between(waitStartTime, waitEndTime).toMinutes();
    }

    /**
     * Gets total dwell time (including buffer) in minutes
     * @return dwell time
     */
    public double getTotalDwellMinutes() {
        return java.time.Duration.between(arrivalTime, departureTime).toMinutes();
    }

    @Override
    public String toString() {
        return String.format("CROSSING @ %s: Train %s waits %.1fmin for Train %s | Segment %d | %s | %s → %s",
                facilityName, waitingTrainId, getWaitDurationMinutes(), passingTrainId,
                segmentId, reason,
                arrivalTime.toLocalTime(), departureTime.toLocalTime());
    }
}