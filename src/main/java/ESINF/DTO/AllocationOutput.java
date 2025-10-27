package ESINF.DTO;

import ESINF.model.Allocation;
import ESINF.model.OrderEligibility;

import java.util.List;

/**
 * DTO (Data Transfer Object) containing the output of the allocation process.
 * Combines order eligibility results and actual allocations.
 *
 * @param results List of order eligibility assessments for each order line
 * @param allocations List of actual allocations showing which boxes are allocated to which order lines
 */
public record AllocationOutput(
        List<OrderEligibility> results,
        List<Allocation> allocations
) {}

