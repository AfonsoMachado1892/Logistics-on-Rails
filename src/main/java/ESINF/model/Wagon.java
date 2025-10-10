package ESINF.model;

import java.util.ArrayList;
import java.util.List;

public class Wagon {

    private String wagonId;        // Identificador do vagão
    private List<Box> boxes;       // Caixas que transporta

    public Wagon(String wagonId) {
        this.wagonId = wagonId;
        this.boxes = new ArrayList<>();
    }

    public void addBox(Box box) {
        boxes.add(box);
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public String getWagonId() {
        return wagonId;
    }

    @Override
    public String toString() {
        return String.format("Wagon[%s] with %d boxes", wagonId, boxes.size());
    }
}
