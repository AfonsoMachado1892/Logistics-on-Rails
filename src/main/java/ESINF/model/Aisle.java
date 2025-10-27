package ESINF.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an aisle within a warehouse.
 * An aisle contains multiple bays.
 */
public class Aisle {

    private int number;                     // Identificador do corredor
    private Map<Integer, Bay> bays;         // Baias dentro deste corredor

    /**
     * Constructs a new aisle with the given number
     *
     * @param number the aisle identifier
     */
    public Aisle(int number) {
        this.number = number;
        this.bays = new HashMap<>();
    }

    /**
     * Adds a bay to this aisle
     *
     * @param bay the bay to add
     */
    public void addBay(Bay bay) {
        bays.put(bay.getNumber(), bay);
    }

    /**
     * Retrieves a bay by its number
     *
     * @param bayNumber the bay number
     * @return the bay if found, null otherwise
     */
    public Bay getBay(int bayNumber) {
        return bays.get(bayNumber);
    }

    /**
     * Gets all bays in this aisle
     *
     * @return map of bay numbers to bay objects
     */
    public Map<Integer, Bay> getBays() {
        return bays;
    }

    /**
     * Gets the aisle number
     *
     * @return the aisle number
     */
    public int getNumber() { return number; }

    @Override
    public String toString() {
        return String.format("Aisle[%d] with %d bays", number, bays.size());
    }
}
