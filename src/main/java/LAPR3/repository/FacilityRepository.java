package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Facility;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FacilityRepository {

    private final List<Facility> facilities;

    public FacilityRepository() {
        this.facilities = new ArrayList<>();
    }

    public void loadFromDatabase() {
        this.facilities.clear();
        String query = "SELECT id, name FROM Facility ORDER BY id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Facility facility = new Facility(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                facilities.add(facility);
            }
            System.out.println("[DB Load] Facilities carregadas: " + facilities.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Facility> findAll() {
        return new ArrayList<>(facilities);
    }

    public Optional<Facility> findById(int id) {
        return facilities.stream()
                .filter(f -> f.getId() == id)
                .findFirst();
    }

    public int count() {
        return facilities.size();
    }
}