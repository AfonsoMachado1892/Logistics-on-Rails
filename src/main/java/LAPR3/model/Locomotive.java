package LAPR3.model;

import java.util.Objects;

public class Locomotive {
    private final int number;
    private final String name;
    private final String make;
    private final String model;
    private final int serviceYear;
    private final int numberBogies;
    private final String bogiesType;
    private final double powerKw;
    private final double lengthMeters;
    private final double widthMeters;
    private final double heightMeters;
    private final double weightTons;
    private final double maxSpeedKmh;
    private final double operationalSpeedKmh;
    private final double tractionKn;
    private final LocomotiveType type;
    private final String voltage;
    private final String frequency;
    private final String operator;
    private final int gauge;
    private final Double fuelCapacityLiters; // null for electric locomotives

    public enum LocomotiveType {
        ELECTRIC, DIESEL
    }

    /**
     * Creates a new Locomotive
     */
    public Locomotive(int number, String name, String make, String model,
                      int serviceYear, int numberBogies, String bogiesType,
                      double powerKw, double lengthMeters, double widthMeters,
                      double heightMeters, double weightTons, double maxSpeedKmh,
                      double operationalSpeedKmh, double tractionKn,
                      LocomotiveType type, String voltage, String frequency,
                      String operator, int gauge, Double fuelCapacityLiters) {
        if (number < 0) {
            throw new IllegalArgumentException("Locomotive number cannot be negative");
        }
        if (operationalSpeedKmh <= 0) {
            throw new IllegalArgumentException("Operational speed must be positive");
        }
        if (gauge <= 0) {
            throw new IllegalArgumentException("Gauge must be positive");
        }
        if (type == LocomotiveType.DIESEL && (fuelCapacityLiters == null || fuelCapacityLiters <= 0)) {
            throw new IllegalArgumentException("Diesel locomotives must have positive fuel capacity");
        }
        if (type == LocomotiveType.ELECTRIC && fuelCapacityLiters != null) {
            throw new IllegalArgumentException("Electric locomotives cannot have fuel capacity");
        }

        this.number = number;
        this.name = name != null ? name.trim() : "";
        this.make = make != null ? make.trim() : "";
        this.model = model != null ? model.trim() : "";
        this.serviceYear = serviceYear;
        this.numberBogies = numberBogies;
        this.bogiesType = bogiesType != null ? bogiesType.trim() : "";
        this.powerKw = powerKw;
        this.lengthMeters = lengthMeters;
        this.widthMeters = widthMeters;
        this.heightMeters = heightMeters;
        this.weightTons = weightTons;
        this.maxSpeedKmh = maxSpeedKmh;
        this.operationalSpeedKmh = operationalSpeedKmh;
        this.tractionKn = tractionKn;
        this.type = type;
        this.voltage = voltage != null ? voltage.trim() : "";
        this.frequency = frequency != null ? frequency.trim() : "";
        this.operator = operator != null ? operator.trim() : "";
        this.gauge = gauge;
        this.fuelCapacityLiters = fuelCapacityLiters;
    }

    /**
     * Gets the locomotive number
     *
     * @return the locomotive number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the locomotive name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the manufacturer
     *
     * @return the manufacturer name
     */
    public String getMake() {
        return make;
    }

    /**
     * Gets the model
     *
     * @return the model name
     */
    public String getModel() {
        return model;
    }

    /**
     * Gets the service year
     *
     * @return the year it entered service
     */
    public int getServiceYear() {
        return serviceYear;
    }

    /**
     * Gets the number of bogies
     *
     * @return the number of bogies
     */
    public int getNumberBogies() {
        return numberBogies;
    }

    /**
     * Gets the bogies type
     *
     * @return the bogies type
     */
    public String getBogiesType() {
        return bogiesType;
    }

    /**
     * Gets the power in kilowatts
     *
     * @return the power in kW
     */
    public double getPowerKw() {
        return powerKw;
    }

    /**
     * Gets the length in meters
     *
     * @return the length in meters
     */
    public double getLengthMeters() {
        return lengthMeters;
    }

    /**
     * Gets the width in meters
     *
     * @return the width in meters
     */
    public double getWidthMeters() {
        return widthMeters;
    }

    /**
     * Gets the height in meters
     *
     * @return the height in meters
     */
    public double getHeightMeters() {
        return heightMeters;
    }

    /**
     * Gets the weight in tons
     *
     * @return the weight in tons
     */
    public double getWeightTons() {
        return weightTons;
    }

    /**
     * Gets the maximum speed in km/h
     *
     * @return maximum speed in km/h
     */
    public double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    /**
     * Gets the operational speed in km/h
     *
     * @return operational speed in km/h
     */
    public double getOperationalSpeedKmh() {
        return operationalSpeedKmh;
    }

    /**
     * Gets the traction force in kilonewtons
     *
     * @return traction in kN
     */
    public double getTractionKn() {
        return tractionKn;
    }

    /**
     * Gets the locomotive type
     *
     * @return ELECTRIC or DIESEL
     */
    public LocomotiveType getType() {
        return type;
    }

    /**
     * Gets the voltage (for electric locomotives)
     *
     * @return the voltage specification
     */
    public String getVoltage() {
        return voltage;
    }

    /**
     * Gets the frequency (for electric locomotives)
     *
     * @return the frequency specification
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Gets the operator
     *
     * @return the operator name
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Gets the gauge in millimeters
     *
     * @return the gauge in mm
     */
    public int getGauge() {
        return gauge;
    }

    /**
     * Gets the fuel capacity in liters (null for electric locomotives)
     *
     * @return fuel capacity in liters, or null
     */
    public Double getFuelCapacityLiters() {
        return fuelCapacityLiters;
    }

    /**
     * Checks if locomotive is compatible with a given gauge
     */
    public boolean isCompatibleWithGauge(int trackGauge) {
        return this.gauge == trackGauge;
    }

    /**
     * Checks if locomotive can run on electrified tracks
     */
    public boolean requiresElectrification() {
        return type == LocomotiveType.ELECTRIC;
    }

    /**
     * Calculate weight per bogie in tons
     */
    public double getWeightPerBogieTons() {
        if (numberBogies == 0) return 0;
        return weightTons / numberBogies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locomotive that = (Locomotive) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return String.format("Locomotive{number=%d, name='%s', type=%s, speed=%.1f km/h, gauge=%dmm}",
                number, name, type, operationalSpeedKmh, gauge);
    }
}
