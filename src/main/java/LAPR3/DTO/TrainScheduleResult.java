package LAPR3.DTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Complete schedule result for a train journey
 * Part of USLP07 - Subtask #66
 */
public class TrainScheduleResult {
    private final String scheduleId;
    private final String trainId;
    private final String routeId;
    private final LocalDateTime departureTime;
    private final List<FacilityETA> facilityETAs;
    private final List<EnhancedSegmentTravelDetail> segmentDetails;

    /**
     * Creates a train schedule result
     *
     * @param scheduleId schedule identifier
     * @param trainId train identifier
     * @param routeId route identifier
     * @param departureTime departure date/time
     * @param facilityETAs list of facility ETAs
     * @param segmentDetails list of segment travel details
     */
    public TrainScheduleResult(String scheduleId, String trainId, String routeId,
                               LocalDateTime departureTime,
                               List<FacilityETA> facilityETAs,
                               List<EnhancedSegmentTravelDetail> segmentDetails) {
        this.scheduleId = scheduleId;
        this.trainId = trainId;
        this.routeId = routeId;
        this.departureTime = departureTime;
        this.facilityETAs = facilityETAs;
        this.segmentDetails = segmentDetails;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getTrainId() {
        return trainId;
    }

    public String getRouteId() {
        return routeId;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public List<FacilityETA> getFacilityETAs() {
        return Collections.unmodifiableList(facilityETAs);
    }

    public List<EnhancedSegmentTravelDetail> getSegmentDetails() {
        return Collections.unmodifiableList(segmentDetails);
    }

    /**
     * Gets arrival time at destination
     * @return arrival time
     */
    public LocalDateTime getArrivalTime() {
        return facilityETAs.get(facilityETAs.size() - 1).getArrivalTime();
    }

    /**
     * Gets total journey time in minutes
     * @return journey time in minutes
     */
    public double getTotalJourneyTimeMinutes() {
        return facilityETAs.get(facilityETAs.size() - 1).getCumulativeTimeMinutes();
    }

    /**
     * Gets total distance in kilometers
     * @return distance in km
     */
    public double getTotalDistanceKm() {
        return facilityETAs.get(facilityETAs.size() - 1).getCumulativeDistanceKm();
    }

    /**
     * Gets average speed in km/h
     * @return average speed
     */
    public double getAverageSpeedKmh() {
        double timeHours = getTotalJourneyTimeMinutes() / 60.0;
        if (timeHours == 0) return 0;
        return getTotalDistanceKm() / timeHours;
    }

    /**
     * Gets number of stops (excluding origin)
     * @return stop count
     */
    public int getStopCount() {
        return (int) facilityETAs.stream()
                .filter(eta -> !eta.isOrigin() && eta.isMustStop())
                .count();
    }

    /**
     * Gets ETA for a specific facility
     * @param facilityId facility ID
     * @return facility ETA or null if not in route
     */
    public FacilityETA getETAForFacility(int facilityId) {
        return facilityETAs.stream()
                .filter(eta -> eta.getFacilityId() == facilityId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.format("Schedule %s: Train %s on Route %s | Depart: %s | Arrive: %s | %.2fkm in %.1fmin",
                scheduleId, trainId, routeId, departureTime, getArrivalTime(),
                getTotalDistanceKm(), getTotalJourneyTimeMinutes());
    }

    /**
     * Gets formatted schedule table
     */
    public String toScheduleTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(100)).append("\n");
        sb.append(String.format("TRAIN SCHEDULE: %s\n", scheduleId));
        sb.append("=".repeat(100)).append("\n");
        sb.append(String.format("Train: %s | Route: %s\n", trainId, routeId));
        sb.append(String.format("Departure: %s | Arrival: %s\n", departureTime, getArrivalTime()));
        sb.append(String.format("Distance: %.2f km | Time: %.1f minutes | Avg Speed: %.1f km/h | Stops: %d\n",
                getTotalDistanceKm(), getTotalJourneyTimeMinutes(), getAverageSpeedKmh(), getStopCount()));
        sb.append("\n");
        sb.append("Facility ETAs:\n");
        sb.append("-".repeat(100)).append("\n");

        for (FacilityETA eta : facilityETAs) {
            sb.append(eta.toString()).append("\n");
        }

        return sb.toString();
    }
}