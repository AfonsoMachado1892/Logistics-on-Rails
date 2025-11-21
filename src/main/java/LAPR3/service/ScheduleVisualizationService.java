package LAPR3.service;

import LAPR3.DTO.*;
import LAPR3.model.Facility;
import LAPR3.repository.FacilityRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for visualizing schedules and crossing operations
 * Part of USLP07 - Subtask #71
 */
public class ScheduleVisualizationService {
    private final FacilityRepository facilityRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int TIMELINE_WIDTH = 100;

    public ScheduleVisualizationService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    /**
     * Creates ASCII timeline visualization of multiple schedules
     */
    public String visualizeTimeline(DispatchPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(120)).append("\n");
        sb.append("TIMELINE VISUALIZATION\n");
        sb.append("═".repeat(120)).append("\n\n");

        // Get all schedules sorted by departure
        List<ResolvedSchedule> schedules = new ArrayList<>(plan.getResolvedSchedules().values());
        schedules.sort(Comparator.comparing(rs ->
                rs.getOriginalSchedule().getDepartureTime()));

        if (schedules.isEmpty()) {
            return sb.append("No schedules to visualize.").toString();
        }

        // Find time range
        LocalDateTime earliest = schedules.get(0).getOriginalSchedule().getDepartureTime();
        LocalDateTime latest = schedules.stream()
                .map(ResolvedSchedule::getAdjustedArrivalTime)
                .max(LocalDateTime::compareTo)
                .orElse(earliest);

        long totalMinutes = java.time.Duration.between(earliest, latest).toMinutes();

        // Create timeline header
        sb.append(String.format("Time Range: %s → %s (%.0f minutes)\n",
                earliest.format(TIME_FORMATTER),
                latest.format(TIME_FORMATTER),
                (double) totalMinutes));
        sb.append("\n");

