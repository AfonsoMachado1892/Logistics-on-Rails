package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Locomotive;
import LAPR3.model.Train;
import LAPR3.model.Wagon;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainRepository {

    private final List<Train> trains;

    public TrainRepository() {
        this.trains = new ArrayList<>();
    }

    public void loadFromDatabase() {
        this.trains.clear();

        // 1. Buscar Comboios (Com JOIN ao Operator para ter o nome, se existir)
        String query =
                "SELECT t.id, t.operator_vat, o.name AS op_name, " +
                        "       t.departure_date, t.departure_time, " +
                        "       t.start_facility_id, t.end_facility_id " +
                        "FROM Train t " +
                        "LEFT JOIN Operator o ON t.operator_vat = o.vat_number";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Mapear dados base do Comboio
                int id = rs.getInt("id");
                String opVat = rs.getString("operator_vat");
                // opName não é usado no construtor Train, apenas opVat

                // Tratar datas SQL
                java.sql.Date sqlDate = rs.getDate("departure_date");
                LocalDate date = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                String timeStr = rs.getString("departure_time");
                LocalTime time = (timeStr != null) ? LocalTime.parse(timeStr) : null;

                int startId = rs.getInt("start_facility_id");
                int endId = rs.getInt("end_facility_id");

                // Criar train com construtor vazio e depois popular
                Train train = new Train();
                train.setId(id);
                train.setOperatorVat(opVat);
                train.setDepartureDate(date);
                train.setDepartureTime(time);
                train.setStartFacilityId(startId);
                train.setEndFacilityId(endId);

                // 2. Preencher as listas de Locomotivas e Vagões
                train.setLocomotives(fetchLocomotives(id, conn));
                train.setWagons(fetchWagons(id, conn));
                train.setPath(fetchPath(id, conn));

                trains.add(train);
            }
            System.out.println("[DB Load] Trains carregados: " + trains.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- HELPER: Buscar Locomotivas ---
    private List<Locomotive> fetchLocomotives(int trainId, Connection conn) throws SQLException {
        List<Locomotive> locos = new ArrayList<>();
        String sql =
                "SELECT l.* FROM Locomotive l " +
                        "JOIN Train_Locomotive tl ON l.loco_number = tl.loco_number " +
                        "WHERE tl.train_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date svcDate = rs.getDate("service_start_date");
                    LocalDate serviceStart = svcDate != null ? svcDate.toLocalDate() : null;

                    // CORRIGIDO: Handle BigDecimal from Oracle
                    Double fuel = null;
                    Object fuelObj = rs.getObject("fuel_capacity_l");
                    if (fuelObj != null) {
                        if (fuelObj instanceof java.math.BigDecimal) {
                            fuel = ((java.math.BigDecimal) fuelObj).doubleValue();
                        } else if (fuelObj instanceof Number) {
                            fuel = ((Number) fuelObj).doubleValue();
                        }
                    }

                    locos.add(new Locomotive(
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
                    ));
                }
            }
        }
        return locos;
    }

    // --- HELPER: Buscar Vagões (AJUSTADO PARA O TEU MODELO) ---
    private List<Wagon> fetchWagons(int trainId, Connection conn) throws SQLException {
        List<Wagon> wagons = new ArrayList<>();

        // Query completa para ir buscar os dados técnicos à tabela Wagon_Model
        String sql =
                "SELECT w.wagon_number, w.operator_vat, " +
                        "       wm.model_name, wm.maker, wm.type_desc, " +
                        "       wm.num_bogies, wm.bogies_type, " +
                        "       wm.weight_tons, wm.payload_tons, wm.max_speed_kmh, " +
                        "       wm.length_mm, wm.width_mm, wm.height_mm, " +
                        "       wm.volume_m3, wm.gauge " +
                        "FROM Train_Freight tf " +
                        "JOIN Freight_Wagon fw ON tf.freight_id = fw.freight_id " +
                        "JOIN Wagon w ON fw.wagon_number = w.wagon_number " +
                        "JOIN Wagon_Model wm ON w.model_id = wm.id " +
                        "WHERE tf.train_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    wagons.add(new Wagon(
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
                    ));
                }
            }
        }
        return wagons;
    }

    // --- HELPER: Path ---
    private List<Integer> fetchPath(int trainId, Connection conn) throws SQLException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT facility_id FROM Train_Path WHERE train_id = ? ORDER BY sequence_order ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("facility_id"));
            }
        }
        return list;
    }

    public List<Train> findAll() {
        return new ArrayList<>(trains);
    }

    /**
     * Finds a train by its ID (String format)
     * @param trainId train identifier
     * @return Optional containing the train if found
     */
    public Optional<Train> findById(String trainId) {
        try {
            int id = Integer.parseInt(trainId);
            return trains.stream()
                    .filter(t -> t.getId() == id)
                    .findFirst();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns count of trains (needed by UI menus)
     */
    public int count() {
        return trains.size();
    }
}