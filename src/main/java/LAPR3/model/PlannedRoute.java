package LAPR3.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a planned route for USLP08
 * Contains ACTIONS (LOAD/UNLOAD) but NO PATH
 * Path will be calculated later in USLP10
 */
public class PlannedRoute {
    private final String id;
    private final RouteType type;
    private final List<Integer> freightIds;
    private final List<RouteAction> actions;
    private List<Integer> path;  // Will be set by USLP10

    public enum RouteType {
        SIMPLE,     // 1 freight
        COMPOSITE   // 2+ freights
    }

    /**
     * Constructor for USLP08 - Route Planning
     * Path is null initially, will be set by USLP10
     */
    public PlannedRoute(String id, RouteType type, List<Integer> freightIds,
                        List<RouteAction> actions) {
        this.id = id;
        this.type = type;
        this.freightIds = new ArrayList<>(freightIds);
        this.actions = new ArrayList<>(actions);
        this.path = null;  // NOT set in USLP08!
    }

    // Getters
    public String getId() {
        return id;
    }

    public RouteType getType() {
        return type;
    }

    public List<Integer> getFreightIds() {
        return new ArrayList<>(freightIds);
    }

    public List<RouteAction> getActions() {
        return new ArrayList<>(actions);
    }

    public List<Integer> getPath() {
        return path != null ? new ArrayList<>(path) : null;
    }

    public boolean hasPath() {
        return path != null && !path.isEmpty();
    }

    /**
     * Set path - called by USLP10 after path calculation
     */
    public void setPath(List<Integer> path) {
        this.path = path != null ? new ArrayList<>(path) : null;
    }

    /**
     * Get mandatory facilities (facilities where actions occur)
     */
    public List<Integer> getMandatoryFacilities() {
        return actions.stream()
                .map(RouteAction::getFacilityId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get actions at a specific facility
     */
    public List<RouteAction> getActionsAt(int facilityId) {
        return actions.stream()
                .filter(a -> a.getFacilityId() == facilityId)
                .collect(Collectors.toList());
    }

    public boolean isSimple() {
        return type == RouteType.SIMPLE;
    }

    public boolean isComposite() {
        return type == RouteType.COMPOSITE;
    }

    public String getRouteType() {
        return type.toString();
    }

    /**
     * Get list of facility IDs where cargo operations occur (for compatibility)
     */
    public List<Integer> getFacilityPath() {
        return getMandatoryFacilities();
    }

    @Override
    public String toString() {
        return String.format("PlannedRoute{id=%s, type=%s, freights=%d, actions=%d, hasPath=%s}",
                id, type, freightIds.size(), actions.size(), hasPath());
    }
}