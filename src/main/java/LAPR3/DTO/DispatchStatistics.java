package LAPR3.DTO;

/**
 * Statistics for a dispatch plan
 * Part of USLP07 - Subtask #69
 */
public class DispatchStatistics {
    private final int totalSchedules;
    private final int schedulesWithConflicts;
    private final int schedulesConflictFree;
    private final int totalCrossingOperations;
    private final double totalDelayMinutes;
    private final double averageDelayPerTrain;
    private final double maxDelayMinutes;
    private final String mostDelayedTrain;
    private final int totalSingleTrackSegments;
    private final int conflictedSingleTrackSegments;

    /**
     * Creates dispatch statistics
     */
    public DispatchStatistics(int totalSchedules, int schedulesWithConflicts,
                              int schedulesConflictFree, int totalCrossingOperations,
                              double totalDelayMinutes, double averageDelayPerTrain,
                              double maxDelayMinutes, String mostDelayedTrain,
                              int totalSingleTrackSegments, int conflictedSingleTrackSegments) {
        this.totalSchedules = totalSchedules;
        this.schedulesWithConflicts = schedulesWithConflicts;
        this.schedulesConflictFree = schedulesConflictFree;
        this.totalCrossingOperations = totalCrossingOperations;
        this.totalDelayMinutes = totalDelayMinutes;
        this.averageDelayPerTrain = averageDelayPerTrain;
        this.maxDelayMinutes = maxDelayMinutes;
        this.mostDelayedTrain = mostDelayedTrain;
        this.totalSingleTrackSegments = totalSingleTrackSegments;
        this.conflictedSingleTrackSegments = conflictedSingleTrackSegments;
    }

    public int getTotalSchedules() {
        return totalSchedules;
    }

    public int getSchedulesWithConflicts() {
        return schedulesWithConflicts;
    }

    public int getSchedulesConflictFree() {
        return schedulesConflictFree;
    }

    public int getTotalCrossingOperations() {
        return totalCrossingOperations;
    }

    public double getTotalDelayMinutes() {
        return totalDelayMinutes;
    }

    public double getAverageDelayPerTrain() {
        return averageDelayPerTrain;
    }

    public double getMaxDelayMinutes() {
        return maxDelayMinutes;
    }

    public String getMostDelayedTrain() {
        return mostDelayedTrain;
    }

    public int getTotalSingleTrackSegments() {
        return totalSingleTrackSegments;
    }

    public int getConflictedSingleTrackSegments() {
        return conflictedSingleTrackSegments;
    }

    /**
     * Gets conflict resolution efficiency
     * @return percentage of schedules that are conflict-free
     */
    public double getConflictFreePercentage() {
        if (totalSchedules == 0) return 100.0;
        return (schedulesConflictFree * 100.0) / totalSchedules;
    }

    /**
     * Gets single-track utilization
     * @return percentage of single-track segments with conflicts
     */
    public double getSingleTrackConflictRate() {
        if (totalSingleTrackSegments == 0) return 0.0;
        return (conflictedSingleTrackSegments * 100.0) / totalSingleTrackSegments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DISPATCH STATISTICS\n");
        sb.append("-".repeat(120)).append("\n");
        sb.append(String.format("Total Schedules: %d\n", totalSchedules));
        sb.append(String.format("  - With Conflicts: %d (%.1f%%)\n",
                schedulesWithConflicts, 100.0 - getConflictFreePercentage()));
        sb.append(String.format("  - Conflict-Free: %d (%.1f%%)\n",
                schedulesConflictFree, getConflictFreePercentage()));
        sb.append(String.format("\nCrossing Operations: %d\n", totalCrossingOperations));
        sb.append(String.format("\nDelay Statistics:\n"));
        sb.append(String.format("  - Total Delay: %.1f minutes\n", totalDelayMinutes));
        sb.append(String.format("  - Average per Train: %.1f minutes\n", averageDelayPerTrain));
        sb.append(String.format("  - Maximum Delay: %.1f minutes (Train %s)\n",
                maxDelayMinutes, mostDelayedTrain != null ? mostDelayedTrain : "N/A"));
        sb.append(String.format("\nSingle-Track Analysis:\n"));
        sb.append(String.format("  - Total Single-Track Segments: %d\n", totalSingleTrackSegments));
        sb.append(String.format("  - Conflicted Segments: %d (%.1f%%)\n",
                conflictedSingleTrackSegments, getSingleTrackConflictRate()));

        return sb.toString();
    }
}