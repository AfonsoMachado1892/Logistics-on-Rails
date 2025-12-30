package LAPR3.model;

public class Segment implements Comparable<Segment> {
    private final int id;
    private final int lineId;
    private final int order;
    private final boolean electrified;
    private final double maxWeightKgPerMeter;
    private final double lengthMeters;
    private final int numberTracks;
    // Novos atributos Sprint 3 (Nullable)
    private final Double sidingPosition;
    private final Double sidingLength;

    public Segment(int id, int lineId, int order, boolean electrified,
                   double maxWeightKgPerMeter, double lengthMeters, int numberTracks,
                   Double sidingPosition, Double sidingLength) {
        this.id = id;
        this.lineId = lineId;
        this.order = order;
        this.electrified = electrified;
        this.maxWeightKgPerMeter = maxWeightKgPerMeter;
        this.lengthMeters = lengthMeters;
        this.numberTracks = numberTracks;
        this.sidingPosition = sidingPosition;
        this.sidingLength = sidingLength;
    }

    // Getters...
    public int getId() { return id; }
    public int getLineId() { return lineId; }
    public int getOrder() { return order; }
    public boolean isElectrified() { return electrified; }
    public double getMaxWeightKgPerMeter() { return maxWeightKgPerMeter; }
    public double getLengthMeters() { return lengthMeters; }
    public int getNumberTracks() { return numberTracks; }
    public Double getSidingPosition() { return sidingPosition; }
    public Double getSidingLength() { return sidingLength; }

    @Override
    public int compareTo(Segment other) {
        return Integer.compare(this.order, other.order);
    }

    @Override
    public String toString() {
        return "Segment " + id + " [Line " + lineId + ", Order " + order + "]";
    }
}