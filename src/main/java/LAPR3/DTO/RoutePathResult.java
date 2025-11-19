package LAPR3.DTO;

import LAPR3.model.Route;

import java.util.Collections;
import java.util.List;

/**
 * Result of resolving a route into legs with concrete segments
 * Part of USLP07 - Subtask #65
 */
public class RoutePathResult {
    private final Route route;
    private final List<RouteLeg> legs;
    private final boolean isValid;
    private final String errorMessage;

    /**
     * Creates a successful route path result
     *
     * @param route the route
     * @param legs list of route legs
     */
    public RoutePathResult(Route route, List<RouteLeg> legs) {
        this.route = route;
        this.legs = legs;
        this.isValid = true;
        this.errorMessage = null;
    }

    /**
     * Creates a failed route path result
     *
     * @param route the route
     * @param errorMessage error description
     */
    public RoutePathResult(Route route, String errorMessage) {
        this.route = route;
        this.legs = Collections.emptyList();
        this.isValid = false;
        this.errorMessage = errorMessage;
    }

    public Route getRoute() {
        return route;
    }

    public List<RouteLeg> getLegs() {
        return Collections.unmodifiableList(legs);
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets total distance of the route
     * @return total distance in meters
     */
    public double getTotalDistance() {
        return legs.stream()
                .mapToDouble(RouteLeg::getTotalDistance)
                .sum();
    }

    /**
     * Gets total number of segments
     * @return total segment count
     */
    public int getTotalSegmentCount() {
        return legs.stream()
                .mapToInt(RouteLeg::getSegmentCount)
                .sum();
    }

    /**
     * Checks if entire route is electrified
     * @return true if all legs are electrified
     */
    public boolean isFullyElectrified() {
        return legs.stream().allMatch(RouteLeg::isFullyElectrified);
    }

    /**
     * Checks if route has any single-track segments
     * @return true if at least one single-track segment exists
     */
    public boolean hasSingleTrackSegments() {
        return legs.stream().anyMatch(RouteLeg::hasSingleTrackSegments);
    }

    /**
     * Gets list of all lines used in the route
     * @return list of line names
     */
    public List<String> getLineNames() {
        return legs.stream()
                .map(leg -> leg.getLine().getName())
                .distinct()
                .toList();
    }

    @Override
    public String toString() {
        if (!isValid) {
            return String.format("Invalid Route %s: %s", route.getRouteId(), errorMessage);
        }

        return String.format("Route %s (%s): %.2fkm, %d legs, %d segments [%s]",
                route.getRouteId(), route.getName(),
                getTotalDistance() / 1000.0,
                legs.size(),
                getTotalSegmentCount(),
                isFullyElectrified() ? "fully electrified" : "mixed electrification");
    }

    /**
     * Gets detailed string representation
     */
    public String toDetailedString() {
        if (!isValid) {
            return toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(toString()).append("\n");
        sb.append("Legs:\n");
        for (int i = 0; i < legs.size(); i++) {
            sb.append(String.format("  %d. %s\n", i + 1, legs.get(i)));
        }
        return sb.toString();
    }
}