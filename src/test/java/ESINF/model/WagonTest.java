package ESINF.model;

import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class WagonTest {

    @Test
    public void testConstructorAndGetters() {
        Wagon wagon = new Wagon("W-A-01");

        assertEquals("W-A-01", wagon.getWagonId());
        assertNotNull("Lista 'boxes' não deve ser null", wagon.getBoxes());
        assertTrue("Lista 'boxes' deve estar vazia", wagon.getBoxes().isEmpty());
    }

    @Test
    public void testAddBox() {
        Wagon wagon = new Wagon("W-A-01");
        Box box1 = new Box("B1", "SKU1", 10, null, LocalDateTime.now());
        Box box2 = new Box("B2", "SKU2", 20, null, LocalDateTime.now());

        wagon.addBox(box1);
        assertEquals(1, wagon.getBoxes().size());

        wagon.addBox(box2);
        assertEquals(2, wagon.getBoxes().size());

        assertTrue(wagon.getBoxes().contains(box1));
        assertTrue(wagon.getBoxes().contains(box2));
    }

    @Test
    public void testToString() {
        Wagon wagon = new Wagon("W-A-01");
        wagon.addBox(new Box("B1", "SKU1", 10, null, LocalDateTime.now()));

        String str = wagon.toString();

        assertTrue(str.contains("Wagon[W-A-01]"));
        assertTrue(str.contains("with 1 boxes"));
    }
}