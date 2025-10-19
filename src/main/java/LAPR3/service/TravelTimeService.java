package LAPR3.service;

import LAPR3.DTO.SegmentTravelDetail;
import LAPR3.DTO.TravelTimeResult;
import LAPR3.model.Facility;
import LAPR3.model.Line;
import LAPR3.model.Locomotive;
import LAPR3.model.Segment;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.LineRepository;
import LAPR3.repository.LocomotiveRepository;
import LAPR3.repository.SegmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TravelTimeService {
    private final FacilityRepository facilityRepository;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;
    private final LocomotiveRepository locomotiveRepository;

    // Physical constant: conversion from km/h to m/s
    private static final double KMH_TO_MS = 1000.0 / 3600.0; // = 0.277778

    public TravelTimeService(FacilityRepository facilityRepository,
                             LineRepository lineRepository,
                             SegmentRepository segmentRepository,
                             LocomotiveRepository locomotiveRepository) {
        this.facilityRepository = facilityRepository;
        this.lineRepository = lineRepository;
        this.segmentRepository = segmentRepository;
        this.locomotiveRepository = locomotiveRepository;
    }

    /**
     * Calculate travel time between two facilities using a specific locomotive
     *
     * @param originFacilityId origin facility ID
     * @param destinationFacilityId destination facility ID
     * @param locomotiveNumber locomotive number
     * @return TravelTimeResult with detailed calculation
     * @throws IllegalArgumentException if parameters are invalid
     */
    public TravelTimeResult calculateTravelTime(int originFacilityId,
                                                int destinationFacilityId,
                                                int locomotiveNumber) {
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

        // 4. Validate locomotive
        Locomotive locomotive = locomotiveRepository.findByNumber(locomotiveNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Locomotive not found: " + locomotiveNumber));

        // 5. Find direct connection line
        Line line = lineRepository.findLineConnecting(originFacilityId, destinationFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No direct line found between %s and %s",
                                origin.getName(), destination.getName())));

        // 6. Validate gauge compatibility
        if (!locomotive.isCompatibleWithGauge(line.getGauge())) {
            throw new IllegalArgumentException(
                    String.format("Locomotive gauge (%dmm) incompatible with line gauge (%dmm)",
                            locomotive.getGauge(), line.getGauge()));
        }

        // 7. Get segments of the line in order
        List<Segment> segments = segmentRepository.findByLineId(line.getId());

        if (segments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Line has no segments: " + line.getName());
        }

        // 8. Check electrification compatibility
        if (locomotive.requiresElectrification()) {
            boolean allElectrified = segments.stream().allMatch(Segment::isElectrified);
            if (!allElectrified) {
                throw new IllegalArgumentException(
                        "Electric locomotive requires fully electrified line");
            }
        }

        // 9. Calculate travel time for each segment
        List<SegmentTravelDetail> segmentDetails = new ArrayList<>();

        for (Segment segment : segments) {
            SegmentTravelDetail detail = calculateSegmentTime(segment, locomotive);
            segmentDetails.add(detail);
        }

        // 10. Build and return result
        return new TravelTimeResult(
                origin.getName(),
                destination.getName(),
                line.getName(),
                locomotive.getNumber(),
                locomotive.getName(),
                segmentDetails
        );
    }

    /**
     * Calculate travel time for a single segment
     * Assumes instantaneous acceleration (as per requirements)
     *
     * @param segment the segment to traverse
     * @param locomotive the locomotive being used
     * @return segment travel detail with time calculation
     */
    private SegmentTravelDetail calculateSegmentTime(Segment segment, Locomotive locomotive) {
        // Speed is limited by locomotive's operational speed
        // (No segment speed limit in current dataset)
        double speedKmh = locomotive.getOperationalSpeedKmh();

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
     * Validate if a journey is possible
     * @return Optional containing error message if journey is not possible
     */
    public Optional<String> validateJourney(int originFacilityId,
                                            int destinationFacilityId,
                                            int locomotiveNumber) {
        try {
            calculateTravelTime(originFacilityId, destinationFacilityId, locomotiveNumber);
            return Optional.empty(); // Valid journey
        } catch (IllegalArgumentException e) {
            return Optional.of(e.getMessage());
        }
    }

    /**
     * Get locomotive operational speed for display purposes
     */
    public Optional<Double> getLocomotiveSpeed(int locomotiveNumber) {
        return locomotiveRepository.findByNumber(locomotiveNumber)
                .map(Locomotive::getOperationalSpeedKmh);
    }
}
