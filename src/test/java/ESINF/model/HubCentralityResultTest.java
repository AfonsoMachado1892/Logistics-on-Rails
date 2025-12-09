package ESINF.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HubCentralityResultTest {

    private final RailwayStation station = new RailwayStation(1, "Station", 0, 0, 0, 0);

    @Test
    public void normalizeScoresReturnsZeroWhenMaxValuesAreZero() {
        // Normalização com máximos a zero não deve produzir NaN ou divisão por zero.
        HubCentralityResult result = new HubCentralityResult(station);
        result.setStrength(5);
        result.setBetweenness(3);
        result.setHarmonicCloseness(2);

        result.normalizeScores(0, 0, 0);
        result.calculateHubScore();

        assertEquals(0.0, result.getHubScore(), 1e-6);
    }

    @Test
    public void calculateHubScoreUsesConfiguredWeights() {
        // Verifica a combinação 0.35 betweenness + 0.35 closeness + 0.30 strength.
        HubCentralityResult result = new HubCentralityResult(station);
        result.setStrength(50);        // /100 -> 0.5
        result.setBetweenness(20);     // /20  -> 1.0
        result.setHarmonicCloseness(10); // /20 -> 0.5

        result.normalizeScores(100, 20, 20);
        result.calculateHubScore();

        double expected = 0.35 * 1.0 + 0.35 * 0.5 + 0.30 * 0.5; // 0.675
        assertEquals(expected, result.getHubScore(), 1e-6);
    }

    @Test
    public void compareToOrdersByHubScoreDescending() {
        // Ordenação: resultado com maior HubScore deve ser "menor" para ficar primeiro.
        HubCentralityResult high = new HubCentralityResult(station);
        high.setStrength(10);
        high.normalizeScores(10, 1, 1);
        high.calculateHubScore(); // hubScore = 0.30

        HubCentralityResult low = new HubCentralityResult(station);
        low.setStrength(1);
        low.normalizeScores(10, 1, 1);
        low.calculateHubScore(); // hubScore = 0.03

        assertTrue(high.compareTo(low) < 0); // menor => aparece primeiro (ordem desc)
    }

    @Test
    public void toStringFormatsAllFields() {
        // Garante que toString contém todos os campos no formato definido.
        HubCentralityResult result = new HubCentralityResult(station);
        result.setDegree(2);
        result.setStrength(3);
        result.setBetweenness(6);
        result.setHarmonicCloseness(9);

        result.normalizeScores(3, 6, 9); // todos normalizados a 1
        result.calculateHubScore(); // hubScore = 1.0

        String expected = "1, Station, 2, 3.0000, 6.0000, 9.0000, 1.0000";
        assertEquals(expected, result.toString());
    }

    @Test
    public void gettersReturnSetValues() {
        // Verifica getters após configurar valores.
        HubCentralityResult result = new HubCentralityResult(station);
        result.setDegree(4);
        result.setStrength(7.5);
        result.setBetweenness(1.2);
        result.setHarmonicCloseness(0.8);

        assertEquals(1, result.getStid());
        assertEquals("Station", result.getStname());
        assertEquals(4.0, result.getDegree(), 1e-6);
        assertEquals(7.5, result.getStrength(), 1e-6);
        assertEquals(1.2, result.getBetweenness(), 1e-6);
        assertEquals(0.8, result.getHarmonicCloseness(), 1e-6);
    }
}

