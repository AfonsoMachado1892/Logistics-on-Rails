package LAPR3.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Route model - represents a planned route through multiple facilities
 */
public class Route {
    private String routeId;
    private String name;
    private RouteType type;
    private List<Integer> facilityIds;

    /**
     * Route types
     */
    public enum RouteType {
        SIMPLE,   // Direct A->B with single freight
        COMPLEX   // Multi-stop with multiple freights
    }

    // Constructor used by database loading
    public Route(String routeId, String name, RouteType type, List<Integer> facilityIds) {
        this.routeId = routeId;
        this.name = name;
        this.type = type;
        this.facilityIds = facilityIds != null ? new ArrayList<>(facilityIds) : new ArrayList<>();
    }

    // Constructor used by FreightManagerMenu.buildRoute()
    public Route(String routeId, String name, List<Integer> facilityIds, RouteType type) {
        this(routeId, name, type, facilityIds);
    }

    // Getters
    public String getRouteId() {
        return routeId;
    }

    public String getName() {
        return name;
    }

    public RouteType getType() {
        return type;
    }

    public List<Integer> getFacilityIds() {
        return new ArrayList<>(facilityIds);
    }

    // Setters
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public void setFacilityIds(List<Integer> facilityIds) {
        this.facilityIds = facilityIds != null ? new ArrayList<>(facilityIds) : new ArrayList<>();
    }

    /**
     * Get origin facility ID
     */
    public int getOriginFacilityId() {
        return facilityIds.isEmpty() ? -1 : facilityIds.get(0);
    }

    /**
     * Get destination facility ID
     */
    public int getDestinationFacilityId() {
        return facilityIds.isEmpty() ? -1 : facilityIds.get(facilityIds.size() - 1);
    }

    /**
     * Get number of facilities in route
     */
    public int getFacilityCount() {
        return facilityIds.size();
    }

    /**
     * Check if route contains a facility
     */
    public boolean containsFacility(int facilityId) {
        return facilityIds.contains(facilityId);
    }

    @Override
    public String toString() {
        return String.format("Route[id=%s, name=%s, type=%s, facilities=%d]",
                routeId, name, type, facilityIds.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return routeId != null && routeId.equals(route.routeId);
    }

    @Override
    public int hashCode() {
        return routeId != null ? routeId.hashCode() : 0;
    }
}