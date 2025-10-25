package LAPR3.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class LineTest {

    @Test
    public void testValidConstructor() {
        Line line = new Line(1, "Linha do Norte", "IP", 10, "Porto", 20, "Lisboa", 1435);

        assertEquals(1, line.getId());
        assertEquals("Linha do Norte", line.getName());
        assertEquals("IP", line.getOwner());
        assertEquals(10, line.getStartFacilityId());
        assertEquals("Porto", line.getStartFacilityName());
        assertEquals(20, line.getEndFacilityId());
        assertEquals("Lisboa", line.getEndFacilityName());
        assertEquals(1435, line.getGauge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NegativeId() {
        new Line(-1, "Name", "Owner", 1, "A", 2, "B", 1435);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullName() {
        new Line(1, null, "Owner", 1, "A", 2, "B", 1435);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_EmptyName() {
        new Line(1, " ", "Owner", 1, "A", 2, "B", 1435);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_SameStartAndEnd() {
        new Line(1, "Name", "Owner", 10, "A", 10, "A", 1435);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroGauge() {
        new Line(1, "Name", "Owner", 1, "A", 2, "B", 0);
    }

    @Test
    public void testConnectsFacilities() {
        Line line = new Line(1, "Linha", "Owner", 10, "A", 20, "B", 1435);

        assertTrue(line.connectsFacilities(10, 20)); // Direção normal
        assertTrue(line.connectsFacilities(20, 10)); // Direção inversa
        assertFalse(line.connectsFacilities(10, 30)); // Destino errado
        assertFalse(line.connectsFacilities(30, 40)); // Totalmente errado
    }

    @Test
    public void testEqualsAndHashCode() {
        Line line1 = new Line(1, "Linha A", "Owner", 10, "A", 20, "B", 1435);
        Line line2 = new Line(1, "Linha A v2", "Owner2", 1, "C", 2, "D", 1000);
        Line line3 = new Line(2, "Linha B", "Owner", 30, "X", 40, "Y", 1435);

        assertEquals(line1, line2); // Iguais se o ID for igual
        assertNotEquals(line1, line3);

        assertEquals(line1.hashCode(), line2.hashCode());
        assertNotEquals(line1.hashCode(), line3.hashCode());

        assertNotEquals(line1, null);
        assertNotEquals(line1, new Object());
    }
}
