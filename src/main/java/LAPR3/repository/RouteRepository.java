package LAPR3.repository;

import LAPR3.database.DatabaseConnection;
import LAPR3.model.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for Route entities
 * Clean version - loads from database, no automatic ID generation
 */
public class RouteRepository {

    private final List<Route> routes;

    public RouteRepository() {
        this.routes = new ArrayList<>();
    }

    /**
     * Loads routes from the database
     */
    public void loadFromDatabase() {
        this.routes.clear();

        String query = "SELECT route_id, name, route_type FROM ROUTE_DEF ORDER BY route_id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String routeId = rs.getString("route_id");
                String name = rs.getString("name");
                String routeTypeStr = rs.getString("route_type");

                Route.RouteType routeType = "SIMPLE".equals(routeTypeStr) ?
                        Route.RouteType.SIMPLE : Route.RouteType.COMPLEX;

                List<Integer> facilityIds = loadRouteFacilities(routeId, conn);
                Route route = new Route(routeId, name, routeType, facilityIds);
                routes.add(route);
            }

            System.out.println("[DB Load] Routes carregadas: " + routes.size());

        } catch (SQLException e) {
            System.err.println("Erro ao carregar Routes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Integer> loadRouteFacilities(String routeId, Connection conn) throws SQLException {
        List<Integer> facilityIds = new ArrayList<>();

        String query = "SELECT facility_id FROM ROUTE_DEF_FACILITY " +
                "WHERE route_id = ? ORDER BY sequence_order ASC";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    facilityIds.add(rs.getInt("facility_id"));
                }
            }
        }
        return facilityIds;
    }

    public Optional<Route> findById(String routeId) {
        return routes.stream()
                .filter(r -> r.getRouteId().equals(routeId))
                .findFirst();
    }

    public List<Route> findConnecting(int originId, int destinationId) {
        return routes.stream()
                .filter(r -> {
                    List<Integer> facilityIds = r.getFacilityIds();
                    return !facilityIds.isEmpty() &&
                            facilityIds.get(0) == originId &&
                            facilityIds.get(facilityIds.size() - 1) == destinationId;
                })
                .collect(Collectors.toList());
    }

    public List<Route> findAll() {
        return new ArrayList<>(routes);
    }

    /**
     * Returns count of routes (needed by UI menus)
     */
    public int count() {
        return routes.size();
    }

    /**
     * Save route (needed by UI menu)
     */
    public void save(Route route) {
        if (!exists(route.getRouteId())) {
            routes.add(route);
        }
    }
    private boolean exists(String routeId) {
        return routes.stream().anyMatch(r -> r.getRouteId().equals(routeId));
    }
}