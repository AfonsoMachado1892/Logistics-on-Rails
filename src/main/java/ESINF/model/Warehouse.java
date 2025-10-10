package ESINF.model;

import java.util.HashMap;
import java.util.Map;

public class Warehouse {

    private String id;                           // Identificador do armazém
    private Map<Integer, Aisle> aisles;          // Corredores

    public Warehouse(String id) {
        this.id = id;
        this.aisles = new HashMap<>();
    }

    public void addAisle(Aisle aisle) {
        aisles.put(aisle.getNumber(), aisle);
    }

    public Aisle getAisle(int aisleNumber) {
        return aisles.get(aisleNumber);
    }

    public Bay findBay(int aisle, int bayNumber) {
        Aisle a = aisles.get(aisle);
        return (a != null) ? a.getBay(bayNumber) : null;
    }

    public Map<Integer, Aisle> getAisles() {
        return aisles;
    }

    public String getId() { return id; }

    @Override
    public String toString() {
        return String.format("Warehouse[%s] with %d aisles", id, aisles.size());
    }
}
