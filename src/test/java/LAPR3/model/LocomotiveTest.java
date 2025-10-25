package LAPR3.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocomotiveTest {

    private Locomotive.LocomotiveType DIESEL = Locomotive.LocomotiveType.DIESEL;
    private Locomotive.LocomotiveType ELECTRIC = Locomotive.LocomotiveType.ELECTRIC;

    @Test
    public void testValidDieselConstructor() {
        Locomotive loco = new Locomotive(101, "DieselMaster", "Make", "Model",
                2020, 4, "TypeA", 3000, 20, 3, 4, 100, 160,
                140, 300, DIESEL, "", "", "CP", 1435, 5000.0);

        assertEquals(101, loco.getNumber());
        assertEquals("DieselMaster", loco.getName());
        assertEquals(140, loco.getOperationalSpeedKmh(), 0.01);
        assertEquals(DIESEL, loco.getType());
        assertEquals(1435, loco.getGauge());
        assertEquals(5000.0, loco.getFuelCapacityLiters(), 0.01);
    }

    @Test
    public void testValidElectricConstructor() {
        Locomotive loco = new Locomotive(201, "ElectricForce", "Make", "Model",
                2022, 4, "TypeB", 5000, 18, 3, 4, 80, 200,
                180, 400, ELECTRIC, "25kV", "50Hz", "CP", 1435, null);

        assertEquals(201, loco.getNumber());
        assertEquals(180, loco.getOperationalSpeedKmh(), 0.01);
        assertEquals(ELECTRIC, loco.getType());
        assertNull(loco.getFuelCapacityLiters());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NegativeNumber() {
        new Locomotive(-1, "Name", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, DIESEL, "", "", "O", 1435, 1000.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ZeroOperationalSpeed() {
        new Locomotive(1, "Name", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                0, 1, DIESEL, "", "", "O", 1435, 1000.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_DieselRequiresFuel() {
        // Diesel com fuel null
        new Locomotive(1, "Name", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, DIESEL, "", "", "O", 1435, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_DieselRequiresPositiveFuel() {
        // Diesel com fuel 0
        new Locomotive(1, "Name", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, DIESEL, "", "", "O", 1435, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ElectricCannotHaveFuel() {
        // Electric com fuel
        new Locomotive(1, "Name", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, ELECTRIC, "", "", "O", 1435, 1000.0);
    }

    @Test
    public void testIsCompatibleWithGauge() {
        Locomotive loco = new Locomotive(101, "L", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, DIESEL, "", "", "O", 1435, 1000.0);

        assertTrue(loco.isCompatibleWithGauge(1435));
        assertFalse(loco.isCompatibleWithGauge(1000));
    }

    @Test
    public void testRequiresElectrification() {
        Locomotive diesel = new Locomotive(101, "L", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, DIESEL, "", "", "O", 1435, 1000.0);
        Locomotive electric = new Locomotive(102, "L", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, ELECTRIC, "", "", "O", 1435, null);

        assertFalse(diesel.requiresElectrification());
        assertTrue(electric.requiresElectrification());
    }

    @Test
    public void testGetWeightPerBogieTons() {
        Locomotive loco = new Locomotive(101, "L", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 100, 1,
                1, 1, DIESEL, "", "", "O", 1435, 1000.0);

        assertEquals(25.0, loco.getWeightPerBogieTons(), 0.01);
    }

    @Test
    public void testGetWeightPerBogieTons_ZeroBogies() {
        // Embora o construtor possa não permitir 0, se o fizesse, testamos a divisão por zero
        Locomotive loco = new Locomotive(101, "L", "M", "M", 2000, 0, "T", 1, 1, 1, 1, 100, 1,
                1, 1, DIESEL, "", "", "O", 1435, 1000.0);

        assertEquals(0.0, loco.getWeightPerBogieTons(), 0.01);
    }

    @Test
    public void testEqualsAndHashCode() {
        Locomotive loco1 = new Locomotive(101, "A", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, DIESEL, "", "", "O", 1435, 1000.0);
        Locomotive loco2 = new Locomotive(101, "B", "M", "M", 2010, 8, "T", 2, 2, 2, 2, 2, 2,
                2, 2, ELECTRIC, "V", "F", "O", 1000, null);
        Locomotive loco3 = new Locomotive(102, "A", "M", "M", 2000, 4, "T", 1, 1, 1, 1, 1, 1,
                1, 1, DIESEL, "", "", "O", 1435, 1000.0);

        assertEquals(loco1, loco2);
        assertNotEquals(loco1, loco3);
        assertEquals(loco1.hashCode(), loco2.hashCode());
        assertNotEquals(loco1.hashCode(), loco3.hashCode());
    }
}
