package LAPR3.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an assembled train for USLP09
 * Contains locomotive(s), wagon(s), and route assignment
 *
 * Stored IN-MEMORY only (not in database)
 */
public class AssembledTrain {
    private final String id;
    private final String operatorVat;
    private final List<String> locomotiveIds;
    private final List<String> wagonIds;
    private final String routeId;
    private final int totalWagons;
    private final double totalPowerKw;
    private final double totalWeightTons;

    public AssembledTrain(String id, String operatorVat,
                          List<String> locomotiveIds, List<String> wagonIds,
                          String routeId, double totalPowerKw, double totalWeightTons) {
        this.id = id;
        this.operatorVat = operatorVat;
        this.locomotiveIds = new ArrayList<>(locomotiveIds);
        this.wagonIds = new ArrayList<>(wagonIds);
        this.routeId = routeId;
        this.totalWagons = wagonIds.size();
        this.totalPowerKw = totalPowerKw;
        this.totalWeightTons = totalWeightTons;
    }

    // Getters
    public String getId() { return id; }
    public String getOperatorVat() { return operatorVat; }
    public List<String> getLocomotiveIds() { return new ArrayList<>(locomotiveIds); }
    public List<String> getWagonIds() { return new ArrayList<>(wagonIds); }
    public String getRouteId() { return routeId; }
    public int getTotalWagons() { return totalWagons; }
    public double getTotalPowerKw() { return totalPowerKw; }
    public double getTotalWeightTons() { return totalWeightTons; }

    public double getPowerToWeightRatio() {
        return totalWeightTons > 0 ? totalPowerKw / totalWeightTons : 0;
    }

    @Override
    public String toString() {
        return String.format("Train %s | Route: %s | %d loco(s), %d wagon(s) | %.0fkW, %.1ft (%.2f kW/t)",
                id, routeId, locomotiveIds.size(), totalWagons,
                totalPowerKw, totalWeightTons, getPowerToWeightRatio());
    }
}