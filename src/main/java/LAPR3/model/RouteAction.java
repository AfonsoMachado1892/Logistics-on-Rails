package LAPR3.model;

/**
 * Represents an action to be performed at a facility as part of a route
 * Used in USLP08 - Route Planning
 */
public class RouteAction {
    private final int facilityId;
    private final ActionType type;
    private final int freightId;
    private final int wagonCount;

    public enum ActionType {
        LOAD,    // Load wagons at this facility
        UNLOAD   // Unload wagons at this facility
    }

    public RouteAction(int facilityId, ActionType type, int freightId, int wagonCount) {
        this.facilityId = facilityId;
        this.type = type;
        this.freightId = freightId;
        this.wagonCount = wagonCount;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public ActionType getType() {
        return type;
    }

    public int getFreightId() {
        return freightId;
    }

    public int getWagonCount() {
        return wagonCount;
    }

    @Override
    public String toString() {
        return String.format("RouteAction{facility=%d, type=%s, freight=%d, wagons=%d}",
                facilityId, type, freightId, wagonCount);
    }
}