package LAPR3.repository;

import LAPR3.model.Route;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for managing Route data
 * Loads from CSV and provides query methods
 * Part of USLP07 - Subtask #62
 */
public class RouteRepository {
    private final Map<String, Route> routes;

    public RouteRepository() {
        this.routes = new HashMap<>();
    }

    /**
     * Loads routes from CSV file
     *
     * @param filePath path to routes.csv
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV(String filePath) throws IOException {
        routes.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Route route = parseRouteFromCSV(line);
                    routes.put(route.getRouteId(), route);
                } catch (Exception e) {
                    System.err.println("Error parsing route line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        System.out.println("Loaded " + routes.size() + " routes");
    }

    /**
     * Parses a route from CSV line
     * Format: routeId,name,facilities,type
     * facilities: "1,5,18" (comma-separated facility IDs)
     */
    private Route parseRouteFromCSV(String line) {
        // Use regex to handle quoted fields
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid CSV format: expected 4 fields");
        }

        String routeId = parts[0].trim();
        String name = parts[1].trim();
        String facilitiesStr = parts[2].trim().replace("\"", "");
        Route.RouteType type = Route.RouteType.valueOf(parts[3].trim());

        // Parse facility IDs
        List<Integer> facilityIds = Arrays.stream(facilitiesStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        return new Route(routeId, name, facilityIds, type);
    }

    /**
     * Saves a route to the repository
     *
     * @param route route to save
     * @throws IllegalArgumentException if route ID already exists
     */
    public void save(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }

        if (routes.containsKey(route.getRouteId())) {
            throw new IllegalArgumentException("Route with ID " + route.getRouteId() + " already exists");
        }

        routes.put(route.getRouteId(), route);
    }

    /**
     * Updates an existing route
     *
     * @param route route to update
     * @throws IllegalArgumentException if route doesn't exist
     */
    public void update(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }

        if (!routes.containsKey(route.getRouteId())) {
            throw new IllegalArgumentException("Route with ID " + route.getRouteId() + " does not exist");
        }

        routes.put(route.getRouteId(), route);
    }

    /**
     * Deletes a route from the repository
     *
     * @param routeId route ID to delete
     * @return true if deleted, false if not found
     */
    public boolean delete(String routeId) {
        return routes.remove(routeId) != null;
    }

    /**
     * Finds a route by ID
     */
    public Optional<Route> findById(String routeId) {
        return Optional.ofNullable(routes.get(routeId));
    }

    /**
     * Gets all routes
     */
    public List<Route> findAll() {
        return new ArrayList<>(routes.values());
    }

    /**
     * Finds routes by type
     */
    public List<Route> findByType(Route.RouteType type) {
        List<Route> result = new ArrayList<>();
        for (Route route : routes.values()) {
            if (route.getType() == type) {
                result.add(route);
            }
        }
        return result;
    }

    /**
     * Finds routes that start at a specific facility
     */
    public List<Route> findByOrigin(int facilityId) {
        List<Route> result = new ArrayList<>();
        for (Route route : routes.values()) {
            if (route.getOriginFacilityId() == facilityId) {
                result.add(route);
            }
        }
        return result;
    }

    /**
     * Finds routes that end at a specific facility
     */
    public List<Route> findByDestination(int facilityId) {
        List<Route> result = new ArrayList<>();
        for (Route route : routes.values()) {
            if (route.getDestinationFacilityId() == facilityId) {
                result.add(route);
            }
        }
        return result;
    }

    /**
     * Finds routes that pass through a specific facility
     */
    public List<Route> findPassingThrough(int facilityId) {
        List<Route> result = new ArrayList<>();
        for (Route route : routes.values()) {
            if (route.passesThrough(facilityId)) {
                result.add(route);
            }
        }
        return result;
    }

    /**
     * Finds direct routes (only origin and destination)
     */
    public List<Route> findDirectRoutes() {
        List<Route> result = new ArrayList<>();
        for (Route route : routes.values()) {
            if (route.isDirect()) {
                result.add(route);
            }
        }
        return result;
    }

    /**
     * Finds routes connecting two facilities (directly or with intermediate stops)
     */
    public List<Route> findConnecting(int originId, int destinationId) {
        List<Route> result = new ArrayList<>();
        for (Route route : routes.values()) {
            if (route.getOriginFacilityId() == originId &&
                    route.getDestinationFacilityId() == destinationId) {
                result.add(route);
            }
        }
        return result;
    }

    /**
     * Gets the number of routes in the repository
     */
    public int count() {
        return routes.size();
    }

    /**
     * Checks if repository contains a route with given ID
     */
    public boolean exists(String routeId) {
        return routes.containsKey(routeId);
    }

    /**
     * Clears all routes from the repository
     */
    public void clear() {
        routes.clear();
    }
}