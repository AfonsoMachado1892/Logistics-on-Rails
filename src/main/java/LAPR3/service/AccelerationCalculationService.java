package LAPR3.service;

import LAPR3.DTO.AccelerationProfile;
import LAPR3.model.Locomotive;
import LAPR3.model.Train;

/**
 * Service for calculating realistic acceleration profiles for trains
 * Uses physics-based model considering tractive effort and resistances
 * Part of USLP07 - Subtask #64
 */
public class AccelerationCalculationService {

    // Physical constants
    private static final double KMH_TO_MS = 1.0 / 3.6;  // km/h to m/s
    private static final double MS_TO_KMH = 3.6;        // m/s to km/h
    private static final double GRAVITY = 9.81;          // m/s²

    // Engineering constants for railway
    private static final double ROLLING_RESISTANCE_COEFFICIENT = 0.0015; // Dimensionless
    private static final double AIR_RESISTANCE_COEFFICIENT = 0.0025;      // kg/m
    private static final double STANDARD_BRAKING_RATE = 0.5;              // m/s² (freight trains)
    private static final double SAFETY_FACTOR = 0.85;                     // 85% of theoretical max

    /**
     * Calculates the acceleration profile for a train on a segment
     *
     * @param train the train composition
     * @param segmentLengthMeters segment length in meters
     * @param maxSpeedKmh maximum speed allowed on segment in km/h
     * @return acceleration profile with three phases
     */
    public AccelerationProfile calculateProfile(Train train, double segmentLengthMeters,
                                                double maxSpeedKmh) {
        // Calculate maximum acceleration rate
        double accelerationRate = calculateAccelerationRate(train);

        // Use standard braking rate for freight
        double decelerationRate = STANDARD_BRAKING_RATE;

        // Convert max speed to m/s
        double maxSpeedMs = maxSpeedKmh * KMH_TO_MS;

        // Calculate distances and times for acceleration and deceleration
        // Using kinematic equation: v² = u² + 2as → s = v² / (2a)
        double accelerationDistance = (maxSpeedMs * maxSpeedMs) / (2 * accelerationRate);
        double decelerationDistance = (maxSpeedMs * maxSpeedMs) / (2 * decelerationRate);

        // Check if segment is long enough to reach max speed
        double totalAccelDecelDistance = accelerationDistance + decelerationDistance;

        if (totalAccelDecelDistance >= segmentLengthMeters) {
            // Short segment: can't reach max speed
            return calculateShortSegmentProfile(train, segmentLengthMeters,
                    accelerationRate, decelerationRate);
        }

        // Long segment: can reach max speed
        double constantSpeedDistance = segmentLengthMeters - totalAccelDecelDistance;

        // Calculate times using: v = u + at → t = v / a
        double accelerationTime = maxSpeedMs / accelerationRate;
        double decelerationTime = maxSpeedMs / decelerationRate;
        double constantSpeedTime = constantSpeedDistance / maxSpeedMs;

        return new AccelerationProfile(
                accelerationDistance,
                accelerationTime,
                constantSpeedDistance,
                constantSpeedTime,
                decelerationDistance,
                decelerationTime,
                maxSpeedKmh,
                accelerationRate,
                decelerationRate
        );
    }

    /**
     * Calculates profile for short segments where max speed cannot be reached
     */
    private AccelerationProfile calculateShortSegmentProfile(Train train, double segmentLengthMeters,
                                                             double accelerationRate,
                                                             double decelerationRate) {
        // In short segments, train accelerates then immediately decelerates
        // Distance ratio based on acceleration/deceleration rates
        double totalRate = accelerationRate + decelerationRate;
        double accelerationDistance = segmentLengthMeters * (accelerationRate / totalRate);
        double decelerationDistance = segmentLengthMeters * (decelerationRate / totalRate);

        // Calculate peak speed reached at transition point
        // Using: v² = 2as
        double peakSpeedMs = Math.sqrt(2 * accelerationRate * accelerationDistance);
        double peakSpeedKmh = peakSpeedMs * MS_TO_KMH;

        // Calculate times
        double accelerationTime = peakSpeedMs / accelerationRate;
        double decelerationTime = peakSpeedMs / decelerationRate;

        return new AccelerationProfile(
                accelerationDistance,
                accelerationTime,
                0.0,  // No constant speed phase
                0.0,
                decelerationDistance,
                decelerationTime,
                peakSpeedKmh,
                accelerationRate,
                decelerationRate
        );
    }

    /**
     * Calculates the acceleration rate of a train
     * Based on tractive effort, weight, and resistances
     *
     * @param train the train composition
     * @return acceleration rate in m/s²
     */
    private double calculateAccelerationRate(Train train) {
        // Get total tractive effort from locomotives
        double totalTractiveEffortKN = train.getLocomotives().stream()
                .mapToDouble(Locomotive::getTractionKn)
                .sum();

        // Convert to Newtons
        double tractiveEffortN = totalTractiveEffortKN * 1000.0;

        // Get train mass in kg
        double massKg = train.getTotalWeightTons() * 1000.0;

        // Calculate resistances
        double rollingResistanceN = massKg * GRAVITY * ROLLING_RESISTANCE_COEFFICIENT;

        // Air resistance at low speeds (simplified - actual is speed-dependent)
        double airResistanceN = AIR_RESISTANCE_COEFFICIENT * train.getTotalLengthMeters() * 20.0;

        // Net force
        double netForceN = tractiveEffortN - rollingResistanceN - airResistanceN;

        if (netForceN <= 0) {
            throw new IllegalStateException(
                    "Train has insufficient tractive effort to overcome resistances"
            );
        }

        // Calculate acceleration: F = ma → a = F/m
        double theoreticalAcceleration = netForceN / massKg;

        // Apply safety factor (tracks, couplings, passenger comfort)
        return theoreticalAcceleration * SAFETY_FACTOR;
    }

    /**
     * Estimates time to reach a specific speed
     *
     * @param train the train
     * @param targetSpeedKmh target speed in km/h
     * @return time to reach speed in seconds
     */
    public double estimateTimeToSpeed(Train train, double targetSpeedKmh) {
        double accelerationRate = calculateAccelerationRate(train);
        double targetSpeedMs = targetSpeedKmh * KMH_TO_MS;
        return targetSpeedMs / accelerationRate;
    }

    /**
     * Estimates distance to reach a specific speed
     *
     * @param train the train
     * @param targetSpeedKmh target speed in km/h
     * @return distance to reach speed in meters
     */
    public double estimateDistanceToSpeed(Train train, double targetSpeedKmh) {
        double accelerationRate = calculateAccelerationRate(train);
        double targetSpeedMs = targetSpeedKmh * KMH_TO_MS;
        return (targetSpeedMs * targetSpeedMs) / (2 * accelerationRate);
    }

    /**
     * Calculates braking distance from a given speed
     *
     * @param currentSpeedKmh current speed in km/h
     * @return braking distance in meters
     */
    public double calculateBrakingDistance(double currentSpeedKmh) {
        double speedMs = currentSpeedKmh * KMH_TO_MS;
        return (speedMs * speedMs) / (2 * STANDARD_BRAKING_RATE);
    }

    /**
     * Gets the standard braking rate for freight trains
     * @return braking rate in m/s²
     */
    public double getStandardBrakingRate() {
        return STANDARD_BRAKING_RATE;
    }
}