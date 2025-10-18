package LAPR3.UI.menu;

import LAPR3.controller.DataController;
import LAPR3.UI.utils.InputValidator;
import LAPR3.UI.utils.TableFormatter;
import ESINF.DTO.AllocationOutput;
import ESINF.model.Allocation;
import ESINF.model.OrderEligibility;
import ESINF.service.AllocationManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Submenu for order management operations (USEI02)
 */
public class OrderManagementMenu {

    private final Scanner scanner;
    private final DataController dataController;
    private final InputValidator validator;
    private final TableFormatter formatter;

    public OrderManagementMenu(Scanner scanner, DataController dataController, InputValidator validator) {
        this.scanner = scanner;
        this.dataController = dataController;
        this.validator = validator;
        this.formatter = new TableFormatter();
    }

    public void show() {
        while (true) {
            displayMenu();
            int choice = validator.readInt("Enter your choice: ", 0, 4);

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
        System.out.println("║         ORDER MANAGEMENT (USEI02)                ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│  1. Check Order Eligibility (Strict Mode)      │");
        System.out.println("│  2. Check Order Eligibility (Partial Mode)     │");
        System.out.println("│  3. Compare Both Modes                          │");
        System.out.println("│  4. View Order Statistics                       │");
        System.out.println("│  0. Back to Main Menu                           │");
        System.out.println("└─────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> checkEligibilityStrict();
            case 2 -> checkEligibilityPartial();
            case 3 -> compareModes();
            case 4 -> viewOrderStatistics();
        }
    }

    private void checkEligibilityStrict() {
        System.out.println("\n═══ ORDER ELIGIBILITY - STRICT MODE ═══");

        if (!validatePrerequisites()) return;

        System.out.println("In STRICT mode, a line is ELIGIBLE only if fully allocated.");
        System.out.println();

        AllocationManager manager = new AllocationManager(dataController.getWarehouseManager());

        System.out.println("⏳ Processing orders in strict mode...");
        AllocationOutput output = manager.allocateOrders(dataController.getOrders(), true);

        displayResults(output, "STRICT");

        boolean save = validator.readConfirmation("\n💾 Save results to CSV?");
        if (save) {
            saveResults(output, "strict");
        }
    }

    private void checkEligibilityPartial() {
        System.out.println("\n═══ ORDER ELIGIBILITY - PARTIAL MODE ═══");

        if (!validatePrerequisites()) return;

        System.out.println("In PARTIAL mode, lines can be partially allocated.");
        System.out.println();

        AllocationManager manager = new AllocationManager(dataController.getWarehouseManager());

        System.out.println("⏳ Processing orders in partial mode...");
        AllocationOutput output = manager.allocateOrders(dataController.getOrders(), false);

        displayResults(output, "PARTIAL");

        boolean save = validator.readConfirmation("\n💾 Save results to CSV?");
        if (save) {
            saveResults(output, "partial");
        }
    }

    private void compareModes() {
        System.out.println("\n═══ COMPARE STRICT vs PARTIAL MODES ═══");

        if (!validatePrerequisites()) return;

        AllocationManager manager = new AllocationManager(dataController.getWarehouseManager());

        System.out.println("⏳ Processing orders in both modes...\n");

        AllocationOutput strict = manager.allocateOrders(dataController.getOrders(), true);
        AllocationOutput partial = manager.allocateOrders(dataController.getOrders(), false);

        displayComparison(strict, partial);

        boolean save = validator.readConfirmation("\n💾 Save both results to CSV?");
        if (save) {
            saveResults(strict, "strict");
            saveResults(partial, "partial");
        }
    }

    private void viewOrderStatistics() {
        System.out.println("\n═══ ORDER STATISTICS ═══");

        if (!dataController.isOrdersLoaded()) {
            System.out.println("❌ No orders loaded!");
            return;
        }

        var orders = dataController.getOrders();
        int totalOrders = orders.size();
        int totalLines = orders.stream().mapToInt(o -> o.getLines().size()).sum();

        System.out.println("Total orders: " + totalOrders);
        System.out.println("Total order lines: " + totalLines);
        System.out.println();

        System.out.println("Orders by priority:");

        // Solução 1: Usar variável final explícita para cada iteração
        for (int i = 1; i <= 3; i++) {
            final int priorityValue = i; // Variável efetivamente final
            long count = orders.stream().filter(o -> o.getPriority() == priorityValue).count();
            String label = switch (priorityValue) {
                case 1 -> "High";
                case 2 -> "Medium";
                case 3 -> "Low";
                default -> "Unknown";
            };
            System.out.printf("  Priority %d (%s): %d orders%n", priorityValue, label, count);
        }
    }

    private void displayResults(AllocationOutput output, String mode) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("RESULTS - " + mode + " MODE");
        System.out.println("═".repeat(80));

        // Eligibility results
        System.out.println("\n📋 ORDER ELIGIBILITY:");
        System.out.println("─".repeat(80));
        System.out.printf("%-10s | %-6s | %-10s | %-8s | %-8s | %-15s%n",
                "OrderID", "Line", "SKU", "Requested", "Allocated", "Status");
        System.out.println("─".repeat(80));

        for (OrderEligibility result : output.results()) {
            System.out.printf("%-10s | %-6d | %-10s | %8d | %8d | %-15s%n",
                    result.getOrderId(),
                    result.getLineNo(),
                    result.getSku(),
                    result.getRequestedQty(),
                    result.getAllocatedQty(),
                    result.getStatus());
        }
        System.out.println("─".repeat(80));

        // Statistics
        long eligible = output.results().stream().filter(r -> r.getStatus().equals("ELIGIBLE")).count();
        long partial = output.results().stream().filter(r -> r.getStatus().equals("PARTIAL")).count();
        long undispatchable = output.results().stream().filter(r -> r.getStatus().equals("UNDISPATCHABLE")).count();

        System.out.println("\n📊 SUMMARY:");
        System.out.println("  ✓ Eligible: " + eligible);
        System.out.println("  ⚠ Partial: " + partial);
        System.out.println("  ✗ Undispatchable: " + undispatchable);
        System.out.println("  Total lines: " + output.results().size());

        // Allocations
        if (!output.allocations().isEmpty()) {
            System.out.println("\n📦 ALLOCATION DETAILS (first 20):");
            System.out.println("─".repeat(90));
            System.out.printf("%-10s | %-6s | %-10s | %-5s | %-15s | %-6s | %-6s%n",
                    "OrderID", "Line", "SKU", "Qty", "BoxID", "Aisle", "Bay");
            System.out.println("─".repeat(90));

            int count = 0;
            for (Allocation alloc : output.allocations()) {
                if (count++ >= 20) {
                    System.out.println("... (" + (output.allocations().size() - 20) + " more allocations)");
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
    }

    private void displayComparison(AllocationOutput strict, AllocationOutput partial) {
        System.out.println("\n" + "═".repeat(80));
        System.out.println("COMPARISON: STRICT vs PARTIAL");
        System.out.println("═".repeat(80));

        long strictEligible = strict.results().stream().filter(r -> r.getStatus().equals("ELIGIBLE")).count();
        long strictUndispatchable = strict.results().stream().filter(r -> r.getStatus().equals("UNDISPATCHABLE")).count();

        long partialEligible = partial.results().stream().filter(r -> r.getStatus().equals("ELIGIBLE")).count();
        long partialPartial = partial.results().stream().filter(r -> r.getStatus().equals("PARTIAL")).count();
        long partialUndispatchable = partial.results().stream().filter(r -> r.getStatus().equals("UNDISPATCHABLE")).count();

        System.out.println("\n📊 STRICT MODE:");
        System.out.println("  ✓ Eligible: " + strictEligible);
        System.out.println("  ✗ Undispatchable: " + strictUndispatchable);
        System.out.println("  Total allocations: " + strict.allocations().size());

        System.out.println("\n📊 PARTIAL MODE:");
        System.out.println("  ✓ Eligible: " + partialEligible);
        System.out.println("  ⚠ Partial: " + partialPartial);
        System.out.println("  ✗ Undispatchable: " + partialUndispatchable);
        System.out.println("  Total allocations: " + partial.allocations().size());

        System.out.println("\n💡 INSIGHTS:");
        int difference = partial.allocations().size() - strict.allocations().size();
        if (difference > 0) {
            System.out.println("  • Partial mode allows " + difference + " more allocations");
        }
        System.out.println("  • Partial mode reduces undispatchable from " + strictUndispatchable + " to " + partialUndispatchable);
    }

    private void saveResults(AllocationOutput output, String mode) {
        try {
            String eligFile = "Logs/eligibility_" + mode + ".csv";
            String allocFile = "Logs/allocation_" + mode + ".csv";

            // Save eligibility
            try (FileWriter writer = new FileWriter(eligFile)) {
                writer.write("orderId,lineNo,sku,requestedQty,allocatedQty,status\n");
                for (OrderEligibility res : output.results()) {
                    writer.write(String.format("%s,%d,%s,%d,%d,%s\n",
                            res.getOrderId(), res.getLineNo(), res.getSku(),
                            res.getRequestedQty(), res.getAllocatedQty(), res.getStatus()));
                }
            }

            // Save allocations
            try (FileWriter writer = new FileWriter(allocFile)) {
                writer.write("orderId,lineNo,sku,qty,boxId,aisle,bay\n");
                for (Allocation alloc : output.allocations()) {
                    writer.write(String.format("%s,%d,%s,%d,%s,%s,%s\n",
                            alloc.getOrderId(), alloc.getLineNo(), alloc.getSku(),
                            alloc.getQty(), alloc.getBoxId(), alloc.getAisle(), alloc.getBay()));
                }
            }

            System.out.println("✓ Results saved:");
            System.out.println("  • " + eligFile);
            System.out.println("  • " + allocFile);

        } catch (IOException e) {
            System.err.println("❌ Error saving files: " + e.getMessage());
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

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}