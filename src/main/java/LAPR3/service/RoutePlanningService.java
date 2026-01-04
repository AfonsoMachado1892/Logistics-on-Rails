package LAPR3.service;

import LAPR3.model.*;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.FreightRepository;
import LAPR3.repository.RouteRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for USLP08 - Route Planning
 * Manages both:
 * - Routes loaded from database (at startup)
 * - New routes created via USLP08
 */
public class RoutePlanningService {
    private final FreightRepository freightRepo;
    private final FacilityRepository facilityRepo;
    private final RouteRepository routeRepo;

    // Unified list: routes from DB + newly created routes
    private final List<PlannedRoute> allRoutes = new ArrayList<>();
    private int nextId = 1;

    /**
     * Constructor - automatically loads routes from database
     */
    public RoutePlanningService(FreightRepository freightRepo,
                                FacilityRepository facilityRepo,
                                RouteRepository routeRepo) {
        this.freightRepo = freightRepo;
        this.facilityRepo = facilityRepo;
        this.routeRepo = routeRepo;

        // Load and convert existing routes from database
        loadAndConvertRoutesFromDatabase();
    }

    /**
     * Loads routes from database and converts them to PlannedRoute format
     * Called automatically at startup
     */
    private void loadAndConvertRoutesFromDatabase() {
        List<Route> dbRoutes = routeRepo.findAll();

        if (dbRoutes.isEmpty()) {
            System.out.println("[RoutePlanningService] No routes found in database.");
            return;
        }

        System.out.println("[RoutePlanningService] Converting " + dbRoutes.size() + " routes from database...");

        int converted = 0;
        for (Route dbRoute : dbRoutes) {
            try {
                PlannedRoute planned = convertDatabaseRouteToPlanned(dbRoute);
                allRoutes.add(planned);
                converted++;

                // Update nextId to avoid conflicts
                updateNextId(dbRoute.getRouteId());

            } catch (Exception e) {
                System.err.println("[RoutePlanningService] Error converting route " +
                        dbRoute.getRouteId() + ": " + e.getMessage());
            }
        }

        System.out.println("[RoutePlanningService] ✓ Converted " + converted + " routes from database");
    }

    /**
     * Converts a Route (from database) to PlannedRoute (in-memory format)
     */
    private PlannedRoute convertDatabaseRouteToPlanned(Route dbRoute) {
        // Convert route type
        PlannedRoute.RouteType type = dbRoute.getType() == Route.RouteType.SIMPLE
                ? PlannedRoute.RouteType.SIMPLE
                : PlannedRoute.RouteType.COMPOSITE;

        // Get facility IDs from database route
        List<Integer> facilityIds = dbRoute.getFacilityIds();

        // Create actions based on facilities
        // Note: Database routes don't have explicit LOAD/UNLOAD actions,
        // so we infer them from origin and destination
        List<RouteAction> actions = inferActionsFromFacilities(facilityIds);

        // Create PlannedRoute
        PlannedRoute planned = new PlannedRoute(
                dbRoute.getRouteId(),
                type,
                Collections.emptyList(),  // Freight IDs unknown for DB routes
                actions
        );

        // Set the path from database
        planned.setPath(new ArrayList<>(facilityIds));

        return planned;
    }

    /**
     * Infers LOAD/UNLOAD actions from facility list
     * First facility = LOAD, Last facility = UNLOAD
     */
    private List<RouteAction> inferActionsFromFacilities(List<Integer> facilityIds) {
        List<RouteAction> actions = new ArrayList<>();

        if (facilityIds.isEmpty()) {
            return actions;
        }

        // First facility = LOAD (origin)
        actions.add(new RouteAction(
                facilityIds.get(0),
                RouteAction.ActionType.LOAD,
                0,  // Freight ID unknown for DB routes
                0   // Wagon count unknown for DB routes
        ));

        // Last facility = UNLOAD (destination)
        if (facilityIds.size() > 1) {
            actions.add(new RouteAction(
                    facilityIds.get(facilityIds.size() - 1),
                    RouteAction.ActionType.UNLOAD,
                    0,
                    0
            ));
        }

        return actions;
    }

