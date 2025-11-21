package LAPR3.DTO;

import java.time.LocalDateTime;

/**
 * Represents when a train occupies a specific segment
 * Part of USLP07 - Subtask #67
 */
public class TrainSegmentOccupancy implements Comparable<TrainSegmentOccupancy> {
    private final String trainId;
    private final int segmentId;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final boolean isSingleTrack;

    /**
     * Creates a train segment occupancy record
     *
     * @param trainId train identifier
     * @param segmentId segment identifier
     * @param entryTime when train enters segment
     * @param exitTime when train exits segment
     * @param isSingleTrack whether segment is single-track
     */
    public TrainSegmentOccupancy(String trainId, int segmentId,
                                 LocalDateTime entryTime, LocalDateTime exitTime,
                                 boolean isSingleTrack) {
        this.trainId = trainId;
        this.segmentId = segmentId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.isSingleTrack = isSingleTrack;
    }

    public String getTrainId() {
        return trainId;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public boolean isSingleTrack() {
        return isSingleTrack;
    }

    /**
     * Checks if this occupancy overlaps with another
     * @param other another occupancy
     * @return true if they overlap in time
     */
    public boolean overlapsWith(TrainSegmentOccupancy other) {
        if (this.segmentId != other.segmentId) {
            return false;
        }

        return !(this.exitTime.isBefore(other.entryTime) ||
                this.entryTime.isAfter(other.exitTime));
    }

    /**
     * Gets occupancy duration in minutes
     * @return duration in minutes
     */
    public double getDurationMinutes() {
        return java.time.Duration.between(entryTime, exitTime).toMinutes();
    }

    @Override
    public int compareTo(TrainSegmentOccupancy other) {
        // Sort by segment, then by entry time
        if (this.segmentId != other.segmentId) {
            return Integer.compare(this.segmentId, other.segmentId);
        }
        return this.entryTime.compareTo(other.entryTime);
    }

    @Override
    public String toString() {
        return String.format("Train %s on Segment %d: %s → %s (%.1fmin) [%s]",
                trainId, segmentId,
                entryTime.toLocalTime(), exitTime.toLocalTime(),
                getDurationMinutes(),
                isSingleTrack ? "single-track" : "multi-track");
    }
}