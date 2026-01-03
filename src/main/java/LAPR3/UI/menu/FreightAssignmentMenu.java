package LAPR3.UI.menu;

import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.RoutePlanningService;
import java.util.*;
import java.util.stream.Collectors;

public class FreightAssignmentMenu {
    private final FreightRepository freightRepo;
    private final FacilityRepository facilityRepo;
    private final LineRepository lineRepo;
    private final RoutePlanningService planningService;
    private final Scanner scanner = new Scanner(System.in);

    public FreightAssignmentMenu(FreightRepository fgr, FacilityRepository fr,
                                 LineRepository lr, RoutePlanningService rps) {
        this.freightRepo = fgr;
        this.facilityRepo = fr;
        this.lineRepo = lr;
        this.planningService = rps;
    }

    public void show() {
        int option;
        do {
            System.out.println("\n=== FREIGHT PLANNER (USLP08) ===");
            System.out.println("1. List All Freights");
            System.out.println("2. Plan Multi-Freight Route");
            System.out.println("0. Back");
            System.out.print("Select an option: ");
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1 -> listAllFreightsDetailed();
                case 2 -> startMultiFreightPlanning();
            }
        } while (option != 0);
    }

    private void listAllFreightsDetailed() {
        List<Freight> all = freightRepo.findAll();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("LOGISTICS AND TECHNICAL FREIGHT REPORT");
        System.out.println("=".repeat(80));

        int freightCount = 1;
        for (Freight f : all) {
            Optional<PlannedRoute> plan = planningService.findPlanByFreight(f.getId());

            // Requirement: Output "Freight 1", "Freight 2", etc.
            System.out.printf("%nFreight %d%n", freightCount++);
            System.out.printf("ID: %d | %s -> %s%n", f.getId(), f.getOriginName(), f.getDestinationName());
            System.out.println("Status: " + (f.getAssignedRouteId() != null || plan.isPresent() ? "ASSIGNED" : "PENDING"));

            if (plan.isPresent()) {
                PlannedRoute pr = plan.get();
                String path = pr.getFacilityPath().stream()
                        .map(id -> facilityRepo.findById(id).map(Facility::getName).orElse("Unknown"))
                        .collect(Collectors.joining(" -> "));
                System.out.println("Route Path: " + path);
                System.out.println("Route Type: " + pr.getRouteType());
            }

            System.out.println("Wagons (" + f.getAssignedWagons().size() + "):");
            for (Wagon w : f.getAssignedWagons()) {
                // FIX: Added w.getMaker() to the output
                System.out.printf("  - ID: %s | Model: %s %s | Max Load: %.1ft | Type: %s | Max Speed: %.0f km/h%n",
                        w.getWagonNumber(), w.getMaker(), w.getModelName(), w.getPayloadTons(), w.getTypeDesc(), w.getMaxSpeedKmh());
            }
            System.out.println("-".repeat(40));
        }
    }

    private void startMultiFreightPlanning() {
        List<Integer> selectedFreightIDs = new ArrayList<>();

        while (true) {
            List<Freight> pending = freightRepo.findAllPending().stream()
                    .filter(f -> planningService.findPlanByFreight(f.getId()).isEmpty())
                    .filter(f -> !selectedFreightIDs.contains(f.getId()))
                    .toList();

            if (pending.isEmpty() && selectedFreightIDs.isEmpty()) {
                System.out.println("No pending freights available.");
                return;
            }

            System.out.println("\nSelect Freight ID to add (or 0 to stop selecting):");
            pending.forEach(f -> System.out.printf("[%d] %s -> %s  ", f.getId(), f.getOriginName(), f.getDestinationName()));
            System.out.print("\nChoice: ");
            int id = scanner.nextInt();
            if (id == 0) break;

            if (pending.stream().anyMatch(f -> f.getId() == id)) {
                selectedFreightIDs.add(id);
                System.out.println("Added Freight " + id);
            } else {
                System.out.println("Invalid or already assigned Freight ID.");
            }
        }

        if (selectedFreightIDs.isEmpty()) return;

        List<Integer> path = new ArrayList<>();
        int firstFreightId = selectedFreightIDs.get(0);
        int currentId = freightRepo.findById(firstFreightId).get().getOriginId();
        path.add(currentId);

// 1. Create a Set of all required destinations (mandatory stops)
        Set<Integer> pendingDestinations = selectedFreightIDs.stream()
                .map(id -> freightRepo.findById(id).get().getDestinationId())
                .collect(Collectors.toCollection(HashSet::new));

// Remove the starting station if it happens to be a destination (rare but possible)
        pendingDestinations.remove(currentId);

        System.out.println("\nBuilding Path starting at: " + facilityRepo.findById(currentId).get().getName());

        while (true) {
            System.out.println("\nCurrent Path: " + path.stream()
                    .map(id -> facilityRepo.findById(id).get().getName())
                    .collect(Collectors.joining(" -> ")));

            // 2. Inform the user about remaining mandatory stops throughout the selection
            if (!pendingDestinations.isEmpty()) {
                String pendingStr = pendingDestinations.stream()
                        .map(id -> facilityRepo.findById(id).get().getName())
                        .collect(Collectors.joining(", "));
                System.out.println("Remaining Mandatory Stops: [" + pendingStr + "]");
            } else {
                System.out.println("✓ All mandatory stops reached. You may now finish the route.");
            }

            // FIX: Using LineRepository to avoid NullNeighbors crash and treat tracks as undirected
            Map<Integer, Line> connections = lineRepo.findDirectlyConnectedFacilities(currentId);

            if (connections.isEmpty()) {
                System.out.println("\n[!] Error: No outgoing tracks found. Path ended prematurely.");
                break;
            }

            System.out.println("Choose Next Direct Connection:");
            for (Integer neighborId : connections.keySet()) {
                String name = facilityRepo.findById(neighborId).map(Facility::getName).orElse("Unknown");
                System.out.printf("[%d] %s  ", neighborId, name);
            }

            System.out.print("\nEnter Station ID (or 0 to finish path): ");
            int nextId = scanner.nextInt();

            if (nextId == 0) {
                // 3. Enforce that ALL mandatory stops must be visited
                if (pendingDestinations.isEmpty()) {
                    break; // Valid exit
                } else {
                    System.out.println("✗ Error: You cannot finish yet. You still need to reach: " +
                            pendingDestinations.stream()
                                    .map(id -> facilityRepo.findById(id).get().getName())
                                    .collect(Collectors.joining(", ")));
                    continue;
                }
            }

            if (connections.containsKey(nextId)) {
                path.add(nextId);
                currentId = nextId;

                // 4. "Check off" the stop if reached
                if (pendingDestinations.contains(nextId)) {
                    System.out.println("✓ Mandatory stop reached: " + facilityRepo.findById(nextId).get().getName());
                    pendingDestinations.remove(nextId);
                }
            } else {
                System.out.println("Invalid choice. Select a directly connected station.");
            }
        }

        PlannedRoute route = planningService.createInMemoryPlan(selectedFreightIDs, path);
        System.out.println("\nSUCCESS: Route planned in-memory!");
    }
}