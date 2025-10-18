package ESINF.model;

import java.util.*;

public class PickingPlan {

    public enum Heuristic { FF, FFD, BFD }

    private final double trolleyCapacity;
    private final Map<String, Item> itemCatalog;

    public PickingPlan(double trolleyCapacity, Map<String, Item> itemCatalog) {
        this.trolleyCapacity = trolleyCapacity;
        this.itemCatalog = itemCatalog;
    }

    /**
     * Packs allocations into trolleys using the selected heuristic.
     */
    public List<Trolley> generatePickingPlan(List<Allocation> allocations, Heuristic heuristic) {
        List<Allocation> workingList = new ArrayList<>(allocations);

        // Compute weight per allocation
        Map<Allocation, Double> allocWeight = new HashMap<>();
        for (Allocation a : workingList) {
            Item item = itemCatalog.get(a.getSku());
            double weight = (item != null ? item.getUnitWeight() : 0) * a.getQty();
            allocWeight.put(a, weight);
        }

        // Sort for FFD / BFD
        if (heuristic == Heuristic.FFD || heuristic == Heuristic.BFD) {
            workingList.sort((a1, a2) -> Double.compare(allocWeight.get(a2), allocWeight.get(a1)));
        }

        List<Trolley> trolleys = new ArrayList<>();

        for (Allocation alloc : workingList) {
            double weight = allocWeight.get(alloc);

            if (heuristic == Heuristic.FF || heuristic == Heuristic.FFD) {
                placeFirstFit(trolleys, alloc, weight);
            } else if (heuristic == Heuristic.BFD) {
                placeBestFit(trolleys, alloc, weight);
            }
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
}
