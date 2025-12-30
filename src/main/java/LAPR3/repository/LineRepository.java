package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Line;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LineRepository {

    private final List<Line> lines;

    public LineRepository() {
        this.lines = new ArrayList<>();
    }

    public void loadFromDatabase() {
        this.lines.clear();
        // JOIN para obter os nomes das estações imediatamente
        String query =
                "SELECT l.id, l.name, l.owner_vat, l.gauge, " +
                        "       l.start_facility_id, f1.name AS start_name, " +
                        "       l.end_facility_id, f2.name AS end_name " +
                        "FROM Line l " +
                        "JOIN Facility f1 ON l.start_facility_id = f1.id " +
                        "JOIN Facility f2 ON l.end_facility_id = f2.id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Line l = new Line(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("owner_vat"),
                        rs.getInt("start_facility_id"),
                        rs.getString("start_name"), // Nome vindo da BD
                        rs.getInt("end_facility_id"),
                        rs.getString("end_name"),   // Nome vindo da BD
                        rs.getDouble("gauge")
                );
                this.lines.add(l);
            }
            System.out.println("[DB Load] Lines carregadas com Nomes de Estação: " + lines.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Line> findAll() { return new ArrayList<>(this.lines); }

    public Optional<Line> findById(int id) {
        return lines.stream().filter(l -> l.getId() == id).findFirst();
    }

    /**
     * Finds a line connecting two facilities (in either direction)
     * @param originId origin facility ID
     * @param destinationId destination facility ID
     * @return Optional containing the line if found
     */
    public Optional<Line> findLineConnecting(int originId, int destinationId) {
        return lines.stream()
                .filter(l -> (l.getStartFacilityId() == originId && l.getEndFacilityId() == destinationId) ||
                        (l.getStartFacilityId() == destinationId && l.getEndFacilityId() == originId))
                .findFirst();
    }

    /**
     * Finds all facilities directly connected to a given facility
     * Returns a map of facility_id -> Line that connects them
     */
    public Map<Integer, Line> findDirectlyConnectedFacilities(int facilityId) {
        Map<Integer, Line> connected = new HashMap<>();

        for (Line line : lines) {
            if (line.getStartFacilityId() == facilityId) {
                connected.put(line.getEndFacilityId(), line);
            } else if (line.getEndFacilityId() == facilityId) {
                connected.put(line.getStartFacilityId(), line);
            }
        }

        return connected;
    }

    /**
     * Returns count of lines (needed by UI menus)
     */
    public int count() {
        return lines.size();
    }


}