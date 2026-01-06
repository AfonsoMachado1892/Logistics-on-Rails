package ESINF.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RailwayNetworkTest {

    @Test
    @DisplayName("Construtor aceita valores válidos e trima o nome")
    void testConstructor_ValidInput() {
        RailwayStation station = new RailwayStation(
                123, "  Porto Campanhã  ", 41.1496, -8.6110, 100.0, 200.0);

        assertEquals(123, station.getStationId());
        assertEquals("Porto Campanhã", station.getName()); // trim aplicado
        assertEquals(41.1496, station.getLatitude(), 0.000001);
        assertEquals(-8.6110, station.getLongitude(), 0.000001);
        assertEquals(100.0, station.getCoordX(), 0.000001);
        assertEquals(200.0, station.getCoordY(), 0.000001);
    }

    @Test
    @DisplayName("Construtor lança exceção se nome for null")
    void testConstructor_NameNull() {
        assertThrows(IllegalArgumentException.class, () ->
                        new RailwayStation(1, null, 0, 0, 0, 0),
                "Station name cannot be null or empty");
    }

    @Test
    @DisplayName("Construtor lança exceção se nome for vazio ou só espaços")
    void testConstructor_NameEmptyOrWhitespace() {
        assertThrows(IllegalArgumentException.class, () ->
                new RailwayStation(1, "", 0, 0, 0, 0));

        assertThrows(IllegalArgumentException.class, () ->
                new RailwayStation(1, "   ", 0, 0, 0, 0));

        assertThrows(IllegalArgumentException.class, () ->
                new RailwayStation(1, "\t\n", 0, 0, 0, 0));
    }

    @Test
    @DisplayName("Getters retornam valores corretos")
    void testGetters() {
        RailwayStation station = new RailwayStation(
                999, "Lisboa Oriente", 38.7674, -9.0993, 300.5, 50.2);

        assertEquals(999, station.getStationId());
        assertEquals("Lisboa Oriente", station.getName());
        assertEquals(38.7674, station.getLatitude(), 0.000001);
        assertEquals(-9.0993, station.getLongitude(), 0.000001);
        assertEquals(300.5, station.getCoordX(), 0.000001);
        assertEquals(50.2, station.getCoordY(), 0.000001);
    }

    @Test
    @DisplayName("equals() e hashCode() baseados apenas no stationId")
    void testEqualsAndHashCode() {
        RailwayStation station1 = new RailwayStation(10, "Station A", 0, 0, 0, 0);
        RailwayStation station1Copy = new RailwayStation(10, "Different Name", 99, 99, 99, 99);
        RailwayStation station2 = new RailwayStation(20, "Station A", 0, 0, 0, 0);

        // Mesmo ID → iguais, mesmo com outros campos diferentes
        assertEquals(station1, station1Copy);
        assertEquals(station1.hashCode(), station1Copy.hashCode());

        // IDs diferentes → diferentes
        assertNotEquals(station1, station2);
        assertNotEquals(station1.hashCode(), station2.hashCode());

        // Reflexividade e comparação com null/outros tipos
        assertEquals(station1, station1);
        assertNotEquals(station1, null);
        assertNotEquals(station1, "Not a station");
    }

    @Test
    @DisplayName("toString() retorna formato correto")
    void testToString() {
        RailwayStation station = new RailwayStation(45, "Braga", 41.5503, -8.4200, 10, 20);

        String expected = "Braga (ID: 45)";
        assertEquals(expected, station.toString());

        // Com espaços no nome (já trimados no construtor)
        RailwayStation stationWithSpaces = new RailwayStation(46, "  Faro  ", 37.0194, -7.9305, 30, 40);
        assertEquals("Faro (ID: 46)", stationWithSpaces.toString());
    }

    @Test
    @DisplayName("Imutabilidade: campos são final e não há setters")
    void testImmutability() {
        RailwayStation station = new RailwayStation(100, "Coimbra", 40.2033, -8.4103, 200, 150);

        // Não há métodos setters → compilador garante imutabilidade
        // Valores permanecem os mesmos
        assertEquals("Coimbra", station.getName());
        assertEquals(100, station.getStationId());
    }
}