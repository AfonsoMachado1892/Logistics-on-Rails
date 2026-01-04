package LAPR3.UI.menu;

import LAPR3.model.*;
import LAPR3.repository.*;
import LAPR3.service.RoutePlanningService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu for USLP08 - Freight Route Planning
 */
public class FreightAssignmentMenu {
    private final FreightRepository freightRepo;
    private final FacilityRepository facilityRepo;
    private final RoutePlanningService planningService;
    private final Scanner scanner = new Scanner(System.in);

    public FreightAssignmentMenu(FreightRepository freightRepo,
                                 FacilityRepository facilityRepo,
                                 RoutePlanningService planningService) {
        this.freightRepo = freightRepo;
        this.facilityRepo = facilityRepo;
        this.planningService = planningService;
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
                    case 0 -> System.out.println("Returning...");
                    default -> System.out.println("Invalid option.");
                }
            } catch (InputMismatchException e) {
                System.out.println("✗ Invalid input.");
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
            System.out.println("\nNo freights found.");
            return;
        }

        System.out.println("\n" + "═".repeat(100));
        System.out.println("FREIGHT LISTING");
        System.out.println("═".repeat(100));

        for (Freight f : all) {
            System.out.printf("\nFreight %d: %s → %s\n",
                    f.getId(), f.getOriginName(), f.getDestinationName());
            System.out.printf("  Wagons: %d | Date: %s\n",
                    f.getAssignedWagons().size(), f.getDate());
        }
    }

    private void createSimpleRoute() {
        System.out.println("\n═══ CREATE SIMPLE ROUTE ═══");

        List<Freight> pending = freightRepo.findAll().stream()
                .filter(f -> f.getAssignedWagons().size() > 0)
                .collect(Collectors.toList());

        if (pending.isEmpty()) {
            System.out.println("\n✗ No freights with wagons available.");
            return;
        }

        System.out.println("\nAvailable Freights:");
        for (Freight f : pending) {
            System.out.printf("[%d] %s → %s (%d wagons)\n",
                    f.getId(), f.getOriginName(), f.getDestinationName(),
                    f.getAssignedWagons().size());
        }

        System.out.print("\nEnter Freight ID: ");
        int freightId = scanner.nextInt();
        scanner.nextLine();

        try {
            PlannedRoute route = planningService.createSimpleRoute(freightId);
            System.out.println("\n✓ Simple Route created!");
            System.out.println("\n" + planningService.formatRoute(route));
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }

    private void createCompositeRoute() {
        System.out.println("\n═══ CREATE COMPOSITE ROUTE ═══");
        System.out.println("Note: Freight chain must be valid (dest[i] = origin[i+1])\n");

        List<Integer> selectedIds = new ArrayList<>();

        while (true) {
            List<Freight> available = freightRepo.findAll().stream()
                    .filter(f -> !selectedIds.contains(f.getId()))
                    .filter(f -> f.getAssignedWagons().size() > 0)
                    .collect(Collectors.toList());

            if (!selectedIds.isEmpty()) {
                System.out.println("\nCurrent Selection:");
                for (int i = 0; i < selectedIds.size(); i++) {
                    Freight f = freightRepo.findById(selectedIds.get(i)).get();
                    System.out.printf("  %d. Freight %d: %s → %s\n",
                            i + 1, f.getId(), f.getOriginName(), f.getDestinationName());
                }

                int lastId = selectedIds.get(selectedIds.size() - 1);
                Freight lastFreight = freightRepo.findById(lastId).get();
                int requiredOrigin = lastFreight.getDestinationId();

                System.out.println("\n⚠ Next freight must start at: " +
                        facilityRepo.findById(requiredOrigin).get().getName());

                available = available.stream()
                        .filter(f -> f.getOriginId() == requiredOrigin)
                        .collect(Collectors.toList());
            }

            if (available.isEmpty() && selectedIds.size() >= 2) {
                System.out.println("\n✓ No more compatible freights. Ready to create route.");
                break;
            }

            System.out.println("\nAvailable Freights:");
            for (Freight f : available) {
                System.out.printf("[%d] %s → %s (%d wagons)\n",
                        f.getId(), f.getOriginName(), f.getDestinationName(),
                        f.getAssignedWagons().size());
            }

            System.out.print("\nEnter Freight ID (0 to finish): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                if (selectedIds.size() < 2) {
                    System.out.println("\n✗ Need at least 2 freights.");
                    continue;
                }
                break;
            }

            if (available.stream().anyMatch(f -> f.getId() == choice)) {
                selectedIds.add(choice);
                System.out.println("✓ Freight " + choice + " added.");
            } else {
                System.out.println("✗ Invalid freight ID.");
            }
        }

        if (selectedIds.size() < 2) {
            System.out.println("\n✗ Cancelled.");
            return;
        }

        try {
            PlannedRoute route = planningService.createCompositeRoute(selectedIds);
            System.out.println("\n✓ Composite Route created!");
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
    }
}