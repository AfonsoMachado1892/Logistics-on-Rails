package LAPR3.controller;

import ESINF.model.*;
import ESINF.DTO.AllocationOutput;
import ESINF.service.AllocationManager;

import java.util.List;

/**
 * Controller for picking plans operations (USEI03)
 */
public class PickingController {

    private final DataController dataController;
    private List<Trolley> lastPickingPlan;
    private PickingPlan.Heuristic lastHeuristic;

    public PickingController(DataController dataController) {
        this.dataController = dataController;
    }

    /**
     * Generate picking plan from allocation output
     */
    public List<Trolley> generatePickingPlan(AllocationOutput allocationOutput,
                                             double trolleyCapacity,
                                             PickingPlan.Heuristic heuristic) {

        if (allocationOutput == null || allocationOutput.allocations().isEmpty()) {
            throw new IllegalStateException("No allocations available for picking plan!");
        }

        if (trolleyCapacity <= 0) {
            throw new IllegalArgumentException("Trolley capacity must be positive!");
        }

        if (heuristic == null) {
            throw new IllegalArgumentException("Heuristic must be specified!");
        }

        // Create picking plan
        PickingPlan pickingPlan = new PickingPlan(trolleyCapacity, dataController.getItems());

        // Generate plan
        List<Trolley> trolleys = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                heuristic
        );

        // Store for later use
        this.lastPickingPlan = trolleys;
        this.lastHeuristic = heuristic;

        // Print statistics
        pickingPlan.printStatistics(trolleys, heuristic);

        return trolleys;
    }

    /**
     * Generate picking plan directly from orders (combines USEI02 + USEI03)
     */
    public List<Trolley> generatePickingPlanFromOrders(List<Order> orders,
                                                       boolean strictMode,
                                                       double trolleyCapacity,
                                                       PickingPlan.Heuristic heuristic) {

        if (!dataController.isWarehouseSelected()) {
            throw new IllegalStateException("Warehouse must be selected!");
        }

        // Step 1: Run allocation (USEI02)
        AllocationManager allocationManager = new AllocationManager(
                dataController.getWarehouseManager()
        );

        AllocationOutput allocationOutput = allocationManager.allocateOrders(orders, strictMode);

        // Step 2: Generate picking plan (USEI03)
        return generatePickingPlan(allocationOutput, trolleyCapacity, heuristic);
    }

    /**
     * Compare all three heuristics
     */
    public void compareHeuristics(AllocationOutput allocationOutput, double trolleyCapacity) {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║          HEURISTICS COMPARISON                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        PickingPlan pickingPlan = new PickingPlan(trolleyCapacity, dataController.getItems());

        // FF
        System.out.println("═══ FIRST FIT (FF) ═══");
        List<Trolley> ff = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                PickingPlan.Heuristic.FF
        );
        pickingPlan.printStatistics(ff, PickingPlan.Heuristic.FF);

        // FFD
        System.out.println("═══ FIRST FIT DECREASING (FFD) ═══");
        List<Trolley> ffd = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                PickingPlan.Heuristic.FFD
        );
        pickingPlan.printStatistics(ffd, PickingPlan.Heuristic.FFD);

        // BFD
        System.out.println("═══ BEST FIT DECREASING (BFD) ═══");
        List<Trolley> bfd = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                PickingPlan.Heuristic.BFD
        );
        pickingPlan.printStatistics(bfd, PickingPlan.Heuristic.BFD);

        // Comparison summary
        printComparisonSummary(ff, ffd, bfd);
    }

    private void printComparisonSummary(List<Trolley> ff, List<Trolley> ffd, List<Trolley> bfd) {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              COMPARISON SUMMARY                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        System.out.printf("%-15s | %-10s | %-15s%n", "Heuristic", "Trolleys", "Avg Util");
        System.out.println("─".repeat(50));

        double ffAvg = ff.stream().mapToDouble(Trolley::getUtilization).average().orElse(0) * 100;
        double ffdAvg = ffd.stream().mapToDouble(Trolley::getUtilization).average().orElse(0) * 100;
        double bfdAvg = bfd.stream().mapToDouble(Trolley::getUtilization).average().orElse(0) * 100;

        System.out.printf("%-15s | %-10d | %14.1f%%%n", "FF", ff.size(), ffAvg);
        System.out.printf("%-15s | %-10d | %14.1f%%%n", "FFD", ffd.size(), ffdAvg);
        System.out.printf("%-15s | %-10d | %14.1f%%%n", "BFD", bfd.size(), bfdAvg);

        System.out.println("─".repeat(50));

        // Best heuristic
        int minTrolleys = Math.min(ff.size(), Math.min(ffd.size(), bfd.size()));
        String best = "";
        if (ff.size() == minTrolleys) best = "FF";
        else if (ffd.size() == minTrolleys) best = "FFD";
        else if (bfd.size() == minTrolleys) best = "BFD";

        System.out.println("\n💡 Best heuristic (fewest trolleys): " + best);

        // Best utilization
        double maxAvg = Math.max(ffAvg, Math.max(ffdAvg, bfdAvg));
        String bestUtil = "";
        if (Math.abs(ffAvg - maxAvg) < 0.01) bestUtil = "FF";
        else if (Math.abs(ffdAvg - maxAvg) < 0.01) bestUtil = "FFD";
        else if (Math.abs(bfdAvg - maxAvg) < 0.01) bestUtil = "BFD";

        System.out.println("💡 Best heuristic (highest utilization): " + bestUtil);
    }

    /**
     * Get last generated picking plan
     */
    public List<Trolley> getLastPickingPlan() {
        return lastPickingPlan;
    }

    /**
     * Check if picking plan was generated
     */
    public boolean hasPickingPlan() {
        return lastPickingPlan != null && !lastPickingPlan.isEmpty();
    }
}