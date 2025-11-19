package LAPR3.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a scheduled train dispatch with departure time and route.
 * Part of USLP07 - Train Scheduling
 */
public class Schedule {
    private final String scheduleId;
    private final String trainId;
    private final String routeId;
    private final LocalDateTime departureDateTime;
    private ScheduleStatus status;

    /**
     * Status of the scheduled train
     */
    public enum ScheduleStatus {
        SCHEDULED,   // Planned but not yet running
        RUNNING,     // Currently in transit
        COMPLETED,   // Arrived at destination
        CANCELLED    // Cancelled before completion
    }

    /**
     * Creates a new Schedule
     *
     * @param scheduleId unique schedule identifier
     * @param trainId train composition ID
     * @param routeId route to follow
     * @param departureDateTime planned departure date and time
     * @param status current status
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Schedule(String scheduleId, String trainId, String routeId,
                    LocalDateTime departureDateTime, ScheduleStatus status) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Schedule ID cannot be null or empty");
        }
        if (trainId == null || trainId.trim().isEmpty()) {
            throw new IllegalArgumentException("Train ID cannot be null or empty");
        }
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be null or empty");
        }
        if (departureDateTime == null) {
            throw new IllegalArgumentException("Departure date/time cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        this.scheduleId = scheduleId.trim();
        this.trainId = trainId.trim();
        this.routeId = routeId.trim();
        this.departureDateTime = departureDateTime;
        this.status = status;
    }

    /**
     * Gets the schedule identifier
     * @return schedule ID
     */
    public String getScheduleId() {
        return scheduleId;
    }

    /**
     * Gets the train identifier
     * @return train ID
     */
    public String getTrainId() {
        return trainId;
    }

    /**
     * Gets the route identifier
     * @return route ID
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Gets the planned departure date and time
     * @return departure date/time
     */
    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    /**
     * Gets the current status
     * @return schedule status
     */
    public ScheduleStatus getStatus() {
        return status;
    }

    /**
     * Updates the schedule status
     * @param newStatus new status
     * @throws IllegalArgumentException if status transition is invalid
     */
    public void setStatus(ScheduleStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Validate status transitions
        if (status == ScheduleStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change status of completed schedule");
        }
        if (status == ScheduleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of cancelled schedule");
        }

        this.status = newStatus;
    }

    /**
     * Checks if schedule is active (scheduled or running)
     * @return true if schedule is scheduled or running
     */
    public boolean isActive() {
        return status == ScheduleStatus.SCHEDULED || status == ScheduleStatus.RUNNING;
    }

    /**
     * Checks if schedule is completed or cancelled
     * @return true if schedule is finished
     */
    public boolean isFinished() {
        return status == ScheduleStatus.COMPLETED || status == ScheduleStatus.CANCELLED;
    }

    /**
     * Checks if departure time is in the past
     * @param currentTime reference time to compare
     * @return true if departure is before current time
     */
    public boolean hasDeparted(LocalDateTime currentTime) {
        return departureDateTime.isBefore(currentTime);
    }

    /**
     * Checks if departure time is in the future
     * @param currentTime reference time to compare
     * @return true if departure is after current time
     */
    public boolean isPending(LocalDateTime currentTime) {
        return departureDateTime.isAfter(currentTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return scheduleId.equals(schedule.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId);
    }

    @Override
    public String toString() {
        return String.format("Schedule{id='%s', train='%s', route='%s', departure=%s, status=%s}",
                scheduleId, trainId, routeId, departureDateTime, status);
    }
}