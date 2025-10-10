package ESINF.model;

public class Item {

    private String sku;          // Identificador único do produto
    private String name;         // Nome do produto
    private String category;     // Categoria (ex: food, beverage, machinery)
    private String unit;         // Unidade (ex: kg, L, units)
    private double volume;       // Volume por unidade (m3, p.ex.)
    private double unitWeight;   // Peso por unidade (kg)

    // Construtor
    public Item(String sku, String name, String category, String unit, double volume, double unitWeight) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.volume = volume;
        this.unitWeight = unitWeight;
    }

    // Getters e Setters
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public double getVolume() { return volume; }
    public double getUnitWeight() { return unitWeight; }

    @Override
    public String toString() {
        return String.format("Item[%s - %s]", sku, name);
    }
}
