package LAPR3.controller;

import ESINF.DTO.AllocationOutput;
import ESINF.model.Order;
import ESINF.service.AllocationManager;

import java.util.List;

/**
 * Controller for order management operations (USEI02)
 * Encapsulates AllocationManager from ESINF backend
 */
public class OrderController {

    private final DataController dataController;
    private AllocationOutput lastAllocationStrict;
    private AllocationOutput lastAllocationPartial;

    public OrderController(DataController dataController) {
        this.dataController = dataController;
    }

    /**
     * Run order allocation in strict or partial mode
     * Uses backend ESINF.service.AllocationManager
     *
     * @param strictMode true for strict, false for partial
     * @return AllocationOutput with results and allocations
     */
    public AllocationOutput allocateOrders(boolean strictMode) {
        if (!dataController.isWarehouseSelected()) {
            throw new IllegalStateException("Warehouse must be selected!");
        }

        if (!dataController.isOrdersLoaded()) {
            throw new IllegalStateException("Orders must be loaded!");
        }

        // Use backend ESINF service (AllocationManager)
        AllocationManager allocationManager = new AllocationManager(
                dataController.getWarehouseManager()
        );

        List<Order> orders = dataController.getOrders();
        AllocationOutput output = allocationManager.allocateOrders(orders, strictMode);

        // Store for later use
        if (strictMode) {
            lastAllocationStrict = output;
        } else {
            lastAllocationPartial = output;
        }

        return output;
    }

    /**
     * Get last allocation in strict mode
     */
    public AllocationOutput getLastAllocationStrict() {
        return lastAllocationStrict;
    }

    /**
     * Get last allocation in partial mode
     */
    public AllocationOutput getLastAllocationPartial() {
        return lastAllocationPartial;
    }

    /**
     * Check if strict allocation was run
     */
    public boolean hasStrictAllocation() {
        return lastAllocationStrict != null;
    }

    /**
     * Check if partial allocation was run
     */
    public boolean hasPartialAllocation() {
        return lastAllocationPartial != null;
    }

    /**
     * Get orders count
     */
    public int getOrdersCount() {
        return dataController.isOrdersLoaded() ? dataController.getOrders().size() : 0;
    }
}
