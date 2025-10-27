package ESINF.model;


import java.util.Collection;
import java.util.PriorityQueue;

/**
 * Represents a bay within an aisle of a warehouse.
 * A bay stores boxes in a priority queue (FEFO/FIFO ordering).
 */
public class Bay {

    private int number;                  // Identificador da baia
    private String warehouseId;          // Id do armazém
    private int aisleNumber;             // Corredor onde se encontra
    private int capacityBoxes;           // Capacidade máxima (nº de caixas)
    private PriorityQueue<Box> boxes;    // Fila ordenada por FEFO/FIFO

    /**
     * Constructs a new bay with the given parameters
     *
     * @param number the bay number
     * @param warehouseId the warehouse identifier
     * @param aisleNumber the aisle number
     * @param capacityBoxes the maximum number of boxes this bay can hold
     */
    public Bay(int number, String warehouseId, int aisleNumber, int capacityBoxes) {
        this.number = number;
        this.warehouseId = warehouseId;
        this.aisleNumber = aisleNumber;
        this.capacityBoxes = capacityBoxes;
        this.boxes = new PriorityQueue<>();  // ordenação automática
    }

    /**
     * Adds a box to this bay if there is available capacity
     *
     * @param box the box to add
     * @return true if the box was added, false if the bay is full
     */
    public boolean addBox(Box box) {
        if (boxes.size() >= capacityBoxes) return false;
        return boxes.add(box);
    }

    /**
     * Removes and returns the next box according to FEFO/FIFO ordering
     *
     * @return the next box to dispatch, or null if the bay is empty
     */
    public Box dispatchBox() {
        return boxes.poll();  // remove o da frente (FEFO/FIFO)
    }

    /**
     * Checks if the bay is at full capacity
     *
     * @return true if the bay is full
     */
    public boolean isFull() {
        return boxes.size() >= capacityBoxes;
    }

    /**
     * Checks if the bay is empty
     *
     * @return true if the bay contains no boxes
     */
    public boolean isEmpty() {
        return boxes.isEmpty();
    }

    /**
     * Gets the bay number
     *
     * @return the bay number
     */
    public int getNumber() { return number; }

    /**
     * Gets the aisle number
     *
     * @return the aisle number
     */
    public int getAisleNumber() { return aisleNumber; }

    /**
     * Gets the maximum capacity of the bay
     *
     * @return the capacity in number of boxes
     */
    public int getCapacityBoxes() { return capacityBoxes; }

    /**
     * Sets the maximum box capacity of the bay
     *
     * @param capacityBoxes the new capacity in number of boxes
     */
    public void setCapacity(int capacityBoxes) {
        this.capacityBoxes = capacityBoxes;
    }

    /**
     * Gets all boxes in this bay
     *
     * @return the priority queue of boxes
     */
    public PriorityQueue<Box> getBoxes() { return boxes;}




    @Override
    public String toString() {
        return String.format("Bay[%d, aisle=%d, %d boxes]", number, aisleNumber, boxes.size());
    }
}
