package LAPR3.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a freight train composition with locomotives and wagons.
 * Part of USLP07 - Train Composition Model
 */
public class Train {
    private final String trainId;
    private final String operator;
    private final List<Locomotive> locomotives;
    private final List<Wagon> wagons;
    private final double totalPayloadTons;

    /**
     * Creates a new Train
     *
     * @param trainId unique train identifier
     * @param operator operating company
     * @param locomotives list of locomotives (must have at least one)
     * @param wagons list of wagons (can be empty for locomotive-only movements)
     * @param totalPayloadTons total cargo weight in tons
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Train(String trainId, String operator, List<Locomotive> locomotives,
                 List<Wagon> wagons, double totalPayloadTons) {
        if (trainId == null || trainId.trim().isEmpty()) {
            throw new IllegalArgumentException("Train ID cannot be null or empty");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        if (locomotives == null || locomotives.isEmpty()) {
            throw new IllegalArgumentException("Train must have at least one locomotive");
        }
        if (wagons == null) {
            throw new IllegalArgumentException("Wagons list cannot be null");
        }
        if (totalPayloadTons < 0) {
            throw new IllegalArgumentException("Total payload cannot be negative");
        }

        // Validate that payload doesn't exceed wagon capacity
        double maxCapacity = wagons.stream()
                .mapToDouble(Wagon::getMaxPayloadTons)
                .sum();

        if (totalPayloadTons > maxCapacity) {
            throw new IllegalArgumentException(
                    String.format("Payload %.2ft exceeds total wagon capacity %.2ft",
                            totalPayloadTons, maxCapacity)
            );
        }

        this.trainId = trainId.trim();
        this.operator = operator.trim();
        this.locomotives = new ArrayList<>(locomotives);
        this.wagons = new ArrayList<>(wagons);
        this.totalPayloadTons = totalPayloadTons;
    }

    /**
     * Gets the train identifier
     * @return train ID
     */
    public String getTrainId() {
        return trainId;
    }

    /**
     * Gets the operator
     * @return operator name
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Gets the list of locomotives
     * @return unmodifiable list of locomotives
     */
    public List<Locomotive> getLocomotives() {
        return Collections.unmodifiableList(locomotives);
    }

    /**
     * Gets the list of wagons
     * @return unmodifiable list of wagons
     */
    public List<Wagon> getWagons() {
        return Collections.unmodifiableList(wagons);
    }

    /**
     * Gets the total payload being transported
     * @return total payload in tons
     */
    public double getTotalPayloadTons() {
        return totalPayloadTons;
    }

    /**
     * Calculates the combined power of all locomotives
     * @return total power in kilowatts
     */
    public double getTotalPowerKw() {
        return locomotives.stream()
                .mapToDouble(Locomotive::getPowerKw)
                .sum();
    }

    /**
     * Calculates total train weight (locomotives + wagons + payload)
     * @return total weight in tons
     */
    public double getTotalWeightTons() {
        double locomotiveWeight = locomotives.stream()
                .mapToDouble(Locomotive::getWeightTons)
                .sum();

        double wagonTareWeight = wagons.stream()
                .mapToDouble(Wagon::getTareWeightTons)
                .sum();

        return locomotiveWeight + wagonTareWeight + totalPayloadTons;
    }

    /**
     * Calculates total train length
     * @return total length in meters
     */
    public double getTotalLengthMeters() {
        double locomotiveLength = locomotives.stream()
                .mapToDouble(Locomotive::getLengthMeters)
                .sum();

        double wagonLength = wagons.stream()
                .mapToDouble(Wagon::getLengthMeters)
                .sum();

        return locomotiveLength + wagonLength;
    }

    /**
     * Calculates weight per meter (for track weight limit validation)
     * @return weight per meter in kg/m
     */
    public double getWeightPerMeterKg() {
        double totalWeightKg = getTotalWeightTons() * 1000.0;
        double totalLength = getTotalLengthMeters();

        if (totalLength == 0) {
            throw new IllegalStateException("Train has zero length");
        }

        return totalWeightKg / totalLength;
    }

    /**
     * Gets the minimum operational speed among all locomotives
     * This is the effective maximum speed for the train
     * @return minimum operational speed in km/h
     */
    public double getOperationalSpeedKmh() {
        return locomotives.stream()
                .mapToDouble(Locomotive::getOperationalSpeedKmh)
                .min()
                .orElse(0.0);
    }

    /**
     * Calculates power-to-weight ratio (indicator of train performance)
     * @return power-to-weight ratio in kW/ton
     */
    public double getPowerToWeightRatio() {
        double totalWeight = getTotalWeightTons();
        if (totalWeight == 0) {
            throw new IllegalStateException("Train has zero weight");
        }
        return getTotalPowerKw() / totalWeight;
    }

    /**
     * Checks if all locomotives are compatible with a given track gauge
     * @param trackGauge track gauge in millimeters
     * @return true if all locomotives are compatible
     */
    public boolean isCompatibleWithGauge(int trackGauge) {
        return locomotives.stream()
                .allMatch(loco -> loco.isCompatibleWithGauge(trackGauge));
    }

    /**
     * Checks if any locomotive requires electrification
     * @return true if at least one locomotive is electric
     */
    public boolean requiresElectrification() {
        return locomotives.stream()
                .anyMatch(Locomotive::requiresElectrification);
    }

    /**
     * Validates if train composition is physically valid
     * @return error message if invalid, empty string if valid
     */
    public String validate() {
        // Check gauge consistency
        if (!locomotives.isEmpty()) {
            int firstGauge = locomotives.get(0).getGauge();
            boolean allSameGauge = locomotives.stream()
                    .allMatch(loco -> loco.getGauge() == firstGauge);

            if (!allSameGauge) {
                return "All locomotives must have the same gauge";
            }

            if (!wagons.isEmpty()) {
                boolean wagonsCompatible = wagons.stream()
                        .allMatch(wagon -> wagon.isCompatibleWithGauge(firstGauge));

                if (!wagonsCompatible) {
                    return "All wagons must be compatible with locomotive gauge";
                }
            }
        }

        return ""; // Valid
    }

    /**
     * Gets number of locomotives in the train
     * @return locomotive count
     */
    public int getLocomotiveCount() {
        return locomotives.size();
    }

    /**
     * Gets number of wagons in the train
     * @return wagon count
     */
    public int getWagonCount() {
        return wagons.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Train train = (Train) o;
        return trainId.equals(train.trainId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainId);
    }

    @Override
    public String toString() {
        return String.format("Train{id='%s', operator='%s', locos=%d, wagons=%d, weight=%.1ft, power=%.0fkW}",
                trainId, operator, locomotives.size(), wagons.size(),
                getTotalWeightTons(), getTotalPowerKw());
    }
}