package LAPR3.service;

import LAPR3.DTO.SpeedCalculationResult;
import LAPR3.model.Segment;
import LAPR3.model.Train;

/**
 * Service for calculating maximum train speed on segments
 * Considers power-to-weight ratio, track weight limits, and locomotive capabilities
 * Part of USLP07 - Subtask #63
 */
public class SpeedCalculationService {

    // Physical constants and engineering factors
    private static final double MINIMUM_POWER_TO_WEIGHT_RATIO = 5.0;  // kW/ton (very heavy freight)
    private static final double OPTIMAL_POWER_TO_WEIGHT_RATIO = 25.0; // kW/ton (passenger-like performance)

    // Speed reduction factors based on power-to-weight ratio
    private static final double LOW_POWER_SPEED_FACTOR = 0.5;     // 50% of max speed
    private static final double MEDIUM_POWER_SPEED_FACTOR = 0.75; // 75% of max speed
    private static final double HIGH_POWER_SPEED_FACTOR = 1.0;    // 100% of max speed

    /**
     * Calculates the maximum speed a train can achieve on a specific segment
     *
     * @param train the train composition
     * @param segment the track segment
     * @return speed calculation result with details
     * @throws IllegalArgumentException if train cannot traverse the segment
     */
    public SpeedCalculationResult calculateMaxSpeed(Train train, Segment segment) {
        // Step 1: Validate weight per meter constraint
        validateWeightPerMeter(train, segment);

        // Step 2: Get base speed from locomotives
        double locomotiveMaxSpeed = train.getOperationalSpeedKmh();

        // Step 3: Calculate power-to-weight ratio
        double powerToWeightRatio = train.getPowerToWeightRatio();

        // Step 4: Determine speed factor based on power-to-weight ratio
        double speedFactor = calculateSpeedFactor(powerToWeightRatio);

        // Step 5: Calculate effective maximum speed
        double effectiveMaxSpeed = locomotiveMaxSpeed * speedFactor;

        // Step 6: Determine limiting factor
        SpeedCalculationResult.SpeedLimitingFactor limitingFactor = determineLimitingFactor(
                locomotiveMaxSpeed, effectiveMaxSpeed, powerToWeightRatio
        );

        // Step 7: Build detailed explanation
        String details = buildDetails(train, segment, powerToWeightRatio,
                speedFactor, locomotiveMaxSpeed, effectiveMaxSpeed);

        return new SpeedCalculationResult(
                segment.getId(),
                effectiveMaxSpeed,
                limitingFactor,
                details
        );
    }

    /**
     * Validates that train weight per meter does not exceed segment limit
     *
     * @param train the train
     * @param segment the segment
     * @throws IllegalArgumentException if weight limit is exceeded
     */
    private void validateWeightPerMeter(Train train, Segment segment) {
        double trainWeightPerMeter = train.getWeightPerMeterKg();
        double segmentWeightLimit = segment.getMaxWeightKgPerMeter();

        if (trainWeightPerMeter > segmentWeightLimit) {
            throw new IllegalArgumentException(
                    String.format(
                            "Train weight per meter (%.2f kg/m) exceeds segment limit (%.2f kg/m). " +
                                    "Train weight: %.1ft, Length: %.2fm",
                            trainWeightPerMeter, segmentWeightLimit,
                            train.getTotalWeightTons(), train.getTotalLengthMeters()
                    )
            );
        }
    }

    /**
     * Calculates speed factor based on power-to-weight ratio
     * Uses engineering approximation for freight train performance
     *
     * @param powerToWeightRatio ratio in kW/ton
     * @return speed factor (0.5 to 1.0)
     */
    private double calculateSpeedFactor(double powerToWeightRatio) {
        if (powerToWeightRatio >= OPTIMAL_POWER_TO_WEIGHT_RATIO) {
            // High power: can maintain full speed even with heavy load
            return HIGH_POWER_SPEED_FACTOR;
        } else if (powerToWeightRatio >= MINIMUM_POWER_TO_WEIGHT_RATIO * 2) {
            // Medium power: can maintain 75% of max speed
            return MEDIUM_POWER_SPEED_FACTOR;
        } else if (powerToWeightRatio >= MINIMUM_POWER_TO_WEIGHT_RATIO) {
            // Low power: limited to 50% of max speed (interpolate)
            double ratio = (powerToWeightRatio - MINIMUM_POWER_TO_WEIGHT_RATIO) /
                    (MINIMUM_POWER_TO_WEIGHT_RATIO);
            return LOW_POWER_SPEED_FACTOR +
                    (MEDIUM_POWER_SPEED_FACTOR - LOW_POWER_SPEED_FACTOR) * ratio;
        } else {
            // Very low power: minimum speed
            return LOW_POWER_SPEED_FACTOR;
        }
    }

