package ESINF.model;

import java.util.Locale;

public class HubCentralityResult implements Comparable<HubCentralityResult> {

    private final RailwayStation station;

    // Valores Brutos
    private double degree = 0;
    private double strength = 0.0;
    private double betweenness = 0.0;
    private double harmonicCloseness = 0.0;

    // Valores Normalizados (Componentes do HubScore)
    private double strengthNorm = 0.0;
    private double betweennessNorm = 0.0;
    private double harmonicClosenessNorm = 0.0;

    private double hubScore = 0.0;

    // Pesos definidos na US
    private static final double WEIGHT_BETWEENNESS = 0.35;
    private static final double WEIGHT_CLOSENESS = 0.35;
    private static final double WEIGHT_STRENGTH = 0.30;

    public HubCentralityResult(RailwayStation station) {
        this.station = station;
    }

    // --- Setters ---
    public void setDegree(double degree) { this.degree = degree; }
    public void setStrength(double strength) { this.strength = strength; }
    public void setBetweenness(double betweenness) { this.betweenness = betweenness; }
    public void setHarmonicCloseness(double harmonicCloseness) { this.harmonicCloseness = harmonicCloseness; }

    /**
     * Normaliza as pontuações e calcula o score final.
     */
    public void normalizeScores(double maxStrength, double maxBetweenness, double maxCloseness) {
        this.strengthNorm = (maxStrength > 0) ? strength / maxStrength : 0.0;
        this.betweennessNorm = (maxBetweenness > 0) ? betweenness / maxBetweenness : 0.0;
        this.harmonicClosenessNorm = (maxCloseness > 0) ? harmonicCloseness / maxCloseness : 0.0;
    }

    public void calculateHubScore() {
        this.hubScore = (WEIGHT_BETWEENNESS * this.betweennessNorm)
                + (WEIGHT_CLOSENESS * this.harmonicClosenessNorm)
                + (WEIGHT_STRENGTH * this.strengthNorm);
    }

    // --- Getters Brutos ---
    public int getStid() { return station.getStationId(); }
    public String getStname() { return station.getName(); }
    public double getDegree() { return degree; }
    public double getStrength() { return strength; }
    public double getBetweenness() { return betweenness; }
    public double getHarmonicCloseness() { return harmonicCloseness; }

    // --- Getters Normalizados ---
    public double getStrengthNorm() { return strengthNorm; }
    public double getBetweennessNorm() { return betweennessNorm; }
    public double getHarmonicClosenessNorm() { return harmonicClosenessNorm; }

    public double getHubScore() { return hubScore; }

    @Override
    public int compareTo(HubCentralityResult other) {
        return Double.compare(other.hubScore, this.hubScore);
    }

    /**
     * Formata a linha da tabela com os valores normalizados.
     */
    @Override
    public String toString() {
        // Formata usando Locale.ROOT para garantir ponto decimal (.) em vez de vírgula
        return String.format(Locale.ROOT,
                " %-4d | %-12s | %-6.0f | %-8.4f | %-11.4f | %-18.4f | %-8.4f ",
                station.getStationId(),
                truncate(station.getName(), 12), // Trunca nomes longos para não partir a tabela
                degree,
                strengthNorm,
                betweennessNorm,
                harmonicClosenessNorm,
                hubScore
        );
    }

    // Auxiliar para cortar strings muito longas na tabela
    private String truncate(String str, int width) {
        if (str.length() > width) {
            return str.substring(0, width);
        }
        return str;
    }
}