package LAPR3.DTO;

import java.time.LocalDateTime;

/**
 * Represents a potential crossing conflict between two trains
 * Part of USLP07 - Subtask #67
 */
public class CrossingConflict {
    private final String train1Id;
    private final String train2Id;
    private final int segmentId;
    private final LocalDateTime train1Entry;
    private final LocalDateTime train1Exit;
    private final LocalDateTime train2Entry;
    private final LocalDateTime train2Exit;
    private final ConflictType conflictType;

    /**
     * Type of conflict
     */
    public enum ConflictType {
        HEAD_ON,          // Trains approaching from opposite directions
        SAME_DIRECTION,   // Slower train ahead of faster train
        OVERTAKING        // Faster train catching up
    }

    /**
     * Creates a crossing conflict
     *
     * @param train1Id first train identifier
     * @param train2Id second train identifier
     * @param segmentId segment where conflict occurs
     * @param train1Entry train 1 entry time
     * @param train1Exit train 1 exit time
     * @param train2Entry train 2 entry time
     * @param train2Exit train 2 exit time
     * @param conflictType type of conflict
     */
    public CrossingConflict(String train1Id, String train2Id, int segmentId,
                            LocalDateTime train1Entry, LocalDateTime train1Exit,
                            LocalDateTime train2Entry, LocalDateTime train2Exit,
                            ConflictType conflictType) {
        this.train1Id = train1Id;
        this.train2Id = train2Id;
        this.segmentId = segmentId;
        this.train1Entry = train1Entry;
        this.train1Exit = train1Exit;
        this.train2Entry = train2Entry;
        this.train2Exit = train2Exit;
        this.conflictType = conflictType;
    }

    public String getTrain1Id() {
        return train1Id;
    }

    public String getTrain2Id() {
        return train2Id;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public LocalDateTime getTrain1Entry() {
        return train1Entry;
    }

    public LocalDateTime getTrain1Exit() {
        return train1Exit;
    }

    public LocalDateTime getTrain2Entry() {
        return train2Entry;
    }

    public LocalDateTime getTrain2Exit() {
        return train2Exit;
    }

    public ConflictType getConflictType() {
        return conflictType;
    }

    /**
     * Calculates overlap time in minutes
     * @return overlap duration in minutes
     */
    public double getOverlapMinutes() {
        LocalDateTime overlapStart = train1Entry.isAfter(train2Entry) ? train1Entry : train2Entry;
        LocalDateTime overlapEnd = train1Exit.isBefore(train2Exit) ? train1Exit : train2Exit;

        if (overlapStart.isBefore(overlapEnd)) {
            return java.time.Duration.between(overlapStart, overlapEnd).toMinutes();
        }
        return 0.0;
    }

    /**
     * Checks if this is a critical conflict (high overlap)
     * @return true if overlap is significant
     */
    public boolean isCritical() {
        return getOverlapMinutes() > 5.0; // More than 5 minutes overlap
    }

    @Override
    public String toString() {
        return String.format("CONFLICT on Segment %d: %s vs %s | Type: %s | Overlap: %.1fmin | %s → %s",
                segmentId, train1Id, train2Id, conflictType, getOverlapMinutes(),
                train1Entry.toLocalTime(), train1Exit.toLocalTime());
    }
}