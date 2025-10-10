package ESINF.model;


import java.util.PriorityQueue;

public class Bay {

    private int number;                  // Identificador da baia
    private String warehouseId;          // Id do armazém
    private int aisleNumber;             // Corredor onde se encontra
    private int capacityBoxes;           // Capacidade máxima (nº de caixas)
    private PriorityQueue<Box> boxes;    // Fila ordenada por FEFO/FIFO

    public Bay(int number, String warehouseId, int aisleNumber, int capacityBoxes) {
        this.number = number;
        this.warehouseId = warehouseId;
        this.aisleNumber = aisleNumber;
        this.capacityBoxes = capacityBoxes;
        this.boxes = new PriorityQueue<>();  // ordenação automática
    }

    public boolean addBox(Box box) {
        if (boxes.size() >= capacityBoxes) return false;
        return boxes.add(box);
    }

    public Box dispatchBox() {
        return boxes.poll();  // remove o da frente (FEFO/FIFO)
    }

    public boolean isFull() {
        return boxes.size() >= capacityBoxes;
    }

    public boolean isEmpty() {
        return boxes.isEmpty();
    }

    public int getNumber() { return number; }
    public int getAisleNumber() { return aisleNumber; }
    public int getCapacityBoxes() { return capacityBoxes; }

    @Override
    public String toString() {
        return String.format("Bay[%d, aisle=%d, %d boxes]", number, aisleNumber, boxes.size());
    }
}
