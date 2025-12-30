package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Locomotive;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocomotiveRepository {

    private final List<Locomotive> locomotives;

    public LocomotiveRepository() {
        this.locomotives = new ArrayList<>();
    }

    public void loadFromDatabase() {
        this.locomotives.clear();
        String query = "SELECT * FROM Locomotive";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Date sqlDate = rs.getDate("service_start_date");
                LocalDate serviceStart = sqlDate != null ? sqlDate.toLocalDate() : null;

                // Tratamento de Fuel (pode ser null na BD) - CORRIGIDO para BigDecimal
                Double fuel = null;
                Object fuelObj = rs.getObject("fuel_capacity_l");
                if (fuelObj != null) {
                    if (fuelObj instanceof java.math.BigDecimal) {
                        fuel = ((java.math.BigDecimal) fuelObj).doubleValue();
                    } else if (fuelObj instanceof Number) {
                        fuel = ((Number) fuelObj).doubleValue();
                    }
                }

                Locomotive loc = new Locomotive(
                        rs.getString("loco_number"),
                        rs.getString("name"),
                        rs.getString("make"),
                        rs.getString("model"),
                        serviceStart,
                        rs.getString("engine_type"),
                        rs.getDouble("power_kw"),
                        rs.getDouble("weight_tons"),
                        rs.getDouble("max_speed_kmh"),
                        rs.getDouble("traction_kn"),
                        rs.getDouble("length_m"),
                        rs.getDouble("width_m"),
                        rs.getDouble("height_m"),
                        rs.getInt("num_bogies"),
                        rs.getString("bogies_type"),
                        rs.getString("voltage"),
                        rs.getString("frequency"),
                        fuel,
                        rs.getDouble("gauge")
                );
                locomotives.add(loc);
            }
            System.out.println("[DB Load] Locomotivas carregadas (Full Data): " + locomotives.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Locomotive> findAll() {
        return new ArrayList<>(locomotives);
    }

    /**
     * Finds a locomotive by its number (String)
     * @param number locomotive number
     * @return Optional containing the locomotive if found
     */
    public Optional<Locomotive> findByNumber(String number) {
        return locomotives.stream()
                .filter(l -> l.getNumber().equals(number))
                .findFirst();
    }

    /**
     * Finds a locomotive by its number (int) - converts to String
     * @param number locomotive number as integer
     * @return Optional containing the locomotive if found
     */
    public Optional<Locomotive> findByNumber(int number) {
        return findByNumber(String.valueOf(number));
    }

    /**
     * Returns count of locomotives (needed by UI menus)
     */
    public int count() {
        return locomotives.size();
    }

    /**
     * Finds locomotives by type (needed by UI menus)
     */
    public List<Locomotive> findByType(Locomotive.LocomotiveType type) {
        // Assuming Locomotive has getEngineType() that returns a string
        String typeStr = type.toString(); // "ELECTRIC" or "DIESEL"
        return locomotives.stream()
                .filter(l -> l.getEngineType() != null && l.getEngineType().equalsIgnoreCase(typeStr))
                .collect(java.util.stream.Collectors.toList());
    }
}