    /**
     * Determines which factor is limiting the train speed
     */
    private SpeedCalculationResult.SpeedLimitingFactor determineLimitingFactor(
            double locomotiveMaxSpeed, double effectiveMaxSpeed, double powerToWeightRatio) {

        // If effective speed equals locomotive max speed, locomotive is the limit
        if (Math.abs(effectiveMaxSpeed - locomotiveMaxSpeed) < 0.1) {
            return SpeedCalculationResult.SpeedLimitingFactor.LOCOMOTIVE_MAX_SPEED;
        }

        // If power-to-weight ratio is low, that's the limiting factor
        if (powerToWeightRatio < OPTIMAL_POWER_TO_WEIGHT_RATIO) {
            return SpeedCalculationResult.SpeedLimitingFactor.POWER_TO_WEIGHT_RATIO;
        }

        // Default: locomotive speed
        return SpeedCalculationResult.SpeedLimitingFactor.LOCOMOTIVE_MAX_SPEED;
    }

    /**
     * Builds detailed explanation of the speed calculation
     */
    private String buildDetails(Train train, Segment segment, double powerToWeightRatio,
                                double speedFactor, double locomotiveMaxSpeed,
                                double effectiveMaxSpeed) {
        StringBuilder details = new StringBuilder();

        details.append(String.format("Train: %.0fkW total power, %.1ft total weight, %.2fm length | ",
                train.getTotalPowerKw(), train.getTotalWeightTons(), train.getTotalLengthMeters()));

        details.append(String.format("P/W ratio: %.2f kW/t | ", powerToWeightRatio));

        details.append(String.format("Weight/m: %.2f kg/m (limit: %.2f kg/m) | ",
                train.getWeightPerMeterKg(), segment.getMaxWeightKgPerMeter()));

        details.append(String.format("Loco max: %.1f km/h | ", locomotiveMaxSpeed));

        details.append(String.format("Speed factor: %.2f | ", speedFactor));

        details.append(String.format("Effective: %.1f km/h", effectiveMaxSpeed));

        return details.toString();
    }

    /**
     * Calculates recommended minimum power for a given train weight and desired speed
     * Utility method for planning purposes
     *
     * @param trainWeightTons total train weight in tons
     * @param desiredSpeedKmh desired operational speed in km/h
     * @return recommended minimum power in kW
     */
    public double calculateRecommendedPower(double trainWeightTons, double desiredSpeedKmh) {
        // For full speed capability with heavy loads
        return trainWeightTons * OPTIMAL_POWER_TO_WEIGHT_RATIO;
    }

    /**
     * Checks if a train has sufficient power for a desired speed
     *
     * @param train the train
     * @param desiredSpeedKmh desired speed in km/h
     * @return true if train can achieve desired speed
     */
    public boolean hasSufficientPower(Train train, double desiredSpeedKmh) {
        if (desiredSpeedKmh > train.getOperationalSpeedKmh()) {
            return false; // Can't exceed locomotive max speed
        }

        double powerToWeightRatio = train.getPowerToWeightRatio();
        double speedFactor = calculateSpeedFactor(powerToWeightRatio);
        double achievableSpeed = train.getOperationalSpeedKmh() * speedFactor;

        return achievableSpeed >= desiredSpeedKmh;
    }

    /**
     * Estimates number of locomotives needed for desired performance
     *
     * @param trainWeightTons total train weight in tons
     * @param locomotivePowerKw power of each locomotive in kW
     * @param desiredSpeedKmh desired operational speed in km/h
     * @return recommended number of locomotives
     */
    public int estimateLocomotivesNeeded(double trainWeightTons,
                                         double locomotivePowerKw,
                                         double desiredSpeedKmh) {
        double requiredPower = calculateRecommendedPower(trainWeightTons, desiredSpeedKmh);
        return (int) Math.ceil(requiredPower / locomotivePowerKw);
    }
}