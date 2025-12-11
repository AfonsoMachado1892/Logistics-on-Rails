package ESINF.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RailwayStationTest {

    @Test
    void testConstructorValid() {
        RailwayStation station = new RailwayStation(1, "Porto", 41.1579, -8.6291, 100.0, 200.0);

        assertEquals(1, station.getStationId(), "Station ID should be 1");
        assertEquals("Porto", station.getName(), "Name should be Porto");
        assertEquals(41.1579, station.getLatitude(), 0.0001, "Latitude should match");
        assertEquals(-8.6291, station.getLongitude(), 0.0001, "Longitude should match");
        assertEquals(100.0, station.getCoordX(), 0.0001, "CoordX should match");
        assertEquals(200.0, station.getCoordY(), 0.0001, "CoordY should match");
    }

    @Test
    void testConstructorNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new RailwayStation(1, null, 41.0, -8.0, 100.0, 200.0),
                "Should throw exception for null name");
    }

    @Test
    void testConstructorEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> new RailwayStation(1, "   ", 41.0, -8.0, 100.0, 200.0),
                "Should throw exception for empty name");
    }

    @Test
    void testConstructorTrimsName() {
        RailwayStation station = new RailwayStation(1, "  Porto  ", 41.0, -8.0, 100.0, 200.0);
        assertEquals("Porto", station.getName(), "Name should be trimmed");
    }

    @Test
    void testEquals() {
        RailwayStation station1 = new RailwayStation(1, "Porto", 41.0, -8.0, 100.0, 200.0);
        RailwayStation station2 = new RailwayStation(1, "Lisboa", 38.0, -9.0, 150.0, 250.0);
        RailwayStation station3 = new RailwayStation(2, "Porto", 41.0, -8.0, 100.0, 200.0);

        assertEquals(station1, station2, "Stations with same ID should be equal");
        assertNotEquals(station1, station3, "Stations with different ID should not be equal");
        assertEquals(station1, station1, "Station should equal itself");
    }

    @Test
    void testEqualsNull() {
        RailwayStation station = new RailwayStation(1, "Porto", 41.0, -8.0, 100.0, 200.0);
        assertNotEquals(null, station, "Station should not equal null");
    }

    @Test
    void testEqualsDifferentClass() {
        RailwayStation station = new RailwayStation(1, "Porto", 41.0, -8.0, 100.0, 200.0);
        assertNotEquals("Porto", station, "Station should not equal a String");
    }

    @Test
    void testHashCode() {
        RailwayStation station1 = new RailwayStation(1, "Porto", 41.0, -8.0, 100.0, 200.0);
        RailwayStation station2 = new RailwayStation(1, "Lisboa", 38.0, -9.0, 150.0, 250.0);
        RailwayStation station3 = new RailwayStation(2, "Porto", 41.0, -8.0, 100.0, 200.0);

        assertEquals(station1.hashCode(), station2.hashCode(),
                "Stations with same ID should have same hash code");
        assertNotEquals(station1.hashCode(), station3.hashCode(),
                "Stations with different ID should have different hash codes");
    }

    @Test
    void testToString() {
        RailwayStation station = new RailwayStation(1, "Porto", 41.0, -8.0, 100.0, 200.0);
        String str = station.toString();

        assertTrue(str.contains("Porto"), "toString should contain name");
        assertTrue(str.contains("1"), "toString should contain ID");
    }

    @Test
    void testNegativeCoordinates() {
        RailwayStation station = new RailwayStation(1, "Station", -10.5, -20.3, -100.0, -200.0);

        assertEquals(-10.5, station.getLatitude(), 0.0001, "Should accept negative latitude");
        assertEquals(-20.3, station.getLongitude(), 0.0001, "Should accept negative longitude");
        assertEquals(-100.0, station.getCoordX(), 0.0001, "Should accept negative coordX");
        assertEquals(-200.0, station.getCoordY(), 0.0001, "Should accept negative coordY");
    }

    @Test
    void testZeroCoordinates() {
        RailwayStation station = new RailwayStation(0, "Origin", 0.0, 0.0, 0.0, 0.0);

        assertEquals(0, station.getStationId(), "Should accept zero ID");
        assertEquals(0.0, station.getLatitude(), 0.0001, "Should accept zero latitude");
        assertEquals(0.0, station.getLongitude(), 0.0001, "Should accept zero longitude");
        assertEquals(0.0, station.getCoordX(), 0.0001, "Should accept zero coordX");
        assertEquals(0.0, station.getCoordY(), 0.0001, "Should accept zero coordY");
    }
}