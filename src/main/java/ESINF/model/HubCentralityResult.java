package ESINF.model;

/**
 * Representa as medidas de centralidade calculadas e o HubScore para uma RailwayStation (USEI13).
 */
public class HubCentralityResult implements Comparable<HubCentralityResult> {

    private final RailwayStation station;
    private double degree = 0;
    private double strength = 0.0;
    private double betweenness = 0.0;
    private double harmonicCloseness = 0.0;

    private double strengthNorm = 0.0;
    private double betweennessNorm = 0.0;
    private double harmonicClosenessNorm = 0.0;
    private double hubScore = 0.0;

    // Cáluclo do HubScore: 0.35 * betw + 0.35 * harmonic_closeness + 0.30 * strengthNorm
    private static final double WEIGHT_BETWEENNESS = 0.35;
    private static final double WEIGHT_CLOSENESS = 0.35;
    private static final double WEIGHT_STRENGTH = 0.30;

    public HubCentralityResult(RailwayStation station) {
        this.station = station;
    }

    public void setDegree(double degree) { this.degree = degree; }
    public void setStrength(double strength) { this.strength = strength; }
    public void setBetweenness(double betweenness) { this.betweenness = betweenness; }
    public void setHarmonicCloseness(double harmonicCloseness) { this.harmonicCloseness = harmonicCloseness; }

    /**
     * Normaliza as pontuações em relação aos valores máximos encontrados na rede.
     */
    public void normalizeScores(double maxStrength, double maxBetweenness, double maxCloseness) {
        this.strengthNorm = (maxStrength > 0) ? strength / maxStrength : 0.0;
        this.betweennessNorm = (maxBetweenness > 0) ? betweenness / maxBetweenness : 0.0;
        this.harmonicClosenessNorm = (maxCloseness > 0) ? harmonicCloseness / maxCloseness : 0.0;
    }

    /**
     * Calcula o HubScore composto usando as métricas normalizadas e os pesos definidos na US.
     */
    public void calculateHubScore() {
        this.hubScore = WEIGHT_BETWEENNESS * this.betweennessNorm
                + WEIGHT_CLOSENESS * this.harmonicClosenessNorm
                + WEIGHT_STRENGTH * this.strengthNorm;
    }

    // --- Getters solicitados na US ---
    public int getStid() { return station.getStationId(); }
    public String getStname() { return station.getName(); }
    public double getDegree() { return degree; }
    public double getStrength() { return strength; }
    public double getBetweenness() { return betweenness; }
    public double getHarmonicCloseness() { return harmonicCloseness; }
    public double getHubScore() { return hubScore; }

    @Override
    public int compareTo(HubCentralityResult other) {
        // Ordenar por HubScore descendente
        return Double.compare(other.hubScore, this.hubScore);
    }

    /**
     * Formata o resultado para apresentação no formato solicitado pela US.
     */
    @Override
    public String toString() {
        // stid, stname, degree, strength, betweenness, harmonic_closeness, hubscore
        // Usa Locale root para garantir ponto decimal independente da locale do SO.
        return String.format(java.util.Locale.ROOT, "%s, %s, %.0f, %.4f, %.4f, %.4f, %.4f",
                station.getStationId(), station.getName(), degree, strength, betweenness, harmonicCloseness, hubScore);
    }
}