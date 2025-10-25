package ESINF.model;

import java.util.*;

public class PickingPlan {

    public enum Heuristic { FF, FFD, BFD }

    private final double trolleyCapacity;
    private final Map<String, Item> itemCatalog;

    public PickingPlan(double trolleyCapacity, Map<String, Item> itemCatalog) {
        if (trolleyCapacity <= 0) {
            throw new IllegalArgumentException("Trolley capacity must be positive");
        }
        if (itemCatalog == null) {
            throw new IllegalArgumentException("Item catalog cannot be null");
        }

        this.trolleyCapacity = trolleyCapacity;
        this.itemCatalog = itemCatalog;
    }

    /**
     * Packs allocations into trolleys using the selected heuristic.
     *
     * @param allocations List of allocations from USEI02
     * @param heuristic FF, FFD, or BFD
     * @return List of trolleys with packed allocations
     */
    public List<Trolley> generatePickingPlan(List<Allocation> allocations, Heuristic heuristic) {

        if (allocations == null || allocations.isEmpty()) {
            System.out.println("⚠️ No allocations to pack. Returning empty picking plan.");
            return new ArrayList<>();
        }


        if (heuristic == null) {
            throw new IllegalArgumentException("Heuristic cannot be null");
        }

        List<Allocation> workingList = new ArrayList<>(allocations);

        // Compute weight per allocation
        Map<Allocation, Double> allocWeight = new HashMap<>();
        List<Allocation> invalidAllocations = new ArrayList<>();

        for (Allocation a : workingList) {
            // Validation 3: Check allocation fields
            if (a == null) {
                System.err.println("Null allocation found - skipping");
                continue;
            }

            if (a.getSku() == null || a.getSku().isEmpty()) {
                System.err.println("Allocation with null/empty SKU - skipping");
                continue;
            }

            if (a.getQty() <= 0) {
                System.err.println("Allocation with invalid quantity (" + a.getQty() + ") - skipping");
                continue;
            }

            // Validation 4: Check if SKU exists in catalog
            Item item = itemCatalog.get(a.getSku());
            if (item == null) {
                System.err.println("Warning: SKU " + a.getSku() + " not found in catalog - assuming weight 0");
            }

            double weight = (item != null ? item.getUnitWeight() : 0) * a.getQty();

            // Validation 5: Check if allocation exceeds trolley capacity
            if (weight > trolleyCapacity) {
                System.err.println("Warning: Allocation " + a.getOrderId() +
                        " line " + a.getLineNo() + " (SKU=" + a.getSku() +
                        ") exceeds trolley capacity!");
                System.err.println("   Weight: " + String.format("%.2f", weight) +
                        " kg > Capacity: " + String.format("%.2f", trolleyCapacity) + " kg");
                System.err.println("   This allocation will be placed alone in a trolley (overweight).");
                invalidAllocations.add(a);
            }

            allocWeight.put(a, weight);
        }


        if (heuristic == Heuristic.FFD || heuristic == Heuristic.BFD) {
            workingList.sort((a1, a2) -> {
                Double w1 = allocWeight.get(a1);
                Double w2 = allocWeight.get(a2);
                if (w1 == null) w1 = 0.0;
                if (w2 == null) w2 = 0.0;
                return Double.compare(w2, w1); // Descending
            });
        }

        List<Trolley> trolleys = new ArrayList<>();

        for (Allocation alloc : workingList) {
            Double weight = allocWeight.get(alloc);
            if (weight == null) {
                System.err.println("No weight calculated for allocation - skipping");
                continue;
            }

            if (heuristic == Heuristic.FF || heuristic == Heuristic.FFD) {
                placeFirstFit(trolleys, alloc, weight);
            } else if (heuristic == Heuristic.BFD) {
                placeBestFit(trolleys, alloc, weight);
            }
        }


        if (trolleys.isEmpty() && !workingList.isEmpty()) {
            System.err.println("Warning: No trolleys created despite having allocations!");
        }

        return trolleys;
    }

    private void placeFirstFit(List<Trolley> trolleys, Allocation alloc, double weight) {

        for (Trolley t : trolleys) {
            if (t.canFit(weight)) {
                t.addAllocation(alloc, weight);
                return;
            }
        }


        Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
        newTrolley.addAllocation(alloc, weight);
        trolleys.add(newTrolley);
    }

    private void placeBestFit(List<Trolley> trolleys, Allocation alloc, double weight) {
        Trolley bestFit = null;
        double minRemaining = Double.MAX_VALUE;


        for (Trolley t : trolleys) {
            if (t.canFit(weight)) {
                double remaining = t.getCapacity() - (t.getUsedWeight() + weight);
                if (remaining < minRemaining) {
                    minRemaining = remaining;
                    bestFit = t;
                }
            }
        }

        if (bestFit == null) {
            
            Trolley newTrolley = new Trolley(trolleys.size() + 1, trolleyCapacity);
            newTrolley.addAllocation(alloc, weight);
            trolleys.add(newTrolley);
        } else {
            bestFit.addAllocation(alloc, weight);
        }
    }

    /**
     * Print statistics about the picking plan
     */
    public void printStatistics(List<Trolley> trolleys, Heuristic heuristic) {
        if (trolleys == null || trolleys.isEmpty()) {
            System.out.println("No trolleys in picking plan.");
            return;
        }

        System.out.println("\n═══ PICKING PLAN STATISTICS ═══");
        System.out.println("Heuristic: " + heuristic);
        System.out.println("Total trolleys: " + trolleys.size());

        int totalAllocations = trolleys.stream()
                .mapToInt(t -> t.getAllocations().size())
                .sum();
        System.out.println("Total allocations: " + totalAllocations);

        double avgUtilization = trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);

        double minUtilization = trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .min()
                .orElse(0.0);

        double maxUtilization = trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .max()
                .orElse(0.0);

        System.out.printf("Average utilization: %.1f%%\n", avgUtilization * 100);
        System.out.printf("Min utilization: %.1f%%\n", minUtilization * 100);
        System.out.printf("Max utilization: %.1f%%\n", maxUtilization * 100);
        System.out.println("═══════════════════════════════\n");
    }
}