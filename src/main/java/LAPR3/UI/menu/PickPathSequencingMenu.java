package LAPR3.UI.menu;

import LAPR3.controller.DataController;
import LAPR3.controller.OrderController;
import LAPR3.controller.PickingController;
import LAPR3.UI.utils.InputValidator;
import ESINF.DTO.AllocationOutput;
import ESINF.model.*;
import ESINF.model.PickPathSequencing;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menu for Pick Path Sequencing (USEI04)
 * Optimizes picking routes using two strategies: Deterministic Sweep and Nearest-Neighbour
 */
public class PickPathSequencingMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final PickingController pickingController;
    private final InputValidator validator;
    private final OrderController orderController;

    private AllocationOutput currentAllocation;
    private List<Trolley> currentPickingPlan;

    public PickPathSequencingMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;
        this.orderController = new OrderController(dataController);
        this.pickingController = new PickingController(dataController, orderController);
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 6);

            if (choice == 0) {
                return; // Back to main menu
            }

            try {
                handleChoice(choice);
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }

            pause();
        }
    }

    private void displayMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         PICK PATH SEQUENCING (USEI04)            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // Show current status
        displayCurrentStatus();
        System.out.println();

        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Generate Allocation (USEI02)                │");
        System.out.println("│  2. Generate Picking Plan (USEI03)              │");
        System.out.println("│  3. Strategy A - Deterministic Sweep            │");
        System.out.println("│  4. Strategy B - Nearest-Neighbour              │");
        System.out.println("│  5. Compare Both Strategies                     │");
        System.out.println("│  6. Export Pick Paths to CSV                    │");
        System.out.println("│  0. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void displayCurrentStatus() {
        if (currentAllocation != null) {
            System.out.println("📊 Allocation: " + currentAllocation.allocations().size() + " allocations loaded");
        } else {
            System.out.println("⚠️  No allocation loaded (run option 1 first)");
        }

        if (currentPickingPlan != null && !currentPickingPlan.isEmpty()) {
            System.out.println("📦 Picking Plan: " + currentPickingPlan.size() + " trolleys ready");
            int totalItems = currentPickingPlan.stream().mapToInt(t -> t.getAllocations().size()).sum();
            System.out.println("   Total items: " + totalItems);
        } else {
            System.out.println("⚠️  No picking plan loaded (run option 2 first)");
        }
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> generateAllocation();
            case 2 -> generatePickingPlan();
            case 3 -> executeStrategyA();
            case 4 -> executeStrategyB();
            case 5 -> compareStrategies();
            case 6 -> exportPickPaths();
        }
    }

    private void generateAllocation() {
        System.out.println("\n═══ GENERATE ALLOCATION (USEI02) ═══");

        if (!validatePrerequisites()) return;

        System.out.println("This will run order allocation to generate allocations for picking.");
        System.out.println();

        System.out.println("Select allocation mode:");
        System.out.println("  1. Strict (only fully allocated orders)");
        System.out.println("  2. Partial (allow partial allocations)");

        int modeChoice = validator.readInt("Mode: ", 1, 2);
        boolean strictMode = (modeChoice == 1);

        System.out.println("\n⏳ Running allocation in " +
                (strictMode ? "STRICT" : "PARTIAL") + " mode...");

        currentAllocation = orderController.allocateOrders(strictMode);

        System.out.println("✓ Allocation completed!");
        System.out.println("  • Total allocations: " + currentAllocation.allocations().size());

        long eligible = currentAllocation.results().stream()
                .filter(r -> r.getStatus().equals("ELIGIBLE"))
                .count();
        long partial = currentAllocation.results().stream()
                .filter(r -> r.getStatus().equals("PARTIAL"))
                .count();

        System.out.println("  • Eligible orders: " + eligible);
        if (!strictMode) {
            System.out.println("  • Partial orders: " + partial);
        }

        System.out.println("\n💡 Now you can generate a picking plan (option 2)");
    }

    private void generatePickingPlan() {
        System.out.println("\n═══ GENERATE PICKING PLAN (USEI03) ═══");

        if (currentAllocation == null) {
            System.out.println("❌ No allocation available!");
            System.out.println("Please run option 1 first to generate allocations.");
            return;
        }

        if (currentAllocation.allocations().isEmpty()) {
            System.out.println("❌ No allocations to pack!");
            return;
        }

        System.out.println("\nSelect packing heuristic:");
        System.out.println("  1. First Fit (FF)");
        System.out.println("  2. First Fit Decreasing (FFD)");
        System.out.println("  3. Best Fit Decreasing (BFD)");

        int heuristicChoice = validator.readInt("Heuristic: ", 1, 3);
        PickingPlan.Heuristic heuristic = switch (heuristicChoice) {
            case 1 -> PickingPlan.Heuristic.FF;
            case 2 -> PickingPlan.Heuristic.FFD;
            case 3 -> PickingPlan.Heuristic.BFD;
            default -> PickingPlan.Heuristic.FF;
        };

        // Ask for trolley capacity
        System.out.println("\nTrolley Configuration:");
        double capacity = readDouble("Enter trolley capacity (kg): ", 1.0, 1000.0);

        System.out.println("\n⏳ Generating picking plan with " + heuristic + "...");

        currentPickingPlan = pickingController.generatePickingPlan(
                currentAllocation,
                capacity,
                heuristic
        );

        System.out.println("✓ Picking plan generated!");
        System.out.println("  • Total trolleys: " + currentPickingPlan.size());
        System.out.println("  • Total items: " +
                currentPickingPlan.stream().mapToInt(t -> t.getAllocations().size()).sum());

        System.out.println("\n💡 Now you can optimize pick paths (options 3-5)");
    }

    private void executeStrategyA() {
        System.out.println("\n═══ STRATEGY A - DETERMINISTIC SWEEP ═══");

        if (!validatePickingPlan()) return;

        System.out.println("⏳ Executing Deterministic Sweep strategy...");
        System.out.println("   (Orders bays by aisle ascending, then bay ascending)");

        List<Allocation> allAllocations = getAllAllocations();
        PickPathSequencing sequencer = new PickPathSequencing(dataController.getItems());
        
        PickPathSequencing.PickPath pathA = sequencer.strategyA(allAllocations);
        
        displayPickPath(pathA, "Strategy A - Deterministic Sweep");
    }

    private void executeStrategyB() {
        System.out.println("\n═══ STRATEGY B - NEAREST-NEIGHBOUR ═══");

        if (!validatePickingPlan()) return;

        System.out.println("⏳ Executing Nearest-Neighbour strategy...");
        System.out.println("   (Greedy algorithm: always go to nearest unvisited bay)");

        List<Allocation> allAllocations = getAllAllocations();
        PickPathSequencing sequencer = new PickPathSequencing(dataController.getItems());
        
        PickPathSequencing.PickPath pathB = sequencer.strategyB(allAllocations);
        
        displayPickPath(pathB, "Strategy B - Nearest-Neighbour");
    }

    private void compareStrategies() {
        System.out.println("\n═══ COMPARE BOTH STRATEGIES ═══");

        if (!validatePickingPlan()) return;

        System.out.println("⏳ Comparing both pick path strategies...");

        List<Allocation> allAllocations = getAllAllocations();
        PickPathSequencing sequencer = new PickPathSequencing(dataController.getItems());
        
        PickPathSequencing.PickPath[] paths = sequencer.generateBothPaths(allAllocations);
        PickPathSequencing.PickPath pathA = paths[0];
        PickPathSequencing.PickPath pathB = paths[1];

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              STRATEGY COMPARISON                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        System.out.printf("%-25s | %-15s | %-10s%n", "Strategy", "Total Distance", "Bays Visited");
        System.out.println("─".repeat(60));

        System.out.printf("%-25s | %-15.2f | %-10d%n", 
                "Deterministic Sweep", pathA.totalDistance, pathA.route.size());
        System.out.printf("%-25s | %-15.2f | %-10d%n", 
                "Nearest-Neighbour", pathB.totalDistance, pathB.route.size());

        System.out.println("─".repeat(60));

        // Determine best strategy
        String bestStrategy;
        double improvement;
        if (pathB.totalDistance < pathA.totalDistance) {
            bestStrategy = "Nearest-Neighbour";
            improvement = ((pathA.totalDistance - pathB.totalDistance) / pathA.totalDistance) * 100;
        } else {
            bestStrategy = "Deterministic Sweep";
            improvement = ((pathB.totalDistance - pathA.totalDistance) / pathB.totalDistance) * 100;
        }

        System.out.println("\n💡 Best strategy: " + bestStrategy);
        System.out.printf("💡 Improvement: %.1f%% distance reduction%n", improvement);

        System.out.println("\n📊 Detailed Results:");
        displayPickPath(pathA, "Strategy A - Deterministic Sweep");
        displayPickPath(pathB, "Strategy B - Nearest-Neighbour");
    }

    private void exportPickPaths() {
        System.out.println("\n═══ EXPORT PICK PATHS ═══");

        if (!validatePickingPlan()) return;

        System.out.println("Select strategy to export:");
        System.out.println("  1. Strategy A - Deterministic Sweep");
        System.out.println("  2. Strategy B - Nearest-Neighbour");
        System.out.println("  3. Both strategies");

        int exportChoice = validator.readInt("Choice: ", 1, 3);

        List<Allocation> allAllocations = getAllAllocations();
        PickPathSequencing sequencer = new PickPathSequencing(dataController.getItems());
        
        String defaultPath = "Logs/pick_paths.csv";
        String path = validator.readFilePath("Enter output CSV path", defaultPath);

        try (java.io.FileWriter writer = new java.io.FileWriter(path)) {
            // Header
            writer.write("strategy,step,bayId,aisle,bay,orderId,lineNo,sku,qty,boxId\n");

            if (exportChoice == 1 || exportChoice == 3) {
                // Export Strategy A
                PickPathSequencing.PickPath pathA = sequencer.strategyA(allAllocations);
                exportPickPathToCSV(writer, pathA, "Strategy_A");
            }

            if (exportChoice == 2 || exportChoice == 3) {
                // Export Strategy B
                PickPathSequencing.PickPath pathB = sequencer.strategyB(allAllocations);
                exportPickPathToCSV(writer, pathB, "Strategy_B");
            }

            System.out.println("✓ Pick paths exported to: " + path);

        } catch (java.io.IOException e) {
            System.err.println("❌ Error writing file: " + e.getMessage());
        }
    }

    private void exportPickPathToCSV(java.io.FileWriter writer, PickPathSequencing.PickPath path, String strategyName) throws java.io.IOException {
        int step = 1;
        for (PickPathSequencing.ConsolidatedBay bay : path.route) {
            for (Allocation alloc : bay.allocations) {
                writer.write(String.format("%s,%d,%s,%s,%s,%s,%d,%s,%d,%s\n",
                        strategyName,
                        step,
                        bay.getKey(),
                        bay.aisle,
                        bay.bay,
                        alloc.getOrderId(),
                        alloc.getLineNo(),
                        alloc.getSku(),
                        alloc.getQty(),
                        alloc.getBoxId()));
            }
            step++;
        }
    }

    private void displayPickPath(PickPathSequencing.PickPath path, String title) {
        System.out.println("\n" + title);
        System.out.println("─".repeat(50));
        System.out.printf("Total Distance: %.2f units%n", path.totalDistance);
        System.out.printf("Bays Visited: %d%n", path.route.size());
        
        System.out.println("\nRoute sequence:");
        for (int i = 0; i < path.route.size(); i++) {
            PickPathSequencing.ConsolidatedBay bay = path.route.get(i);
            System.out.printf("  %d. Aisle %d, Bay %d (Coordinate: %s) - %d items%n",
                    i + 1, bay.aisle, bay.bay, bay.getCoordinate(), bay.totalQty);
        }

        System.out.println("\nDetailed picking list:");
        for (PickPathSequencing.ConsolidatedBay bay : path.route) {
            System.out.printf("  Aisle %d, Bay %d:%n", bay.aisle, bay.bay);
            for (Allocation alloc : bay.allocations) {
                System.out.printf("    - Order %s, Line %d | SKU %s | Qty %d | Box %s%n",
                        alloc.getOrderId(), alloc.getLineNo(), alloc.getSku(),
                        alloc.getQty(), alloc.getBoxId());
            }
        }
        System.out.println();
    }

    private List<Allocation> getAllAllocations() {
        List<Allocation> allAllocations = new ArrayList<>();
        for (Trolley trolley : currentPickingPlan) {
            allAllocations.addAll(trolley.getAllocations());
        }
        return allAllocations;
    }

    private boolean validatePrerequisites() {
        if (!dataController.isWarehouseSelected()) {
            System.out.println("❌ No warehouse selected!");
            System.out.println("Please select a warehouse from the Data Import menu first.");
            return false;
        }

        if (!dataController.isOrdersLoaded()) {
            System.out.println("❌ No orders loaded!");
            System.out.println("Please load orders from the Data Import menu first.");
            return false;
        }

        return true;
    }

    private boolean validatePickingPlan() {
        if (currentPickingPlan == null || currentPickingPlan.isEmpty()) {
            System.out.println("❌ No picking plan available!");
            System.out.println("Please run options 1 and 2 first to generate allocations and picking plan.");
            return false;
        }

        return true;
    }

    private double readDouble(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("❌ Input cannot be empty.");
                    continue;
                }

                double value = Double.parseDouble(input);

                if (value < min || value > max) {
                    System.out.printf("❌ Value must be between %.1f and %.1f.%n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format.");
            }
        }
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
