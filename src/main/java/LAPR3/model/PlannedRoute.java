package LAPR3.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a planned route for USLP08
 * Contains ACTIONS (LOAD/UNLOAD) but NO PATH initially
 * Path will be calculated later in USLP10
 *
 * Stored IN-MEMORY only (not in database)
 */
public class PlannedRoute {
    private final String id;
    private final RouteType type;
    private final List<Integer> freightIds;
    private final List<RouteAction> actions;
    private List<Integer> path;  // Will be set by USLP10
    private String assignedTrainId;  // Will be set by USLP09

    public enum RouteType {
        SIMPLE,     // 1 freight
        COMPOSITE   // 2+ freights
    }

    public PlannedRoute(String id, RouteType type, List<Integer> freightIds,
                        List<RouteAction> actions) {
        this.id = id;
        this.type = type;
        this.freightIds = new ArrayList<>(freightIds);
        this.actions = new ArrayList<>(actions);
        this.path = null;
        this.assignedTrainId = null;
    }

    // Getters
    public String getId() { return id; }
    public RouteType getType() { return type; }
    public List<Integer> getFreightIds() { return new ArrayList<>(freightIds); }
    public List<RouteAction> getActions() { return new ArrayList<>(actions); }
    public List<Integer> getPath() { return path != null ? new ArrayList<>(path) : null; }
    public String getAssignedTrainId() { return assignedTrainId; }

    public boolean hasPath() { return path != null && !path.isEmpty(); }
    public boolean isAssigned() { return assignedTrainId != null; }

    // Setters
    public void setPath(List<Integer> path) {
        this.path = path != null ? new ArrayList<>(path) : null;
    }

    public void setAssignedTrainId(String trainId) {
        this.assignedTrainId = trainId;
    }

    // Helper methods
    public List<Integer> getMandatoryFacilities() {
        return actions.stream()
                .map(RouteAction::getFacilityId)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<RouteAction> getActionsAt(int facilityId) {
        return actions.stream()
                .filter(a -> a.getFacilityId() == facilityId)
                .collect(Collectors.toList());
    }

    public boolean isSimple() { return type == RouteType.SIMPLE; }
    public boolean isComposite() { return type == RouteType.COMPOSITE; }

    @Override
    public String toString() {
        return String.format("Route %s (%s) - %d freight(s)%s",
                id, type, freightIds.size(),
                isAssigned() ? " - Assigned to Train " + assignedTrainId : "");
    }
}