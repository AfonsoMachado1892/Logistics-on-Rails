package LAPR3.service;

import LAPR3.DTO.*;
import LAPR3.model.Segment;
import LAPR3.repository.SegmentRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for detecting crossing conflicts on single-track segments
 * Part of USLP07 - Subtask #67
 */
public class CrossingConflictDetectionService {
    private final ScheduleCalculationService scheduleService;
    private final SegmentRepository segmentRepository;

    public CrossingConflictDetectionService(ScheduleCalculationService scheduleService,
                                            SegmentRepository segmentRepository) {
        this.scheduleService = scheduleService;
        this.segmentRepository = segmentRepository;
    }

    /**
     * Detects all crossing conflicts between multiple schedules
     *
     * @param scheduleIds list of schedule identifiers
     * @return list of detected conflicts
     */
    public List<CrossingConflict> detectConflicts(List<String> scheduleIds) {
        // Calculate occupancy for all trains
        List<TrainSegmentOccupancy> allOccupancies = new ArrayList<>();

        for (String scheduleId : scheduleIds) {
            List<TrainSegmentOccupancy> occupancies = calculateSegmentOccupancies(scheduleId);
            allOccupancies.addAll(occupancies);
        }

        // Sort by segment and time
        allOccupancies.sort(Comparator.naturalOrder());

        // Detect conflicts
        List<CrossingConflict> conflicts = new ArrayList<>();

        // Group by segment
        Map<Integer, List<TrainSegmentOccupancy>> bySegment = allOccupancies.stream()
                .collect(Collectors.groupingBy(TrainSegmentOccupancy::getSegmentId));

        // Check each segment for conflicts
        for (Map.Entry<Integer, List<TrainSegmentOccupancy>> entry : bySegment.entrySet()) {
            int segmentId = entry.getKey();
            List<TrainSegmentOccupancy> occupancies = entry.getValue();

            // Only check single-track segments
            if (occupancies.isEmpty() || !occupancies.get(0).isSingleTrack()) {
                continue;
            }

            // Check all pairs of trains on this segment
            for (int i = 0; i < occupancies.size(); i++) {
                for (int j = i + 1; j < occupancies.size(); j++) {
                    TrainSegmentOccupancy occ1 = occupancies.get(i);
                    TrainSegmentOccupancy occ2 = occupancies.get(j);

                    if (occ1.overlapsWith(occ2)) {
                        CrossingConflict conflict = createConflict(occ1, occ2, segmentId);
                        conflicts.add(conflict);
                    }
                }
            }
        }

        return conflicts;
    }

    /**
     * Calculates segment occupancy times for a scheduled train
     */
    private List<TrainSegmentOccupancy> calculateSegmentOccupancies(String scheduleId) {
        TrainScheduleResult schedule = scheduleService.calculateSchedule(scheduleId);
        List<TrainSegmentOccupancy> occupancies = new ArrayList<>();

        LocalDateTime currentTime = schedule.getDepartureTime();

        for (EnhancedSegmentTravelDetail detail : schedule.getSegmentDetails()) {
            LocalDateTime entryTime = currentTime;
            LocalDateTime exitTime = currentTime.plusSeconds((long) detail.getTimeSeconds());

            // Get segment info
            Segment segment = segmentRepository.findById(detail.getSegmentId()).orElseThrow();
            boolean isSingleTrack = segment.getNumberTracks() == 1;

            occupancies.add(new TrainSegmentOccupancy(
                    schedule.getTrainId(),
                    detail.getSegmentId(),
                    entryTime,
                    exitTime,
                    isSingleTrack
            ));

            currentTime = exitTime;
        }

        return occupancies;
    }

    /**
     * Creates a conflict object from two overlapping occupancies
     */
    private CrossingConflict createConflict(TrainSegmentOccupancy occ1,
                                            TrainSegmentOccupancy occ2,
                                            int segmentId) {
        CrossingConflict.ConflictType type = determineConflictType(occ1, occ2);

        return new CrossingConflict(
                occ1.getTrainId(),
                occ2.getTrainId(),
                segmentId,
                occ1.getEntryTime(),
                occ1.getExitTime(),
                occ2.getEntryTime(),
                occ2.getExitTime(),
                type
        );
    }

    /**
     * Determines the type of conflict
     */
    private CrossingConflict.ConflictType determineConflictType(TrainSegmentOccupancy occ1,
                                                                TrainSegmentOccupancy occ2) {
        // If one enters before the other exits, likely head-on
        // This is simplified - in reality would need direction info
        if (occ1.getEntryTime().isBefore(occ2.getExitTime()) &&
                occ2.getEntryTime().isBefore(occ1.getExitTime())) {
            return CrossingConflict.ConflictType.HEAD_ON;
        }

        // If they're going same direction
        return CrossingConflict.ConflictType.SAME_DIRECTION;
    }

    /**
     * Gets all single-track segments that have conflicts
     */
    public List<Integer> getConflictedSegments(List<String> scheduleIds) {
        List<CrossingConflict> conflicts = detectConflicts(scheduleIds);

        return conflicts.stream()
                .map(CrossingConflict::getSegmentId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Gets conflicts for a specific train
     */
    public List<CrossingConflict> getConflictsForTrain(List<String> scheduleIds, String trainId) {
        List<CrossingConflict> allConflicts = detectConflicts(scheduleIds);

        return allConflicts.stream()
                .filter(c -> c.getTrain1Id().equals(trainId) || c.getTrain2Id().equals(trainId))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a specific schedule has any conflicts
     */
    public boolean hasConflicts(List<String> scheduleIds, String checkScheduleId) {
        List<CrossingConflict> conflicts = detectConflicts(scheduleIds);

        TrainScheduleResult schedule = scheduleService.calculateSchedule(checkScheduleId);
        String trainId = schedule.getTrainId();

        return conflicts.stream()
                .anyMatch(c -> c.getTrain1Id().equals(trainId) || c.getTrain2Id().equals(trainId));
    }

    /**
     * Gets conflict summary report
     */
    public String getConflictReport(List<String> scheduleIds) {
        List<CrossingConflict> conflicts = detectConflicts(scheduleIds);

        if (conflicts.isEmpty()) {
            return "No conflicts detected. All trains can proceed safely.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(100)).append("\n");
        sb.append("CROSSING CONFLICT DETECTION REPORT\n");
        sb.append("=".repeat(100)).append("\n");
        sb.append(String.format("Schedules analyzed: %d\n", scheduleIds.size()));
        sb.append(String.format("Total conflicts found: %d\n", conflicts.size()));
        sb.append(String.format("Critical conflicts: %d\n",
                conflicts.stream().filter(CrossingConflict::isCritical).count()));
        sb.append("\n");

        // Group by segment
        Map<Integer, List<CrossingConflict>> bySegment = conflicts.stream()
                .collect(Collectors.groupingBy(CrossingConflict::getSegmentId));

        sb.append("Conflicts by Segment:\n");
        sb.append("-".repeat(100)).append("\n");

        for (Map.Entry<Integer, List<CrossingConflict>> entry : bySegment.entrySet()) {
            sb.append(String.format("\nSegment %d: %d conflicts\n",
                    entry.getKey(), entry.getValue().size()));

            for (CrossingConflict conflict : entry.getValue()) {
                sb.append("  ").append(conflict.toString()).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Gets critical conflicts that require immediate attention
     */
    public List<CrossingConflict> getCriticalConflicts(List<String> scheduleIds) {
        return detectConflicts(scheduleIds).stream()
                .filter(CrossingConflict::isCritical)
                .collect(Collectors.toList());
    }
}