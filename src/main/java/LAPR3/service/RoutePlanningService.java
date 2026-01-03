package LAPR3.service;

import LAPR3.model.*;
import java.util.*;

public class RoutePlanningService {
    // In-memory store for session-based routes
    private final List<PlannedRoute> plannedRoutes = new ArrayList<>();
    private int nextId = 1;

    public PlannedRoute createInMemoryPlan(List<Integer> freightIDs, List<Integer> facilityPath) {
        PlannedRoute route = new PlannedRoute(nextId++, freightIDs, facilityPath);
        plannedRoutes.add(route);
        return route;
    }

    public Optional<PlannedRoute> findPlanByFreight(int freightId) {
        return plannedRoutes.stream()
                .filter(r -> r.getFreightIDs().contains(freightId))
                .findFirst();
    }
}