package ESINF.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class ItemTest {

    @Test
    public void testConstructorAndGetters() {
        String sku = "SKU123";
        String name = "Produto Teste";
        String category = "food";
        String unit = "kg";
        double volume = 0.5;
        double unitWeight = 2.0;

        Item item = new Item(sku, name, category, unit, volume, unitWeight);

        assertEquals(sku, item.getSku());
        assertEquals(name, item.getName());
        assertEquals(category, item.getCategory());
        assertEquals(unit, item.getUnit());
        assertEquals(volume, item.getVolume(), 0.001);
        assertEquals(unitWeight, item.getUnitWeight(), 0.001);
    }

    @Test
    public void testToString() {
        Item item = new Item("SKU123", "Produto Teste", "food", "kg", 0.5, 2.0);
        String str = item.toString();

        assertTrue(str.contains("Item[SKU123"));
        assertTrue(str.contains("Produto Teste]"));
    }
}