package LAPR3.service;

import LAPR3.DTO.AccelerationProfile;
import LAPR3.DTO.EnhancedSegmentTravelDetail;
import LAPR3.model.*;
import LAPR3.repository.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating travel times with realistic acceleration
 * Replaces instantaneous acceleration with physics-based model
 * Part of USLP07 - Subtask #64
 */
public class RealisticTravelTimeService {
    private final FacilityRepository facilityRepository;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;
    private final TrainRepository trainRepository;
    private final SpeedCalculationService speedCalculationService;
    private final AccelerationCalculationService accelerationCalculationService;

    public RealisticTravelTimeService(FacilityRepository facilityRepository,
                                      LineRepository lineRepository,
                                      SegmentRepository segmentRepository,
                                      TrainRepository trainRepository) {
        this.facilityRepository = facilityRepository;
        this.lineRepository = lineRepository;
        this.segmentRepository = segmentRepository;
        this.trainRepository = trainRepository;
        this.speedCalculationService = new SpeedCalculationService();
        this.accelerationCalculationService = new AccelerationCalculationService();
    }

    /**
     * Calculate travel time with realistic acceleration between two facilities
     *
     * @param originFacilityId origin facility ID
     * @param destinationFacilityId destination facility ID
     * @param trainId train composition ID
     * @return list of enhanced segment travel details
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<EnhancedSegmentTravelDetail> calculateRealisticTravelTime(
            int originFacilityId, int destinationFacilityId, String trainId) {

        // 1. Validate facilities
        Facility origin = facilityRepository.findById(originFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Origin facility not found: " + originFacilityId));

        Facility destination = facilityRepository.findById(destinationFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination facility not found: " + destinationFacilityId));

        if (originFacilityId == destinationFacilityId) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }

        // 2. Validate train
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainId));

        // 3. Find direct connection line
        Line line = lineRepository.findLineConnecting(originFacilityId, destinationFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No direct line found between %s and %s",
                                origin.getName(), destination.getName())));

        // 4. Validate compatibility
        validateTrainLineCompatibility(train, line);

        // 5. Get segments
        List<Segment> segments = segmentRepository.findByLineId(line.getId());
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Line has no segments: " + line.getName());
        }

        // 6. Calculate for each segment with acceleration
        List<EnhancedSegmentTravelDetail> details = new ArrayList<>();
        for (Segment segment : segments) {
            EnhancedSegmentTravelDetail detail = calculateSegmentWithAcceleration(segment, train);
            details.add(detail);
        }

        return details;
    }

    /**
     * Validates train-line compatibility
     */
    private void validateTrainLineCompatibility(Train train, Line line) {
        // Check gauge
        if (!train.isCompatibleWithGauge(line.getGauge())) {
            throw new IllegalArgumentException(
                    String.format("Train gauge incompatible with line gauge (%dmm vs %dmm)",
                            train.getLocomotives().get(0).getGauge(), line.getGauge()));
        }

        // Check electrification if needed
        if (train.requiresElectrification()) {
            List<Segment> segments = segmentRepository.findByLineId(line.getId());
            boolean allElectrified = segments.stream().allMatch(Segment::isElectrified);
            if (!allElectrified) {
                throw new IllegalArgumentException("Electric train requires fully electrified line");
            }
        }
    }

    /**
     * Calculates travel detail for a segment with realistic acceleration
     */
    private EnhancedSegmentTravelDetail calculateSegmentWithAcceleration(Segment segment, Train train) {
        // Calculate max speed for this segment
        var speedResult = speedCalculationService.calculateMaxSpeed(train, segment);
        double maxSpeedKmh = speedResult.getMaxSpeedKmh();

        // Calculate acceleration profile
        AccelerationProfile profile = accelerationCalculationService.calculateProfile(
                train, segment.getLengthMeters(), maxSpeedKmh
        );

        // Total time is sum of all phases
        double totalTime = profile.getTotalTime();

        return new EnhancedSegmentTravelDetail(
                segment.getId(),
                segment.getOrder(),
                segment.getLengthMeters(),
                maxSpeedKmh,
                totalTime,
                segment.isElectrified(),
                segment.getNumberTracks(),
                profile
        );
    }

    /**
     * Calculates total travel time for all segments
     */
    public double calculateTotalTime(int originFacilityId, int destinationFacilityId, String trainId) {
        List<EnhancedSegmentTravelDetail> details =
                calculateRealisticTravelTime(originFacilityId, destinationFacilityId, trainId);

        return details.stream()
                .mapToDouble(EnhancedSegmentTravelDetail::getTimeSeconds)
                .sum();
    }

    /**
     * Calculates total distance
     */
    public double calculateTotalDistance(int originFacilityId, int destinationFacilityId, String trainId) {
        List<EnhancedSegmentTravelDetail> details =
                calculateRealisticTravelTime(originFacilityId, destinationFacilityId, trainId);

        return details.stream()
                .mapToDouble(EnhancedSegmentTravelDetail::getLengthMeters)
                .sum();
    }

    /**
     * Calculates average speed for entire journey
     */
    public double calculateAverageSpeed(int originFacilityId, int destinationFacilityId, String trainId) {
        double totalDistanceKm = calculateTotalDistance(originFacilityId, destinationFacilityId, trainId) / 1000.0;
        double totalTimeHours = calculateTotalTime(originFacilityId, destinationFacilityId, trainId) / 3600.0;

        if (totalTimeHours == 0) return 0;
        return totalDistanceKm / totalTimeHours;
    }

    /**
     * Gets formatted travel summary
     */
    public String getTravelSummary(int originFacilityId, int destinationFacilityId, String trainId) {
        Facility origin = facilityRepository.findById(originFacilityId).orElseThrow();
        Facility destination = facilityRepository.findById(destinationFacilityId).orElseThrow();

        List<EnhancedSegmentTravelDetail> details =
                calculateRealisticTravelTime(originFacilityId, destinationFacilityId, trainId);

        double totalTime = details.stream().mapToDouble(EnhancedSegmentTravelDetail::getTimeSeconds).sum();
        double totalDistance = details.stream().mapToDouble(EnhancedSegmentTravelDetail::getLengthMeters).sum();
        double avgSpeed = (totalDistance / 1000.0) / (totalTime / 3600.0);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Journey: %s → %s (Train %s)\n",
                origin.getName(), destination.getName(), trainId));
        sb.append(String.format("Distance: %.2f km\n", totalDistance / 1000.0));
        sb.append(String.format("Time: %.1f minutes (%.2f hours)\n",
                totalTime / 60.0, totalTime / 3600.0));
        sb.append(String.format("Average Speed: %.1f km/h\n\n", avgSpeed));

        sb.append("Segment Details:\n");
        sb.append("-".repeat(80)).append("\n");
        for (EnhancedSegmentTravelDetail detail : details) {
            sb.append(detail.toDetailedString()).append("\n");
        }

        return sb.toString();
    }
}