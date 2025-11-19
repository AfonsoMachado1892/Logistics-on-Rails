package LAPR3.repository;

import LAPR3.model.Schedule;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for managing Schedule data
 * Loads from CSV and provides query methods
 * Part of USLP07 - Subtask #62
 */
public class ScheduleRepository {
    private final Map<String, Schedule> schedules;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public ScheduleRepository() {
        this.schedules = new HashMap<>();
    }

    /**
     * Loads schedules from CSV file
     *
     * @param filePath path to schedules.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        schedules.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Schedule schedule = parseScheduleFromCSV(line);
                    schedules.put(schedule.getScheduleId(), schedule);
                } catch (Exception e) {
                    System.err.println("Error parsing schedule line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        System.out.println("Loaded " + schedules.size() + " schedules");
    }

    /**
     * Parses a schedule from CSV line
     * Format: scheduleId,trainId,routeId,departureDateTime,status
     */
    private Schedule parseScheduleFromCSV(String line) {
        String[] parts = line.split(",");

        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid CSV format: expected 5 fields");
        }

        String scheduleId = parts[0].trim();
        String trainId = parts[1].trim();
        String routeId = parts[2].trim();
        LocalDateTime departureDateTime = LocalDateTime.parse(parts[3].trim(), DATE_FORMATTER);
        Schedule.ScheduleStatus status = Schedule.ScheduleStatus.valueOf(parts[4].trim());

        return new Schedule(scheduleId, trainId, routeId, departureDateTime, status);
    }

    /**
     * Finds a schedule by ID
     */
    public Optional<Schedule> findById(String scheduleId) {
        return Optional.ofNullable(schedules.get(scheduleId));
    }

    /**
     * Gets all schedules
     */
    public List<Schedule> findAll() {
        return new ArrayList<>(schedules.values());
    }

    /**
     * Finds schedules by train ID
     */
    public List<Schedule> findByTrainId(String trainId) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : schedules.values()) {
            if (schedule.getTrainId().equals(trainId)) {
                result.add(schedule);
            }
        }
        return result;
    }

    /**
     * Finds schedules by route ID
     */
    public List<Schedule> findByRouteId(String routeId) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : schedules.values()) {
            if (schedule.getRouteId().equals(routeId)) {
                result.add(schedule);
            }
        }
        return result;
    }

    /**
     * Finds schedules by status
     */
    public List<Schedule> findByStatus(Schedule.ScheduleStatus status) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : schedules.values()) {
            if (schedule.getStatus() == status) {
                result.add(schedule);
            }
        }
        return result;
    }

    /**
     * Finds active schedules (SCHEDULED or RUNNING)
     */
    public List<Schedule> findActiveSchedules() {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : schedules.values()) {
            if (schedule.isActive()) {
                result.add(schedule);
            }
        }
        return result;
    }

    /**
     * Finds schedules within a date range
     */
    public List<Schedule> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : schedules.values()) {
            LocalDateTime departure = schedule.getDepartureDateTime();
            if (!departure.isBefore(start) && !departure.isAfter(end)) {
                result.add(schedule);
            }
        }
        return result;
    }

    /**
     * Finds schedules departing on a specific date (ignoring time)
     */
    public List<Schedule> findByDate(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return findByDateRange(startOfDay, endOfDay);
    }

    /**
     * Finds schedules sorted by departure time
     */
    public List<Schedule> findAllSortedByDeparture() {
        return schedules.values().stream()
                .sorted(Comparator.comparing(Schedule::getDepartureDateTime))
                .collect(Collectors.toList());
    }

    /**
     * Finds upcoming schedules (after current time)
     */
    public List<Schedule> findUpcoming(LocalDateTime currentTime) {
        List<Schedule> result = new ArrayList<>();
        for (Schedule schedule : schedules.values()) {
            if (schedule.isPending(currentTime) && schedule.isActive()) {
                result.add(schedule);
            }
        }
        // Sort by departure time
        result.sort(Comparator.comparing(Schedule::getDepartureDateTime));
        return result;
    }

    /**
     * Gets the number of schedules in the repository
     */
    public int count() {
        return schedules.size();
    }

    /**
     * Checks if repository contains a schedule with given ID
     */
    public boolean exists(String scheduleId) {
        return schedules.containsKey(scheduleId);
    }

    /**
     * Updates a schedule's status
     */
    public void updateStatus(String scheduleId, Schedule.ScheduleStatus newStatus) {
        Schedule schedule = schedules.get(scheduleId);
        if (schedule != null) {
            schedule.setStatus(newStatus);
        }
    }
}