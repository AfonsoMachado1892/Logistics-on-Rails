package LAPR3.service;

import LAPR3.DTO.SegmentTravelDetail;
import LAPR3.DTO.SpeedCalculationResult;
import LAPR3.DTO.TravelTimeResult;
import LAPR3.model.*;
import LAPR3.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced service for calculating travel times with Train compositions
 * Extends USLP03 functionality to work with USLP07 Train model
 * Part of USLP07 - Subtask #63
 */
public class EnhancedTravelTimeService {
    private final FacilityRepository facilityRepository;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;
    private final TrainRepository trainRepository;
    private final SpeedCalculationService speedCalculationService;

    // Physical constant: conversion from km/h to m/s
    private static final double KMH_TO_MS = 1000.0 / 3600.0;

    /**
     * Constructs enhanced travel time service
     */
    public EnhancedTravelTimeService(FacilityRepository facilityRepository,
                                     LineRepository lineRepository,
                                     SegmentRepository segmentRepository,
                                     TrainRepository trainRepository) {
        this.facilityRepository = facilityRepository;
        this.lineRepository = lineRepository;
        this.segmentRepository = segmentRepository;
        this.trainRepository = trainRepository;
        this.speedCalculationService = new SpeedCalculationService();
    }

    /**
     * Calculate travel time for a train between two facilities
     *
     * @param originFacilityId origin facility ID
     * @param destinationFacilityId destination facility ID
     * @param trainId train composition ID
     * @return TravelTimeResult with detailed calculation
     * @throws IllegalArgumentException if parameters are invalid
     */
    public TravelTimeResult calculateTravelTime(int originFacilityId,
                                                int destinationFacilityId,
                                                String trainId) {
        // 1. Validate origin facility
        Facility origin = facilityRepository.findById(originFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Origin facility not found: " + originFacilityId));

        // 2. Validate destination facility
        Facility destination = facilityRepository.findById(destinationFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination facility not found: " + destinationFacilityId));

        // 3. Check if origin and destination are different
        if (originFacilityId == destinationFacilityId) {
            throw new IllegalArgumentException(
                    "Origin and destination must be different");
        }

        // 4. Validate train
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Train not found: " + trainId));

        // 5. Find direct connection line
        Line line = lineRepository.findLineConnecting(originFacilityId, destinationFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No direct line found between %s and %s",
                                origin.getName(), destination.getName())));

        // 6. Validate gauge compatibility
        if (!train.isCompatibleWithGauge(line.getGauge())) {
            throw new IllegalArgumentException(
                    String.format("Train gauge (%dmm) incompatible with line gauge (%dmm)",
                            train.getLocomotives().get(0).getGauge(), line.getGauge()));
        }

        // 7. Get segments of the line in order
        List<Segment> segments = segmentRepository.findByLineId(line.getId());

        if (segments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Line has no segments: " + line.getName());
        }

        // 8. Check electrification compatibility
        if (train.requiresElectrification()) {
            boolean allElectrified = segments.stream().allMatch(Segment::isElectrified);
            if (!allElectrified) {
                throw new IllegalArgumentException(
                        "Electric train requires fully electrified line");
            }
        }

        // 9. Calculate travel time for each segment with dynamic speed
        List<SegmentTravelDetail> segmentDetails = new ArrayList<>();

        for (Segment segment : segments) {
            SegmentTravelDetail detail = calculateSegmentTimeWithTrain(segment, train);
            segmentDetails.add(detail);
        }

        // 10. Build and return result
        // Convert locomotive number from String to int for TravelTimeResult constructor
        String locoNumber = train.getLocomotives().get(0).getNumber();
        int locoNumberInt;
        try {
            locoNumberInt = Integer.parseInt(locoNumber);
        } catch (NumberFormatException e) {
            locoNumberInt = 0; // Default if not numeric
        }

        return new TravelTimeResult(
                origin.getName(),
                destination.getName(),
                line.getName(),
                locoNumberInt,
                train.getLocomotives().get(0).getName(),
                segmentDetails
        );
    }

    /**
     * Calculate travel time for a single segment with Train composition
     * Uses SpeedCalculationService to determine speed based on power/weight
     *
     * @param segment the segment to traverse
     * @param train the train composition
     * @return segment travel detail with time calculation
     */
    private SegmentTravelDetail calculateSegmentTimeWithTrain(Segment segment, Train train) {
        // Calculate maximum speed using power-to-weight ratio
        SpeedCalculationResult speedResult = speedCalculationService.calculateMaxSpeed(train, segment);
        double speedKmh = speedResult.getMaxSpeedKmh();

        // Convert speed to m/s
        double speedMs = speedKmh * KMH_TO_MS;

        // Calculate time: distance / speed (in seconds)
        // With instantaneous acceleration: time = distance / constant_speed
        double timeSeconds = segment.getLengthMeters() / speedMs;

        return new SegmentTravelDetail(
                segment.getId(),
                segment.getOrder(),
                segment.getLengthMeters(),
                speedKmh,
                timeSeconds,
                segment.isElectrified(),
                segment.getNumberTracks()
        );
    }

    /**
     * Validate if a journey is possible with given train
     *
     * @return Optional containing error message if journey is not possible
     */
    public Optional<String> validateJourney(int originFacilityId,
                                            int destinationFacilityId,
                                            String trainId) {
        try {
            calculateTravelTime(originFacilityId, destinationFacilityId, trainId);
            return Optional.empty(); // Valid journey
        } catch (IllegalArgumentException e) {
            return Optional.of(e.getMessage());
        }
    }

    /**
     * Calculate if train can traverse a specific segment
     *
     * @param trainId train ID
     * @param segmentId segment ID
     * @return speed calculation result
     */
    public Optional<SpeedCalculationResult> calculateSegmentSpeed(String trainId, int segmentId) {
        try {
            Train train = trainRepository.findById(trainId)
                    .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainId));

            Segment segment = segmentRepository.findById(segmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Segment not found: " + segmentId));

            SpeedCalculationResult result = speedCalculationService.calculateMaxSpeed(train, segment);
            return Optional.of(result);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Get recommended number of locomotives for a route
     *
     * @param trainId train ID (for wagon composition)
     * @param lineId line ID
     * @param desiredSpeedKmh desired operational speed
     * @return recommended locomotive count
     */
    public int getRecommendedLocomotiveCount(String trainId, int lineId, double desiredSpeedKmh) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainId));

        double locomotivePower = train.getLocomotives().get(0).getPowerKw();
        double trainWeight = train.getTotalWeightTons();

        return speedCalculationService.estimateLocomotivesNeeded(
                trainWeight, locomotivePower, desiredSpeedKmh
        );
    }
}