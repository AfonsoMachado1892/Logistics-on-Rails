package ESINF.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AisleTest {

    private Aisle aisle;
    private Bay bay1;

    @Before
    public void setUp() {
        aisle = new Aisle(1);
        bay1 = new Bay(1, "W01", 1, 10);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(1, aisle.getNumber());
        assertNotNull("Map 'bays' não deve ser null", aisle.getBays());
        assertTrue("Map 'bays' deve estar vazio", aisle.getBays().isEmpty());
    }

    @Test
    public void testAddAndGetBay() {
        assertNull("Get baia inexistente deve retornar null", aisle.getBay(1));

        aisle.addBay(bay1);

        Bay retrievedBay = aisle.getBay(1);
        assertNotNull("Baia não deve ser null após ser adicionada", retrievedBay);
        assertSame("Baia 1 deve ser a baia 1", bay1, retrievedBay);
        assertEquals(1, aisle.getBays().size());
    }

    @Test
    public void testToString() {
        aisle.addBay(bay1);
        String str = aisle.toString();

        assertTrue(str.contains("Aisle[1]"));
        assertTrue(str.contains("with 1 bays"));
    }
}