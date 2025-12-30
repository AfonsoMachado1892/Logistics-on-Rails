package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Segment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SegmentRepository {

    private final List<Segment> segments;

    public SegmentRepository() {
        this.segments = new ArrayList<>();
    }

    public void loadFromDatabase() {
        this.segments.clear();
        // Mapeamento correto com tabela Segment e novos campos de Siding
        String query = "SELECT id, line_id, sequence_order, electrified, max_weight_kg_m, " +
                "length_m, number_tracks, siding_position, siding_length " +
                "FROM Segment ORDER BY line_id, sequence_order";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int lineId = rs.getInt("line_id");
                int order = rs.getInt("sequence_order");

                // Conversão 'Yes'/'No' (CHAR(3)) para boolean
                String electStr = rs.getString("electrified");
                boolean electrified = "Yes".equalsIgnoreCase(electStr);

                double maxWeight = rs.getDouble("max_weight_kg_m");
                double length = rs.getDouble("length_m");
                int tracks = rs.getInt("number_tracks");

                // Tratamento de Nulls para Siding - CORRIGIDO para BigDecimal
                Double sPos = null;
                Object sPosObj = rs.getObject("siding_position");
                if (sPosObj != null) {
                    if (sPosObj instanceof java.math.BigDecimal) {
                        sPos = ((java.math.BigDecimal) sPosObj).doubleValue();
                    } else if (sPosObj instanceof Number) {
                        sPos = ((Number) sPosObj).doubleValue();
                    }
                }

                Double sLen = null;
                Object sLenObj = rs.getObject("siding_length");
                if (sLenObj != null) {
                    if (sLenObj instanceof java.math.BigDecimal) {
                        sLen = ((java.math.BigDecimal) sLenObj).doubleValue();
                    } else if (sLenObj instanceof Number) {
                        sLen = ((Number) sLenObj).doubleValue();
                    }
                }

                segments.add(new Segment(id, lineId, order, electrified, maxWeight, length, tracks, sPos, sLen));
            }
            System.out.println("[DB Load] Segmentos carregados: " + segments.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Segment> findByLineId(int lineId) {
        return segments.stream()
                .filter(s -> s.getLineId() == lineId)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Segment> findAll() {
        return new ArrayList<>(segments);
    }

    /**
     * Finds a segment by its ID
     * @param id segment identifier
     * @return Optional containing the segment if found
     */
    public Optional<Segment> findById(int id) {
        return segments.stream()
                .filter(s -> s.getId() == id)
                .findFirst();
    }

    /**
     * Returns count of segments (needed by UI menus)
     */
    public int count() {
        return segments.size();
    }
}