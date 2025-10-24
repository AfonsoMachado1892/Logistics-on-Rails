package ESINF.model;

import java.util.ArrayList;
import java.util.List;

public class Trolley {
    private final int id;
    private final double capacity;
    private double usedWeight;
    private final List<Allocation> allocations;

    public Trolley(int id) {
        this(id, 1000.0);
    }

    public void addAllocation(Allocation alloc) {
        addAllocation(alloc, 0.0);
    }

    public Trolley(int id, double capacity) {
        this.id = id;
        this.capacity = capacity;
        this.allocations = new ArrayList<>();
        this.usedWeight = 0;
    }

    public boolean canFit(double weight) {
        return usedWeight + weight <= capacity;
    }

    public void addAllocation(Allocation alloc, double weight) {
        allocations.add(alloc);
        usedWeight += weight;
    }

    public double getUsedWeight() { return usedWeight; }
    public double getCapacity() { return capacity; }
    public double getUtilization() { return usedWeight / capacity; }
    public List<Allocation> getAllocations() { return allocations; }
    public int getId() { return id; }

    @Override
    public String toString() {
        return String.format("Trolley[%d] %.1f/%.1f kg (%.1f%% full)",
                id, usedWeight, capacity, getUtilization() * 100);
    }
}
