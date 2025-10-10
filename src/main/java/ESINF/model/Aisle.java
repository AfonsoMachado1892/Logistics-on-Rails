package ESINF.model;

import java.util.HashMap;
import java.util.Map;

public class Aisle {

    private int number;                     // Identificador do corredor
    private Map<Integer, Bay> bays;         // Baias dentro deste corredor

    public Aisle(int number) {
        this.number = number;
        this.bays = new HashMap<>();
    }

    public void addBay(Bay bay) {
        bays.put(bay.getNumber(), bay);
    }

    public Bay getBay(int bayNumber) {
        return bays.get(bayNumber);
    }

    public Map<Integer, Bay> getBays() {
        return bays;
    }

    public int getNumber() { return number; }

    @Override
    public String toString() {
        return String.format("Aisle[%d] with %d bays", number, bays.size());
    }
}
