package LAPR3.service;

import LAPR3.DTO.*;
import LAPR3.model.*;
import LAPR3.repository.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating complete journey with realistic physics
 * Combines RoutePathService, SpeedCalculationService, and AccelerationCalculationService
 * Part of USLP07 - Subtasks #64 and #65 Integration
 */
public class CompleteJourneyService {
    private final RoutePathService routePathService;
    private final TrainRepository trainRepository;
    private final SpeedCalculationService speedCalculationService;
    private final AccelerationCalculationService accelerationCalculationService;

    public CompleteJourneyService(RouteRepository routeRepository,
                                  FacilityRepository facilityRepository,
                                  LineRepository lineRepository,
                                  SegmentRepository segmentRepository,
                                  TrainRepository trainRepository) {
        this.routePathService = new RoutePathService(routeRepository, facilityRepository,
                lineRepository, segmentRepository);
        this.trainRepository = trainRepository;
        this.speedCalculationService = new SpeedCalculationService();
        this.accelerationCalculationService = new AccelerationCalculationService();
    }

    /**
     * Calculates complete journey for a train on a route
     *
     * @param routeId route identifier
     * @param trainId train identifier
     * @return list of enhanced segment details with acceleration profiles
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<EnhancedSegmentTravelDetail> calculateCompleteJourney(String routeId, String trainId) {
        // Get route path
        RoutePathResult routePath = routePathService.resolveRoutePath(routeId);
        if (!routePath.isValid()) {
            throw new IllegalArgumentException(
                    "Invalid route: " + routePath.getErrorMessage());
        }

        // Get train
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Train not found: " + trainId));

        // Validate compatibility
        validateTrainRouteCompatibility(train, routePath);

        // Calculate for each segment
        List<EnhancedSegmentTravelDetail> allDetails = new ArrayList<>();

        for (RouteLeg leg : routePath.getLegs()) {
            for (Segment segment : leg.getSegments()) {
                EnhancedSegmentTravelDetail detail = calculateSegmentDetail(segment, train);
                allDetails.add(detail);
            }
        }

        return allDetails;
    }

    /**
     * Validates train-route compatibility
     */
    private void validateTrainRouteCompatibility(Train train, RoutePathResult routePath) {
        // Check gauge compatibility
        for (RouteLeg leg : routePath.getLegs()) {
            if (!train.isCompatibleWithGauge(leg.getLine().getGauge())) {
                throw new IllegalArgumentException(
                        String.format("Train gauge incompatible with line %s (%dmm)",
                                leg.getLine().getName(), leg.getLine().getGauge()));
            }
        }

        // Check electrification
        if (train.requiresElectrification() && !routePath.isFullyElectrified()) {
            throw new IllegalArgumentException(
                    "Electric train requires fully electrified route");
        }
    }

    /**
     * Calculates detail for a single segment
     */
    private EnhancedSegmentTravelDetail calculateSegmentDetail(Segment segment, Train train) {
        // Calculate max speed
        var speedResult = speedCalculationService.calculateMaxSpeed(train, segment);
        double maxSpeedKmh = speedResult.getMaxSpeedKmh();

        // Calculate acceleration profile
        AccelerationProfile profile = accelerationCalculationService.calculateProfile(
                train, segment.getLengthMeters(), maxSpeedKmh
        );

        return new EnhancedSegmentTravelDetail(
                segment.getId(),
                segment.getOrder(),
                segment.getLengthMeters(),
                maxSpeedKmh,
                profile.getTotalTime(),
                segment.isElectrified(),
                segment.getNumberTracks(),
                profile
        );
    }

    /**
     * Gets journey summary with detailed statistics
     */
    public String getJourneySummary(String routeId, String trainId) {
        List<EnhancedSegmentTravelDetail> details = calculateCompleteJourney(routeId, trainId);

        double totalDistance = details.stream()
                .mapToDouble(EnhancedSegmentTravelDetail::getLengthMeters)
                .sum();

        double totalTime = details.stream()
                .mapToDouble(EnhancedSegmentTravelDetail::getTimeSeconds)
                .sum();

        double avgSpeed = (totalDistance / 1000.0) / (totalTime / 3600.0);

        // Get route and train info
        RoutePathResult routePath = routePathService.resolveRoutePath(routeId);
        Train train = trainRepository.findById(trainId).orElseThrow();

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("JOURNEY PLAN: Route %s with Train %s\n", routeId, trainId));
        sb.append("=".repeat(80)).append("\n\n");

        // Route info
        sb.append("Route: ").append(routePath.getRoute().getName()).append("\n");
        sb.append(String.format("  Facilities: %s\n",
                routePath.getRoute().getFacilityIds().toString()));
        sb.append(String.format("  Type: %s\n", routePath.getRoute().getType()));
        sb.append(String.format("  Legs: %d\n", routePath.getLegs().size()));
        sb.append("\n");

        // Train info
        sb.append(String.format("Train: %s (%s)\n", train.getTrainId(), train.getOperator()));
        sb.append(String.format("  Locomotives: %d × %.0fkW = %.0fkW total\n",
                train.getLocomotiveCount(),
                train.getLocomotives().get(0).getPowerKw(),
                train.getTotalPowerKw()));
        sb.append(String.format("  Wagons: %d\n", train.getWagonCount()));
        sb.append(String.format("  Total weight: %.1ft\n", train.getTotalWeightTons()));
        sb.append(String.format("  Power-to-weight: %.2f kW/t\n", train.getPowerToWeightRatio()));
        sb.append("\n");

        // Journey statistics
        sb.append("Journey Statistics:\n");
        sb.append(String.format("  Total distance: %.2f km\n", totalDistance / 1000.0));
        sb.append(String.format("  Total time: %.1f minutes (%.2f hours)\n",
                totalTime / 60.0, totalTime / 3600.0));
        sb.append(String.format("  Average speed: %.1f km/h\n", avgSpeed));
        sb.append(String.format("  Segments: %d\n", details.size()));
        sb.append("\n");

        // Detailed segment breakdown
        sb.append("Segment-by-Segment Breakdown:\n");
        sb.append("-".repeat(80)).append("\n");

        for (EnhancedSegmentTravelDetail detail : details) {
            sb.append(detail.toDetailedString()).append("\n\n");
        }

        return sb.toString();
    }

    /**
     * Calculates total journey time
     */
    public double calculateTotalTime(String routeId, String trainId) {
        List<EnhancedSegmentTravelDetail> details = calculateCompleteJourney(routeId, trainId);
        return details.stream()
                .mapToDouble(EnhancedSegmentTravelDetail::getTimeSeconds)
                .sum();
    }

    /**
     * Calculates total journey distance
     */
    public double calculateTotalDistance(String routeId, String trainId) {
        List<EnhancedSegmentTravelDetail> details = calculateCompleteJourney(routeId, trainId);
        return details.stream()
                .mapToDouble(EnhancedSegmentTravelDetail::getLengthMeters)
                .sum();
    }
}