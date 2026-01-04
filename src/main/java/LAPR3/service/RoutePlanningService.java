package LAPR3.service;

import LAPR3.model.*;
import LAPR3.repository.FacilityRepository;
import LAPR3.repository.FreightRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for USLP08 - Route Planning
 * Creates routes with ACTIONS (LOAD/UNLOAD) based on freight constraints
 * Routes are stored IN-MEMORY only
 */
public class RoutePlanningService {
    private final FreightRepository freightRepo;
    private final FacilityRepository facilityRepo;

    private final List<PlannedRoute> plannedRoutes = new ArrayList<>();
    private int nextId = 1;

    public RoutePlanningService(FreightRepository freightRepo,
                                FacilityRepository facilityRepo) {
        this.freightRepo = freightRepo;
        this.facilityRepo = facilityRepo;
    }

    /**
     * USLP08 - Create Simple Route (1 freight)
     */
    public PlannedRoute createSimpleRoute(int freightId) {
        Freight freight = freightRepo.findById(freightId)
                .orElseThrow(() -> new IllegalArgumentException("Freight not found: " + freightId));

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

        plannedRoutes.add(route);
        return route;
    }

    /**
     * USLP08 - Create Composite Route (multiple freights)
     * Constraint: destination of F[i] must equal origin of F[i+1]
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
            if (f.getAssignedWagons().isEmpty()) {
                throw new IllegalArgumentException("Freight " + f.getId() + " has no wagons");
            }
        }

        if (!validateFreightChain(freights)) {
            throw new IllegalArgumentException(
                    "Invalid freight chain: destination of freight[i] must equal origin of freight[i+1]");
        }

        List<RouteAction> actions = buildCompositeActions(freights);

        PlannedRoute route = new PlannedRoute(
                generateRouteId(),
                PlannedRoute.RouteType.COMPOSITE,
                freightIds,
                actions
        );

        plannedRoutes.add(route);
        return route;
    }

    private boolean validateFreightChain(List<Freight> freights) {
        for (int i = 0; i < freights.size() - 1; i++) {
            if (freights.get(i).getDestinationId() != freights.get(i + 1).getOriginId()) {
                return false;
            }
        }
        return true;
    }

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

    public List<PlannedRoute> getAllRoutes() {
        return new ArrayList<>(plannedRoutes);
    }

    public List<PlannedRoute> getUnassignedRoutes() {
        return plannedRoutes.stream()
                .filter(r -> !r.isAssigned())
                .collect(Collectors.toList());
    }

    public Optional<PlannedRoute> getRoute(String routeId) {
        return plannedRoutes.stream()
                .filter(r -> r.getId().equals(routeId))
                .findFirst();
    }

    private String generateRouteId() {
        return "R-" + String.format("%03d", nextId++);
    }

    public String formatRoute(PlannedRoute route) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══ Route ").append(route.getId()).append(" (").append(route.getType()).append(") ═══\n");
        sb.append("Freights: ").append(route.getFreightIds().stream()
                .map(String::valueOf).collect(Collectors.joining(", "))).append("\n");
        sb.append("Status: ").append(route.isAssigned() ? "Assigned to Train " + route.getAssignedTrainId() : "Unassigned").append("\n\n");
        sb.append("Actions:\n");
        for (RouteAction action : route.getActions()) {
            String facilityName = facilityRepo.findById(action.getFacilityId())
                    .map(Facility::getName).orElse("Unknown");
            sb.append(String.format("  %s at %s: Freight %d (%d wagons)\n",
                    action.getType(), facilityName, action.getFreightId(), action.getWagonCount()));
        }
        if (route.hasPath()) {
            sb.append("\nPath: ").append(route.getPath().stream()
                    .map(id -> facilityRepo.findById(id).map(Facility::getName).orElse(String.valueOf(id)))
                    .collect(Collectors.joining(" → ")));
        }
        return sb.toString();
    }
}