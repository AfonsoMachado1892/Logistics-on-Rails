package LAPR3.DTO;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Complete dispatch plan for multiple trains
 * Part of USLP07 - Subtask #69
 */
public class DispatchPlan {
    private final LocalDateTime generatedAt;
    private final Map<String, ResolvedSchedule> resolvedSchedules;
    private final List<CrossingOperation> allCrossingOperations;
    private final DispatchStatistics statistics;

    /**
     * Creates a dispatch plan
     *
     * @param generatedAt when plan was generated
     * @param resolvedSchedules map of schedule ID to resolved schedule
     * @param allCrossingOperations all crossing operations in the plan
     * @param statistics dispatch statistics
     */
    public DispatchPlan(LocalDateTime generatedAt,
                        Map<String, ResolvedSchedule> resolvedSchedules,
                        List<CrossingOperation> allCrossingOperations,
                        DispatchStatistics statistics) {
        this.generatedAt = generatedAt;
        this.resolvedSchedules = resolvedSchedules;
        this.allCrossingOperations = allCrossingOperations;
        this.statistics = statistics;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Map<String, ResolvedSchedule> getResolvedSchedules() {
        return Collections.unmodifiableMap(resolvedSchedules);
    }

    public List<CrossingOperation> getAllCrossingOperations() {
        return Collections.unmodifiableList(allCrossingOperations);
    }

    public DispatchStatistics getStatistics() {
        return statistics;
    }

    /**
     * Gets resolved schedule for a specific schedule ID
     */
    public ResolvedSchedule getSchedule(String scheduleId) {
        return resolvedSchedules.get(scheduleId);
    }

    /**
     * Gets all schedule IDs in the plan
     */
    public Set<String> getScheduleIds() {
        return resolvedSchedules.keySet();
    }

    /**
     * Gets crossing operations for a specific train
     */
    public List<CrossingOperation> getCrossingOperationsForTrain(String trainId) {
        return allCrossingOperations.stream()
                .filter(op -> op.getWaitingTrainId().equals(trainId))
                .toList();
    }

    /**
     * Checks if plan is conflict-free
     */
    public boolean isConflictFree() {
        return allCrossingOperations.isEmpty();
    }

    /**
     * Gets total number of trains in the plan
     */
    public int getTotalTrains() {
        return resolvedSchedules.size();
    }

    @Override
    public String toString() {
        return String.format("DispatchPlan[generated=%s, trains=%d, crossings=%d, conflicts=%s]",
                generatedAt, getTotalTrains(), allCrossingOperations.size(),
                isConflictFree() ? "NONE" : "RESOLVED");
    }

    /**
     * Gets comprehensive dispatch report
     */
    public String toDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(120)).append("\n");
        sb.append("DISPATCH PLAN - TRAIN SCHEDULER\n");
        sb.append("═".repeat(120)).append("\n");
        sb.append(String.format("Generated: %s\n", generatedAt));
        sb.append(String.format("Total Trains: %d\n", getTotalTrains()));
        sb.append(String.format("Crossing Operations: %d\n", allCrossingOperations.size()));
        sb.append(String.format("Status: %s\n\n", isConflictFree() ? "✓ NO CONFLICTS" : "⚠ CONFLICTS RESOLVED"));

        // Statistics
        sb.append(statistics.toString()).append("\n\n");

        // Schedule by schedule
        sb.append("═".repeat(120)).append("\n");
        sb.append("INDIVIDUAL TRAIN SCHEDULES\n");
        sb.append("═".repeat(120)).append("\n\n");

        List<String> sortedIds = new ArrayList<>(resolvedSchedules.keySet());
        Collections.sort(sortedIds);

        for (String scheduleId : sortedIds) {
            ResolvedSchedule resolved = resolvedSchedules.get(scheduleId);
            sb.append(resolved.toDetailedString()).append("\n");
        }

        // Crossing operations timeline
        if (!allCrossingOperations.isEmpty()) {
            sb.append("═".repeat(120)).append("\n");
            sb.append("CROSSING OPERATIONS TIMELINE\n");
            sb.append("═".repeat(120)).append("\n");

            List<CrossingOperation> sorted = new ArrayList<>(allCrossingOperations);
            sorted.sort(Comparator.comparing(CrossingOperation::getWaitStartTime));

            for (CrossingOperation op : sorted) {
                sb.append(op.toString()).append("\n");
            }
        }

        return sb.toString();
    }
}