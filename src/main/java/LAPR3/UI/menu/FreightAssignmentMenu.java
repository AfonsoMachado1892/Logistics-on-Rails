package LAPR3.UI.menu;

import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.RoutePlanningService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu for USLP08 - Freight Route Planning
 * Creates routes with ACTIONS only (no path)
 */
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
            System.out.println("\n" + "═".repeat(60));
            System.out.println("      FREIGHT ROUTE PLANNER (USLP08)");
            System.out.println("═".repeat(60));
            System.out.println("1. List All Freights");
            System.out.println("2. Create Simple Route (1 Freight)");
            System.out.println("3. Create Composite Route (Multiple Freights)");
            System.out.println("4. View Planned Routes");
            System.out.println("0. Back");
            System.out.println("═".repeat(60));
            System.out.print("Select option: ");

            try {
                option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1 -> listAllFreights();
                    case 2 -> createSimpleRoute();
                    case 3 -> createCompositeRoute();
                    case 4 -> viewPlannedRoutes();
                    case 0 -> System.out.println("Returning to main menu...");
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("✗ Invalid input. Please enter a number.");
                scanner.nextLine();
                option = -1;
            }

            if (option != 0 && option != -1) {
                System.out.print("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        } while (option != 0);
    }

    private void listAllFreights() {
        List<Freight> all = freightRepo.findAll();

        if (all.isEmpty()) {
            System.out.println("\nNo freights found in the system.");
            return;
        }

        System.out.println("\n" + "═".repeat(100));
        System.out.println("FREIGHT LISTING");
        System.out.println("═".repeat(100));

        int count = 1;
        for (Freight f : all) {
            Optional<PlannedRoute> route = planningService.findPlanByFreight(f.getId());

            System.out.printf("\n[%d] Freight %d\n", count++, f.getId());
            System.out.printf("    Origin: %s (ID: %d)\n", f.getOriginName(), f.getOriginId());
            System.out.printf("    Destination: %s (ID: %d)\n", f.getDestinationName(), f.getDestinationId());
            System.out.printf("    Status: %s\n",
                    route.isPresent() ? "✓ PLANNED (Route: " + route.get().getId() + ")" : "○ PENDING");
            System.out.printf("    Wagons: %d\n", f.getAssignedWagons().size());

            if (!f.getAssignedWagons().isEmpty()) {
                Wagon firstWagon = f.getAssignedWagons().get(0);
                System.out.printf("    Type: %s\n", firstWagon.getTypeDesc());
            }

            System.out.println("    " + "─".repeat(80));
        }

        System.out.printf("\nTotal: %d freights (%d pending, %d planned)\n",
                all.size(),
                freightRepo.findAllPending().stream()
                        .filter(f -> planningService.findPlanByFreight(f.getId()).isEmpty())
                        .count(),
                all.size() - freightRepo.findAllPending().stream()
                        .filter(f -> planningService.findPlanByFreight(f.getId()).isEmpty())
                        .count());
    }

    private void createSimpleRoute() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  CREATE SIMPLE ROUTE (1 Freight)");
        System.out.println("═".repeat(60));

        // List pending freights
        List<Freight> pending = freightRepo.findAllPending().stream()
                .filter(f -> planningService.findPlanByFreight(f.getId()).isEmpty())
                .collect(Collectors.toList());

        if (pending.isEmpty()) {
            System.out.println("\n✗ No pending freights available.");
            return;
        }

        System.out.println("\nAvailable Freights:\n");
        for (Freight f : pending) {
            System.out.printf("[%d] %s → %s (%d wagons, %s)\n",
                    f.getId(),
                    f.getOriginName(),
                    f.getDestinationName(),
                    f.getAssignedWagons().size(),
                    f.getAssignedWagons().isEmpty() ? "no type" :
                            f.getAssignedWagons().get(0).getTypeDesc());
        }

        System.out.print("\nEnter Freight ID: ");
        int freightId;
        try {
            freightId = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("\n✗ Invalid input.");
            scanner.nextLine();
            return;
        }

        try {
            PlannedRoute route = planningService.createSimpleRoute(freightId);
            System.out.println("\n" + "✓".repeat(60));
            System.out.println("  Simple Route created successfully!");
            System.out.println("✓".repeat(60));
            System.out.println("\n" + planningService.formatRoute(route));

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }

    private void createCompositeRoute() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  CREATE COMPOSITE ROUTE (Multiple Freights)");
        System.out.println("═".repeat(60));
        System.out.println("\nNote: Freights must form a chain:");
        System.out.println("  Destination of Freight[i] = Origin of Freight[i+1]");
        System.out.println();

        List<Integer> selectedFreightIds = new ArrayList<>();

        while (true) {
            // List available freights (not yet selected)
            List<Freight> available = freightRepo.findAllPending().stream()
                    .filter(f -> planningService.findPlanByFreight(f.getId()).isEmpty())
                    .filter(f -> !selectedFreightIds.contains(f.getId()))
                    .collect(Collectors.toList());

            if (available.isEmpty() && selectedFreightIds.isEmpty()) {
                System.out.println("\n✗ No pending freights available.");
                return;
            }

            // Show current selection
            if (!selectedFreightIds.isEmpty()) {
                System.out.println("\n" + "─".repeat(60));
                System.out.println("Current Selection:");
                System.out.println("─".repeat(60));
                for (int i = 0; i < selectedFreightIds.size(); i++) {
                    int id = selectedFreightIds.get(i);
                    Freight f = freightRepo.findById(id).get();
                    System.out.printf("  %d. Freight %d: %s → %s (%d wagons)\n",
                            i + 1, id, f.getOriginName(), f.getDestinationName(),
                            f.getAssignedWagons().size());
                }
                System.out.println("─".repeat(60));
            }

            // Validate chain if we have at least one freight
            if (!selectedFreightIds.isEmpty()) {
                int lastId = selectedFreightIds.get(selectedFreightIds.size() - 1);
                Freight lastFreight = freightRepo.findById(lastId).get();
                int requiredOrigin = lastFreight.getDestinationId();

                System.out.println("\n⚠ Next freight must start at: " +
                        facilityRepo.findById(requiredOrigin).get().getName() +
                        " (ID: " + requiredOrigin + ")");

                // Filter available freights to only those starting at required origin
                available = available.stream()
                        .filter(f -> f.getOriginId() == requiredOrigin)
                        .collect(Collectors.toList());

                if (available.isEmpty() && selectedFreightIds.size() >= 2) {
                    System.out.println("\n✓ No more compatible freights.");
                    System.out.println("  You can finish the route now or cancel.");
                }
            }

            System.out.println("\nAvailable Freights:");
            if (available.isEmpty()) {
                System.out.println("  (none)");
            } else {
                for (Freight f : available) {
                    System.out.printf("  [%d] %s → %s (%d wagons)\n",
                            f.getId(),
                            f.getOriginName(),
                            f.getDestinationName(),
                            f.getAssignedWagons().size());
                }
            }

            System.out.print("\nEnter Freight ID (0 to finish, -1 to cancel): ");
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("\n✗ Invalid input.");
                scanner.nextLine();
                continue;
            }

            if (choice == -1) {
                System.out.println("\n✗ Cancelled.");
                return;
            }

            if (choice == 0) {
                if (selectedFreightIds.size() < 2) {
                    System.out.println("\n✗ Composite route requires at least 2 freights.");
                    continue;
                }
                break;
            }

            if (available.stream().anyMatch(f -> f.getId() == choice)) {
                selectedFreightIds.add(choice);
                System.out.println("✓ Freight " + choice + " added to selection.");
            } else {
                System.out.println("✗ Invalid freight ID or not compatible with chain.");
            }
        }

        if (selectedFreightIds.size() < 2) {
            System.out.println("\n✗ Cancelled. Composite route requires at least 2 freights.");
            return;
        }

        try {
            PlannedRoute route = planningService.createCompositeRoute(selectedFreightIds);
            System.out.println("\n" + "✓".repeat(60));
            System.out.println("  Composite Route created successfully!");
            System.out.println("✓".repeat(60));
            System.out.println("\n" + planningService.formatRoute(route));

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }

    private void viewPlannedRoutes() {
        List<PlannedRoute> routes = planningService.getAllRoutes();

        if (routes.isEmpty()) {
            System.out.println("\n✗ No routes planned yet.");
            return;
        }

        System.out.println("\n" + "═".repeat(100));
        System.out.println("PLANNED ROUTES");
        System.out.println("═".repeat(100));

        for (PlannedRoute route : routes) {
            System.out.println("\n" + planningService.formatRoute(route));
            System.out.println("─".repeat(100));
        }

        System.out.printf("\nTotal: %d routes (%d simple, %d composite)\n",
                routes.size(),
                routes.stream().filter(PlannedRoute::isSimple).count(),
                routes.stream().filter(PlannedRoute::isComposite).count());
    }
}