    /**
     * Updates nextId based on existing route ID to avoid conflicts
     */
    private void updateNextId(String routeId) {
        try {
            // Extract numbers from route ID (e.g., "R-001" -> 1, "PLR-0042" -> 42)
            String numStr = routeId.replaceAll("[^0-9]", "");
            if (!numStr.isEmpty()) {
                int num = Integer.parseInt(numStr);
                if (num >= nextId) {
                    nextId = num + 1;
                }
            }
        } catch (NumberFormatException e) {
            // Ignore if route ID doesn't contain numbers
        }
    }

    // ============================================================================
    // USLP08 - CREATE NEW ROUTES
    // ============================================================================

    /**
     * USLP08 - Create Simple Route (1 freight)
     */
    public PlannedRoute createSimpleRoute(int freightId) {
        Freight freight = freightRepo.findById(freightId)
                .orElseThrow(() -> new IllegalArgumentException("Freight not found: " + freightId));

        if (freight.getAssignedRouteId() != null) {
            throw new IllegalArgumentException("Freight already assigned to route");
        }

        if (freight.getAssignedWagons().isEmpty()) {
            throw new IllegalArgumentException("Freight has no assigned wagons");
        }

        List<RouteAction> actions = List.of(
                new RouteAction(
                        freight.getOriginId(),
                        RouteAction.ActionType.LOAD,
                        freightId,
                        freight.getAssignedWagons().size()
                ),
                new RouteAction(
                        freight.getDestinationId(),
                        RouteAction.ActionType.UNLOAD,
                        freightId,
                        freight.getAssignedWagons().size()
                )
        );

        PlannedRoute route = new PlannedRoute(
                generateRouteId(),
                PlannedRoute.RouteType.SIMPLE,
                List.of(freightId),
                actions
        );

        allRoutes.add(route);
        freight.setAssignedRouteId(route.getId());

        // TODO: Save to database via PL/SQL (USBD40)

        return route;
    }

