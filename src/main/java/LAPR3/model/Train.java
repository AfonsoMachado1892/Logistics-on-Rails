package LAPR3.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Train model - represents a complete train with locomotives and wagons
 */
public class Train {
    private int id;
    private String operatorVat;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private int startFacilityId;
    private int endFacilityId;
    private List<Locomotive> locomotives;
    private List<Wagon> wagons;
    private List<Integer> path;

    // Constructors
    public Train() {
        this.locomotives = new ArrayList<>();
        this.wagons = new ArrayList<>();
        this.path = new ArrayList<>();
    }

    public Train(int id, String operatorVat, LocalDate departureDate, LocalTime departureTime,
                 int startFacilityId, int endFacilityId,
                 List<Locomotive> locomotives, List<Wagon> wagons, List<Integer> path) {
        this.id = id;
        this.operatorVat = operatorVat;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.startFacilityId = startFacilityId;
        this.endFacilityId = endFacilityId;
        this.locomotives = locomotives != null ? locomotives : new ArrayList<>();
        this.wagons = wagons != null ? wagons : new ArrayList<>();
        this.path = path != null ? path : new ArrayList<>();
    }

    public Train(int id, String opVat, String opName, LocalDate date, LocalTime time, int startId, int endId) {
    }

    // Basic Getters/Setters
    public int getId() { return id; }
    public String getTrainId() { return String.valueOf(id); } // Alias for compatibility
    public void setId(int id) { this.id = id; }

    public String getOperatorVat() { return operatorVat; }
    public String getOperator() { return operatorVat; } // Alias for compatibility
    public void setOperatorVat(String operatorVat) { this.operatorVat = operatorVat; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public int getStartFacilityId() { return startFacilityId; }
    public void setStartFacilityId(int startFacilityId) { this.startFacilityId = startFacilityId; }

    public int getEndFacilityId() { return endFacilityId; }
    public void setEndFacilityId(int endFacilityId) { this.endFacilityId = endFacilityId; }

    public List<Locomotive> getLocomotives() { return locomotives; }
    public void setLocomotives(List<Locomotive> locomotives) { this.locomotives = locomotives; }

    public List<Wagon> getWagons() { return wagons; }
    public void setWagons(List<Wagon> wagons) { this.wagons = wagons; }

    public List<Integer> getPath() { return path; }
    public void setPath(List<Integer> path) { this.path = path; }

    // ========== CALCULATED METHODS (needed by Services) ==========

    /**
     * Returns total weight in tons (locomotives + wagons)
     */
    public double getTotalWeightTons() {
        double locoWeight = locomotives.stream()
                .mapToDouble(Locomotive::getWeightTons)
                .sum();
        double wagonWeight = wagons.stream()
                .mapToDouble(w -> w.getWeightTons() + w.getPayloadTons()) // Empty + payload
                .sum();
        return locoWeight + wagonWeight;
    }

    /**
     * Returns total payload capacity in tons (only wagons payload)
     */
    public double getTotalPayloadTons() {
        return wagons.stream()
                .mapToDouble(Wagon::getPayloadTons)
                .sum();
    }

    /**
     * Returns total length in meters
     */
    public double getTotalLengthMeters() {
        double locoLength = locomotives.stream()
                .mapToDouble(Locomotive::getLengthM)
                .sum();
        double wagonLength = wagons.stream()
                .mapToDouble(w -> w.getLengthMm() / 1000.0) // Convert mm to m
                .sum();
        return locoLength + wagonLength;
    }

    /**
     * Returns total power in kW (sum of all locomotives)
     */
    public double getTotalPowerKw() {
        return locomotives.stream()
                .mapToDouble(Locomotive::getPowerKw)
                .sum();
    }

    /**
     * Returns power-to-weight ratio (kW/ton)
     */
    public double getPowerToWeightRatio() {
        double totalWeight = getTotalWeightTons();
        if (totalWeight == 0) return 0;
        return getTotalPowerKw() / totalWeight;
    }

    /**
     * Returns weight per meter (kg/m)
     */
    public double getWeightPerMeterKg() {
        double totalLength = getTotalLengthMeters();
        if (totalLength == 0) return 0;
        return (getTotalWeightTons() * 1000) / totalLength; // Convert tons to kg
    }

    /**
     * Returns operational speed (minimum of all locomotives' operational speeds)
     */
    public double getOperationalSpeedKmh() {
        if (locomotives.isEmpty()) return 0;
        return locomotives.stream()
                .mapToDouble(Locomotive::getOperationalSpeedKmh)
                .min()
                .orElse(0);
    }

    /**
     * Returns gauge of the train (assumes all locomotives have same gauge)
     */
    public double getGauge() {
        if (locomotives.isEmpty()) return 0;
        return locomotives.get(0).getGauge();
    }

    /**
     * Checks if train is compatible with a given gauge
     */
    public boolean isCompatibleWithGauge(double gauge) {
        // Check locomotives
        boolean locosCompatible = locomotives.stream()
                .allMatch(l -> Math.abs(l.getGauge() - gauge) < 0.01);

        // Check wagons
        boolean wagonsCompatible = wagons.stream()
                .allMatch(w -> Math.abs(w.getGauge() - gauge) < 0.01);

        return locosCompatible && wagonsCompatible;
    }

    /**
     * Returns number of locomotives
     */
    public int getLocomotiveCount() {
        return locomotives.size();
    }

    /**
     * Returns number of wagons
     */
    public int getWagonCount() {
        return wagons.size();
    }

    /**
     * Checks if train requires electrification (any electric locomotive)
     */
    public boolean requiresElectrification() {
        return locomotives.stream()
                .anyMatch(l -> "Electric".equalsIgnoreCase(l.getEngineType()));
    }

    @Override
    public String toString() {
        return String.format("Train[id=%d, operator=%s, locos=%d, wagons=%d, power=%.0fkW, weight=%.1ft]",
                id, operatorVat, locomotives.size(), wagons.size(), getTotalPowerKw(), getTotalWeightTons());
    }
    public void setPathFacilityIds(List<Integer> facilityIds) {
        this.path = facilityIds;
    }
}