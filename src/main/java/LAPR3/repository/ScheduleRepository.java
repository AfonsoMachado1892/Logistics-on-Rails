package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Schedule;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for Schedule entities
 * Clean version - loads from database, no automatic ID generation
 */
public class ScheduleRepository {

    private final List<Schedule> schedules;

    public ScheduleRepository() {
        this.schedules = new ArrayList<>();
    }

    public void loadFromDatabase() {
        this.schedules.clear();

        String query = "SELECT schedule_id, train_id, route_id, departure_date, " +
                "       departure_time, status " +
                "FROM TRAIN_SCHEDULE " +
                "ORDER BY departure_date, departure_time";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String scheduleId = rs.getString("schedule_id");
                String trainId = String.valueOf(rs.getInt("train_id"));
                String routeId = rs.getString("route_id");

                Date sqlDate = rs.getDate("departure_date");
                LocalDate departureDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                String timeStr = rs.getString("departure_time");
                LocalTime departureTime = parseTime(timeStr);

                LocalDateTime departureDateTime = null;
                if (departureDate != null && departureTime != null) {
                    departureDateTime = LocalDateTime.of(departureDate, departureTime);
                }

                String statusStr = rs.getString("status");
                Schedule.ScheduleStatus status = parseStatus(statusStr);

                Schedule schedule = new Schedule(
                        scheduleId,
                        trainId,
                        routeId,
                        departureDateTime,
                        status
                );

                schedules.add(schedule);
            }

            System.out.println("[DB Load] Schedules carregadas: " + schedules.size());

        } catch (SQLException e) {
            System.err.println("Erro ao carregar Schedules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            if (timeStr.length() == 5) {
                return LocalTime.parse(timeStr + ":00");
            }
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            System.err.println("Erro ao fazer parse de time: " + timeStr);
            return null;
        }
    }

    private Schedule.ScheduleStatus parseStatus(String statusStr) {
        if (statusStr == null) {
            return Schedule.ScheduleStatus.SCHEDULED;
        }
        try {
            return Schedule.ScheduleStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return Schedule.ScheduleStatus.SCHEDULED;
        }
    }

    public Optional<Schedule> findById(String scheduleId) {
        return schedules.stream()
                .filter(s -> s.getScheduleId().equals(scheduleId))
                .findFirst();
    }

    public List<Schedule> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return schedules.stream()
                .filter(s -> {
                    LocalDateTime departure = s.getDepartureDateTime();
                    if (departure == null) return false;
                    return !departure.isBefore(start) && !departure.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    public List<Schedule> findByDate(LocalDate date) {
        return schedules.stream()
                .filter(s -> {
                    LocalDateTime departure = s.getDepartureDateTime();
                    return departure != null && departure.toLocalDate().equals(date);
                })
                .collect(Collectors.toList());
    }

    public boolean exists(String scheduleId) {
        return schedules.stream()
                .anyMatch(s -> s.getScheduleId().equals(scheduleId));
    }

    public List<Schedule> findAll() {
        return new ArrayList<>(schedules);
    }

    /**
     * Returns count of schedules (needed by UI menus)
     */
    public int count() {
        return schedules.size();
    }

    /**
     * Save schedule (needed by UI menu)
     */
    public void save(Schedule schedule) {
        if (!exists(schedule.getScheduleId())) {
            schedules.add(schedule);
        }
    }
}