    /**
     * USLP08 - Create Composite Route (multiple freights)
     */
    public PlannedRoute createCompositeRoute(List<Integer> freightIds) {
        if (freightIds.size() < 2) {
            throw new IllegalArgumentException("Composite route requires at least 2 freights");
        }

        List<Freight> freights = freightIds.stream()
                .map(id -> freightRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Freight not found: " + id)))
                .collect(Collectors.toList());

        for (Freight f : freights) {
            if (f.getAssignedRouteId() != null) {
                throw new IllegalArgumentException("Freight " + f.getId() + " already assigned to route");
            }
            if (f.getAssignedWagons().isEmpty()) {
                throw new IllegalArgumentException("Freight " + f.getId() + " has no assigned wagons");
            }
        }

        if (!validateFreightChain(freights)) {
            throw new IllegalArgumentException(
                    "Freights must form a valid chain (destination of F[i] = origin of F[i+1])");
        }

        List<RouteAction> actions = buildCompositeActions(freights);

        PlannedRoute route = new PlannedRoute(
                generateRouteId(),
                PlannedRoute.RouteType.COMPOSITE,
                freightIds,
                actions
        );

        allRoutes.add(route);

        for (Freight f : freights) {
            f.setAssignedRouteId(route.getId());
        }

        // TODO: Save to database via PL/SQL

        return route;
    }

    // ============================================================================
    // QUERY METHODS
    // ============================================================================

    /**
     * Get all routes (database + newly created)
     */
    public List<PlannedRoute> getAllRoutes() {
        return new ArrayList<>(allRoutes);
    }

    /**
     * Get route by ID
     */
    public Optional<PlannedRoute> getRoute(String routeId) {
        return allRoutes.stream()
                .filter(r -> r.getId().equals(routeId))
                .findFirst();
    }

    /**
     * Find route by freight ID
     */
    public Optional<PlannedRoute> findPlanByFreight(int freightId) {
        return allRoutes.stream()
                .filter(r -> r.getFreightIds().contains(freightId))
                .findFirst();
    }

    /**
     * Get routes from database only (excluding newly created)
     */
    public List<PlannedRoute> getDatabaseRoutes() {
        return allRoutes.stream()
                .filter(r -> !r.getId().startsWith("PLR-"))  // PLR- prefix = newly created
                .collect(Collectors.toList());
    }

    /**
     * Get newly created routes only (from USLP08)
     */
    public List<PlannedRoute> getNewlyCreatedRoutes() {
        return allRoutes.stream()
                .filter(r -> r.getId().startsWith("PLR-"))
                .collect(Collectors.toList());
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Validates that freights form a valid chain
     */
    private boolean validateFreightChain(List<Freight> freights) {
        for (int i = 0; i < freights.size() - 1; i++) {
            Freight current = freights.get(i);
            Freight next = freights.get(i + 1);
            if (current.getDestinationId() != next.getOriginId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds actions for composite route
     */
    private List<RouteAction> buildCompositeActions(List<Freight> freights) {
        List<RouteAction> actions = new ArrayList<>();
        for (Freight freight : freights) {
            actions.add(new RouteAction(
                    freight.getOriginId(),
                    RouteAction.ActionType.LOAD,
                    freight.getId(),
                    freight.getAssignedWagons().size()
            ));
            actions.add(new RouteAction(
                    freight.getDestinationId(),
                    RouteAction.ActionType.UNLOAD,
                    freight.getId(),
                    freight.getAssignedWagons().size()
            ));
        }
        return actions;
    }

    /**
     * Generate unique route ID for newly created routes
     */
    private String generateRouteId() {
        return "PLR-" + String.format("%04d", nextId++);
    }

    /**
     * Format route for display
     */
    public String formatRoute(PlannedRoute route) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══ Route ").append(route.getId()).append(" (")
                .append(route.getType()).append(") ═══\n");

        // Show if from database or newly created
        if (route.getId().startsWith("PLR-")) {
            sb.append("Source: Newly created (USLP08)\n");
        } else {
            sb.append("Source: Database\n");
        }
        sb.append("\n");

        if (!route.getFreightIds().isEmpty()) {
            sb.append("Freights: ");
            sb.append(route.getFreightIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ")));
            sb.append("\n\n");
        }

        sb.append("Actions:\n");
        if (route.getActions().isEmpty()) {
            sb.append("  (No explicit actions - inferred from facilities)\n");
        } else {
            for (RouteAction action : route.getActions()) {
                Facility facility = facilityRepo.findById(action.getFacilityId()).orElse(null);
                String facilityName = facility != null ? facility.getName() : "Unknown";

                if (action.getFreightId() == 0) {
                    // Action inferred from database
                    sb.append(String.format("  %s at %s\n", action.getType(), facilityName));
                } else {
                    // Action from USLP08
                    sb.append(String.format("  %s at %s: Freight %d (%d wagons)\n",
                            action.getType(), facilityName,
                            action.getFreightId(), action.getWagonCount()));
                }
            }
        }

        if (route.hasPath()) {
            sb.append("\nPath: ");
            sb.append(route.getPath().stream()
                    .map(id -> facilityRepo.findById(id)
                            .map(Facility::getName)
                            .orElse(String.valueOf(id)))
                    .collect(Collectors.joining(" → ")));
        } else {
            sb.append("\nPath: Not yet calculated (will be defined in USLP10)");
        }

        return sb.toString();
    }

    // ============================================================================
    // LEGACY COMPATIBILITY (DEPRECATED)
    // ============================================================================

    /**
     * @deprecated Use createSimpleRoute() or createCompositeRoute() instead
     */
    @Deprecated
    public PlannedRoute createInMemoryPlan(List<Integer> freightIDs, List<Integer> facilityPath) {
        if (freightIDs.size() == 1) {
            try {
                return createSimpleRoute(freightIDs.get(0));
            } catch (Exception e) {
                return createLegacyPlan(freightIDs, facilityPath);
            }
        } else {
            try {
                return createCompositeRoute(freightIDs);
            } catch (Exception e) {
                return createLegacyPlan(freightIDs, facilityPath);
            }
        }
    }

    @Deprecated
    private PlannedRoute createLegacyPlan(List<Integer> freightIDs, List<Integer> facilityPath) {
        List<RouteAction> actions = new ArrayList<>();
        PlannedRoute.RouteType type = freightIDs.size() == 1
                ? PlannedRoute.RouteType.SIMPLE
                : PlannedRoute.RouteType.COMPOSITE;

        PlannedRoute route = new PlannedRoute(generateRouteId(), type, freightIDs, actions);
        route.setPath(facilityPath);
        allRoutes.add(route);
        return route;
    }
}