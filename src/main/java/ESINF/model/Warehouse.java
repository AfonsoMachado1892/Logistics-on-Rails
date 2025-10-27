package ESINF.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a warehouse containing multiple aisles.
 * The warehouse structure is organized as: Warehouse -> Aisles -> Bays -> Boxes
 */
public class Warehouse {

    private String id;                           // Identificador do armazém
    private Map<Integer, Aisle> aisles;          // Corredores

    /**
     * Constructs a new warehouse with the given identifier
     *
     * @param id the warehouse identifier
     */
    public Warehouse(String id) {
        this.id = id;
        this.aisles = new HashMap<>();
    }

    /**
     * Adds an aisle to this warehouse
     *
     * @param aisle the aisle to add
     */
    public void addAisle(Aisle aisle) {
        aisles.put(aisle.getNumber(), aisle);
    }

    /**
     * Gets an aisle by its number
     *
     * @param aisleNumber the aisle number
     * @return the aisle if found, null otherwise
     */
    public Aisle getAisle(int aisleNumber) {
        return aisles.get(aisleNumber);
    }

    /**
     * Finds a bay within the warehouse by aisle and bay numbers
     *
     * @param aisle the aisle number
     * @param bayNumber the bay number
     * @return the bay if found, null otherwise
     */
    public Bay findBay(int aisle, int bayNumber) {
        Aisle a = aisles.get(aisle);
        return (a != null) ? a.getBay(bayNumber) : null;
    }

    /**
     * Gets all aisles in this warehouse
     *
     * @return map of aisle numbers to aisle objects
     */
    public Map<Integer, Aisle> getAisles() {
        return aisles;
    }

    /**
     * Gets the warehouse identifier
     *
     * @return the warehouse ID
     */
    public String getId() { return id; }

    @Override
    public String toString() {
        return String.format("Warehouse[%s] with %d aisles", id, aisles.size());
    }
}
