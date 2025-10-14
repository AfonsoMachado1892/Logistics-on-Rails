package ESINF.service;

import ESINF.DTO.AllocationOutput;
import ESINF.model.*;

import java.util.*;

/**
 * Responsável por verificar a elegibilidade das encomendas e alocar quantidades
 * de caixas (boxes) em FEFO/FIFO order, produzindo listas de estados e alocações.
 *
 * USEI02 - Order Eligibility & Allocation
 */
public class AllocationManager {

    private final WarehouseManager warehouseManager;

    public AllocationManager(WarehouseManager warehouseManager) {
        this.warehouseManager = warehouseManager;
    }

    /**
     * Processa todas as encomendas (orders) segundo as regras da USEI02.
     *
     * @param orders lista de encomendas carregadas
     * @param strictMode se true → uma linha só é ELIGIBLE se for totalmente alocada
     * @return objeto AllocationOutput com listas de resultados e alocações
     */
    public AllocationOutput allocateOrders(List<Order> orders, boolean strictMode) {
        List<OrderEligibility> results = new ArrayList<>();
        List<Allocation> allocations = new ArrayList<>();

        // Ordenar encomendas por prioridade, dueDate e orderId (implementado em Order.compareTo)
        Collections.sort(orders);

        // Cópia de trabalho do stock atual
        Map<String, Integer> availableQtyByBox = new HashMap<>();
        for (Box box : warehouseManager.getAllBoxes()) {
            availableQtyByBox.put(box.getBoxId(), box.getQty());
        }

        for (Order order : orders) {
            order.sortLines(); // garante lineNo ASC

            for (OrderLine line : order.getLines()) {
                String sku = line.getSku();
                int requested = line.getQty();
                int allocated = 0;

                // Obter todas as boxes deste SKU (ordenadas FEFO/FIFO)
                List<Box> candidateBoxes = warehouseManager.getBoxesForSku(sku);

                for (Box box : candidateBoxes) {
                    if (allocated >= requested) break; // já foi satisfeito

                    int available = availableQtyByBox.getOrDefault(box.getBoxId(), 0);
                    if (available <= 0) continue;

                    int take = Math.min(requested - allocated, available);
                    if (take <= 0) continue;

                    allocated += take;
                    availableQtyByBox.put(box.getBoxId(), available - take);

                    allocations.add(new Allocation(
                            order.getOrderId(),
                            line.getLineNo(),
                            sku,
                            take,
                            box.getBoxId(),
                            String.valueOf(box.getAisleNumber()),
                            String.valueOf(box.getBay().getNumber())
                    ));
                }

                // Determinar estado da linha
                String status;
                if (strictMode) {
                    if (allocated == requested) {
                        status = "ELIGIBLE";
                    } else {
                        status = "UNDISPATCHABLE";
                        // no modo strict, ignora alocações parciais
                        allocations.removeIf(a ->
                                a.getOrderId().equals(order.getOrderId()) &&
                                        a.getLineNo() == line.getLineNo());
                        allocated = 0;
                    }
                } else { // modo partial
                    if (allocated == 0) status = "UNDISPATCHABLE";
                    else if (allocated < requested) status = "PARTIAL";
                    else status = "ELIGIBLE";
                }

                results.add(new OrderEligibility(
                        order.getOrderId(),
                        line.getLineNo(),
                        sku,
                        requested,
                        allocated,
                        status
                ));
            }
        }

        return new AllocationOutput(results, allocations);
    }
}
