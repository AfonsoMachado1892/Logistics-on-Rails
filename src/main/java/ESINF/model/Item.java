package ESINF.model;

/**
 * Represents a product/item in the warehouse management system.
 * Contains basic information about the product including its physical properties.
 */
public class Item {

    private String sku;          // Identificador único do produto
    private String name;         // Nome do produto
    private String category;     // Categoria (ex: food, beverage, machinery)
    private String unit;         // Unidade (ex: kg, L, units)
    private double volume;       // Volume por unidade (m3, p.ex.)
    private double unitWeight;   // Peso por unidade (kg)

    /**
     * Constructs a new item
     *
     * @param sku the product SKU (unique identifier)
     * @param name the product name
     * @param category the product category
     * @param unit the unit of measurement
     * @param volume the volume per unit
     * @param unitWeight the weight per unit
     */
    public Item(String sku, String name, String category, String unit, double volume, double unitWeight) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.volume = volume;
        this.unitWeight = unitWeight;
    }

    /**
     * Gets the product SKU
     *
     * @return the SKU
     */
    public String getSku() { return sku; }

    /**
     * Gets the product name
     *
     * @return the product name
     */
    public String getName() { return name; }

    /**
     * Gets the product category
     *
     * @return the category
     */
    public String getCategory() { return category; }

    /**
     * Gets the unit of measurement
     *
     * @return the unit
     */
    public String getUnit() { return unit; }

    /**
     * Gets the volume per unit
     *
     * @return the volume per unit
     */
    public double getVolume() { return volume; }

    /**
     * Gets the weight per unit
     *
     * @return the weight per unit
     */
    public double getUnitWeight() { return unitWeight; }

    @Override
    public String toString() {
        return String.format("Item[%s - %s]", sku, name);
    }
}
