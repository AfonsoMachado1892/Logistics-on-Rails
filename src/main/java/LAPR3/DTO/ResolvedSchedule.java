package LAPR3.DTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Schedule with resolved crossing conflicts
 * Part of USLP07 - Subtask #68
 */
public class ResolvedSchedule {
    private final TrainScheduleResult originalSchedule;
    private final List<FacilityETA> adjustedETAs;
    private final List<CrossingOperation> crossingOperations;
    private final double totalDelayMinutes;

    /**
     * Creates a resolved schedule
     *
     * @param originalSchedule original schedule without conflicts
     * @param adjustedETAs adjusted facility ETAs with crossing waits
     * @param crossingOperations list of crossing operations
     * @param totalDelayMinutes total delay caused by crossings
     */
    public ResolvedSchedule(TrainScheduleResult originalSchedule,
                            List<FacilityETA> adjustedETAs,
                            List<CrossingOperation> crossingOperations,
                            double totalDelayMinutes) {
        this.originalSchedule = originalSchedule;
        this.adjustedETAs = adjustedETAs;
        this.crossingOperations = crossingOperations;
        this.totalDelayMinutes = totalDelayMinutes;
    }

    public TrainScheduleResult getOriginalSchedule() {
        return originalSchedule;
    }

    public List<FacilityETA> getAdjustedETAs() {
        return Collections.unmodifiableList(adjustedETAs);
    }

    public List<CrossingOperation> getCrossingOperations() {
        return Collections.unmodifiableList(crossingOperations);
    }

    public double getTotalDelayMinutes() {
        return totalDelayMinutes;
    }

    /**
     * Gets adjusted arrival time at destination
     * @return arrival time with delays
     */
    public LocalDateTime getAdjustedArrivalTime() {
        return adjustedETAs.get(adjustedETAs.size() - 1).getArrivalTime();
    }

    /**
     * Gets original arrival time (without conflicts)
     * @return original arrival time
     */
    public LocalDateTime getOriginalArrivalTime() {
        return originalSchedule.getArrivalTime();
    }

    /**
     * Checks if schedule was modified
     * @return true if crossing operations were added
     */
    public boolean hasModifications() {
        return !crossingOperations.isEmpty();
    }

    /**
     * Gets number of crossing operations
     * @return crossing count
     */
    public int getCrossingCount() {
        return crossingOperations.size();
    }

    @Override
    public String toString() {
        if (!hasModifications()) {
            return String.format("Schedule %s: No conflicts - proceed as planned",
                    originalSchedule.getScheduleId());
        }

        return String.format("Schedule %s: %d crossing operations, %.1fmin total delay | Original: %s → Adjusted: %s",
                originalSchedule.getScheduleId(), getCrossingCount(), totalDelayMinutes,
                getOriginalArrivalTime().toLocalTime(), getAdjustedArrivalTime().toLocalTime());
    }

    /**
     * Gets detailed schedule with crossing operations
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(100)).append("\n");
        sb.append(String.format("RESOLVED SCHEDULE: %s (Train %s)\n",
                originalSchedule.getScheduleId(), originalSchedule.getTrainId()));
        sb.append("=".repeat(100)).append("\n\n");

        if (!hasModifications()) {
            sb.append("✓ No conflicts detected - train can proceed as originally scheduled\n");
            return sb.toString();
        }

        sb.append(String.format("⚠ Crossing operations required: %d\n", getCrossingCount()));
        sb.append(String.format("Total delay: %.1f minutes\n", totalDelayMinutes));
        sb.append(String.format("Original arrival: %s\n", getOriginalArrivalTime()));
        sb.append(String.format("Adjusted arrival: %s\n\n", getAdjustedArrivalTime()));

        sb.append("Crossing Operations:\n");
        sb.append("-".repeat(100)).append("\n");
        for (CrossingOperation op : crossingOperations) {
            sb.append(op.toString()).append("\n");
        }
        sb.append("\n");

        sb.append("Adjusted Facility ETAs:\n");
        sb.append("-".repeat(100)).append("\n");
        for (FacilityETA eta : adjustedETAs) {
            sb.append(eta.toString()).append("\n");
        }

        return sb.toString();
    }
}