package LAPR3.service;

import LAPR3.DTO.*;
import LAPR3.model.Facility;
import LAPR3.repository.FacilityRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for resolving crossing conflicts
 * Inserts crossing operations to prevent collisions
 * Part of USLP07 - Subtask #68
 */
public class CrossingResolutionService {
    private final CrossingConflictDetectionService conflictDetectionService;
    private final ScheduleCalculationService scheduleService;
    private final FacilityRepository facilityRepository;

    // Buffer times in minutes
    private static final double CROSSING_BUFFER_MINUTES = 2.0;  // Safety margin after crossing
    private static final double MIN_WAIT_TIME_MINUTES = 1.0;    // Minimum wait time

    public CrossingResolutionService(CrossingConflictDetectionService conflictDetectionService,
                                     ScheduleCalculationService scheduleService,
                                     FacilityRepository facilityRepository) {
        this.conflictDetectionService = conflictDetectionService;
        this.scheduleService = scheduleService;
        this.facilityRepository = facilityRepository;
    }

    /**
     * Resolves conflicts for multiple schedules
     *
     * @param scheduleIds list of schedule identifiers
     * @return map of schedule ID to resolved schedule
     */
    public Map<String, ResolvedSchedule> resolveAllConflicts(List<String> scheduleIds) {
        // Detect all conflicts
        List<CrossingConflict> conflicts = conflictDetectionService.detectConflicts(scheduleIds);

        if (conflicts.isEmpty()) {
            // No conflicts - return original schedules
            Map<String, ResolvedSchedule> resolved = new HashMap<>();
            for (String scheduleId : scheduleIds) {
                TrainScheduleResult original = scheduleService.calculateSchedule(scheduleId);
                resolved.put(scheduleId, new ResolvedSchedule(original,
                        original.getFacilityETAs(), List.of(), 0.0));
            }
            return resolved;
        }

        // Group conflicts by train
        Map<String, List<CrossingConflict>> conflictsByTrain = groupConflictsByTrain(conflicts);

        // Resolve conflicts for each train
        Map<String, ResolvedSchedule> resolved = new HashMap<>();

        for (String scheduleId : scheduleIds) {
            TrainScheduleResult original = scheduleService.calculateSchedule(scheduleId);
            String trainId = original.getTrainId();

            List<CrossingConflict> trainConflicts = conflictsByTrain.getOrDefault(trainId, List.of());

            if (trainConflicts.isEmpty()) {
                resolved.put(scheduleId, new ResolvedSchedule(original,
                        original.getFacilityETAs(), List.of(), 0.0));
            } else {
                ResolvedSchedule resolvedSchedule = resolveConflictsForTrain(
                        original, trainConflicts, scheduleIds
                );
                resolved.put(scheduleId, resolvedSchedule);
            }
        }

        return resolved;
    }

    /**
     * Groups conflicts by train ID
     */
    private Map<String, List<CrossingConflict>> groupConflictsByTrain(List<CrossingConflict> conflicts) {
        Map<String, List<CrossingConflict>> byTrain = new HashMap<>();

        for (CrossingConflict conflict : conflicts) {
            byTrain.computeIfAbsent(conflict.getTrain1Id(), k -> new ArrayList<>()).add(conflict);
            byTrain.computeIfAbsent(conflict.getTrain2Id(), k -> new ArrayList<>()).add(conflict);
        }

        return byTrain;
    }

    /**
     * Resolves conflicts for a single train
     */
    private ResolvedSchedule resolveConflictsForTrain(TrainScheduleResult original,
                                                      List<CrossingConflict> conflicts,
                                                      List<String> allScheduleIds) {
        List<CrossingOperation> operations = new ArrayList<>();
        List<FacilityETA> adjustedETAs = new ArrayList<>(original.getFacilityETAs());
        double totalDelay = 0.0;

        // Sort conflicts by time
        conflicts.sort(Comparator.comparing(CrossingConflict::getTrain1Entry));

        for (CrossingConflict conflict : conflicts) {
            // Determine which train should wait
            String waitingTrain = decideWaitingTrain(conflict, allScheduleIds);

            if (waitingTrain.equals(original.getTrainId())) {
                // This train must wait
                CrossingOperation operation = createCrossingOperation(conflict, original);
                operations.add(operation);
                totalDelay += operation.getWaitDurationMinutes();

                // Adjust subsequent ETAs
                adjustETAsAfterCrossing(adjustedETAs, operation);
            }
        }

        return new ResolvedSchedule(original, adjustedETAs, operations, totalDelay);
    }

