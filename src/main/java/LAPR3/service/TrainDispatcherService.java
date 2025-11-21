package LAPR3.service;

import LAPR3.DTO.*;
import LAPR3.model.Schedule;
import LAPR3.model.Segment;
import LAPR3.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Complete train dispatcher service
 * Orchestrates schedule calculation, conflict detection, and resolution
 * Part of USLP07 - Subtask #69
 */
public class TrainDispatcherService {
    private final ScheduleCalculationService scheduleService;
    private final CrossingConflictDetectionService conflictService;
    private final CrossingResolutionService resolutionService;
    private final ScheduleRepository scheduleRepository;
    private final SegmentRepository segmentRepository;

    public TrainDispatcherService(ScheduleCalculationService scheduleService,
                                  CrossingConflictDetectionService conflictService,
                                  CrossingResolutionService resolutionService,
                                  ScheduleRepository scheduleRepository,
                                  SegmentRepository segmentRepository) {
        this.scheduleService = scheduleService;
        this.conflictService = conflictService;
        this.resolutionService = resolutionService;
        this.scheduleRepository = scheduleRepository;
        this.segmentRepository = segmentRepository;
    }

    /**
     * Creates complete dispatch plan for given schedules
     *
     * @param scheduleIds list of schedule identifiers
     * @return complete dispatch plan with all schedules and crossing operations
     */
    public DispatchPlan createDispatchPlan(List<String> scheduleIds) {
        LocalDateTime generatedAt = LocalDateTime.now();

        // Validate all schedules exist
        validateSchedules(scheduleIds);

        // Resolve all conflicts
        Map<String, ResolvedSchedule> resolvedSchedules =
                resolutionService.resolveAllConflicts(scheduleIds);

        // Collect all crossing operations
        List<CrossingOperation> allCrossings = resolvedSchedules.values().stream()
                .flatMap(rs -> rs.getCrossingOperations().stream())
                .collect(Collectors.toList());

        // Calculate statistics
        DispatchStatistics statistics = calculateStatistics(resolvedSchedules, allCrossings);

        return new DispatchPlan(generatedAt, resolvedSchedules, allCrossings, statistics);
    }

    /**
     * Creates dispatch plan for all active schedules in a time window
     */
    public DispatchPlan createDispatchPlanForTimeWindow(LocalDateTime start, LocalDateTime end) {
        List<Schedule> schedulesInWindow = scheduleRepository.findByDateRange(start, end);

        List<String> scheduleIds = schedulesInWindow.stream()
                .filter(s -> s.getStatus() == Schedule.ScheduleStatus.SCHEDULED)
                .map(Schedule::getScheduleId)
                .collect(Collectors.toList());

        return createDispatchPlan(scheduleIds);
    }

    /**
     * Creates dispatch plan for all schedules on a specific date
     */
    public DispatchPlan createDailyDispatchPlan(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        return createDispatchPlanForTimeWindow(startOfDay, endOfDay);
    }

    /**
     * Validates that all schedules exist and are valid
     */
    private void validateSchedules(List<String> scheduleIds) {
        for (String scheduleId : scheduleIds) {
            if (!scheduleRepository.exists(scheduleId)) {
                throw new IllegalArgumentException("Schedule not found: " + scheduleId);
            }
        }
    }

