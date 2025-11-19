package LAPR3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a railway route - an ordered sequence of facilities.
 * Part of USLP07 - Route Management
 */
public class Route {
    private final String routeId;
    private final String name;
    private final List<Integer> facilityIds;
    private final RouteType type;

    /**
     * Route type according to freight complexity
     */
    public enum RouteType {
        SIMPLE,     // Single freight from origin to destination
        COMPLEX     // Multiple freights with intermediate loading/unloading
    }

    /**
     * Creates a new Route
     *
     * @param routeId unique route identifier
     * @param name descriptive route name
     * @param facilityIds ordered list of facility IDs (A → B → C → D)
     * @param type route type (SIMPLE or COMPLEX)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Route(String routeId, String name, List<Integer> facilityIds, RouteType type) {
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Route name cannot be null or empty");
        }
        if (facilityIds == null || facilityIds.size() < 2) {
            throw new IllegalArgumentException("Route must have at least 2 facilities");
        }
        if (type == null) {
            throw new IllegalArgumentException("Route type cannot be null");
        }

        // Check for duplicate facilities
        if (facilityIds.stream().distinct().count() != facilityIds.size()) {
            throw new IllegalArgumentException("Route cannot have duplicate facilities");
        }

        this.routeId = routeId.trim();
        this.name = name.trim();
        this.facilityIds = new ArrayList<>(facilityIds);
        this.type = type;
    }

    /**
     * Gets the route identifier
     * @return route ID
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Gets the route name
     * @return route name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the ordered list of facility IDs
     * @return unmodifiable list of facility IDs
     */
    public List<Integer> getFacilityIds() {
        return Collections.unmodifiableList(facilityIds);
    }

    /**
     * Gets the route type
     * @return route type (SIMPLE or COMPLEX)
     */
    public RouteType getType() {
        return type;
    }

    /**
     * Gets the origin (first) facility ID
     * @return origin facility ID
     */
    public int getOriginFacilityId() {
        return facilityIds.get(0);
    }

    /**
     * Gets the destination (last) facility ID
     * @return destination facility ID
     */
    public int getDestinationFacilityId() {
        return facilityIds.get(facilityIds.size() - 1);
    }

    /**
     * Gets all intermediate facilities (excluding origin and destination)
     * @return list of intermediate facility IDs (empty if direct route)
     */
    public List<Integer> getIntermediateFacilities() {
        if (facilityIds.size() <= 2) {
            return Collections.emptyList();
        }
        return facilityIds.subList(1, facilityIds.size() - 1);
    }

    /**
     * Gets the number of facilities in the route
     * @return facility count
     */
    public int getFacilityCount() {
        return facilityIds.size();
    }

    /**
     * Gets the number of segments (legs) in the route
     * For n facilities, there are (n-1) segments
     * @return segment count
     */
    public int getSegmentCount() {
        return facilityIds.size() - 1;
    }

    /**
     * Gets a pair of consecutive facilities for a given leg
     *
     * @param legIndex leg index (0 = first leg from origin)
     * @return array with [fromFacilityId, toFacilityId]
     * @throws IndexOutOfBoundsException if legIndex is invalid
     */
    public int[] getLeg(int legIndex) {
        if (legIndex < 0 || legIndex >= getSegmentCount()) {
            throw new IndexOutOfBoundsException(
                    String.format("Leg index %d out of bounds [0, %d)",
                            legIndex, getSegmentCount())
            );
        }
        return new int[] { facilityIds.get(legIndex), facilityIds.get(legIndex + 1) };
    }

    /**
     * Checks if route passes through a specific facility
     *
     * @param facilityId facility ID to check
     * @return true if route includes this facility
     */
    public boolean passesThrough(int facilityId) {
        return facilityIds.contains(facilityId);
    }

    /**
     * Gets the position of a facility in the route
     *
     * @param facilityId facility ID
     * @return position index (0-based), or -1 if not in route
     */
    public int getFacilityPosition(int facilityId) {
        return facilityIds.indexOf(facilityId);
    }

    /**
     * Checks if this is a direct route (no intermediate stops)
     * @return true if route has only origin and destination
     */
    public boolean isDirect() {
        return facilityIds.size() == 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return routeId.equals(route.routeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId);
    }

    @Override
    public String toString() {
        return String.format("Route{id='%s', name='%s', type=%s, facilities=%d}",
                routeId, name, type, facilityIds.size());
    }
}