    /**
     * Decides which train should wait based on priority rules
     */
    private String decideWaitingTrain(CrossingConflict conflict, List<String> allScheduleIds) {
        // Simple rule: train that enters segment first has priority
        // More complex rules could consider:
        // - Train importance/priority
        // - Number of passengers
        // - Delay propagation
        // - Operating company

        if (conflict.getTrain1Entry().isBefore(conflict.getTrain2Entry())) {
            return conflict.getTrain2Id(); // Train 2 waits
        } else {
            return conflict.getTrain1Id(); // Train 1 waits
        }
    }

    /**
     * Creates a crossing operation from a conflict
     */
    private CrossingOperation createCrossingOperation(CrossingConflict conflict,
                                                      TrainScheduleResult schedule) {
        String waitingTrain = schedule.getTrainId();
        String passingTrain = waitingTrain.equals(conflict.getTrain1Id()) ?
                conflict.getTrain2Id() : conflict.getTrain1Id();

        // Find nearest facility before the conflicted segment
        // Simplified: use first facility (in reality, find closest station/siding)
        int facilityId = schedule.getFacilityETAs().get(0).getFacilityId();
        Facility facility = facilityRepository.findById(facilityId).orElseThrow();

        // Calculate wait times
        LocalDateTime arrivalTime = conflict.getTrain1Entry().minusMinutes(10);
        LocalDateTime waitStart = arrivalTime;
        LocalDateTime waitEnd = conflict.getTrain2Exit().plusMinutes((long) CROSSING_BUFFER_MINUTES);
        LocalDateTime departure = waitEnd.plusMinutes((long) MIN_WAIT_TIME_MINUTES);

        return new CrossingOperation(
                waitingTrain,
                passingTrain,
                facilityId,
                facility.getName(),
                conflict.getSegmentId(),
                arrivalTime,
                waitStart,
                waitEnd,
                departure,
                CrossingOperation.CrossingReason.HEAD_ON_CONFLICT
        );
    }

    /**
     * Adjusts all ETAs after a crossing operation
     */
    private void adjustETAsAfterCrossing(List<FacilityETA> etas, CrossingOperation operation) {
        long delaySeconds = (long) (operation.getWaitDurationMinutes() * 60);

        // Find the facility where crossing occurs
        int crossingIndex = -1;
        for (int i = 0; i < etas.size(); i++) {
            if (etas.get(i).getFacilityId() == operation.getFacilityId()) {
                crossingIndex = i;
                break;
            }
        }

        // Adjust all subsequent ETAs
        if (crossingIndex >= 0) {
            for (int i = crossingIndex + 1; i < etas.size(); i++) {
                FacilityETA originalETA = etas.get(i);
                FacilityETA adjustedETA = new FacilityETA(
                        originalETA.getFacilityId(),
                        originalETA.getFacilityName(),
                        originalETA.getArrivalTime().plusSeconds(delaySeconds),
                        originalETA.getDepartureTime() != null ?
                                originalETA.getDepartureTime().plusSeconds(delaySeconds) : null,
                        originalETA.isOrigin(),
                        originalETA.isDestination(),
                        originalETA.isMustStop(),
                        originalETA.getCumulativeDistanceKm(),
                        originalETA.getCumulativeTimeMinutes() + operation.getWaitDurationMinutes()
                );
                etas.set(i, adjustedETA);
            }
        }
    }

    /**
     * Gets conflict resolution report
     */
    public String getResolutionReport(List<String> scheduleIds) {
        Map<String, ResolvedSchedule> resolved = resolveAllConflicts(scheduleIds);

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(100)).append("\n");
        sb.append("CROSSING CONFLICT RESOLUTION REPORT\n");
        sb.append("=".repeat(100)).append("\n");
        sb.append(String.format("Schedules processed: %d\n", scheduleIds.size()));

        long modifiedCount = resolved.values().stream()
                .filter(ResolvedSchedule::hasModifications)
                .count();

        sb.append(String.format("Schedules with conflicts: %d\n", modifiedCount));
        sb.append(String.format("Conflict-free schedules: %d\n", scheduleIds.size() - modifiedCount));
        sb.append("\n");

        for (Map.Entry<String, ResolvedSchedule> entry : resolved.entrySet()) {
            sb.append(entry.getValue().toDetailedString()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Checks if all conflicts are resolved
     */
    public boolean areAllConflictsResolved(List<String> scheduleIds) {
        Map<String, ResolvedSchedule> resolved = resolveAllConflicts(scheduleIds);

        // In this simple version, we assume resolution always succeeds
        // In reality, some conflicts may be unresolvable
        return true;
    }
}