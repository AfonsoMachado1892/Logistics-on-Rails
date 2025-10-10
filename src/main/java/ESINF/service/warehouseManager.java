package ESINF.service;

import ESINF.model.*;
import java.util.*;

public class warehouseManager {

    private Warehouse warehouse;

    public warehouseManager(Warehouse warehouse) {
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
                            break;
                        }
                    }
                }
            }
        }
    }

    public void printInventory() {
        for (Aisle a : warehouse.getAisles().values()) {
            for (Bay b : a.getBays().values()) {
                System.out.println(b);
            }
        }
    }
}
