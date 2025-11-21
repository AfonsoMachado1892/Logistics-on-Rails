package LAPR3.service;

import LAPR3.DTO.*;
import LAPR3.model.*;
import LAPR3.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating train schedules with ETAs
 * Part of USLP07 - Subtask #66
 */
public class ScheduleCalculationService {
    private final CompleteJourneyService journeyService;
    private final RouteRepository routeRepository;
    private final FacilityRepository facilityRepository;
    private final ScheduleRepository scheduleRepository;

    // Default dwell times in minutes
    private static final double DEFAULT_STOP_TIME_MINUTES = 5.0;      // Intermediate stops
    private static final double TERMINAL_STOP_TIME_MINUTES = 10.0;    // Origin/destination

    /**
     * Creates schedule calculation service
     */
    public ScheduleCalculationService(RouteRepository routeRepository,
                                      FacilityRepository facilityRepository,
                                      LineRepository lineRepository,
                                      SegmentRepository segmentRepository,
                                      TrainRepository trainRepository,
                                      ScheduleRepository scheduleRepository) {
        this.journeyService = new CompleteJourneyService(routeRepository, facilityRepository,
                lineRepository, segmentRepository,
                trainRepository);
        this.routeRepository = routeRepository;
        this.facilityRepository = facilityRepository;
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * Calculates complete schedule for a train
     *
     * @param scheduleId schedule identifier
     * @return train schedule result with ETAs
     * @throws IllegalArgumentException if schedule not found or invalid
     */
    public TrainScheduleResult calculateSchedule(String scheduleId) {
        // Get schedule
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        return calculateSchedule(schedule);
    }

    /**
     * Calculates schedule from Schedule object
     */
    public TrainScheduleResult calculateSchedule(Schedule schedule) {
        // Get route
        Route route = routeRepository.findById(schedule.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Route not found: " + schedule.getRouteId()));

        // Calculate journey segments
        List<EnhancedSegmentTravelDetail> segmentDetails =
                journeyService.calculateCompleteJourney(schedule.getRouteId(), schedule.getTrainId());

        // Calculate facility ETAs
        List<FacilityETA> facilityETAs = calculateFacilityETAs(
                route, segmentDetails, schedule.getDepartureDateTime()
        );

        return new TrainScheduleResult(
                schedule.getScheduleId(),
                schedule.getTrainId(),
                schedule.getRouteId(),
                schedule.getDepartureDateTime(),
                facilityETAs,
                segmentDetails
        );
    }

    /**
     * Calculates ETAs for all facilities in the route
     */
    private List<FacilityETA> calculateFacilityETAs(Route route,
                                                    List<EnhancedSegmentTravelDetail> segmentDetails,
                                                    LocalDateTime departureTime) {
        List<FacilityETA> etas = new ArrayList<>();
        List<Integer> facilityIds = route.getFacilityIds();

        double cumulativeTimeSeconds = 0;
        double cumulativeDistanceMeters = 0;
        LocalDateTime currentTime = departureTime;

        // Origin facility
        int originId = facilityIds.get(0);
        Facility originFacility = facilityRepository.findById(originId).orElseThrow();

        etas.add(new FacilityETA(
                originId,
                originFacility.getName(),
                departureTime,  // Arrival = departure at origin
                departureTime,
                true,  // isOrigin
                false, // isDestination
                true,  // mustStop
                0.0,
                0.0
        ));

        // Track which segment we're processing
        int segmentIndex = 0;

        // Intermediate and destination facilities
        for (int i = 1; i < facilityIds.size(); i++) {
            int facilityId = facilityIds.get(i);
            Facility facility = facilityRepository.findById(facilityId).orElseThrow();
            boolean isDestination = (i == facilityIds.size() - 1);

            // Sum segments until we reach this facility
            // In simple cases, there's one segment per leg, but we handle multiple
            while (segmentIndex < segmentDetails.size()) {
                EnhancedSegmentTravelDetail segment = segmentDetails.get(segmentIndex);
                cumulativeTimeSeconds += segment.getTimeSeconds();
                cumulativeDistanceMeters += segment.getLengthMeters();
                segmentIndex++;

                // Check if we've completed this leg (simplified logic)
                // In reality, need to map segments to legs properly
                if (segmentIndex >= (i * segmentDetails.size() / (facilityIds.size() - 1))) {
                    break;
                }
            }

            // Calculate arrival time
            LocalDateTime arrivalTime = departureTime.plusSeconds((long) cumulativeTimeSeconds);

            // Calculate departure time if stopping
            LocalDateTime departureTimeAtFacility = null;
            boolean mustStop = isDestination || shouldStopAt(route, facilityId);

            if (mustStop && !isDestination) {
                double dwellTime = isDestination ? 0 : DEFAULT_STOP_TIME_MINUTES;
                departureTimeAtFacility = arrivalTime.plusMinutes((long) dwellTime);
                cumulativeTimeSeconds += dwellTime * 60; // Add dwell time to cumulative
            }

            etas.add(new FacilityETA(
                    facilityId,
                    facility.getName(),
                    arrivalTime,
                    departureTimeAtFacility,
                    false,
                    isDestination,
                    mustStop,
                    cumulativeDistanceMeters / 1000.0,
                    cumulativeTimeSeconds / 60.0
            ));
        }

        return etas;
    }

    /**
     * Determines if train should stop at a facility
     * In this version, only origin and destination are mandatory stops
     * Future: consider complex routes with intermediate freight operations
     */
    private boolean shouldStopAt(Route route, int facilityId) {
        // For SIMPLE routes, no intermediate stops
        // For COMPLEX routes, would need stop schedule
        return route.getType() == Route.RouteType.COMPLEX;
    }

    /**
     * Calculates schedule with custom stop times
     */
    public TrainScheduleResult calculateScheduleWithStops(String scheduleId,
                                                          List<Integer> stopFacilityIds,
                                                          double stopTimeMinutes) {
        // Future enhancement: allow custom stop configuration
        return calculateSchedule(scheduleId);
    }

    /**
     * Gets estimated arrival time at a specific facility
     */
    public LocalDateTime getETAAtFacility(String scheduleId, int facilityId) {
        TrainScheduleResult result = calculateSchedule(scheduleId);
        FacilityETA eta = result.getETAForFacility(facilityId);

        if (eta == null) {
            throw new IllegalArgumentException(
                    "Facility " + facilityId + " is not on the route");
        }

        return eta.getArrivalTime();
    }

    /**
     * Checks if a schedule is on time at a given facility
     * Compares actual time with scheduled time
     */
    public boolean isOnTime(String scheduleId, int facilityId,
                            LocalDateTime actualTime, double toleranceMinutes) {
        LocalDateTime eta = getETAAtFacility(scheduleId, facilityId);
        long differenceMinutes = Math.abs(
                java.time.Duration.between(eta, actualTime).toMinutes()
        );

        return differenceMinutes <= toleranceMinutes;
    }

    /**
     * Calculates delay at a facility
     * @return delay in minutes (positive = late, negative = early)
     */
    public double calculateDelay(String scheduleId, int facilityId,
                                 LocalDateTime actualTime) {
        LocalDateTime eta = getETAAtFacility(scheduleId, facilityId);
        return java.time.Duration.between(eta, actualTime).toMinutes();
    }
}