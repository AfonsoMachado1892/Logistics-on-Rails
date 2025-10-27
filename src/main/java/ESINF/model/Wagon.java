package ESINF.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a wagon that delivers boxes to the warehouse.
 * Each wagon can carry multiple boxes.
 */
public class Wagon {

    private String wagonId;        // Identificador do vagão
    private List<Box> boxes;       // Caixas que transporta

    /**
     * Constructs a new wagon with the given identifier
     *
     * @param wagonId the wagon identifier
     */
    public Wagon(String wagonId) {
        this.wagonId = wagonId;
        this.boxes = new ArrayList<>();
    }

    /**
     * Adds a box to this wagon
     *
     * @param box the box to add
     */
    public void addBox(Box box) {
        boxes.add(box);
    }

    /**
     * Gets all boxes in this wagon
     *
     * @return the list of boxes
     */
    public List<Box> getBoxes() {
        return boxes;
    }

    /**
     * Gets the wagon identifier
     *
     * @return the wagon ID
     */
    public String getWagonId() {
        return wagonId;
    }

    @Override
    public String toString() {
        return String.format("Wagon[%s] with %d boxes", wagonId, boxes.size());
    }
}
