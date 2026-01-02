package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Wagon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WagonRepository {

    private final List<Wagon> wagons;

    public WagonRepository() {
        this.wagons = new ArrayList<>();
    }

    public void loadFromDatabase() {
        this.wagons.clear();

        String query = "SELECT w.wagon_number, w.operator_vat, " +
                "       wm.model_name, wm.maker, wm.type_desc, " +
                "       wm.num_bogies, wm.bogies_type, " +
                "       wm.weight_tons, wm.payload_tons, wm.max_speed_kmh, " +
                "       wm.length_mm, wm.width_mm, wm.height_mm, " +
                "       wm.volume_m3, wm.gauge " +
                "FROM Wagon w " +
                "JOIN Wagon_Model wm ON w.model_id = wm.id " +
                "ORDER BY w.wagon_number";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Wagon wagon = new Wagon(
                        rs.getString("wagon_number"),
                        rs.getString("operator_vat"),
                        rs.getString("model_name"),
                        rs.getString("maker"),
                        rs.getString("type_desc"),
                        rs.getInt("num_bogies"),
                        rs.getString("bogies_type"),
                        rs.getDouble("weight_tons"),
                        rs.getDouble("payload_tons"),
                        rs.getDouble("max_speed_kmh"),
                        rs.getDouble("length_mm"),
                        rs.getDouble("width_mm"),
                        rs.getDouble("height_mm"),
                        rs.getDouble("volume_m3"),
                        rs.getDouble("gauge")
                );
                wagons.add(wagon);
            }
            System.out.println("[DB Load] Wagons carregados com dados completos: " + wagons.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Wagon> findAll() {
        return new ArrayList<>(wagons);
    }

    public Optional<Wagon> findById(String wagonNumber) {
        return wagons.stream()
                .filter(w -> w.getWagonNumber().equals(wagonNumber))
                .findFirst();
    }

    public int count() {
        return wagons.size();
    }
}