package ESINF.DTO;

import ESINF.model.Allocation;
import ESINF.model.OrderEligibility;

import java.util.List;

public record AllocationOutput(
        List<OrderEligibility> results,
        List<Allocation> allocations
) {}

