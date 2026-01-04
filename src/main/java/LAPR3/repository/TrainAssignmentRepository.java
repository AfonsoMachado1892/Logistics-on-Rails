package LAPR3.repository;

import LAPR3.DTO.FacilityDTO;
import LAPR3.DTO.OperatorDTO;
import LAPR3.DTO.RouteDto;
import LAPR3.database.DatabaseConnection;
import LAPR3.DTO.RollingStockItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainAssignmentRepository {

    public List<RollingStockItem> getAvailableRollingStock() throws SQLException {
        List<RollingStockItem> list = new ArrayList<>();

        // Query corrigida:
        // 1. Usa subqueries para evitar o erro ORA-00904 caso a coluna falte numa das tabelas
        // 2. Colunas batem com o teu DDL: LOCO_NUMBER, WAGON_NUMBER, CURRENT_FACILITY_ID
        String sql =
                "SELECT * FROM (" +
                        "  SELECT LOCO_NUMBER AS ID, 'Locomotive' AS Type, 'In Transit' AS Status, " +
                        "  'En Route' AS Location, 0 AS Distance FROM LOCOMOTIVE " +
                        "  UNION ALL " +
                        "  SELECT WAGON_NUMBER AS ID, 'Wagon' AS Type, " +
                        "  CASE WHEN CURRENT_FACILITY_ID IS NULL THEN 'In Transit' ELSE 'Parked' END AS Status, " +
                        "  NVL((SELECT NAME FROM FACILITY WHERE ID = w.CURRENT_FACILITY_ID), 'En Route') AS Location, " +
                        "  CASE WHEN CURRENT_FACILITY_ID IS NOT NULL THEN 1000 ELSE 0 END AS Distance " +
                        "  FROM WAGON w" +
                        ") ResultTable " +
                        "ORDER BY Status DESC, Distance DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new RollingStockItem(
                        rs.getString("ID"),
                        rs.getString("Type"),
                        rs.getString("Status"),
                        rs.getString("Location"),
                        rs.getDouble("Distance")
                ));
            }
        }
        return list;
    }
    public List<OperatorDTO> getAvailableOperators() throws SQLException {
        List<OperatorDTO> list = new ArrayList<>();
        // Ajustado para o nome da coluna vat_number
        String sql = "SELECT vat_number, name FROM operator ORDER BY name";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new OperatorDTO(rs.getString("vat_number"), rs.getString("name")));
            }
        }
        return list;
    }
    public List<FacilityDTO> getAvailableFacilities() throws SQLException {
        List<FacilityDTO> list = new ArrayList<>();
        // Ajusta os nomes das colunas (ID, NAME, TYPE) conforme o teu DDL de FACILITY
        String sql = "SELECT CAST(id AS VARCHAR2(50)) AS id, name FROM facility ORDER BY id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new FacilityDTO(
                        rs.getString("id"),
                        rs.getString("name")
                ));
            }
        }
        return list;
    }
    public boolean assignTrainToRoute(String locoId, List<String> wagonIds, int routeId, String operatorVat) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false); // Iniciar transaÃ§Ã£o

            // 1. Inserir na tabela TRAIN (Assumindo que associa a locomotiva Ã  rota)
            // Ajusta os nomes das colunas conforme o teu DDL exato de TRAIN
            String sqlTrain = "INSERT INTO TRAIN (OPERATOR_VAT, LOCO_NUMBER, ROUTE_ID) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlTrain)) {
                stmt.setString(1, operatorVat);
                stmt.setString(2, locoId);
                stmt.setInt(3, routeId);
                stmt.executeUpdate();
            }

            // 2. Atualizar os vagÃµes para ficarem associados (ou marcar como em trÃ¢nsito)
            String sqlWagon = "UPDATE WAGON SET CURRENT_FACILITY_ID = NULL WHERE WAGON_NUMBER = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlWagon)) {
                for (String wId : wagonIds) {
                    stmt.setString(1, wId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    public List<RouteDto> getAvailableRoutes() throws SQLException {
        List<RouteDto> list = new ArrayList<>();
        String sql = "SELECT route_id, name FROM route_def ORDER BY route_id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Ler como String
                list.add(new RouteDto(rs.getString("route_id"), rs.getString("name")));
            }
        }
        return list;
    }

    // Assinatura atualizada com routeId como String
    public boolean createFreight(List<String> wagonIds, String locoId, String originId, String destId, String departureDateTime, String operatorVat, String routeId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // SQLs
        String sqlFreight = "INSERT INTO FREIGHT (id, origin_facility_id, dest_facility_id, freight_date) VALUES (freight_seq.NEXTVAL, ?, ?, TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI'))";
        String sqlTrain = "INSERT INTO TRAIN (id, operator_vat, departure_date, departure_time, start_facility_id, end_facility_id) VALUES (train_seq.NEXTVAL, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)";

        // Tabela TRAIN_SCHEDULE
        String sqlSchedule = "INSERT INTO train_schedule (schedule_id, train_id, route_id, departure_date, departure_time, status) VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, 'SCHEDULED')";

        // Tabelas de ligaÃ§Ã£o
        String sqlTrainLoco = "INSERT INTO TRAIN_LOCOMOTIVE (train_id, loco_number) VALUES (?, ?)";
        String sqlFreightWagon = "INSERT INTO FREIGHT_WAGON (freight_id, wagon_number) VALUES (?, ?)";
        String sqlTrainFreight = "INSERT INTO TRAIN_FREIGHT (train_id, freight_id) VALUES (?, ?)";

        try {
            conn.setAutoCommit(false);
            int genFreightId = -1;
            int genTrainId = -1;

            // 1. FREIGHT
            try (PreparedStatement stmt = conn.prepareStatement(sqlFreight)) {
                stmt.setString(1, originId);
                stmt.setString(2, destId);
                stmt.setString(3, departureDateTime);
                stmt.executeUpdate();
            }
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT freight_seq.CURRVAL FROM DUAL")) {
                if (rs.next()) genFreightId = rs.getInt(1);
            }

            // 2. TRAIN
            String[] parts = departureDateTime.trim().split("\\s+");
            if (parts.length < 2) throw new SQLException("Formato de data/hora invÃ¡lido.");
            String datePart = parts[0];
            String timePart = parts[1];

            try (PreparedStatement stmt = conn.prepareStatement(sqlTrain)) {
                stmt.setString(1, operatorVat);
                stmt.setString(2, datePart);
                stmt.setString(3, timePart);
                stmt.setString(4, originId);
                stmt.setString(5, destId);
                stmt.executeUpdate();
            }
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT train_seq.CURRVAL FROM DUAL")) {
                if (rs.next()) genTrainId = rs.getInt(1);
            }

            // 3. TRAIN_SCHEDULE
            String scheduleId = generateScheduleId(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sqlSchedule)) {
                stmt.setString(1, scheduleId);
                stmt.setInt(2, genTrainId);
                stmt.setString(3, routeId); // String "r001"
                stmt.setString(4, datePart);
                stmt.setString(5, timePart);
                stmt.executeUpdate();
            }

            // 4. LIGAÃ‡Ã•ES RESTANTES
            try (PreparedStatement stmt = conn.prepareStatement(sqlTrainFreight)) {
                stmt.setInt(1, genTrainId);
                stmt.setInt(2, genFreightId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlTrainLoco)) {
                stmt.setInt(1, genTrainId);
                stmt.setString(2, locoId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlFreightWagon)) {
                for (String wId : wagonIds) {
                    stmt.setInt(1, genFreightId);
                    stmt.setString(2, wId.trim());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Gerador de IDs automÃ¡ticos: s001, s002...
    private String generateScheduleId(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) + 1 FROM train_schedule";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return String.format("S%03d", rs.getInt(1));
            }
        }
        return "S001";
    }}