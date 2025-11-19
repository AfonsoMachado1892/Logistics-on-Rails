package LAPR3.service;

import LAPR3.DTO.RouteLeg;
import LAPR3.DTO.RoutePathResult;
import LAPR3.model.Facility;
import LAPR3.model.Line;
import LAPR3.model.Route;
import LAPR3.model.Segment;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.LineRepository;
import LAPR3.repository.RouteRepository;
import LAPR3.repository.SegmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for resolving routes into concrete paths with segments
 * Handles multi-facility routes (A → B → C → D)
 * Part of USLP07 - Subtask #65
 */
public class RoutePathService {
    private final RouteRepository routeRepository;
    private final FacilityRepository facilityRepository;
    private final LineRepository lineRepository;
    private final SegmentRepository segmentRepository;

    public RoutePathService(RouteRepository routeRepository,
                            FacilityRepository facilityRepository,
                            LineRepository lineRepository,
                            SegmentRepository segmentRepository) {
        this.routeRepository = routeRepository;
        this.facilityRepository = facilityRepository;
        this.lineRepository = lineRepository;
        this.segmentRepository = segmentRepository;
    }

    /**
     * Resolves a route into a path with concrete legs and segments
     *
     * @param routeId route identifier
     * @return route path result with legs and segments
     */
    public RoutePathResult resolveRoutePath(String routeId) {
        // Get route
        Optional<Route> routeOpt = routeRepository.findById(routeId);
        if (routeOpt.isEmpty()) {
            return new RoutePathResult(null, "Route not found: " + routeId);
        }

        Route route = routeOpt.get();

        try {
            List<RouteLeg> legs = new ArrayList<>();

            // For each consecutive pair of facilities, find the connecting leg
            List<Integer> facilityIds = route.getFacilityIds();
            for (int i = 0; i < facilityIds.size() - 1; i++) {
                int fromId = facilityIds.get(i);
                int toId = facilityIds.get(i + 1);

                RouteLeg leg = resolveRouteLeg(fromId, toId);
                legs.add(leg);
            }

            return new RoutePathResult(route, legs);

        } catch (IllegalArgumentException e) {
            return new RoutePathResult(route, e.getMessage());
        }
    }

    /**
     * Resolves a single leg between two consecutive facilities
     *
     * @param fromFacilityId origin facility ID
     * @param toFacilityId destination facility ID
     * @return route leg with line and segments
     * @throws IllegalArgumentException if connection doesn't exist
     */
    private RouteLeg resolveRouteLeg(int fromFacilityId, int toFacilityId) {
        // Get facilities
        Facility from = facilityRepository.findById(fromFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Facility not found: " + fromFacilityId));

        Facility to = facilityRepository.findById(toFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Facility not found: " + toFacilityId));

        // Find connecting line
        Line line = lineRepository.findLineConnecting(fromFacilityId, toFacilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No direct line between %s and %s",
                                from.getName(), to.getName())));

        // Get segments of the line
        List<Segment> segments = segmentRepository.findByLineId(line.getId());
        if (segments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Line has no segments: " + line.getName());
        }

        return new RouteLeg(
                fromFacilityId, from.getName(),
                toFacilityId, to.getName(),
                line, segments
        );
    }

    /**
     * Validates if a route is traversable
     *
     * @param routeId route identifier
     * @return validation result with error message if invalid
     */
    public Optional<String> validateRoute(String routeId) {
        RoutePathResult result = resolveRoutePath(routeId);

        if (!result.isValid()) {
            return Optional.of(result.getErrorMessage());
        }

        return Optional.empty();
    }

    /**
     * Gets all segments for a route in order
     *
     * @param routeId route identifier
     * @return ordered list of all segments
     */
    public List<Segment> getAllSegments(String routeId) {
        RoutePathResult result = resolveRoutePath(routeId);

        if (!result.isValid()) {
            return List.of();
        }

        List<Segment> allSegments = new ArrayList<>();
        for (RouteLeg leg : result.getLegs()) {
            allSegments.addAll(leg.getSegments());
        }

        return allSegments;
    }

    /**
     * Calculates total distance of a route
     *
     * @param routeId route identifier
     * @return total distance in meters
     */
    public double calculateRouteDistance(String routeId) {
        RoutePathResult result = resolveRoutePath(routeId);

        if (!result.isValid()) {
            return 0.0;
        }

        return result.getTotalDistance();
    }

    /**
     * Finds routes connecting two facilities (with any intermediate stops)
     *
     * @param originId origin facility ID
     * @param destinationId destination facility ID
     * @return list of routes connecting these facilities
     */
    public List<Route> findRoutesConnecting(int originId, int destinationId) {
        return routeRepository.findConnecting(originId, destinationId);
    }

    /**
     * Gets the shortest route between two facilities
     *
     * @param originId origin facility ID
     * @param destinationId destination facility ID
     * @return shortest route or empty if none exists
     */
    public Optional<Route> findShortestRoute(int originId, int destinationId) {
        List<Route> routes = findRoutesConnecting(originId, destinationId);

        if (routes.isEmpty()) {
            return Optional.empty();
        }

        Route shortest = routes.get(0);
        double shortestDistance = calculateRouteDistance(shortest.getRouteId());

        for (Route route : routes) {
            double distance = calculateRouteDistance(route.getRouteId());
            if (distance < shortestDistance) {
                shortest = route;
                shortestDistance = distance;
            }
        }

        return Optional.of(shortest);
    }

    /**
     * Checks if a route requires electric locomotives
     *
     * @param routeId route identifier
     * @return true if any segment is electrified-only
     */
    public boolean requiresElectrification(String routeId) {
        RoutePathResult result = resolveRoutePath(routeId);

        if (!result.isValid()) {
            return false;
        }

        // If not fully electrified, diesel can run anywhere
        // Only return true if we want to enforce electric-only policy
        return result.isFullyElectrified();
    }

    /**
     * Gets summary of route characteristics
     *
     * @param routeId route identifier
     * @return formatted summary string
     */
    public String getRouteSummary(String routeId) {
        RoutePathResult result = resolveRoutePath(routeId);

        if (!result.isValid()) {
            return "Invalid route: " + result.getErrorMessage();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(result.toDetailedString());

        if (result.hasSingleTrackSegments()) {
            sb.append("\n⚠ Warning: Route contains single-track segments (crossing points may be needed)");
        }

        return sb.toString();
    }
}