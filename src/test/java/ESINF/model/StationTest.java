package ESINF.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Station model class
 * Tests getters, compareTo, equals, and hashCode
 */
public class StationTest {

    // ============ Constructor and Getters Tests ============

    @Test
    public void testGetName() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals("Lisboa Santa Apolónia", station.getName());
    }

    @Test
    public void testGetLatitude() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals(38.71387, station.getLatitude(), 0.00001);
    }

    @Test
    public void testGetLongitude() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals(-9.122271, station.getLongitude(), 0.00001);
    }

    @Test
    public void testGetCountry() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals("PT", station.getCountry());
    }

    @Test
    public void testGetTimeZone() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals("Europe/Lisbon", station.getTimeZone());
    }

    @Test
    public void testGetTimeZoneGroup() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals("WET/GMT", station.getTimeZoneGroup());
    }

    @Test
    public void testIsCity() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertTrue(station.isCity());
    }

    @Test
    public void testIsMainStation() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertTrue(station.isMainStation());
    }

    @Test
    public void testIsAirport() {
        Station station = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertFalse(station.isAirport());
    }

    @Test
    public void testIsCityFalse() {
        Station station = new Station(
                "Small Station", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", false, false, false
        );
        assertFalse(station.isCity());
    }

    @Test
    public void testIsMainStationFalse() {
        Station station = new Station(
                "Small Station", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", false, false, false
        );
        assertFalse(station.isMainStation());
    }

    @Test
    public void testIsAirportTrue() {
        Station station = new Station(
                "Airport Station", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", false, false, true
        );
        assertTrue(station.isAirport());
    }

    // ============ CompareTo Tests (Natural ordering by name) ============

    @Test
    public void testCompareToEqual() {
        Station station1 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Lisboa", 40.0, -8.0, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals(0, station1.compareTo(station2));
    }

    @Test
    public void testCompareToLessThan() {
        Station station1 = new Station(
                "Aveiro", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Braga", 40.0, -8.0, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertTrue(station1.compareTo(station2) < 0);
    }

    @Test
    public void testCompareToGreaterThan() {
        Station station1 = new Station(
                "Porto", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Braga", 40.0, -8.0, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertTrue(station1.compareTo(station2) > 0);
    }

    @Test
    public void testCompareToWithSpecialCharacters() {
        Station station1 = new Station(
                "Ávila", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Braga", 40.0, -8.0, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertTrue(station1.compareTo(station2) < 0);
    }

    // ============ Equals Tests ============

    @Test
    public void testEqualsSameObject() {
        Station station = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertTrue(station.equals(station));
    }

    @Test
    public void testEqualsIdenticalStations() {
        Station station1 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertTrue(station1.equals(station2));
    }

    @Test
    public void testEqualsDifferentNames() {
        Station station1 = new Station(
                "Lisboa Santa Apolónia", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Lisboa Oriente", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertFalse(station1.equals(station2));
    }

    @Test
    public void testEqualsDifferentLatitudes() {
        Station station1 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Lisboa", 38.71388, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertFalse(station1.equals(station2));
    }

    @Test
    public void testEqualsDifferentLongitudes() {
        Station station1 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Lisboa", 38.71387, -9.122272, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertFalse(station1.equals(station2));
    }

    @Test
    public void testEqualsWithNull() {
        Station station = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertFalse(station.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        Station station = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertFalse(station.equals("Lisboa"));
    }

    @Test
    public void testEqualsSameCoordinatesDifferentAttributes() {
        Station station1 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Lisboa", 38.71387, -9.122271, "ES",
                "Europe/Madrid", "CET", false, false, false
        );
        assertTrue(station1.equals(station2));
    }

    // ============ HashCode Tests ============

    @Test
    public void testHashCodeConsistency() {
        Station station = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        int hash1 = station.hashCode();
        int hash2 = station.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeEqualObjects() {
        Station station1 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertEquals(station1.hashCode(), station2.hashCode());
    }

    @Test
    public void testHashCodeDifferentObjects() {
        Station station1 = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        Station station2 = new Station(
                "Porto", 41.0, -8.0, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        assertNotEquals(station1.hashCode(), station2.hashCode());
    }

    // ============ ToString Tests ============

    @Test
    public void testToString() {
        Station station = new Station(
                "Lisboa", 38.71387, -9.122271, "PT",
                "Europe/Lisbon", "WET/GMT", true, true, false
        );
        String expected = "Station{name='Lisboa', lat=38.71387, lon=-9.12227, country='PT', tzGroup='WET/GMT'}";
        assertEquals(expected, station.toString());
    }
}