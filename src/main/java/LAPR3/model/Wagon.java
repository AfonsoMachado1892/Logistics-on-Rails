package LAPR3.model;

import java.util.Objects;

/**
 * Represents a railway freight wagon.
 * Part of USLP07 - Train Composition Model
 */
public class Wagon {
    private final String wagonId;
    private final WagonType type;
    private final String operator;
    private final int gauge;
    private final double tareWeightTons;
    private final double maxPayloadTons;
    private final double lengthMeters;
    private final double volumeM3;

    /**
     * Types of freight wagons according to cargo
     */
    public enum WagonType {
        BOXCAR,                 // General goods
        FLATCAR,                // Containers, vehicles, machinery
        HOPPER,                 // Coal, grains, minerals
        TANKER_CHEMICAL,        // Chemicals with varying densities
        TANKER_MINERAL_OIL,     // Petrochemicals and mineral oils
        TANKER_PRESSURE_GAS,    // Liquefied gases under pressure
        REFRIGERATED            // Perishable goods
    }

    /**
     * Creates a new Wagon
     *
     * @param wagonId unique wagon identifier
     * @param type wagon type
     * @param operator operating company
     * @param gauge track gauge in millimeters
     * @param tareWeightTons empty weight in tons
     * @param maxPayloadTons maximum cargo capacity in tons
     * @param lengthMeters wagon length in meters
     * @param volumeM3 internal volume in cubic meters (0 for flatcars)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Wagon(String wagonId, WagonType type, String operator, int gauge,
                 double tareWeightTons, double maxPayloadTons,
                 double lengthMeters, double volumeM3) {
        if (wagonId == null || wagonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Wagon ID cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Wagon type cannot be null");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        if (gauge <= 0) {
            throw new IllegalArgumentException("Gauge must be positive");
        }
        if (tareWeightTons <= 0) {
            throw new IllegalArgumentException("Tare weight must be positive");
        }
        if (maxPayloadTons <= 0) {
            throw new IllegalArgumentException("Max payload must be positive");
        }
        if (lengthMeters <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (volumeM3 < 0) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }

        this.wagonId = wagonId.trim();
        this.type = type;
        this.operator = operator.trim();
        this.gauge = gauge;
        this.tareWeightTons = tareWeightTons;
        this.maxPayloadTons = maxPayloadTons;
        this.lengthMeters = lengthMeters;
        this.volumeM3 = volumeM3;
    }

    /**
     * Gets the wagon identifier
     * @return wagon ID
     */
    public String getWagonId() {
        return wagonId;
    }

    /**
     * Gets the wagon type
     * @return wagon type
     */
    public WagonType getType() {
        return type;
    }

    /**
     * Gets the operator
     * @return operator name
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Gets the track gauge in millimeters
     * @return gauge in mm
     */
    public int getGauge() {
        return gauge;
    }

    /**
     * Gets the tare (empty) weight in tons
     * @return tare weight in tons
     */
    public double getTareWeightTons() {
        return tareWeightTons;
    }

    /**
     * Gets the maximum payload capacity in tons
     * @return max payload in tons
     */
    public double getMaxPayloadTons() {
        return maxPayloadTons;
    }

    /**
     * Gets the wagon length in meters
     * @return length in meters
     */
    public double getLengthMeters() {
        return lengthMeters;
    }

    /**
     * Gets the internal volume in cubic meters
     * @return volume in m³ (0 for flatcars)
     */
    public double getVolumeM3() {
        return volumeM3;
    }

    /**
     * Calculates total weight when loaded with specific payload
     *
     * @param loadedPayloadTons actual cargo weight in tons
     * @return total weight (tare + payload) in tons
     * @throws IllegalArgumentException if payload exceeds maximum
     */
    public double getTotalWeightTons(double loadedPayloadTons) {
        if (loadedPayloadTons < 0) {
            throw new IllegalArgumentException("Payload cannot be negative");
        }
        if (loadedPayloadTons > maxPayloadTons) {
            throw new IllegalArgumentException(
                    String.format("Payload %.2ft exceeds maximum capacity %.2ft",
                            loadedPayloadTons, maxPayloadTons)
            );
        }
        return tareWeightTons + loadedPayloadTons;
    }

    /**
     * Checks if wagon is compatible with a given track gauge
     *
     * @param trackGauge track gauge in millimeters
     * @return true if compatible
     */
    public boolean isCompatibleWithGauge(int trackGauge) {
        return this.gauge == trackGauge;
    }

    /**
     * Checks if wagon is currently empty (no payload)
     * Used for weight calculations when payload is unknown
     *
     * @return true if considering tare weight only
     */
    public boolean isEmpty() {
        return true; // Default assumption unless specified otherwise
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wagon wagon = (Wagon) o;
        return wagonId.equals(wagon.wagonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wagonId);
    }

    @Override
    public String toString() {
        return String.format("Wagon{id='%s', type=%s, gauge=%dmm, tare=%.1ft, maxLoad=%.1ft, length=%.2fm}",
                wagonId, type, gauge, tareWeightTons, maxPayloadTons, lengthMeters);
    }
}