        // Draw timeline for each train
        for (ResolvedSchedule resolved : schedules) {
            sb.append(drawTrainTimeline(resolved, earliest, totalMinutes));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Draws timeline for a single train
     */
    private String drawTrainTimeline(ResolvedSchedule resolved,
                                     LocalDateTime startTime, long totalMinutes) {
        TrainScheduleResult schedule = resolved.getOriginalSchedule();
        StringBuilder sb = new StringBuilder();

        String trainId = schedule.getTrainId();
        LocalDateTime departure = schedule.getDepartureTime();
        LocalDateTime arrival = resolved.getAdjustedArrivalTime();

        // Calculate positions
        long startOffset = java.time.Duration.between(startTime, departure).toMinutes();
        long duration = java.time.Duration.between(departure, arrival).toMinutes();

        int startPos = (int) ((startOffset * TIMELINE_WIDTH) / totalMinutes);
        int endPos = (int) (((startOffset + duration) * TIMELINE_WIDTH) / totalMinutes);

        // Draw train line
        sb.append(String.format("%-8s |", trainId));

        // Empty space before departure
        sb.append(" ".repeat(Math.max(0, startPos)));

        // Train journey
        if (resolved.hasModifications()) {
            // Show crossings with different symbol
            sb.append(drawJourneyWithCrossings(resolved, startPos, endPos));
        } else {
            // Simple journey
            sb.append("▓".repeat(Math.max(1, endPos - startPos)));
        }

        // Time labels
        sb.append(String.format(" | %s → %s (%.0fmin)",
                departure.format(TIME_FORMATTER),
                arrival.format(TIME_FORMATTER),
                (double) duration));

        if (resolved.hasModifications()) {
            sb.append(String.format(" [%d crossings, +%.0fmin delay]",
                    resolved.getCrossingCount(), resolved.getTotalDelayMinutes()));
        }

        return sb.toString();
    }

    /**
     * Draws journey with crossing indicators
     */
    private String drawJourneyWithCrossings(ResolvedSchedule resolved,
                                            int startPos, int endPos) {
        int length = endPos - startPos;
        char[] journey = new char[length];
        Arrays.fill(journey, '▓');

        // Mark crossing points with 'X'
        List<CrossingOperation> crossings = resolved.getCrossingOperations();
        if (!crossings.isEmpty()) {
            // Mark middle for simplicity
            journey[length / 2] = 'X';
        }

        return new String(journey);
    }

    /**
     * Creates train-diagram style visualization (Bildfahrplan)
     */
    public String visualizeTrainDiagram(DispatchPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(120)).append("\n");
        sb.append("TRAIN DIAGRAM (Bildfahrplan)\n");
        sb.append("═".repeat(120)).append("\n\n");

        // Get all unique facilities in order
        Set<Integer> allFacilityIds = new LinkedHashSet<>();
        for (ResolvedSchedule rs : plan.getResolvedSchedules().values()) {
            for (FacilityETA eta : rs.getAdjustedETAs()) {
                allFacilityIds.add(eta.getFacilityId());
            }
        }

        // Get facility names
        Map<Integer, String> facilityNames = new HashMap<>();
        for (Integer id : allFacilityIds) {
            Facility facility = facilityRepository.findById(id).orElse(null);
            facilityNames.put(id, facility != null ? facility.getName() : "Unknown");
        }

        // Draw diagram header
        sb.append(String.format("%-25s", "Station"));
        for (ResolvedSchedule rs : plan.getResolvedSchedules().values()) {
            sb.append(String.format(" | %-10s", rs.getOriginalSchedule().getTrainId()));
        }
        sb.append("\n");
        sb.append("─".repeat(120)).append("\n");

        // Draw each facility row
        for (Integer facilityId : allFacilityIds) {
            String name = facilityNames.get(facilityId);
            sb.append(String.format("%-25s", truncate(name, 25)));

            for (ResolvedSchedule rs : plan.getResolvedSchedules().values()) {
                FacilityETA eta = rs.getAdjustedETAs().stream()
                        .filter(e -> e.getFacilityId() == facilityId)
                        .findFirst()
                        .orElse(null);

                if (eta != null) {
                    if (eta.isOrigin()) {
                        sb.append(String.format(" | %-10s", "DEP:" + eta.getDepartureTime().format(TIME_FORMATTER)));
                    } else if (eta.isDestination()) {
                        sb.append(String.format(" | %-10s", "ARR:" + eta.getArrivalTime().format(TIME_FORMATTER)));
                    } else {
                        sb.append(String.format(" | %-10s", eta.getArrivalTime().format(TIME_FORMATTER)));
                    }
                } else {
                    sb.append(" |           ");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Creates crossing operations visualization
     */
    public String visualizeCrossingOperations(DispatchPlan plan) {
        List<CrossingOperation> crossings = plan.getAllCrossingOperations();

        if (crossings.isEmpty()) {
            return "No crossing operations required - all trains can proceed without conflicts.\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(120)).append("\n");
        sb.append("CROSSING OPERATIONS VISUALIZATION\n");
        sb.append("═".repeat(120)).append("\n\n");

        // Sort by time
        List<CrossingOperation> sorted = new ArrayList<>(crossings);
        sorted.sort(Comparator.comparing(CrossingOperation::getWaitStartTime));

        // Group by facility
        Map<Integer, List<CrossingOperation>> byFacility = sorted.stream()
                .collect(Collectors.groupingBy(CrossingOperation::getFacilityId));

        for (Map.Entry<Integer, List<CrossingOperation>> entry : byFacility.entrySet()) {
            String facilityName = entry.getValue().get(0).getFacilityName();

            sb.append(String.format("📍 %s\n", facilityName));
            sb.append("─".repeat(120)).append("\n");

            for (CrossingOperation op : entry.getValue()) {
                sb.append(visualizeSingleCrossing(op));
                sb.append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Visualizes a single crossing operation
     */
    private String visualizeSingleCrossing(CrossingOperation op) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("  Time: %s → %s (wait: %.0f min)\n",
                op.getWaitStartTime().format(TIME_FORMATTER),
                op.getWaitEndTime().format(TIME_FORMATTER),
                op.getWaitDurationMinutes()));

        sb.append(String.format("  Waiting: %-10s ⏸  [───────────────────────────] ⏸\n",
                op.getWaitingTrainId()));
        sb.append(String.format("  Passing: %-10s    [→→→→→→→→→→→→→→→→→→→→→→→→→→→]\n",
                op.getPassingTrainId()));
        sb.append(String.format("  Segment: %d | Reason: %s\n",
                op.getSegmentId(), op.getReason()));

        return sb.toString();
    }

    /**
     * Creates conflict heatmap
     */
    public String visualizeConflictHeatmap(DispatchPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(120)).append("\n");
        sb.append("CONFLICT HEATMAP\n");
        sb.append("═".repeat(120)).append("\n\n");

        // Count conflicts per segment
        Map<Integer, Long> conflictsPerSegment = plan.getAllCrossingOperations().stream()
                .collect(Collectors.groupingBy(CrossingOperation::getSegmentId,
                        Collectors.counting()));

        if (conflictsPerSegment.isEmpty()) {
            return sb.append("No conflicts detected.\n").toString();
        }

        long maxConflicts = conflictsPerSegment.values().stream()
                .max(Long::compareTo)
                .orElse(1L);

        sb.append("Segment | Conflicts | Heatmap\n");
        sb.append("─".repeat(120)).append("\n");

        List<Integer> sortedSegments = new ArrayList<>(conflictsPerSegment.keySet());
        Collections.sort(sortedSegments);

        for (Integer segmentId : sortedSegments) {
            long count = conflictsPerSegment.get(segmentId);
            int barLength = (int) ((count * 50) / maxConflicts);

            sb.append(String.format("%-7d | %-9d | %s (%d)\n",
                    segmentId, count, "█".repeat(barLength), count));
        }

        return sb.toString();
    }

    /**
     * Creates delay statistics visualization
     */
    public String visualizeDelayStatistics(DispatchPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(120)).append("\n");
        sb.append("DELAY STATISTICS\n");
        sb.append("═".repeat(120)).append("\n\n");

        List<ResolvedSchedule> withDelays = plan.getResolvedSchedules().values().stream()
                .filter(ResolvedSchedule::hasModifications)
                .sorted(Comparator.comparing(ResolvedSchedule::getTotalDelayMinutes).reversed())
                .toList();

        if (withDelays.isEmpty()) {
            return sb.append("No delays - all trains on schedule!\n").toString();
        }

        double maxDelay = withDelays.get(0).getTotalDelayMinutes();

        sb.append("Train    | Delay (min) | Visualization\n");
        sb.append("─".repeat(120)).append("\n");

        for (ResolvedSchedule rs : withDelays) {
            String trainId = rs.getOriginalSchedule().getTrainId();
            double delay = rs.getTotalDelayMinutes();
            int barLength = (int) ((delay * 50) / maxDelay);

            sb.append(String.format("%-8s | %11.1f | %s\n",
                    trainId, delay, "░".repeat(barLength)));
        }

        return sb.toString();
    }

    private String truncate(String str, int length) {
        return str.length() <= length ? str : str.substring(0, length - 3) + "...";
    }
}