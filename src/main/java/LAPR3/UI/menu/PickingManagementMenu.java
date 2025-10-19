package LAPR3.UI.menu;

import LAPR3.controller.DataController;
import LAPR3.controller.PickingController;
import LAPR3.UI.utils.InputValidator;
import ESINF.DTO.AllocationOutput;
import ESINF.model.*;
import ESINF.service.AllocationManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Submenu for picking plans management (USEI03)
 */
public class PickingManagementMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final PickingController pickingController;
    private final InputValidator validator;

    private AllocationOutput currentAllocation;

    public PickingManagementMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;
        this.pickingController = new PickingController(dataController);
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
        System.out.println("║         PICKING PLANS MANAGEMENT (USEI03)       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // Show current allocation status
        if (currentAllocation != null) {
            System.out.println("📊 Current Allocation: " +
                    currentAllocation.allocations().size() + " allocations loaded");
        } else {
            System.out.println("⚠️  No allocation loaded (run option 1 first)");
        }
        System.out.println();

        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Generate Allocation (USEI02)                │");
        System.out.println("│  2. Generate Picking Plan (FF)                  │");
        System.out.println("│  3. Generate Picking Plan (FFD)                 │");
        System.out.println("│  4. Generate Picking Plan (BFD)                 │");
        System.out.println("│  5. Compare All Heuristics                      │");
        System.out.println("│  6. Export Picking Plan to CSV                  │");
        System.out.println("│  0. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> generateAllocation();
            case 2 -> generatePickingPlan(PickingPlan.Heuristic.FF);
            case 3 -> generatePickingPlan(PickingPlan.Heuristic.FFD);
            case 4 -> generatePickingPlan(PickingPlan.Heuristic.BFD);
            case 5 -> compareHeuristics();
            case 6 -> exportPickingPlan();
        }
    }

    private void generateAllocation() {
        System.out.println("\n═══ GENERATE ALLOCATION (USEI02) ═══");

        if (!validatePrerequisites()) return;

        System.out.println("This will run order allocation to generate allocations for picking.");
        System.out.println();

        // Ask for mode
        System.out.println("Select allocation mode:");
        System.out.println("  1. Strict (only fully allocated orders)");
        System.out.println("  2. Partial (allow partial allocations)");

        int modeChoice = validator.readInt("Mode: ", 1, 2);
        boolean strictMode = (modeChoice == 1);

        System.out.println("\n⏳ Running allocation in " +
                (strictMode ? "STRICT" : "PARTIAL") + " mode...");

        AllocationManager allocationManager = new AllocationManager(
                dataController.getWarehouseManager()
        );

        currentAllocation = allocationManager.allocateOrders(
                dataController.getOrders(),
                strictMode
        );

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

        System.out.println("\n💡 Now you can generate picking plans (options 2-5)");
    }

    private void generatePickingPlan(PickingPlan.Heuristic heuristic) {
        System.out.println("\n═══ GENERATE PICKING PLAN - " + heuristic + " ═══");

        if (currentAllocation == null) {
            System.out.println("❌ No allocation available!");
            System.out.println("Please run option 1 first to generate allocations.");
            return;
        }

        if (currentAllocation.allocations().isEmpty()) {
            System.out.println("❌ No allocations to pack!");
            return;
        }

        // Ask for trolley capacity
        System.out.println("\nTrolley Configuration:");
        double capacity = readDouble("Enter trolley capacity (kg): ", 1.0, 1000.0);

        System.out.println("\n⏳ Generating picking plan with " + heuristic + "...");

        List<Trolley> trolleys = pickingController.generatePickingPlan(
                currentAllocation,
                capacity,
                heuristic
        );

        displayPickingPlan(trolleys, heuristic);
    }

    private void compareHeuristics() {
        System.out.println("\n═══ COMPARE ALL HEURISTICS ═══");

        if (currentAllocation == null) {
            System.out.println("❌ No allocation available!");
            System.out.println("Please run option 1 first to generate allocations.");
            return;
        }

        if (currentAllocation.allocations().isEmpty()) {
            System.out.println("❌ No allocations to pack!");
            return;
        }

        // Ask for trolley capacity
        double capacity = readDouble("Enter trolley capacity (kg): ", 1.0, 1000.0);

        System.out.println("\n⏳ Comparing all heuristics...");

        pickingController.compareHeuristics(currentAllocation, capacity);
    }

    private void displayPickingPlan(List<Trolley> trolleys, PickingPlan.Heuristic heuristic) {
        System.out.println("\n═══ PICKING PLAN - " + heuristic + " ═══");

        if (trolleys.isEmpty()) {
            System.out.println("No trolleys needed.");
            return;
        }

        System.out.println("\n📊 Summary:");
        System.out.println("  • Total trolleys: " + trolleys.size());
        System.out.println("  • Total items: " +
                trolleys.stream().mapToInt(t -> t.getAllocations().size()).sum());

        // Show first 3 trolleys in detail
        int toShow = Math.min(3, trolleys.size());
        System.out.println("\n📦 Trolley Details (first " + toShow + "):");

        for (int i = 0; i < toShow; i++) {
            Trolley t = trolleys.get(i);
            System.out.println("\n" + t);
            System.out.println("─".repeat(90));
            System.out.printf("%-10s | %-6s | %-10s | %-5s | %-15s | %-6s | %-6s%n",
                    "OrderID", "Line", "SKU", "Qty", "BoxID", "Aisle", "Bay");
            System.out.println("─".repeat(90));

            int itemCount = 0;
            for (Allocation alloc : t.getAllocations()) {
                if (itemCount++ >= 10) {
                    System.out.println("... (" + (t.getAllocations().size() - 10) + " more items)");
                    break;
                }
                System.out.printf("%-10s | %-6d | %-10s | %5d | %-15s | %-6s | %-6s%n",
                        alloc.getOrderId(),
                        alloc.getLineNo(),
                        alloc.getSku(),
                        alloc.getQty(),
                        alloc.getBoxId(),
                        alloc.getAisle(),
                        alloc.getBay());
            }
            System.out.println("─".repeat(90));
        }

        if (trolleys.size() > 3) {
            System.out.println("\n... (" + (trolleys.size() - 3) + " more trolleys)");
        }

        // Utilization distribution
        System.out.println("\n📊 Utilization Distribution:");
        for (Trolley t : trolleys) {
            int barLength = (int)(t.getUtilization() * 50);
            String bar = "█".repeat(barLength) + "░".repeat(50 - barLength);
            System.out.printf("Trolley %2d: [%s] %.1f%%%n",
                    t.getId(), bar, t.getUtilization() * 100);
        }
    }

    private void exportPickingPlan() {
        System.out.println("\n═══ EXPORT PICKING PLAN ═══");

        if (!pickingController.hasPickingPlan()) {
            System.out.println("❌ No picking plan generated yet!");
            System.out.println("Generate a picking plan first (options 2-4).");
            return;
        }

        List<Trolley> trolleys = pickingController.getLastPickingPlan();

        String defaultPath = "Logs/picking_plan.csv";
        String path = validator.readFilePath("Enter output CSV path", defaultPath);

        try (FileWriter writer = new FileWriter(path)) {
            // Header
            writer.write("trolleyId,orderId,lineNo,sku,qty,boxId,aisle,bay\n");

            // Data
            for (Trolley trolley : trolleys) {
                for (Allocation alloc : trolley.getAllocations()) {
                    writer.write(String.format("%d,%s,%d,%s,%d,%s,%s,%s\n",
                            trolley.getId(),
                            alloc.getOrderId(),
                            alloc.getLineNo(),
                            alloc.getSku(),
                            alloc.getQty(),
                            alloc.getBoxId(),
                            alloc.getAisle(),
                            alloc.getBay()));
                }
            }

            System.out.println("✓ Picking plan exported to: " + path);
            System.out.println("  • Total trolleys: " + trolleys.size());
            System.out.println("  • Total lines: " +
                    trolleys.stream().mapToInt(t -> t.getAllocations().size()).sum());

        } catch (IOException e) {
            System.err.println("❌ Error writing file: " + e.getMessage());
        }
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