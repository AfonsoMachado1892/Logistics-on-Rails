package ESINF.service;

import ESINF.model.*;
import java.util.*;

public class WarehouseManager {

    private Warehouse warehouse;

    public WarehouseManager(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void unloadWagons(List<Wagon> wagons) {
        for (Wagon wagon : wagons) {
            for (Box box : wagon.getBoxes()) {
                // exemplo simples: escolher a 1ª baia disponível no armazém
                for (Aisle aisle : warehouse.getAisles().values()) {
                    for (Bay bay : aisle.getBays().values()) {
                        if (!bay.isFull()) {
                            bay.addBox(box);
                            box.setBay(bay);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void printInventory() {
        System.out.println("\n===== WAREHOUSE INVENTORY =====");
        System.out.println("Warehouse: " + warehouse.getId());

        for (Aisle aisle : warehouse.getAisles().values()) {
            System.out.println("Aisle " + aisle.getNumber() + ":");

            for (Bay bay : aisle.getBays().values()) {
                System.out.println("  Bay " + bay.getNumber() + " (" +
                        bay.getBoxes().size() + " boxes):");

                if (bay.getBoxes().isEmpty()) {
                    System.out.println("    (empty)");
                    continue;
                }

                // Cria snapshot ordenado (para visualização estável)
                List<Box> boxList = new ArrayList<>(bay.getBoxes());
                boxList.sort(Comparator.naturalOrder()); // usa compareTo() da Box (FEFO/FIFO)

                for (Box box : boxList) {
                    System.out.println("    - BoxID=" + box.getBoxId() +
                            " | SKU=" + box.getSku() +
                            " | Qty=" + box.getQty() +
                            (box.getExpiryDate() != null
                                    ? " | Exp=" + box.getExpiryDate()
                                    : " | Exp=N/A") +
                            " | Rec=" + box.getReceivedAt());
                }
            }
        }
        System.out.println("================================\n");
    }

    /**
     * Dispatches (removes) a given quantity of a SKU from the warehouse,
     * always consuming from the "front" of each bay list (FEFO/FIFO order).
     * Bays remain in the system; empty boxes are removed.
     * Partial dispatch across multiple bays is supported.
     */
    public List<Box> dispatch(String sku, int quantityRequested) {
        List<Box> dispatched = new ArrayList<>();

        if (quantityRequested <= 0) return dispatched;

        // Obter todas as bays em ordem crescente
        List<Bay> orderedBays = new ArrayList<>();
        for (Aisle a : warehouse.getAisles().values()) {
            orderedBays.addAll(a.getBays().values());
        }
        orderedBays.sort(Comparator.comparingInt(Bay::getNumber));

        int remaining = quantityRequested;

        for (Bay bay : orderedBays) {
            PriorityQueue<Box> boxes = bay.getBoxes();
            List<Box> toRemove = new ArrayList<>();

            // Percorrer boxes FEFO/FIFO
            for (Box box : new ArrayList<>(boxes)) {
                if (!box.getSku().equals(sku)) continue;

                int available = box.getQty();
                if (available <= 0) {
                    toRemove.add(box);
                    continue;
                }

                if (available >= remaining) {
                    // dispatch parcial da box
                    box.setQty(available - remaining);
                    Box dispatchedPart = new Box(box.getBoxId(), box.getSku(), remaining,
                            box.getExpiryDate(), box.getReceivedAt());
                    dispatched.add(dispatchedPart);
                    remaining = 0;
                    if (box.getQty() == 0) toRemove.add(box);
                    break;
                } else {
                    // dispatch total desta box
                    dispatched.add(box);
                    remaining -= available;
                    toRemove.add(box);
                }
            }

            // remover boxes vazias
            for (Box b : toRemove) boxes.remove(b);

            if (remaining == 0) break;
        }

        if (remaining > 0) {
            System.err.println("⚠️ Not enough stock for SKU " + sku +
                    " (missing " + remaining + " units)");
        }

        return dispatched;
    }

}
