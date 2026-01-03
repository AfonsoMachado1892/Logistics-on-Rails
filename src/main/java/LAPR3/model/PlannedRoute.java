package LAPR3.model;

import java.util.List;

public class PlannedRoute {
    private final int id;
    private final List<Integer> freightIDs;
    private final List<Integer> facilityPath;

    public PlannedRoute(int id, List<Integer> freightIDs, List<Integer> facilityPath) {
        this.id = id;
        this.freightIDs = freightIDs;
        this.facilityPath = facilityPath;
    }

    public int getId() { return id; }
    public List<Integer> getFreightIDs() { return freightIDs; }
    public List<Integer> getFacilityPath() { return facilityPath; }

    public boolean isComplex() {
        return freightIDs.size() > 1; //
    }

    public String getRouteType() {
        return isComplex() ? "COMPLEX" : "SIMPLE"; //
    }
}