package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Freight;
import LAPR3.model.Wagon;
import java.sql.*;
import java.util.*;

public class FreightRepository {
    private final List<Freight> freights = new ArrayList<>();

    public List<Freight> findAll() { return new ArrayList<>(freights); }

    public Optional<Freight> findById(int id) {
        return freights.stream().filter(f -> f.getId() == id).findFirst();
    }

    public void loadFromDatabase() {
        this.freights.clear();
        Map<Integer, Freight> freightsMap = new HashMap<>();

        // Updated query to fetch Facility IDs (f.ORIGIN_FACILITY_ID, f.DEST_FACILITY_ID)
        String query = "SELECT f.ID, f.FREIGHT_DATE, f.ORIGIN_FACILITY_ID, f.DEST_FACILITY_ID, " +
                "fo.NAME AS ORIGIN_NAME, fd.NAME AS DEST_NAME, ts.ROUTE_ID, " +
                "w.WAGON_NUMBER, w.OPERATOR_VAT, wm.MODEL_NAME, wm.MAKER, wm.TYPE_DESC, " +
                "wm.NUM_BOGIES, wm.BOGIES_TYPE, wm.WEIGHT_TONS, wm.PAYLOAD_TONS, " +
                "wm.MAX_SPEED_KMH, wm.LENGTH_MM, wm.WIDTH_MM, wm.HEIGHT_MM, " +
                "wm.VOLUME_M3, wm.GAUGE " +
                "FROM SYSTEM.FREIGHT f " +
                "JOIN SYSTEM.FACILITY fo ON f.ORIGIN_FACILITY_ID = fo.ID " +
                "JOIN SYSTEM.FACILITY fd ON f.DEST_FACILITY_ID = fd.ID " +
                "LEFT JOIN SYSTEM.TRAIN_FREIGHT tf ON f.ID = tf.FREIGHT_ID " +
                "LEFT JOIN SYSTEM.TRAIN_SCHEDULE ts ON tf.TRAIN_ID = ts.TRAIN_ID " +
                "LEFT JOIN SYSTEM.FREIGHT_WAGON fw ON f.ID = fw.FREIGHT_ID " +
                "LEFT JOIN SYSTEM.WAGON w ON fw.WAGON_NUMBER = w.WAGON_NUMBER " +
                "LEFT JOIN SYSTEM.WAGON_MODEL wm ON w.MODEL_ID = wm.ID";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("ID");
                Freight freight = freightsMap.computeIfAbsent(id, k -> {
                    try {
                        Freight f = new Freight(
                                k,
                                rs.getDate("FREIGHT_DATE").toLocalDate(),
                                rs.getInt("ORIGIN_FACILITY_ID"),
                                rs.getInt("DEST_FACILITY_ID"),
                                rs.getString("ORIGIN_NAME"),
                                rs.getString("DEST_NAME")
                        );
                        f.setAssignedRouteId(rs.getString("ROUTE_ID"));
                        return f;
                    } catch (SQLException e) { throw new RuntimeException(e); }
                });

                if (rs.getString("WAGON_NUMBER") != null) {
                    freight.addWagon(new Wagon(
                            rs.getString("WAGON_NUMBER"), rs.getString("OPERATOR_VAT"),
                            rs.getString("MODEL_NAME"),   rs.getString("MAKER"),
                            rs.getString("TYPE_DESC"),    rs.getInt("NUM_BOGIES"),
                            rs.getString("BOGIES_TYPE"),  rs.getDouble("WEIGHT_TONS"),
                            rs.getDouble("PAYLOAD_TONS"), rs.getDouble("MAX_SPEED_KMH"),
                            rs.getDouble("LENGTH_MM"),    rs.getDouble("WIDTH_MM"),
                            rs.getDouble("HEIGHT_MM"),    rs.getDouble("VOLUME_M3"),
                            rs.getDouble("GAUGE")
                    ));
                }
            }
            this.freights.addAll(freightsMap.values());
        } catch (SQLException e) {
            System.err.println("Error loading Freights: " + e.getMessage());
        }
    }

    public List<Freight> findAllPending() {
        return freights.stream()
                .filter(f -> f.getAssignedRouteId() == null)
                .toList();
    }
}