    /**
     * Calculates dispatch statistics
     */
    private DispatchStatistics calculateStatistics(Map<String, ResolvedSchedule> resolvedSchedules,
                                                   List<CrossingOperation> allCrossings) {
        int totalSchedules = resolvedSchedules.size();

        int schedulesWithConflicts = (int) resolvedSchedules.values().stream()
                .filter(ResolvedSchedule::hasModifications)
                .count();

        int schedulesConflictFree = totalSchedules - schedulesWithConflicts;

        double totalDelay = resolvedSchedules.values().stream()
                .mapToDouble(ResolvedSchedule::getTotalDelayMinutes)
                .sum();

        double avgDelay = totalSchedules > 0 ? totalDelay / totalSchedules : 0;

        // Find most delayed train
        String mostDelayed = null;
        double maxDelay = 0;

        for (ResolvedSchedule rs : resolvedSchedules.values()) {
            if (rs.getTotalDelayMinutes() > maxDelay) {
                maxDelay = rs.getTotalDelayMinutes();
                mostDelayed = rs.getOriginalSchedule().getTrainId();
            }
        }

        // Count single-track segments
        List<Segment> allSegments = segmentRepository.findAll();
        int totalSingleTrack = (int) allSegments.stream()
                .filter(s -> s.getNumberTracks() == 1)
                .count();

        Set<Integer> conflictedSegments = allCrossings.stream()
                .map(CrossingOperation::getSegmentId)
                .collect(Collectors.toSet());

        int conflictedSingleTrack = conflictedSegments.size();

        return new DispatchStatistics(
                totalSchedules,
                schedulesWithConflicts,
                schedulesConflictFree,
                allCrossings.size(),
                totalDelay,
                avgDelay,
                maxDelay,
                mostDelayed,
                totalSingleTrack,
                conflictedSingleTrack
        );
    }

    /**
     * Gets dispatch plan with priority ordering
     * Orders schedules by departure time
     */
    public DispatchPlan createPrioritizedDispatchPlan(List<String> scheduleIds) {
        // Sort schedules by departure time
        List<String> sortedIds = scheduleIds.stream()
                .sorted((id1, id2) -> {
                    Schedule s1 = scheduleRepository.findById(id1).orElseThrow();
                    Schedule s2 = scheduleRepository.findById(id2).orElseThrow();
                    return s1.getDepartureDateTime().compareTo(s2.getDepartureDateTime());
                })
                .collect(Collectors.toList());

        return createDispatchPlan(sortedIds);
    }

    /**
     * Validates dispatch plan for safety
     * Ensures no remaining conflicts
     */
    public ValidationResult validateDispatchPlan(DispatchPlan plan) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check for unresolved conflicts
        List<CrossingConflict> remainingConflicts =
                conflictService.detectConflicts(new ArrayList<>(plan.getScheduleIds()));

        if (!remainingConflicts.isEmpty()) {
            errors.add(String.format("Unresolved conflicts detected: %d",
                    remainingConflicts.size()));
        }

        // Check for excessive delays
        double maxAcceptableDelay = 60.0; // 1 hour
        for (ResolvedSchedule rs : plan.getResolvedSchedules().values()) {
            if (rs.getTotalDelayMinutes() > maxAcceptableDelay) {
                warnings.add(String.format("Train %s has excessive delay: %.1f minutes",
                        rs.getOriginalSchedule().getTrainId(), rs.getTotalDelayMinutes()));
            }
        }

        // Check for crossing operation density
        if (plan.getAllCrossingOperations().size() > plan.getTotalTrains() * 2) {
            warnings.add("High number of crossing operations - consider schedule optimization");
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    /**
     * Exports dispatch plan to string format for logging/display
     */
    public String exportDispatchPlan(DispatchPlan plan) {
        return plan.toDetailedReport();
    }

    /**
     * Gets simplified dispatch summary
     */
    public String getDispatchSummary(DispatchPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("DISPATCH SUMMARY\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("Generated: %s\n", plan.getGeneratedAt()));
        sb.append(String.format("Trains: %d\n", plan.getTotalTrains()));
        sb.append(String.format("Crossings: %d\n", plan.getAllCrossingOperations().size()));
        sb.append(String.format("Status: %s\n",
                plan.isConflictFree() ? "NO CONFLICTS" : "CONFLICTS RESOLVED"));
        sb.append(String.format("Total Delay: %.1f minutes\n",
                plan.getStatistics().getTotalDelayMinutes()));

        return sb.toString();
    }

    /**
     * Validation result for dispatch plans
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Result: ").append(valid ? "VALID" : "INVALID").append("\n");

            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                errors.forEach(e -> sb.append("  - ").append(e).append("\n"));
            }

            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                warnings.forEach(w -> sb.append("  - ").append(w).append("\n"));
            }

            return sb.toString();
        }
    }
}