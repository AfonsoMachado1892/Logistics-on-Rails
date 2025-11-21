package LAPR3.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents estimated times for a facility in a schedule
 * Part of USLP07 - Subtask #66
 */
public class FacilityETA {
    private final int facilityId;
    private final String facilityName;
    private final LocalDateTime arrivalTime;
    private final LocalDateTime departureTime;
    private final boolean isOrigin;
    private final boolean isDestination;
    private final boolean mustStop;
    private final double cumulativeDistanceKm;
    private final double cumulativeTimeMinutes;

    /**
     * Creates a facility ETA
     *
     * @param facilityId facility identifier
     * @param facilityName facility name
     * @param arrivalTime estimated arrival time
     * @param departureTime estimated departure time (null if no stop)
     * @param isOrigin true if this is the origin
     * @param isDestination true if this is the destination
     * @param mustStop true if train must stop here
     * @param cumulativeDistanceKm distance from origin in km
     * @param cumulativeTimeMinutes travel time from origin in minutes
     */
    public FacilityETA(int facilityId, String facilityName,
                       LocalDateTime arrivalTime, LocalDateTime departureTime,
                       boolean isOrigin, boolean isDestination, boolean mustStop,
                       double cumulativeDistanceKm, double cumulativeTimeMinutes) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.isOrigin = isOrigin;
        this.isDestination = isDestination;
        this.mustStop = mustStop;
        this.cumulativeDistanceKm = cumulativeDistanceKm;
        this.cumulativeTimeMinutes = cumulativeTimeMinutes;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public boolean isOrigin() {
        return isOrigin;
    }

    public boolean isDestination() {
        return isDestination;
    }

    public boolean isMustStop() {
        return mustStop;
    }

    public double getCumulativeDistanceKm() {
        return cumulativeDistanceKm;
    }

    public double getCumulativeTimeMinutes() {
        return cumulativeTimeMinutes;
    }

    /**
     * Gets dwell time at this facility in minutes
     * @return dwell time or 0 if no stop
     */
    public double getDwellTimeMinutes() {
        if (departureTime == null || arrivalTime == null) {
            return 0.0;
        }
        return java.time.Duration.between(arrivalTime, departureTime).toMinutes();
    }

    /**
     * Checks if train stops at this facility
     * @return true if train stops
     */
    public boolean hasStop() {
        return mustStop || isOrigin || isDestination;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        if (isOrigin) {
            return String.format("%s | Departure: %s | Distance: %.2fkm | %s",
                    facilityName, departureTime.format(formatter),
                    cumulativeDistanceKm, "ORIGIN");
        } else if (isDestination) {
            return String.format("%s | Arrival: %s | Distance: %.2fkm | Time: %.1fmin | %s",
                    facilityName, arrivalTime.format(formatter),
                    cumulativeDistanceKm, cumulativeTimeMinutes, "DESTINATION");
        } else if (mustStop) {
            return String.format("%s | Arr: %s | Dep: %s | Distance: %.2fkm | Time: %.1fmin | STOP (%.0fmin)",
                    facilityName, arrivalTime.format(formatter), departureTime.format(formatter),
                    cumulativeDistanceKm, cumulativeTimeMinutes, getDwellTimeMinutes());
        } else {
            return String.format("%s | Pass: %s | Distance: %.2fkm | Time: %.1fmin | PASS-THROUGH",
                    facilityName, arrivalTime.format(formatter),
                    cumulativeDistanceKm, cumulativeTimeMinutes);
        }
    }
}