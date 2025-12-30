package LAPR3.model;

import java.time.LocalDate;

/**
 * Locomotive model with complete technical specifications
 */
public class Locomotive {
    private String locoNumber;
    private String name;
    private String make;
    private String model;
    private LocalDate serviceStartDate;
    private String engineType; // "Electric" or "Diesel"
    private double powerKw;
    private double weightTons;
    private double maxSpeedKmh;
    private double tractionKn;
    private double lengthM;
    private double widthM;
    private double heightM;
    private int numBogies;
    private String bogiesType;
    private String voltage;
    private String frequency;
    private Double fuelCapacityL;
    private double gauge;
    private double operationalSpeedKmh; // Added field

    // Full constructor
    public Locomotive(String locoNumber, String name, String make, String model,
                      LocalDate serviceStartDate, String engineType, double powerKw,
                      double weightTons, double maxSpeedKmh, double tractionKn,
                      double lengthM, double widthM, double heightM,
                      int numBogies, String bogiesType, String voltage, String frequency,
                      Double fuelCapacityL, double gauge) {
        this.locoNumber = locoNumber;
        this.name = name;
        this.make = make;
        this.model = model;
        this.serviceStartDate = serviceStartDate;
        this.engineType = engineType;
        this.powerKw = powerKw;
        this.weightTons = weightTons;
        this.maxSpeedKmh = maxSpeedKmh;
        this.tractionKn = tractionKn;
        this.lengthM = lengthM;
        this.widthM = widthM;
        this.heightM = heightM;
        this.numBogies = numBogies;
        this.bogiesType = bogiesType;
        this.voltage = voltage;
        this.frequency = frequency;
        this.fuelCapacityL = fuelCapacityL;
        this.gauge = gauge;
        // Operational speed is typically 85-90% of max speed
        this.operationalSpeedKmh = maxSpeedKmh * 0.85;
    }

    // Getters
    public String getLocoNumber() { return locoNumber; }
    public String getNumber() { return locoNumber; } // Alias for compatibility
    public String getName() { return name; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public LocalDate getServiceStartDate() { return serviceStartDate; }
    public String getEngineType() { return engineType; }
    public double getPowerKw() { return powerKw; }
    public double getWeightTons() { return weightTons; }
    public double getMaxSpeedKmh() { return maxSpeedKmh; }
    public double getOperationalSpeedKmh() { return operationalSpeedKmh; }
    public double getTractionKn() { return tractionKn; }
    public double getLengthM() { return lengthM; }
    public double getWidthM() { return widthM; }
    public double getHeightM() { return heightM; }
    public int getNumBogies() { return numBogies; }
    public String getBogiesType() { return bogiesType; }
    public String getVoltage() { return voltage; }
    public String getFrequency() { return frequency; }
    public Double getFuelCapacityL() { return fuelCapacityL; }
    public double getGauge() { return gauge; }

    // Setters
    public void setLocoNumber(String locoNumber) { this.locoNumber = locoNumber; }
    public void setName(String name) { this.name = name; }
    public void setMake(String make) { this.make = make; }
    public void setModel(String model) { this.model = model; }
    public void setServiceStartDate(LocalDate serviceStartDate) { this.serviceStartDate = serviceStartDate; }
    public void setEngineType(String engineType) { this.engineType = engineType; }
    public void setPowerKw(double powerKw) { this.powerKw = powerKw; }
    public void setWeightTons(double weightTons) { this.weightTons = weightTons; }
    public void setMaxSpeedKmh(double maxSpeedKmh) {
        this.maxSpeedKmh = maxSpeedKmh;
        this.operationalSpeedKmh = maxSpeedKmh * 0.85;
    }
    public void setOperationalSpeedKmh(double operationalSpeedKmh) { this.operationalSpeedKmh = operationalSpeedKmh; }
    public void setTractionKn(double tractionKn) { this.tractionKn = tractionKn; }
    public void setLengthM(double lengthM) { this.lengthM = lengthM; }
    public void setWidthM(double widthM) { this.widthM = widthM; }
    public void setHeightM(double heightM) { this.heightM = heightM; }
    public void setNumBogies(int numBogies) { this.numBogies = numBogies; }
    public void setBogiesType(String bogiesType) { this.bogiesType = bogiesType; }
    public void setVoltage(String voltage) { this.voltage = voltage; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setFuelCapacityL(Double fuelCapacityL) { this.fuelCapacityL = fuelCapacityL; }
    public void setGauge(double gauge) { this.gauge = gauge; }

    /**
     * Checks if locomotive is compatible with a given gauge
     */
    public boolean isCompatibleWithGauge(double gauge) {
        return Math.abs(this.gauge - gauge) < 0.01;
    }

    /**
     * Checks if locomotive requires electrification
     */
    public boolean requiresElectrification() {
        return "Electric".equalsIgnoreCase(this.engineType);
    }

    /**
     * Enum for locomotive types (needed by TravelTimeMenu)
     */
    public enum LocomotiveType {
        ELECTRIC,
        DIESEL
    }

    /**
     * Get locomotive type as enum
     */
    public LocomotiveType getType() {
        if ("Electric".equalsIgnoreCase(this.engineType)) {
            return LocomotiveType.ELECTRIC;
        } else {
            return LocomotiveType.DIESEL;
        }
    }

    /**
     * Get service year (needed by TravelTimeMenu)
     */
    public int getServiceYear() {
        return serviceStartDate != null ? serviceStartDate.getYear() : 0;
    }

    /**
     * Get owner (alias for operator - needed for compatibility)
     */
    public String getOwner() {
        return "N/A"; // Owner is stored at Train level, not Locomotive
    }

    /**
     * Get operator (alias for owner - needed for compatibility)
     */
    public String getOperator() {
        return "N/A"; // Operator is stored at Train level, not Locomotive
    }

    @Override
    public String toString() {
        return String.format("Locomotive[%s, %s, %.0fkW, %.1ft, %dmm]",
                locoNumber, engineType, powerKw, weightTons, (int)gauge);
    }
}