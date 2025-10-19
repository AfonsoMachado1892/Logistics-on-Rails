package LAPR3.controller;

import ESINF.model.*;
import ESINF.DTO.AllocationOutput;

import java.util.List;

/**
 * Controller for picking plans operations (USEI03)
 * Integrates with OrderController for USEI02 allocations
 */
public class PickingController {

    private final DataController dataController;
    private final OrderController orderController;
    private List<Trolley> lastPickingPlan;
    private PickingPlan.Heuristic lastHeuristic;

    public PickingController(DataController dataController, OrderController orderController) {
        this.dataController = dataController;
        this.orderController = orderController;
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

        PickingPlan pickingPlan = new PickingPlan(trolleyCapacity, dataController.getItems());

        List<Trolley> trolleys = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                heuristic
        );

        this.lastPickingPlan = trolleys;
        this.lastHeuristic = heuristic;

        pickingPlan.printStatistics(trolleys, heuristic);

        return trolleys;
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

        System.out.println("═══ FIRST FIT (FF) ═══");
        List<Trolley> ff = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                PickingPlan.Heuristic.FF
        );
        pickingPlan.printStatistics(ff, PickingPlan.Heuristic.FF);

        System.out.println("═══ FIRST FIT DECREASING (FFD) ═══");
        List<Trolley> ffd = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                PickingPlan.Heuristic.FFD
        );
        pickingPlan.printStatistics(ffd, PickingPlan.Heuristic.FFD);

        System.out.println("═══ BEST FIT DECREASING (BFD) ═══");
        List<Trolley> bfd = pickingPlan.generatePickingPlan(
                allocationOutput.allocations(),
                PickingPlan.Heuristic.BFD
        );
        pickingPlan.printStatistics(bfd, PickingPlan.Heuristic.BFD);

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

        int minTrolleys = Math.min(ff.size(), Math.min(ffd.size(), bfd.size()));
        String best = "";
        if (ff.size() == minTrolleys) best = "FF";
        else if (ffd.size() == minTrolleys) best = "FFD";
        else if (bfd.size() == minTrolleys) best = "BFD";

        System.out.println("\n💡 Best heuristic (fewest trolleys): " + best);

        double maxAvg = Math.max(ffAvg, Math.max(ffdAvg, bfdAvg));
        String bestUtil = "";
        if (Math.abs(ffAvg - maxAvg) < 0.01) bestUtil = "FF";
        else if (Math.abs(ffdAvg - maxAvg) < 0.01) bestUtil = "FFD";
        else if (Math.abs(bfdAvg - maxAvg) < 0.01) bestUtil = "BFD";

        System.out.println("💡 Best heuristic (highest utilization): " + bestUtil);
    }

    public List<Trolley> getLastPickingPlan() {
        return lastPickingPlan;
    }

    public boolean hasPickingPlan() {
        return lastPickingPlan != null && !lastPickingPlan.isEmpty();
    }
}